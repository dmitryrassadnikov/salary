FROM radut/openjdk-21:latest

USER root

COPY target/salary-calc-0.0.1.jar app.jar

EXPOSE 8080
ENTRYPOINT java ${JAVA_OPTS:--XX:MaxRAMPercentage=90.0} -Dfile.encoding=UTF-8 -jar app.jar