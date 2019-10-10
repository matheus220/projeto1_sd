#!/usr/bin/env python

import asyncio
import json
import logging
import websockets
import http.server
import socketserver
import socket
import os
import struct
import sys
from threading import Thread
from datetime import datetime

logging.basicConfig()

STATE = {"value": 0}

SENSORS = {}

USERS = set()

PORT = 80
Handler = http.server.SimpleHTTPRequestHandler

web_dir = os.path.join(os.path.dirname(__file__), 'web_interface')
os.chdir(web_dir)


def state_event():
    sensors = {}
    for key, value in SENSORS.items():
        sensors[value['sensor_id']] = value

    return json.dumps({"type": "data", "sensors": sensors})


def users_event():
    return json.dumps({"type": "users", "count": len(USERS)})


async def notify_state():
    if USERS:  # asyncio.wait doesn't accept an empty list
        message = state_event()
        await asyncio.wait([user.send(message) for user in USERS])


async def notify_users():
    if USERS:  # asyncio.wait doesn't accept an empty list
        message = users_event()
        await asyncio.wait([user.send(message) for user in USERS])


async def register(websocket):
    USERS.add(websocket)
    await notify_users()


async def unregister(websocket):
    USERS.remove(websocket)
    await notify_users()


async def counter(websocket, path):
    # register(websocket) sends user_event() to websocket
    await register(websocket)
    try:
        await websocket.send(state_event())
        async for message in websocket:
            data = json.loads(message)
            if data["action"] == "minus":
                STATE["value"] -= 1
                await notify_state()
            elif data["action"] == "plus":
                STATE["value"] += 1
                await notify_state()
            else:
                logging.error("unsupported event: {}", data)
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
            if(len(split) == 3):
                device_id = split[0]
                sensor_type = split[1]
                sensor_port = int(split[2])
                sensor_ip = address[0]
                sensor_id = device_id + "_" + sensor_type
                if(sensor_id not in SENSORS.keys()):
                    SENSORS[(sensor_ip, sensor_port)] = {'sensor_id': sensor_id, 'type': sensor_type, 'last_msg_date':  datetime.now().strftime("%H:%M:%S")}
                else:
                    SENSORS[(sensor_ip, sensor_port)]['last_msg_date'] = datetime.now().strftime("%H:%M:%S")
                await notify_state()
                print('New sensor identified: {}'.format(sensor_id))

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
            if(SENSORS[addr]['type'] == 'LIGHT'):
                SENSORS[addr]['data'] = float(data.decode("UTF-8"))
                SENSORS[addr]['last_msg_date'] = datetime.now().strftime("%H:%M:%S");
            elif(SENSORS[addr]['type'] == 'MAGNETIC'):
                split = data.decode("UTF-8").split(',')
                SENSORS[addr]['data'] = {'x': split[0], 'y': split[1], 'z': split[2]}
                SENSORS[addr]['last_msg_date'] = datetime.now().strftime("%H:%M:%S");
        await notify_state()
        print("New UDP message from ", addr)
        print(data.decode("UTF-8"))

if __name__ == '__main__':
    Thread(target=multicast_thread, daemon=True).start()
    Thread(target=data_listener_thread, daemon=True).start()
    Thread(target=http_server, daemon=True).start()

    start_server = websockets.serve(counter, "", 6789)

    asyncio.get_event_loop().run_until_complete(start_server)
    asyncio.get_event_loop().run_forever()