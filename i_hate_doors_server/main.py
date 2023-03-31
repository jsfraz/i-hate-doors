#!/usr/bin/python3

# https://pimylifeup.com/raspberry-pi-distance-sensor/
# https://www.emqx.com/en/blog/how-to-use-mqtt-in-python
# https://roboticsbackend.com/raspberry-pi-gpio-interrupts-tutorial/
# https://www.maketecheasier.com/piezo-speaker-raspberry-pi/

import sys
import time
import json
import random
import RPi.GPIO as GPIO
from message import Message
from threading import Thread
from message_type import MessageType
from paho.mqtt import client as mqtt_client

PIN_TRIGGER = 7     # HC-SR04 trigger pin
PIN_ECHO = 11       # HC-SR04 echo pin
PIN_BUTTON = 13     # start/stop button pin
PIN_PIEZO = 15      # piezo pin

SERVER = 'localhost'        # mqtt broker address
PORT = 1883     # mqtt broker port
TOPIC = 'sensor/values'     # mqtt topic
CLIENT_ID = f'python-mqtt-{random.randint(0, 1000)}'        # mqtt client id

OPENED_MIN_CM = 10      # greater distance will be considered as opened door

GPIO.setmode(GPIO.BOARD)
GPIO.setup(PIN_TRIGGER, GPIO.OUT)
GPIO.setup(PIN_ECHO, GPIO.IN)
GPIO.output(PIN_TRIGGER, GPIO.LOW)
GPIO.setup(PIN_BUTTON, GPIO.IN, pull_up_down=GPIO.PUD_UP)
GPIO.setup(PIN_PIEZO, GPIO.OUT)

print('Waiting for sensor to settle...')
time.sleep(2)
print('Done.')

# piezo beep


def beep(count):
    pin = GPIO.PWM(PIN_PIEZO, 520)      # pwm instance
    for x in range(count):
        pin.start(50)       # start with duty cycle 50
        pin.ChangeFrequency(520)
        GPIO.output(PIN_PIEZO, GPIO.HIGH)
        time.sleep(0.5)
        pin.stop()
        GPIO.output(PIN_PIEZO, GPIO.LOW)
        if x != count - 1:
            time.sleep(0.3)

# mqtt callback methods


def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print('Connected to MQTT broker.')
        beep(1)
    else:
        print('Failed to connect to MQTT broker, return code', rc)
        beep(3)
        sys.exit()


def on_disconnect(client, userdata, rc):
    print('Disconnected from MQTT broker, return code', rc)
    beep(3)


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
        beep(2)


global run_mqtt
run_mqtt = True

global mqtt_thread
mqtt_thread = Thread(target=start_mqtt, name='mqtt')
mqtt_thread.start()


def start_button_listener():
    pressed = False
    while True:
        # button is pressed when pin is LOW
        if not GPIO.input(PIN_BUTTON):
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
                    mqtt_thread = mqtt_thread = Thread(
                        target=start_mqtt, name='mqtt')
                    mqtt_thread.start()
                time.sleep(2)
        # button not pressed (or released)
        else:
            pressed = False
        time.sleep(0.1)


button_thread = Thread(target=start_button_listener, name='button')
time.sleep(2)
if mqtt_thread.is_alive():
    button_thread.start()
