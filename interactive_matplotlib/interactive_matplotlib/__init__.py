import sys
import os

backend_name = 'backend_interagg'
sys.path.append(os.getcwd() + '/interactive_matplotlib')
import matplotlib

matplotlib.use('module://' + backend_name)