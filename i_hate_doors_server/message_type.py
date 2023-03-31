from enum import Enum


class MessageType(str, Enum):
    stopBroadcast = 'stopBroadcast',
    doorOpened = 'doorOpened',
    doorClosed = 'doorClosed'
