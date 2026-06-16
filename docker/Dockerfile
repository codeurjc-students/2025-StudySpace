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
FROM ghcr.io/graalvm/native-image-community:17 AS backend-build
WORKDIR /app/backend

COPY backend/.mvn ./.mvn
COPY backend/mvnw ./
RUN chmod +x ./mvnw

COPY backend/pom.xml ./
COPY backend/src ./src

RUN mkdir -p src/main/resources/static
COPY --from=frontend-build /app/frontend/dist/frontend/browser/ src/main/resources/static/

RUN ./mvnw -Pnative native:compile -DskipTests

# ==========================================
# 3. Final Image (Debian Slim)
# ==========================================
#debian-slim instead of alpine. It is compatible with the glibc binary.
FROM debian:bookworm-slim
WORKDIR /app


COPY --from=backend-build /app/backend/target/backend ./app-executable

EXPOSE 8080

ENTRYPOINT ["./app-executable"]