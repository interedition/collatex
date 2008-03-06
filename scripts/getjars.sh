#!/bin/sh
# get all the jar files from the specified directory ($1) and make a classpath string
# from them - separated by ':'
echo $1/*.jar | sed -e 's/ /:/g'
