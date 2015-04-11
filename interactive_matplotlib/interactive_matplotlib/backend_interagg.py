from matplotlib.backends.backend_agg import FigureCanvasAgg
from matplotlib.backend_bases import FigureManagerBase, ShowBase
from matplotlib._pylab_helpers import Gcf
from matplotlib.figure import Figure
from params import NAME_PREFIX, CHART_DIR
import matplotlib
import os

rcParams = matplotlib.rcParams
verbose = matplotlib.verbose

# Interactive backend for pycharm
# Now as simple as f... BUT!
# in the future i'm going to implement more usefull features like redraw after changing parameters


supported_widgets = {'x_scale':['slider', 'checker'],
                     'y_scale': ['slider', 'checker']}




def get_last_chart_idx():
    return len([name for name in os.listdir(CHART_DIR) if os.path.isfile(os.path.join(CHART_DIR, name))])


def draw_if_interactive():
    if matplotlib.is_interactive():
        figManager = Gcf.get_active()
        if figManager is not None:
            figManager.show()

class Show(ShowBase):
    def __call__(self, **kwargs):
        managers = Gcf.get_all_fig_managers()
        if not managers:
            return

        for manager in managers:
            manager.show(**kwargs)

    def mainloop(self):
        print "In da mainloop"


show = Show()


def new_figure_manager(num, *args, **kwargs):
    FigureClass = kwargs.pop('FigureClass', Figure)
    figure = FigureClass(*args, **kwargs)
    return new_figure_manager_given_figure(num, figure)


def new_figure_manager_given_figure(num, figure):
    canvas = FigureCanvasInterAgg(figure)
    manager = FigureManagerInterAgg(canvas, num)
    if matplotlib.is_interactive():
        manager.show()
    return manager


class FigureCanvasInterAgg(FigureCanvasAgg):
    def __init__(self, figure):
        FigureCanvasAgg.__init__(self, figure)

    def get_default_filetype(self):
        return 'png'

    def show(self, chart_num):
        FigureCanvasAgg.print_png(self, NAME_PREFIX + str(chart_num) + ".png")


def get_chart_name():
    pass


class FigureManagerInterAgg(FigureManagerBase):
    def __init__(self, canvas, num):
        FigureManagerBase.__init__(self, canvas, num)
        self.canvas = canvas
        self._num = num
        self._shown = False

    def show(self, **kwargs):
        chart_last_idx = get_last_chart_idx()
        # server = SocketServer.TCPServer((HOST, PORT), MyTCPHandler)
        # server.serve_forever()

        self.canvas.show(chart_last_idx)
        Gcf.destroy(self._num)


########################################################################
#
# Now just provide the standard names that backend.__init__ is expecting
#
########################################################################

FigureCanvas = FigureCanvasInterAgg
FigureManager = FigureManagerInterAgg