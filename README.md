Развёртывание Java‑приложения на Maven через Docker на Render — пошаговый гайд.

1. Подготовка проекта
Убедитесь, что в проекте есть:

pom.xml (конфигурация Maven);

src/main/java/ (исходный код);

src/main/resources/ (конфиги, свойства).

2. Создайте Dockerfile в корне проекта
Пример для Maven‑проекта:

dockerfile
# Используем официальный образ с Maven и JDK
FROM maven:3.9-jdk-17-eclipse-temurin as builder


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
EXPOSE 8080

# Команда запуска
CMD ["java", "-jar", "app.jar"]
Пояснения:

maven:3.9-jdk-17-eclipse-temurin — образ с Maven 3.9 и JDK 17 (актуальная LTS‑версия).

mvn dependency:go-offline — кеширует зависимости для ускорения последующих сборок.

mvn package -DskipTests — собирает JAR без запуска тестов (можно убрать -DskipTests, если тесты нужны).

EXPOSE 8080 — укажите порт, на котором ваше приложение слушает запросы.

3. Создайте .dockerignore (опционально, но рекомендуется)
Файл .dockerignore в корне проекта:

.git
.gitignore
target/
!target/*.jar
logs/
*.log
Это ускорит сборку, исключив ненужные файлы.

4. Настройте Render
Войдите в панель управления Render ().

Нажмите New → Web Service.

Выберите репозиторий с вашим проектом (GitHub/GitLab/Bitbucket).

В поле Language выберите Docker.

Убедитесь, что:

Dockerfile Path — /Dockerfile (если лежит в корне).

Port — порт из EXPOSE (например, 8080).

В разделе Environment Variables добавьте переменные окружения (если нужны):

JAVA_OPTS (например, -Xmx512m для ограничения памяти).

Другие переменные (DB_URL, API_KEY и т. п.).

Нажмите Create Web Service.

5. Проверка и отладка
Следите за логами:

После нажатия Create Render начнёт сборку.

Перейдите на вкладку Logs — там будут сообщения о ходе сборки и запуске.

Если ошибка — читайте лог, исправляйте Dockerfile/код, коммитите и пушите в репозиторий (Render пересоберёт автоматически).

Проверьте URL:

После успешной сборки Render выдаст URL вида your-app.onrender.com.

Откройте его в браузере — должно появиться ваше приложение.

Тестирование порта:

Убедитесь, что приложение слушает тот же порт, что указан в EXPOSE и в настройках Render.

В коде Java (например, для Spring Boot) укажите:

java
server.port=${PORT:8080}  // PORT — переменная окружения Render
6. Оптимизация (по желанию)
Многоэтапная сборка (уменьшает размер образа):

dockerfile
FROM maven:3.9-jdk-17-eclipse-temurin as builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests


FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
Первый этап (builder) собирает JAR.

Второй этап (eclipse-temurin:17-jre) берёт только JAR и запускает его (образ меньше).

Ограничение памяти:

В переменных окружения Render добавьте:

JAVA_OPTS=-Xmx512m
Это ограничит использование памяти JVM (важно для бесплатного тарифа).

7. Автоматическое обновление
Render автоматически пересобирает сервис при:

новом коммите в ветку (по умолчанию main/master);

изменении Dockerfile.

Чтобы отключить автосборку:

В настройках сервиса → Deploy → снимите галочку Auto Deploy.

8. Распространённые проблемы и решения
«Приложение не стартует»:

Проверьте, что CMD указывает на корректный JAR.

Убедитесь, что порт в EXPOSE совпадает с настройками Render.

«Ошибки зависимостей»:

Проверьте pom.xml — все зависимости должны быть доступны в Maven Central или ваших репозиториях.

Добавьте учётные данные для приватных репозиториев в настройках Render.

«Нехватка памяти»:

Ограничьте память через JAVA_OPTS.

Используйте многоэтапную сборку (см. выше).

«Ошибка сборки Docker»:

Проверьте синтаксис Dockerfile.

Посмотрите логи в Logs на Render.

Итог
Создайте Dockerfile и .dockerignore.

На Render выберите New → Web Service → Docker.

Укажите порт и переменные окружения.

Запустите сборку — Render сам соберёт и развернёт ваше приложение.

Проверьте URL и логи.

После этого ваше Java‑приложение на Maven будет работать на Render 24/7.