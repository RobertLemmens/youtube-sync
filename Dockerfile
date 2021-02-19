FROM registry.access.redhat.com/ubi8/openjdk-11
MAINTAINER Robert Lemmens

### Required OpenShift Labels
LABEL name="youtube-sync" \
      maintainer="robert lemmens" \
      version="0.0.1" \
      release="1" \
      summary="Sync youtube playback between browsers" \
      description="Sync youtube playback between browsers"

COPY backend/target/scala-2.12/backend-assembly-*.jar app.jar

CMD ["java", "-jar", "app.jar"]