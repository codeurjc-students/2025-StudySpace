# ==========================================
# Build Frontend (Angular)
# ==========================================
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend

# dependencies and install them
COPY frontend/package*.json ./
RUN npm ci

# copy the rest of the frontend code and build it
COPY frontend/ ./
RUN npm run build -- --configuration production

# ==========================================
# Build Backend (Spring Boot)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17 AS backend-build
WORKDIR /app/backend

# Copy the pom.xml and the code
COPY backend/pom.xml ./
COPY backend/src ./src

# create the static folder
RUN mkdir -p src/main/resources/static

# Copy Angular on the static folder we created
COPY --from=frontend-build /app/frontend/dist/frontend/browser/ src/main/resources/static/

# Copy .jar without tests
RUN mvn clean package -DskipTests

# ==========================================
# Final Image
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy last .jar from the backend-build stage
COPY --from=backend-build /app/backend/target/*.jar app.jar

# port of backend 8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]