from interactive_matplotlib import interactive
import matplotlib
from matplotlib import pylab as plt
import numpy as np

x = np.arange(0, 10, 0.1)


def f(k, b=4):
    if k:
        plt.plot(x, x ** b)
    else:
        plt.plot(x, np.sin(x) + b)
    plt.show()

# plt.plot(x, np.sin(x))
# plt.ylim([-2, 2])
# plt.show()
#
# plt.plot(x, np.sin(x))
# plt.show()

interactive(f, k=True, b=(1, 4, 1))