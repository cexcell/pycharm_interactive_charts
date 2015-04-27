__author__ = 'cexcell'
from tcp_server import add_function, MyTCPServer, MyTCPServerHandler
from abc import abstractmethod
from utils import check_directory_charts
from params import CHART_DIR, HOST, PORT
import json


class Widget(object):
    def __init__(self, name, value):
        self.name_ = name
        self.value_ = value

    def get_value(self):
        return self.value_

    @abstractmethod
    def serialize(self):
        return {"widgetType": "Widget", "name": self.name_, "value": self.value_}
        # file.write(json.dumps(data))
        # file.write("Widget:")
        # file.write(self.name_ + ",")
        # file.write(str(self.value_) + "\n")


class WidgetInt(Widget):
    def __init__(self, name, values):
        if len(values) == 3:
            self.min_, self.max_, self.step_ = values
        else:
            self.min_, self.max_ = values
            self.step_ = 1
        super(WidgetInt, self).__init__(name, (self.min_ + self.max_) / 2)

    def serialize(self):
        return {"widgetType": "WidgetInt", "name": self.name_, "min": self.min_, "max": self.max_, "step": self.step_,
                "value": self.value_}
        # file.write(json.dumps(data))
        # file.write("WidgetInt:")
        # file.write(str(self.name_) + ",")
        # file.write(str(self.min_) + ",")
        # file.write(str(self.max_) + ",")
        # file.write(str(self.step_) + ",")
        # file.write(str(self.value_) + "\n")


class WidgetText(Widget):
    def serialize(self):
        return {"widgetType": "WidgetText", "name": self.name_, "value": self.value_}
        # file.write(json.dumps(data))
        # file.write("WidgetText:")
        # file.write(self.name_ + ",")
        # file.write(self.value_ + "\n")


class WidgetBool(Widget):
    def __init__(self, name, val):
        super(WidgetBool, self).__init__(name, val)

    def serialize(self):
        return {"widgetType": "WidgetBool", "name": self.name_, "value": self.value_}
        # file.write(json.dumps(data))
        # file.write("WidgetBool:")
        # file.write(self.name_ + ",")
        # file.write(str(self.value_) + ",")


def parse_widget(k, v):
    if isinstance(v, tuple):
        return WidgetInt(k, v)
    if isinstance(v, bool):
        return WidgetBool(k, v)
    if isinstance(v, int):
        return WidgetInt(k, (0, v + v))
    return WidgetText(k, v)


def write_list(xs, file):
    for x in xs:
        file.write(str(x) + ",")
    file.write("\n")


def write_widgets_info(start_id, end_id, widgets):
    from params import NAME_PREFIX, INFO_EXT

    connected_image = [i for i in range(start_id, end_id)]
    for i in range(start_id, end_id):
        with open(NAME_PREFIX + str(i) + INFO_EXT, 'w') as output:
            data = {"funcId": end_id, "relatedCharts": connected_image, "widgetsNumber": len(widgets)}
            # output.write(str(end_id) + "\n")
            # write_list(connected_image, output)
            # output.write(str(len(widgets)) + "\n")
            for j, widget in enumerate(widgets):
                data["widget" + str(j)] = widget.serialize()
            output.write(json.dumps(data))


def interactive(func, **kwargs):
    widgets = []
    for k, v in kwargs.iteritems():
        widgets.append(parse_widget(k, v))
    default_values_dict = {}
    for widget in widgets:
        default_values_dict[widget.name_] = widget.get_value()
    orig_charts_name_list = check_directory_charts(CHART_DIR)

    # first call
    func(**default_values_dict)

    modified_charts_name_list = check_directory_charts(CHART_DIR)
    id = len(modified_charts_name_list)
    charts_created = id - len(orig_charts_name_list)
    write_widgets_info(id - charts_created, id, widgets)
    add_function(id, (func, default_values_dict))
    server = MyTCPServer((HOST, PORT), MyTCPServerHandler)
    server.serve_forever()