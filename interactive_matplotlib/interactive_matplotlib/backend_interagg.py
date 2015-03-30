from matplotlib.backends.backend_agg import FigureCanvasAgg
from matplotlib.backend_bases import FigureManagerBase, ShowBase
from matplotlib._pylab_helpers import Gcf
import matplotlib


rcParams = matplotlib.rcParams
verbose = matplotlib.verbose

# Interactive backend for pycharm
# Now as simple as .... BUT!
# in the future i'm going to implement more usefull features like redraw after changing parameters


def draw_if_interactive():
    if matplotlib.is_interactive():
        figManager = Gcf.get_active()
        if figManager is not None:
            figManager.show()


class Show(ShowBase):
    def __call__(self, block=None):
        managers = Gcf.get_all_fig_managers()
        if not managers:
            return

        for manager in managers:
            manager.show()


show = Show()


class FigureCanvasInterAgg(FigureCanvasAgg):
    def __init__(self, figure):
        FigureCanvasAgg.__init__(self, figure)

    def draw(self):
        self.show()

    def draw_idle(self):
        self.draw()

    def show(self):
        FigureCanvasAgg.print_png(self, "WHOOP.png")


class FigureManagerInterAgg(FigureManagerBase):
    def __init__(self, canvas, num):
        FigureManagerBase.__init__(self, canvas, num)
        self.canvas = canvas
        self._num = num
        self._shown = False

    def show(self):
        if not self._shown:
            self.canvas.show()
        self._shown = True