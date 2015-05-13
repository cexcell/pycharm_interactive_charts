import os
import sys

import SocketServer
import json
from params import NAME_PREFIX, CHART_EXT, INFO_EXT


functions_ = {}


def add_function(unique_id, (f, arg)):
    functions_[unique_id] = (f, arg)


class MyTCPServer(SocketServer.ThreadingTCPServer):
    allow_reuse_address = True


def remove_and_rewrite(chart_id, name, value):
    chart = NAME_PREFIX + str(chart_id) + CHART_EXT
    dat = NAME_PREFIX + str(chart_id) + INFO_EXT
    if (os.path.isfile(dat)):
        data_file = open(dat, "r")
        json_data = json.load(data_file)
        widgets_n = int(json_data["widgetsNumber"])
        for i in range(widgets_n):
            widget = json_data["widget" + str(i)]
            if (name == widget["name"]):
                widget["value"] = value
                json_data["widget" + str(i)] = widget
        data_file.close()
        data_file = open(dat, "w")
        data_file.write(json.dumps(json_data))
        data_file.close()
    os.remove(chart)


class MyTCPServerHandler(SocketServer.BaseRequestHandler):
    def update_charts(self, data):
        func_id = data["funcId"]
        arg = json.loads(data["arg"])
        related_charts = tuple(map(int, str(data["relatedCharts"][1:-1]).split(',')))
        argname = str(arg["name"])
        value = arg["value"]
        type = arg["type"]
        if type == "WidgetFloat":
            value = float(value)
        elif type == "WidgetInt":
            value = int(value)
        elif type == "WidgetBool":
            value = True if value == "true" else False
        else:
            value = str(value)
        for chart in related_charts:
            remove_and_rewrite(chart, argname, value)
        func, args = functions_[func_id]
        args[argname] = value
        func(**args)
        self.request.send("OK")

    def handle(self):
        try:
            data = json.loads(self.request.recv(1024).strip())
            if (data["cmd"] == "update"):
                self.update_charts(data)
            if (data["cmd"] == "finish"):
                self.server.shutdown()
        except Exception, e:
            print "Exception wile receiving message: ", e
