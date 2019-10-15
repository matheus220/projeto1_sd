#!/usr/bin/env python

import os
import sys
import json
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
import sensor_pb2;

logging.basicConfig()

SENSORS = {}

USERS = set()

_sensor = sensor_pb2.Sensor()
_sensors = {}

PORT = 80
Handler = http.server.SimpleHTTPRequestHandler

web_dir = os.path.join(os.path.dirname(__file__), 'web_interface')
os.chdir(web_dir)


def state_event():
    sensors = {}    
    #for key, value in SENSORS.items():
    #    sensors[value['sensor_id']] = value
    #return json.dumps({"type": "data", "sensors": sensors})
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


async def counter(websocket, path):
    # register(websocket) sends user_event() to websocket
    serverSock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    serverSock.bind(("", 5004))

    await register(websocket)
    try:
        await websocket.send(state_event().SerializeToString())
        async for message in websocket:
            comm = sensor_pb2.Command()
            comm.ParseFromString(message)
            for key, value in SENSORS.items():
                if(value['sensor_id'] == comm.id):
                    serverSock.sendto(comm.command.encode(), key)
    finally:
        await unregister(websocket)


def http_server():
    with socketserver.TCPServer(("", PORT), Handler) as httpd:
        print(f"Serving at {socket.gethostbyname(socket.gethostname())}:{PORT}")
        httpd.serve_forever()        


def multicast_thread():
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    print("Waiting for group communication")

    loop.run_until_complete(multicast_handler())
    loop.close()


async def multicast_handler():

    multicast_group = '239.0.1.2'
    port_group = 20480
    server_address = ('', port_group)
    # Create the socket
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    # Bind to the server address
    sock.bind(server_address)

    # Tell the operating system to add the socket to the multicast group
    # on all interfaces.
    group = socket.inet_aton(multicast_group)
    mreq = struct.pack('4sL', group, socket.INADDR_ANY)
    sock.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)

    sock.sendto('SERVER'.encode(), (multicast_group, port_group))    

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
                if(sensor_id not in SENSORS.keys()):                    
                    SENSORS[(sensor_ip, sensor_port)] = {'sensor_id': sensor_id, 'type': sensor_type, 'last_msg_date':  datetime.now().strftime("%H:%M:%S")}
                else:
                    SENSORS[(sensor_ip, sensor_port)]['last_msg_date'] = datetime.now().strftime("%H:%M:%S")
                await notify_state()
                print('Sensor identified: {}'.format(sensor_id))


def serialize_obj(obj,_,address,port,data):
    typ = _['sensor_id'].split('_')[1]
    obj.id = _['sensor_id']
    obj.addr = address
    obj.port = port
    if data:
        obj.data = json.dumps(data)
    obj.last_msg_date = str(datetime.now().strftime("%H:%M:%S"))
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
            SENSORS[addr]['last_msg_date'] = datetime.now().strftime("%H:%M:%S");
            if(SENSORS[addr]['type'] == 'LIGHT'):
                SENSORS[addr]['data'] = float(data.decode("UTF-8"))
            elif(SENSORS[addr]['type'] == 'MAGNETIC'):
                split = data.decode("UTF-8").split(',')
                SENSORS[addr]['data'] = {'x': split[0], 'y': split[1], 'z': split[2]}
            elif(SENSORS[addr]['type'] == 'LED'):
                SENSORS[addr]['data'] = data.decode("UTF-8")
            elif(SENSORS[addr]['type'] == 'SOUND'):
                split = data.decode("UTF-8").split(',')
                SENSORS[addr]['data'] = {'status': split[0], 'volume': split[1]}
        await notify_state()
        print("New UDP message from ", addr)
        print(data.decode("UTF-8"))


if __name__ == '__main__':
    for line in fileinput.input(['index.html'], inplace=True):
        if line.strip().startswith('var serverIP = '):
            line = "        var serverIP = \"" + socket.gethostbyname(socket.gethostname()) + "\"\n"
        sys.stdout.write(line)

    Thread(target=multicast_thread, daemon=True).start()
    Thread(target=data_listener_thread, daemon=True).start()
    Thread(target=http_server, daemon=True).start()

    start_server = websockets.serve(counter, "", 6789)

    asyncio.get_event_loop().run_until_complete(start_server)
    asyncio.get_event_loop().run_forever()