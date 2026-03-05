# Compile Angular app
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration production

# Nginx (light web server)
FROM nginx:alpine
# Angular build to Nginx
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
# Basic Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 443
