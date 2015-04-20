__author__ = 'cexcell'
import os
from params import INFO_EXT, CHART_EXT


def check_directory_charts(path):
    return filter(lambda a: a.endswith(CHART_EXT), os.listdir(path))


def check_directory_datas(path):
    return filter(lambda a: a.endswith(INFO_EXT), os.listdir(path))