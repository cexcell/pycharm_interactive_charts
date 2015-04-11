__author__ = 'cexcell'
from tcp_server import add_function
from abc import abstractmethod
from utils import check_charts_directory
from params import CHART_DIR


class Widget(object):
    def __init__(self, name, value):
        self.name_ = name
        self.value_ = value

    def get_value(self):
        return self.value_

    @abstractmethod
    def serialize(self, file):
        file.write("Widget:")
        file.write(self.name_ + ",")
        file.write(str(self.value_) + "\n")


class WidgetInt(Widget):
    def __init__(self, name, values):
        if len(values) == 3:
            self.min_, self.max_, self.step_ = values
        else:
            self.min_, self.max_ = values
            self.step_ = 1
        super(WidgetInt, self).__init__(name, (self.min_ + self.max_) / 2)

    def serialize(self, file):
        file.write("WidgetInt:")
        file.write(str(self.name_) + ",")
        file.write(str(self.min_) + ",")
        file.write(str(self.max_) + ",")
        file.write(str(self.step_) + ",")
        file.write(str(self.value_) + "\n")


class WidgetText(Widget):
    def serialize(self, file):
        file.write("WidgetText:")
        file.write(self.name_ + ",")
        file.write(self.value_ + "\n")


class WidgetBool(Widget):
    def __init__(self, name, val):
        super(WidgetBool, self).__init__(name, val)

    def serialize(self, file):
        file.write("WidgetBool:")
        file.write(self.name_ + ",")
        file.write(str(self.value_) + ",")


def parse_widget(k, v):
    if isinstance(v, tuple):
        return WidgetInt(k, v)
    if isinstance(v, bool):
        return WidgetBool(k, v)
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
            output.write(str(end_id) + "\n")
            write_list(connected_image, output)
            for widget in widgets:
                widget.serialize(output)


def interactive(func, **kwargs):
    widgets = []
    for k, v in kwargs.iteritems():
        widgets.append(parse_widget(k, v))
    default_values_dict = {}
    for widget in widgets:
        default_values_dict[widget.name_] = widget.get_value()
    orig_charts_name_list = check_charts_directory(CHART_DIR)
    func(**default_values_dict)
    modified_charts_name_list = check_charts_directory(CHART_DIR)
    id = len(modified_charts_name_list)
    charts_created = id - len(orig_charts_name_list)
    write_widgets_info(id - charts_created, id, widgets)
    add_function(id, func)