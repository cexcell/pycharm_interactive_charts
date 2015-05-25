Pycharm interactive charts is a plugin for PyCharm IDE that allows to use interactie features of matplotlib.

Here's a description of how to use this plugin

## Basic
For example, you have a foo function that draw some function's chart, for example, sinus.

```Python
from matplotlib import pylab as plt
import numpy as np

def foo():
    x = np.arange(0, 10, 0.02)
    plt.plot(x, np.sin(x))
    plt.show()

if __name__ == "__main__":
    foo()
```

You will see something like that:
![Non -interactive](http://drive.google.com/uc?export=view&id=0B6IyJYTLLzzbNGVOb2k2am9UNzA)

But if you decide that you want not only `sin(x)` function, but `sin(2*x)`, `sin(3*x)` and so on.
Of course, you can do make this kind of code

```Python
from matplotlib import pylab as plt
import numpy as np

def foo(i):
    x = np.arange(0, 10, 0.02)
    plt.plot(x, np.sin(i * x))
    plt.show()

if __name__ == "__main__":
    for i in range(1, 10):
        foo(i)
```

But that's quite ugly, doesn't it? You will see all 9 images of `sin(i*x)` function, but only one at a time and after you close it, it will have never been shown again. Also, you can stack all this charts into figure, but that need some skill :-)

With this plugin, it can be done in more elegant way

```Python
from interactive_matplotlib import interactive
from matplotlib import pylab as plt
import numpy as np

def foo(i):
    x = np.arange(0, 10, 0.02)
    plt.plot(x, np.sin(i * x))
    plt.show()

if __name__ == "__main__":
    interactive(foo, i=(1,10))
```

Simple enough. Now, you can just drag the slider, that represents `i` parameter and see how your sinus function chart changes as you change your parameter, like on pictures below:

![Interactive-1-1](http://drive.google.com/uc?export=view&id=0B6IyJYTLLzzbdWlIVHo3bUdkMnc)

![Interactive-1-2](http://drive.google.com/uc?export=view&id=0B6IyJYTLLzzbT05raTZVNnVZSFU)

To end interactive session, just press refresh button.

## Advanced

Also, if you want so, you can display several charts. You can just call `plt.plot()` with `plt.show()` as many times as you need to. As a result, all charts will display with the same amount of controlling widgets.

```Python
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
```
Now, you can simply switch between charts using left or right arrows.

![Interactive-2-1](http://drive.google.com/uc?export=view&id=0B6IyJYTLLzzbdlpldkp2ZjF3Y2c)

![Interactive-2-2](http://drive.google.com/uc?export=view&id=0B6IyJYTLLzzbVlJneHdQMjE1TGs)

![Interactive-2-3](http://drive.google.com/uc?export=view&id=0B6IyJYTLLzzbNXBfZkJZbFNKN2c)

That's all. Good luck!
