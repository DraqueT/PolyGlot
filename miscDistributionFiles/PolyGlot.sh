#!/bin/bash

basedir=$(dirname $(readlink -f $0))

java -jar $basedir/PolyGlot.jar
