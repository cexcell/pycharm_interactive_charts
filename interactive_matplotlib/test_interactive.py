from interactive_matplotlib import interactive
import matplotlib
from matplotlib import pylab as plt
import numpy as np

x = np.arange(0, 10, 0.1)


def f(k, b=4):
    plt.plot(x, x ** k + b)
    plt.show()
    plt.plot(x, x ** (k + 1) - b)
    plt.show()

plt.plot(x, np.sin(x))
plt.ylim([-2, 2])
plt.show()

plt.plot(x, np.sin(x))
plt.show()

# interactive(f, k=(1, 3), b=(2, 5, 2))