#!/bin/sh
sudo docker run -it --rm -v $PWD:/app -w /app mozilla/sbt sbt "project backend" assembly
