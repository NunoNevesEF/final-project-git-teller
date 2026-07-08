# Build stage
FROM node:22-alpine AS builder

WORKDIR /app

COPY frontend/git-teller/package*.json ./

RUN npm install

COPY frontend/git-teller/ .

RUN npx expo export --platform web


# Runtime stage
FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]