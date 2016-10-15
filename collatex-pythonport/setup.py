#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import sys


try:
    from setuptools import setup
except ImportError:
    from distutils.core import setup

if sys.argv[-1] == 'publish':
    os.system('python setup.py sdist upload')
    sys.exit()

readme = open('README.rst').read()
history = open('HISTORY.rst').read().replace('.. :changelog:', '')

setup(
    name='collatex',
    version='2.0.0',
    description='CollateX is a collation tool.',
    long_description=readme + '\n\n' + history,
    author='Ronald Haentjens Dekker',
    author_email='ronald.dekker@huygens.knaw.nl',
    url='https://github.com/interedition/collatex',
    packages=[
        'collatex','ClusterShell'
    ],
    package_dir={'collatex':
                 'collatex'},
    include_package_data=True,
    install_requires=['networkx','prettytable'
    ],
    license="GPLv3",
    zip_safe=False,
    keywords='CollateX',
    classifiers=[
        'Development Status :: 4 - Beta',
        'Intended Audience :: Developers',
        'License :: OSI Approved :: GNU General Public License v3 (GPLv3)',
        'Natural Language :: English',
        'Programming Language :: Python :: 2.7',
        'Programming Language :: Python :: 3',
        'Programming Language :: Python :: 3.3',
        'Programming Language :: Python :: 3.5',
    ],
    test_suite='tests',
)
