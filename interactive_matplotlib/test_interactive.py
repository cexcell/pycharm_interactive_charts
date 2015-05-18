from interactive_matplotlib import interactive
import matplotlib
from matplotlib import pylab as plt
import numpy as np

x = np.arange(0, 10, 0.1)


def f(b, c, d):
    plt.xlabel(c)
    plt.ylabel(d)
    plt.plot(x, np.sin(b*x))
    plt.show()
    plt.xlabel(c)
    plt.ylabel(d)
    plt.plot(x, np.tan(b*x))
    plt.show()
    plt.xlabel(c)
    plt.ylabel(d)
    plt.plot(x, x ** b)
    plt.show()


# plt.plot(x, np.sin(x))
# plt.ylim([-2, 2])
# plt.show()
#
# plt.plot(x, np.sin(x))
# plt.show()
interactive(f, b=(1,10,1), c="First Axis", d="Second Axis")