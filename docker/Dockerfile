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
# 2. Build Backend (Spring Boot Native)
# ==========================================
#  This uses the standard Oracle Linux/glibc-based compiler.
FROM maven:3.9.6-eclipse-temurin-17 AS backend-build
WORKDIR /app/backend

COPY backend/pom.xml ./
COPY backend/src ./src

RUN mkdir -p src/main/resources/static
COPY --from=frontend-build /app/frontend/dist/frontend/browser/ src/main/resources/static/

RUN mvn clean package -DskipTests



# ==========================================
# 3. Final Image (Debian Slim)
# ==========================================
#debian-slim instead of alpine. It is compatible with the glibc binary.
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=backend-build /app/backend/target/*.jar app.jar

EXPOSE 8080

# Optimization at code level (AOT)
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:TieredStopAtLevel=1", "-jar", "app.jar"]
