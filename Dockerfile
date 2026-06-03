# --- Stage 1: Frontend build ---
FROM node:20-alpine AS frontend
WORKDIR /build
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# --- Stage 2: Backend build ---
FROM eclipse-temurin:21-jdk-alpine AS backend
WORKDIR /build
COPY backend/ ./
RUN ./gradlew bootJar --no-daemon

# --- Stage 3: Runtime ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend /build/build/libs/*.jar app.jar
COPY --from=frontend /build/dist/frontend ./static
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
