#!/bin/sh
docker run -it --rm -v $PWD:/app -w /app mozilla/sbt:8u292_1.5.7 sbt "project backend" assembly
docker build -t rlemmens/youtube-sync:latest .
