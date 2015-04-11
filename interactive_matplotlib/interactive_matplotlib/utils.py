__author__ = 'cexcell'
import os


def check_charts_directory(path):
    return filter(lambda a: a.endswith(".png"), os.listdir(path))