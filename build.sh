#!/bin/bash

executableName="randpx"

gcc main.c -o $executableName $(sdl2-config --cflags --libs)
