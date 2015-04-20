import sys
import os

from widgets import interactive


backend_name_ = 'backend_interagg'
sys.path.append(os.getcwd() + '/interactive_matplotlib')
import matplotlib

matplotlib.use('module://' + backend_name_)
