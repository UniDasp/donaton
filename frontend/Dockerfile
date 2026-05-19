# Multi-stage build para Frontend (Vite + React)
# STAGE 1: Builder - Construir aplicación con Node.js
FROM node:20-alpine AS builder

WORKDIR /app

# Copiar package.json y package-lock.json
COPY package*.json ./

# Instalar dependencias
RUN npm ci

# Copiar código fuente
COPY . .

# Compilar aplicación Vite
RUN npm run build

# STAGE 2: Runtime - Servir con Nginx
FROM nginx:alpine

# Copiar configuración de Nginx personalizada
COPY nginx.conf /etc/nginx/nginx.conf

# Copiar archivos compilados del stage anterior
COPY --from=builder /app/dist /usr/share/nginx/html

# Exponer puerto 3000 (mapeado desde 80 en contenedor)
EXPOSE 80

# Iniciar Nginx
CMD ["nginx", "-g", "daemon off;"]
