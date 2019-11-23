# Container Instrumentation

You can instrument a Java-based container to use the [OpenTracing Special Agent](https://github.com/opentracing-contrib/java-specialagent) by following the steps on this page.

## Sample Dockerfile

For simplicity, the steps below assume a sample Dockerfile as follows:

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

export JAVA_ARGS="-Xmx4G"
java $JAVA_ARGS -jar /app/my-java-application.jar
```

Follow the steps below to instrument the container.


## 1. Edit the Dockerfile to Download the OpenTracing Special Agent JAR File

Edit the sample Dockerfile as follows:

```
FROM ubuntu:18.04

RUN apt-get -y update
RUN apt-get install -y openjdk-8-jdk

COPY target/my-java-application.jar /app/my-java-application.jar

# Install wget
RUN apt-get-install -y wget

# Download the latest stable release of the Special Agent as per https://github.com/opentracing-contrib/java-specialagent
wget -O /app/opentracing-specialagent-1.5.1.jar "http://central.maven.org/maven2/io/opentracing/contrib/specialagent/opentracing-specialagent/1.5.1/opentracing-specialagent-1.5.1.jar"

ENTRYPOINT /bin/bash run.sh
```

## 2. Edit the Java Command to Statically Include the OpenTracing Special Agent JAR File
Edit the `run.sh` file as follows:

```
#!/bin/bash

export JAVA_ARGS="-Xmx4G"
java $JAVA_ARGS \
    -javaagent:/app/opentracing-specialagent-1.5.1.jar \
    -Dsa.tracer=wavefront \
    -Dwf.application=myApplication \
    -Dwf.service=myService \
    -Dwf.reportingMechanism=direct \
    -Dwf.server=https://<YOUR_CLUSTER>.wavefront.com \
    -Dwf.token=<YOUR_WAVEFRONT_TOKEN> \
    -jar /app/my-java-application.jar
```
Refer to the [documentation](https://github.com/wavefrontHQ/wavefront-opentracing-bundle-java#parameters) for all the parameters supported by the Wavefront tracer.
