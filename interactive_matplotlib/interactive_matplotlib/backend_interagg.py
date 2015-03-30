from matplotlib.backends.backend_agg import FigureCanvasAgg
from matplotlib.backend_bases import FigureManagerBase, ShowBase
from matplotlib._pylab_helpers import Gcf
from matplotlib.figure import Figure
import matplotlib
import os


NAME_PREFIX = ".idea/charts/picharts"
CHART_DIR = ".idea/charts"

rcParams = matplotlib.rcParams
verbose = matplotlib.verbose

# Interactive backend for pycharm
# Now as simple as .... BUT!
# in the future i'm going to implement more usefull features like redraw after changing parameters


def get_last_chart_idx():
    return len([name for name in os.listdir(CHART_DIR) if os.path.isfile(os.path.join(CHART_DIR, name))])


def draw_if_interactive():
    if matplotlib.is_interactive():
        figManager = Gcf.get_active()
        if figManager is not None:
            figManager.show()


show = ShowBase()


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

    def show(self):
        chart_last_idx = get_last_chart_idx()
        FigureCanvasAgg.print_png(self, NAME_PREFIX + str(chart_last_idx))


class FigureManagerInterAgg(FigureManagerBase):
    def __init__(self, canvas, num):
        FigureManagerBase.__init__(self, canvas, num)
        self.canvas = canvas
        self._num = num
        self._shown = False

    def show(self):
        self.canvas.show()
        Gcf.destroy(self._num)


########################################################################
#
# Now just provide the standard names that backend.__init__ is expecting
#
########################################################################

FigureCanvas = FigureCanvasInterAgg
FigureManager = FigureManagerInterAgg