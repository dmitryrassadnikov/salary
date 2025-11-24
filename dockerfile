FROM radut/openjdk-21:latest
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests
RUN cp target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
