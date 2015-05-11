from interactive_matplotlib import interactive
import matplotlib
from matplotlib import pylab as plt
import numpy as np

x = np.arange(0, 10, 0.1)


def f(b, c):
    plt.xlabel(c)
    plt.plot(x, np.sin(b*x))
    plt.show()
    plt.ylabel(c)
    plt.plot(x, np.tan(b*x))
    plt.show()

# plt.plot(x, np.sin(x))
# plt.ylim([-2, 2])
# plt.show()
#
# plt.plot(x, np.sin(x))
# plt.show()

interactive(f, b=(1.2, 4.6, 0.1), c="Xlabel")