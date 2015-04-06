import interactive_matplotlib
import matplotlib
from matplotlib import pylab as plt
import numpy as np

x = np.arange(0, 10, 0.1)
plt.plot(x, np.sin(x))
plt.ylim([-2, 2])
plt.show()

plt.plot(x, np.sin(x))
plt.show()
