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


def remove_chart(chart_id):
    chart = NAME_PREFIX + str(chart_id) + CHART_EXT
    os.remove(chart)


def rewrite(chart_id, values):
    dat = NAME_PREFIX + str(chart_id) + INFO_EXT
    if os.path.isfile(dat):
        data_file = open(dat, "r")
        json_data = json.load(data_file)
        widgets_n = int(json_data["widgetsNumber"])
        for i in range(widgets_n):
            widget = json_data["widget" + str(i)]
            widget["value"] = values[widget["name"]]
            json_data["widget" + str(i)] = widget
        # print json_data
        data_file.close()
        data_file = open(dat, "w")
        data_file.write(json.dumps(json_data))
        data_file.close()


class MyTCPServerHandler(SocketServer.BaseRequestHandler):
    def update_charts(self, data, is_rewrite=False):
        func_id = data["funcId"]
        func, args = functions_[func_id]
        related_charts = tuple(map(int, str(data["relatedCharts"][1:-1]).split(',')))
        arg_n = data["argN"]
        for i in range(arg_n):
            arg = json.loads(data["arg" + str(i)])
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
            args[argname] = value
        # print args
        if is_rewrite:
            for chart in related_charts:
                # print "refreshing: " + str(chart)
                rewrite(chart, args)
        if not is_rewrite:
            for chart in related_charts:
                # print "removing " + str(chart)
                remove_chart(chart)
        if not is_rewrite:
            func(**args)
            functions_[func_id] = (func, args)
        self.request.send("OK")

    def handle(self):
        try:
            data = json.loads(self.request.recv(1024).strip())
            if data["cmd"] == "update":
                # print "updating"
                self.update_charts(data)
            if data["cmd"] == "finish":
                # print "finishing"
                self.server.shutdown()
            if data["cmd"] == "rewrite":
                # print "rewriting"
                self.update_charts(data, True)
        except Exception, e:
            print "Exception wile receiving message: ", e
