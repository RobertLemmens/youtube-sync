#!/bin/sh
docker tag rlemmens/youtube-sync:latest gcr.io/$(gcloud config list --format 'value(core.project)' 2>/dev/null)/youtube-sync:latest
docker push gcr.io/$(gcloud config list --format 'value(core.project)' 2>/dev/null)/youtube-sync:latest
