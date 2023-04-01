#!/usr/bin/python3

# https://pimylifeup.com/raspberry-pi-distance-sensor/
# https://www.emqx.com/en/blog/how-to-use-mqtt-in-python
# https://roboticsbackend.com/raspberry-pi-gpio-interrupts-tutorial/
# https://www.maketecheasier.com/piezo-speaker-raspberry-pi/

import sys
import time
import json
import random
import socket
import RPi.GPIO as GPIO
from message import Message
from threading import Thread
from message_type import MessageType
from discover_data import DicoverData
from paho.mqtt import client as mqtt_client

PIN_TRIGGER = 7     # HC-SR04 trigger pin
PIN_ECHO = 11       # HC-SR04 echo pin
PIN_START_STOP_BUTTON = 13     # start/stop button pin
PIN_PIEZO = 15      # piezo pin
PIN_PAIR_BUTTON = 19        # pair button pin

SERVER = 'localhost'        # mqtt broker address
PORT = 1883     # mqtt broker port
TOPIC = 'sensor/values'     # mqtt topic for sensor values
CLIENT_ID = f'python-mqtt-{random.randint(0, 1000)}'        # mqtt client id

OPENED_MIN_CM = 10      # greater distance will be considered as opened door

UDP_PORT = 52375     # UDP port for pairing
MAX_PAIR_ATTEMPTS = 30      # max pair packet count to send before stopping
COMMAND_TOPIC = 'sensor/commands'       # for commands from client

GPIO.setmode(GPIO.BOARD)
GPIO.setup(PIN_TRIGGER, GPIO.OUT)
GPIO.setup(PIN_ECHO, GPIO.IN)
GPIO.output(PIN_TRIGGER, GPIO.LOW)
GPIO.setup(PIN_START_STOP_BUTTON, GPIO.IN, pull_up_down=GPIO.PUD_UP)
GPIO.setup(PIN_PIEZO, GPIO.OUT)
GPIO.setup(PIN_PAIR_BUTTON, GPIO.IN, pull_up_down=GPIO.PUD_UP)

print('Waiting for sensor to settle...')
time.sleep(2)
print('Done.')

# piezo beep


def beep(count, duration):
    pin = GPIO.PWM(PIN_PIEZO, 520)      # pwm instance
    for x in range(count):
        pin.start(50)       # start with duty cycle 50
        pin.ChangeFrequency(520)
        GPIO.output(PIN_PIEZO, GPIO.HIGH)
        time.sleep(duration)
        pin.stop()
        GPIO.output(PIN_PIEZO, GPIO.LOW)
        if x != count - 1:
            time.sleep(0.3)

# mqtt callback methods


def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print('Connected to MQTT broker.')
        beep(1, 0.25)
    else:
        print('Failed to connect to MQTT broker, return code', rc)
        beep(3)
        sys.exit()


def on_disconnect(client, userdata, rc):
    print('Disconnected from MQTT broker, return code', rc)
    beep(3, 0.25)


def on_publish(client, userdata, result):
    print('Message sent.')


# mqtt client
mqtt = mqtt_client.Client(CLIENT_ID)
mqtt.on_connect = on_connect
mqtt.on_disconnect = on_disconnect
mqtt.on_publish = on_publish


def start_mqtt():
    try:
        print('Connecting to MQTT broker...')
        mqtt.connect(SERVER, PORT)
        mqtt.loop_start()

        # measured distance
        def get_distance():
            # sensor trigger
            GPIO.output(PIN_TRIGGER, GPIO.HIGH)
            # HC-SR04 distance sensor requires a pulse of 1 nanosecond to trigger it
            time.sleep(0.00001)
            GPIO.output(PIN_TRIGGER, GPIO.LOW)

            # get start and end time
            while GPIO.input(PIN_ECHO) == 0:
                pulse_start_time = time.time()
            while GPIO.input(PIN_ECHO) == 1:
                pulse_end_time = time.time()

            # calculate and return distance
            pulse_duration = pulse_end_time - pulse_start_time
            return round(pulse_duration * 17150, 2)

        # current status
        start_distance = get_distance()
        current_status = MessageType.doorClosed
        if start_distance > OPENED_MIN_CM:
            current_status = MessageType.doorOpened
        print('Current status: ' + current_status + '.')
        # previous status (for next loop round)
        previous_status = current_status

        print('Waiting for 1 second...')
        time.sleep(1)
        print('Started.')

        global run_mqtt
        while run_mqtt:
            distance = get_distance()

            # is door opened
            if distance > OPENED_MIN_CM:
                current_status = MessageType.doorOpened     # opened
            else:
                current_status = current_status.doorClosed      # closed

            if current_status != previous_status:       # current status changed
                print('Current status: ' + current_status + '.')
                # is connected
                if mqtt.is_connected():
                    message = Message(current_status)
                    json_string = json.dumps(message.__dict__)
                    mqtt.publish(TOPIC, json_string)       # sends message
                else:
                    print('Not connected to MQTT broker!')

            # setting previous status
            previous_status = current_status
            # delay until next round
            time.sleep(0.25)

    except Exception as e:
        print('Error:', e)
    finally:
        print('Stopped.')
        beep(2, 0.25)


global run_mqtt
run_mqtt = True

global mqtt_thread
mqtt_thread = Thread(target=start_mqtt, name='mqtt')
mqtt_thread.start()


def start_stop_button_listener():
    pressed = False
    while True:
        # button is pressed when pin is LOW
        if not GPIO.input(PIN_START_STOP_BUTTON):
            if not pressed:
                print('On/off button pressed!')
                pressed = True
                global mqtt_thread
                global run_mqtt
                if mqtt_thread.is_alive():
                    print('Stopping...')
                    run_mqtt = False
                else:
                    print('Starting...')
                    run_mqtt = True
                    mqtt_thread = Thread(
                        target=start_mqtt, name='mqtt')
                    mqtt_thread.start()
                time.sleep(2)
        # button not pressed (or released)
        else:
            pressed = False
        time.sleep(0.1)


start_stop_button_thread = Thread(
    target=start_stop_button_listener, name='startStopButton')


def mqtt_subscribe():
    def on_message(client, userdata, msg):
        print(f'Received message from `{msg.topic}` topic')

        # json string to object: https://stackoverflow.com/questions/15476983/deserialize-a-json-string-to-an-object-in-python
        def as_message(dct):
            return Message(dct['type'])

        data = json.loads(msg.payload.decode(), object_hook=as_message)
        if data.type == MessageType.stopBroadcast:
            global broadcasting
            broadcasting = False

    mqtt.subscribe(COMMAND_TOPIC)
    mqtt.on_message = on_message


def broadcast_pair_packet():
    global broadcasting
    broadcasting = True

    # https://tecadmin.net/python-how-to-find-local-ip-address/
    def get_local_ip():
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        try:
            # doesn't even have to be reachable
            s.connect(('192.255.255.255', 1))
            ip = s.getsockname()[0]
        except:
            ip = '127.0.0.1'
        finally:
            s.close()
        print(ip)
        return ip

    # https://github.com/ninedraft/python-udp
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    s.settimeout(1)
    obj = DicoverData(socket.gethostname(), get_local_ip())
    i = 0
    while True:
        if i == MAX_PAIR_ATTEMPTS or broadcasting == False:
            break

        print('Broadcasting pairing packet ' + str(i + 1) + '.')
        s.sendto(str.encode(json.dumps(obj.__dict__)),
                 ('255.255.255.255', UDP_PORT))

        i += 1
        time.sleep(1)
    print('Pairing stopped.')
    broadcasting = False


global broadcasting
broadcasting = False
broadcast_thread = Thread(target=broadcast_pair_packet, name='broadcast')


def pair_button_listener():
    pressed = False
    while True:
        # button is pressed when pin is LOW
        if not GPIO.input(PIN_PAIR_BUTTON):
            if not pressed:
                if broadcasting == False:
                    broadcast_thread = Thread(
                        target=broadcast_pair_packet, name='broadcast')
                    broadcast_thread.start()
                    beep(1, 0.75)
        # button not pressed (or released)
        else:
            pressed = False
        time.sleep(0.1)


pair_button_thread = Thread(target=pair_button_listener, name='pairButton')

time.sleep(2)
if mqtt_thread.is_alive():
    start_stop_button_thread.start()
    mqtt_subscribe()
    pair_button_thread.start()
