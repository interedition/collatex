===============================
CollateX-Python
===============================
..
  .. image:: https://badge.fury.io/py/collatex.png
		:target: http://badge.fury.io/py/collatex

  .. image:: https://travis-ci.org/rhdekker/collatex.png?branch=master
		:target: https://travis-ci.org/rhdekker/collatex

  .. image:: https://pypip.in/d/collatex/badge.png
		:target: https://pypi.python.org/pypi/collatex


CollateX is a software to

- read multiple (>= 2) versions of a text, splitting each version into parts (tokens) to be compared,
- identify similarities of and differences between the versions (including moved/transposed segments) by aligning tokens, and
- output the alignment results in a variety of formats for further processing, for instance to support the production of a critical apparatus or the stemmatical analysis of a text's genesis.

* Free software: GPLv3 license
* Documentation: http://collatex.obdurodon.org/

Features
--------

* Non progressive multiple sequence alignment
* Multiple output formats: alignment table, variant graph
* Near matching (optional)
* Supports Python 3
* Supports unicode (Python 3 only)


How to install:
---------------

Mac/Linux:
sudo pip3 install collatex

if you don't have pip installed, install it first with:
sudo easy_install3 pip

For near matching functionality python-levenshtein C library is required.

Install it with (on Mac OS X and Linux):

sudo pip3 install python-levenshtein

Windows users need a precompiled binary distribution of this library if they want to use near matching.

Problem with newer version of CollateX:
AttributeError: 'DiGraph' object has no attribute 'edges_iter'

CollateX Python at the moment only works with networkx version 1.X.
Check the current version of networkx with 'pip show networkx'
If NetworkX version > 1.xx:
First uninstall the current networkx version by typing ‘pip uninstall networkx’ (without the quotes) and then install the working version by typing 'pip install -Iv networkx==1.11'without the qoutes.


Simple example:
---------------
::

  from collatex import *

  collation = Collation()
  collation.add_plain_witness("A", "The quick brown fox jumps over the dog.")
  collation.add_plain_witness("B", "The brown fox jumps over the lazy dog.")

  alignment_table = collate(collation)

Add
::

  print(alignment_table)

to show the results.

When running from the command shell run the example script with:
::

	python3 ./nameofscript.py



