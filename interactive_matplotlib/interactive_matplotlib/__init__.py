import SocketServer
import sys
import os
from params import HOST, PORT, CHART_DIR
from tcp_server import MyTCPHandler
from widgets import interactive

backend_name_ = 'backend_interagg'
sys.path.append(os.getcwd() + '/interactive_matplotlib')
import matplotlib

matplotlib.use('module://' + backend_name_)

# server = SocketServer.TCPServer((HOST, PORT), MyTCPHandler)
