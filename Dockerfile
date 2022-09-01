FROM openjdk:11
COPY target/*.jar configmap.jar
CMD java ${JAVA_OPTS} -jar configmap.jar
