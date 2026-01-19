# Etapa 1: Compilar la aplicación Angular
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration production

# Etapa 2: Servir con Nginx (Servidor web ligero)
FROM nginx:alpine
# Copiamos el build de Angular a la carpeta de Nginx
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
# Copiamos una configuración básica de Nginx (la crearemos abajo)
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
