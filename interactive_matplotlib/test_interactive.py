from interactive_matplotlib import interactive
import numpy as np
from matplotlib import pylab as plt

x = np.arange(0, 10, 0.02)

def f(b, c):
    plt.xlabel(c)
    plt.plot(x, np.sin(b*x))
    plt.show()
    plt.xlabel(c)
    plt.plot(x, np.tan(b*x))
    plt.show()
    plt.xlabel(c)
    plt.plot(x, x ** b)
    plt.show()

interactive(f, b=(1,11,5), c="First Axis")