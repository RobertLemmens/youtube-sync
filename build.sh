#!/bin/sh
docker run -it --rm -v $PWD:/app -w /app mozilla/sbt sbt "project backend" assembly
docker build -t rlemmens/youtube-sync:latest .
