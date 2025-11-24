#
# Build stage
#

FROM maven:3.9.9-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean install


#
# Package stage
#

FROM radut/openjdk-21:latest
COPY --from=build /target/salary-calc-0.0.1.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
