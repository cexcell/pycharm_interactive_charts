__author__ = 'cexcell'
import SocketServer
from abc import ABCMeta, abstractmethod

class MyTCPHandler(SocketServer.BaseRequestHandler):
    """
    The RequestHandler class for our server.

    It is instantiated once per connection to the server, and must
    override the handle() method to implement communication to the
    client.
    """
    def handle(self):
        from matplotlib import pyplot as plt
        # self.request is the TCP socket connected to the client
        self.data = self.request.recv(1024).strip()
        print "{} wrote:".format(self.client_address[0])
        print self.data
        # just send back the same data, but upper-cased
        self.request.sendall(self.data.upper())

class Widget(object):
    def __init__(self, value):
        self.current_value = value

    @abstractmethod
    def get_value(self):
        return self.current_value

class WidgetInt(Widget):
    def __init__(self, start, end, step):
        self.start = start
        self.end = end
        self.step = step
        self.current_value = start + end / 2


class InteractiveChartsServer(object):
    def __init__(self, widgets):
        self.widgets = widgets
        self.handler = MyTCPHandler

def interactive(func, **kwargs):
    # TODO: need to create some widget parsing method that will
    # universal for all widgets
    widgets = []
    for k, v in kwargs:
        if v.startswith('(') and v.endswith(')'):
            start, end = tuple(map(int, k[1:-1].split(',')))
            step = 1
            widgets.append((k, WidgetInt(start, end, step)))
    server =