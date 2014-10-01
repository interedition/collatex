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

- read multiple (≥ 2) versions of a text, splitting each version into parts (tokens) to be compared,
- identify similarities of and differences between the versions (including moved/transposed segments) by aligning tokens, and
- output the alignment results in a variety of formats for further processing, for instance to support the production of a critical apparatus or the stemmatical analysis of a text's genesis.

* Free software: GPLv3 license
* Documentation: http://collatex.rtfd.org.

Features
--------

* non progressive multiple sequence alignment
* multiple output formats: alignment table, variant graph
* near matching (optional)

How to install:
---------------

Mac/Linux:
sudo pip install --pre collatex

if you don't have pip installed, install it first with:
sudo easy_install pip

For near matching functionality python-levensthein C library is required.

Install it with (on Mac OS X and Linux):

sudo pip install python-levensthein

Windows users need a precompiled binary distribution of this library if they want to use near matching.

Windows:
There is no official Windows binary distribution of pygraphviz, which is needed for SVG
rendering of the variant graph. To add SVG support in Windows, before doing the above, 
install an "unofficial" Windows pygraphviz binary from the link at 
http://www.lfd.uci.edu/~gohlke/pythonlibs/, along with the main Graphviz file at the link
provided there. Then add the path to the graphviz installation (specifically, to dot.exe) 
to the system path.

Simple example:
---------------
::

  from collatex import *

  collation = Collation()
  collation.add_witness("A", "The quick brown fox jumps over the dog.")
  collation.add_witness("B", "The brown fox jumps over the lazy dog.")

  alignment_table = collate(collation)

When running from the command shell run the example script with:
::

	python ./nameofscript.py

When using IPython Notebook a nice HTML representation of the alignment table is shown when the collate function is called.
When using a textual Python prompt add
::

  print(alignment_table)
	
to show the results.
Output can also be shown as a graph instead of a table when graphviz and pygraphviz are installed:
::

  collate(collation, output="graph")
  


