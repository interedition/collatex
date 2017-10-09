#!/usr/bin/env python
# -*- coding: utf-8 -*-

def unit_disabled(func):
    def wrapper(func):
        func.__test__ = False
        return func

    return wrapper
