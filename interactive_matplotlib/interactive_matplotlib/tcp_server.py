__author__ = 'cexcell'
import SocketServer
import json

functions_ = {}


def add_function(unique_id, f):
    functions_[unique_id] = f


class MyTCPServer(SocketServer.ThreadingTCPServer):
    allow_reuse_address = True


class MyTCPServerHandler(SocketServer.BaseRequestHandler):
    def handle(self):
        try:
            data = json.loads(self.request.recv(1024).strip())
            # process the data, i.e. print it:
            print data
            # send some 'ok' back
            self.request.sendall(json.dumps({'return': 'ok'}))
        except Exception, e:
            print "Exception wile receiving message: ", e
