#Build
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /project
COPY pom.xml .
# load dependencies
RUN mvn dependency:go-offline

# copy code
COPY src ./src
#create package
RUN mvn clean package -DskipTests

# generate image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
#copy .jar
COPY --from=builder /project/target/*.jar app.jar
COPY keystore.jks /app/keystore.jks
# expose 8443 (SSL)
EXPOSE 8443

# execute
ENTRYPOINT ["java", "-jar", "app.jar"]