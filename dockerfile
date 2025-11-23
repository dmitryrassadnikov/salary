# Используем официальный образ с Maven и JDK
FROM maven:3.9.4-eclipse-temurin-17 as build

# Рабочая директория в контейнере
WORKDIR /app

# Копируем pom.xml и загружаем зависимости (кешируется)
COPY pom.xml .
RUN mvn dependency:go-offline

# Копируем исходный код
COPY src ./src

# Собираем JAR (финальный артефакт)
RUN mvn package -DskipTests

# Переносим JAR в корневую директорию
RUN cp target/*.jar app.jar

# Открываем порт (укажите свой, например 8080)
EXPOSE 10000

# Команда запуска
CMD ["java", "-jar", "app.jar"]

