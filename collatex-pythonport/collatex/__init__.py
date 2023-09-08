#!/usr/bin/env python
# -*- coding: utf-8 -*-

__name__ = 'CollateX'
__author__ = 'Ronald Haentjens Dekker'
__email__ = 'ronald.dekker@di.huc.knaw.nl'
__version__ = '2.3'


from collatex.core_classes import Collation
from collatex.core_functions import collate

__all__ = ["Collation", "collate"]
