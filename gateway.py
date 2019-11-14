#!/usr/bin/env python

import os
import sys
import json
import time
import pika
import struct
import socket
import asyncio
import logging
import fileinput
import websockets
import http.server
import socketserver

from threading import Thread
from datetime import datetime
import sensor_pb2

logging.basicConfig()

SENSORS = {}
ADDR_ID_MAP = {}

USERS = set()

PORT = 80
Handler = http.server.SimpleHTTPRequestHandler

web_dir = os.path.join(os.path.dirname(__file__), 'web_interface')
os.chdir(web_dir)

multicast_group = '239.0.1.2'
multicast_port = 20480

CREDENTIALS = pika.PlainCredentials('projetosd', 'projetosd')
RABBITMQ_HOST = socket.gethostbyname(socket.gethostname())


def state_event():
    return build_message()


def users_event():
    return json.dumps({"type": "users", "count": len(USERS)})


async def notify_state():
    if USERS:  # asyncio.clswait doesn't accept an empty list
        _ = sensor_pb2.Message()
        await asyncio.wait([user.send(build_message().SerializeToString()) for user in USERS])


async def notify_users():
    if USERS:  # asyncio.wait doesn't accept an empty list
        await asyncio.wait([user.send(build_message().SerializeToString()) for user in USERS])


def build_message():
    _ = sensor_pb2.Message()
    for a,b in SENSORS:
        serialize_obj(_.sensors.add(),SENSORS[(a,b)],a,b,SENSORS[(a,b)].get('data'))
    return _


async def register(websocket):
    USERS.add(websocket)
    await notify_users()


async def unregister(websocket):
    USERS.remove(websocket)
    await notify_users()


class Consumer(Thread):
    EXCHANGE_NAME = 'topic_logs'

    def __init__(self, websocket):
        Thread.__init__(self)
        self.setDaemon(True)

        self.websocket = websocket
        self.connection = pika.BlockingConnection(
            pika.ConnectionParameters(host=RABBITMQ_HOST, credentials=CREDENTIALS))
        
        self.channel = self.connection.channel()
        self.channel.exchange_declare(exchange=Consumer.EXCHANGE_NAME, exchange_type='topic')

        result = self.channel.queue_declare('', exclusive=True)
        self.queue_name = result.method.queue

        self.channel.basic_consume(queue=self.queue_name, on_message_callback=self.callback, auto_ack=True)

        print("\n[#] New connection established")
        print(" |--- Exchange name: " + Consumer.EXCHANGE_NAME)
        print(" |--- Queue name: " + self.queue_name)

    def run(self):
        self.channel.start_consuming()

    def callback(self, ch, method, properties, body):
        print("\n[<-] %r:%r" % (method.routing_key, body))
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)
        loop.run_until_complete(self.websocket.send(json.dumps({"type": "state", "value": body.decode("utf-8")})))
        loop.close()

    def queue_bind(self, routing_key):
        self.channel.queue_bind(exchange=Consumer.EXCHANGE_NAME, queue=self.queue_name, routing_key=routing_key)
        print("\n[+] Queue bind")
        print(" |--- Exchange name: " + Consumer.EXCHANGE_NAME)
        print(" |--- Queue name: " + self.queue_name)
        print(" |--- Routing key: " + routing_key)

    def queue_unbind(self, routing_key):
        self.channel.queue_unbind(exchange=Consumer.EXCHANGE_NAME, queue=self.queue_name, routing_key=routing_key)
        print("\n[-] Queue unbind")
        print(" |--- Exchange name: " + Consumer.EXCHANGE_NAME)
        print(" |--- Queue name: " + self.queue_name)
        print(" |--- Routing key: " + routing_key)

    def disconnect(self):
        self.channel.stop_consuming()
        time.sleep(0.5)
        self.channel.close()
        time.sleep(0.5)
        self.connection.close()
        print("\n[x] Disconnection")
        print(" |--- Exchange name: " + Consumer.EXCHANGE_NAME)
        print(" |--- Queue name: " + self.queue_name)


async def connection_handler(websocket, path):
    consumer = Consumer(websocket)
    consumer.start()

    try:
        async for message in websocket:
            comm = sensor_pb2.Command()
            comm.ParseFromString(message)
            if comm.id == "bind":
                consumer.queue_bind(comm.command)
            elif comm.id == "unbind":
                consumer.queue_unbind(comm.command)
            elif comm.id == "grpc":
                print(comm.command)
            elif comm.id == "rrpc":
                print(comm.command)
            else:
                logging.error("unsupported event")
    finally:
        consumer.disconnect()
        consumer.join()


def http_server():
    with socketserver.TCPServer(("", PORT), Handler) as httpd:
        print(f"Serving at {socket.gethostbyname(socket.gethostname())}:{PORT}")
        httpd.serve_forever()


def sensor_finder_thread():
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    print("Sensor search initiated on the network")

    loop.run_until_complete(sensor_finder_handler())
    loop.close()


async def sensor_finder_handler():

    server_address = ('', multicast_port)
    # Create the socket
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

    # Bind to the server address
    sock.bind(server_address)

    # Tell the operating system to add the socket to the multicast group
    # on all interfaces.
    group = socket.inet_aton(multicast_group)
    mreq = struct.pack('4sL', group, socket.INADDR_ANY)
    sock.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)

    while True:
        sock.sendto('SERVER'.encode(), (multicast_group, multicast_port))
        time.sleep(10)
        keys_to_remove = []
        for key, value in SENSORS.items():
            dt = (datetime.now()-datetime.strptime(value['last_msg_date'], '%Y-%m-%d %H:%M:%S')).total_seconds()
            if dt > 15.0:
                keys_to_remove.append(key)
        if len(keys_to_remove):
            for key in keys_to_remove:
                print("Sensor " + SENSORS[key]['sensor_id'] + " removed")
                del ADDR_ID_MAP[SENSORS[key]['sensor_id']]
                del SENSORS[key]
            await notify_state()


def multicast_thread():
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    print("Waiting for group communication")

    loop.run_until_complete(multicast_handler())
    loop.close()


async def multicast_handler():

    server_address = ('', multicast_port)
    # Create the socket
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

    # Bind to the server address
    sock.bind(server_address)

    # Tell the operating system to add the socket to the multicast group
    # on all interfaces.
    group = socket.inet_aton(multicast_group)
    mreq = struct.pack('4sL', group, socket.INADDR_ANY)
    sock.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)

    sock.sendto('SERVER'.encode(), (multicast_group, multicast_port))

    # Receive/respond loop
    while True:
        data, address = sock.recvfrom(1024)
        if(data == b'SENSOR'):
            sock.sendto('SERVER'.encode(), address)
        else:
            split = data.decode().split('_')
            if(len(split) == 4 and split[0]=="SENSOR"):
                device_id = split[1]
                sensor_type = split[2]
                sensor_port = int(split[3])
                sensor_ip = address[0]
                sensor_id = device_id + "_" + sensor_type
                if(sensor_id not in ADDR_ID_MAP.keys()):
                    ADDR_ID_MAP[sensor_id] = (sensor_ip, sensor_port)
                    SENSORS[(sensor_ip, sensor_port)] = {'sensor_id': sensor_id, 'type': sensor_type, 'last_msg_date':  datetime.now().strftime("%Y-%m-%d %H:%M:%S")}
                else:
                    if ADDR_ID_MAP[sensor_id] != (sensor_ip, sensor_port):
                        SENSORS[(sensor_ip, sensor_port)] = SENSORS[ADDR_ID_MAP[sensor_id]]
                        del SENSORS[ADDR_ID_MAP[sensor_id]]
                        ADDR_ID_MAP[sensor_id] = (sensor_ip, sensor_port)
                    SENSORS[(sensor_ip, sensor_port)]['last_msg_date'] = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
                await notify_state()
                print('Sensor identified: {}'.format(sensor_id))


def serialize_obj(obj,_,address,port,data):
    typ = _['sensor_id'].split('_')[1]
    obj.id = _['sensor_id']
    obj.addr = address
    obj.port = port
    if data:
        obj.data = json.dumps(data)
    obj.last_msg_date = str(datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
    if typ == 'LIGHT':
        obj.type =  sensor_pb2.Sensor.LIGHT
    if typ == 'MAGNETIC':
        obj.type = sensor_pb2.Sensor.MAGNETIC
    if typ == 'SOUND':
        obj.type = sensor_pb2.Sensor.SOUND
    if typ == 'LED':
        obj.type = sensor_pb2.Sensor.LED

def data_listener_thread():
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    print("Waiting for UDP messages")

    loop.run_until_complete(udp_handler())
    loop.close()


async def udp_handler():
    UDP_PORT_NO = 5003
    BUFFER_SIZE = 1024

    serverSock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    serverSock.bind(("", UDP_PORT_NO))    

    # Receive/respond loop
    while True:
        data, addr = serverSock.recvfrom(BUFFER_SIZE)
        if addr in SENSORS.keys():
            SENSORS[addr]['last_msg_date'] = datetime.now().strftime("%Y-%m-%d %H:%M:%S");
            if(SENSORS[addr]['type'] == 'LIGHT'):
                SENSORS[addr]['data'] = float(data.decode("UTF-8"))
            elif(SENSORS[addr]['type'] == 'MAGNETIC'):
                split = data.decode("UTF-8").split(',')
                SENSORS[addr]['data'] = {'x': "{0:.2f}".format(float(split[0])), 'y': "{0:.2f}".format(float(split[1])), 'z': "{0:.2f}".format(float(split[2]))}
            elif(SENSORS[addr]['type'] == 'LED'):
                SENSORS[addr]['data'] = data.decode("UTF-8")
            elif(SENSORS[addr]['type'] == 'SOUND'):
                split = data.decode("UTF-8").split(',')
                SENSORS[addr]['data'] = {'status': split[0], 'volume': split[1]}
            await notify_state()
            print("New message from ", SENSORS[addr]['sensor_id'])


if __name__ == '__main__':
    for line in fileinput.input(['index.html'], inplace=True):
        if line.strip().startswith('var serverIP = '):
            line = "        var serverIP = \"" + socket.gethostbyname(socket.gethostname()) + "\";\n"
        sys.stdout.write(line)

    Thread(target=multicast_thread, daemon=True).start()
    Thread(target=data_listener_thread, daemon=True).start()
    Thread(target=http_server, daemon=True).start()
    Thread(target=sensor_finder_thread, daemon=True).start()

    start_server = websockets.serve(connection_handler, "", 6789)

    asyncio.get_event_loop().run_until_complete(start_server)
    asyncio.get_event_loop().run_forever()