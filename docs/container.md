# Container Instrumentation

This document details the steps required to instrument a Java based container to use the opentracing [specialagent](https://github.com/opentracing-contrib/java-specialagent).

## Introduction

The steps below explain how to modify the Dockerfile for a Java application to automatically instrument your application using the opentracing specialagent.

For simplicity, we will assume a Dockerfile as follows:
```
FROM ubuntu:18.04

RUN apt-get -y update
RUN apt-get install -y openjdk-8-jdk

COPY target/my-java-application.jar /app/my-java-application.jar

ENTRYPOINT /bin/bash run.sh
```

And the contents of `run.sh`:
```
#!/bin/bash
java -jar /app/my-java-application.jar
```

Follow the steps below to instrument the container.


## 1. Edit the Dockerfile to Download the Specialagent JAR File

Edit the above Dockerfile as follows:

```
FROM ubuntu:18.04

RUN apt-get -y update
RUN apt-get install -y openjdk-8-jdk

COPY target/my-java-application.jar /app/my-java-application.jar

# Install wget
RUN apt-get-install -y wget

# Download the latest stable release of the specialagent as per https://github.com/opentracing-contrib/java-specialagent
wget -O /app/opentracing-specialagent-1.5.1.jar "http://central.maven.org/maven2/io/opentracing/contrib/specialagent/opentracing-specialagent/1.5.1/opentracing-specialagent-1.5.1.jar"

ENTRYPOINT /bin/bash run.sh
```

## 2. Edit the Java Command to Statically Include the Specialagent JAR File
Edit the `run.sh` file as follows:

```
#!/bin/bash
java -javaagent:/app/opentracing-specialagent-1.5.1.jar \
    -Dsa.tracer=wavefront \
    -Dwf.application=myApplication \
    -Dwf.service=myService \
    -Dwf.reportingMechanism=direct
    -Dwf.server=https://YOUR_CLUSTER.wavefront.com
    -Dwf.token=<YOUR_WAVEFRONT_TOKEN>
    -jar /app/my-java-application.jar
```
Refer to the [documentation](https://github.com/wavefrontHQ/wavefront-opentracing-bundle-java#parameters) for all the parameters supported by the Wavefront tracer.
