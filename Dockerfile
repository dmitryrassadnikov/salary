# Базовый образ с Java
FROM openjdk:21

# Устанавливаем рабочую директорию
WORKDIR d:\_QA_Automation\_GIT\salary\

# Копируем jar-файл
COPY target/salary-calc-0.0.1.jar /app.jar

# Открываем порт
EXPOSE 8080

# Команда для запуска
CMD ["java", "-jar", "app.jar"]
