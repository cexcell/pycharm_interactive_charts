import interactive_matplotlib
import matplotlib
from matplotlib import pyplot as plt
print matplotlib.get_backend()

plt.plot([0, 1, 2, 3, 4, 5, 6])
plt.show()

plt.plot([0, 1, 2, 3, 4, 5], [0, 1, 4, 9, 16, 25])
plt.show()

plt.plot([0, 1, 2, 3, 4, 5, 6], [1, 1, 1, 1, 1, 1, 1])
plt.show()