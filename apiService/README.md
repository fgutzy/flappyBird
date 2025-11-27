# API Service — Deployment & Usage Guide

This project provides a Spring Boot API backed by Redis, packaged and deployed using Docker and Docker Compose.

---

## 🚀 Prerequisites

To run this project, you only need:
- Docker
- Docker Compose

Check installation:
```bash
docker compose version
```

No Java or Maven is required to run the service.

---

## ▶️ Running the API Service

Start the API + Redis with:
```bash
docker compose up -d
```

Check running containers:
```bash
docker compose ps
```

---

## 🌐 Accessing the API

**Local:**
```
http://localhost:8080
```

**Example endpoint:**
```
http://localhost:8080/api/login
```

**Remote server:**
```
http://SERVER_IP:8080
```

---

## 🛑 Stopping the Service
```bash
docker compose down
```

Stop and remove data volumes:
```bash
docker compose down --volumes
```

---

## 🔁 Updating the API Image

**On development machine:**
```bash
docker save -o my-spring-app-amd.tar my-spring-app-amd
```

**Copy to server:**
```bash
scp my-spring-app-amd.tar user@SERVER_IP:/home/user/
```

**On server:**
```bash
docker load -i my-spring-app-amd.tar
docker compose down
docker compose up -d
```

---

## 🧩 docker-compose.yml
```yaml
version: "3.9"

services:
  redis:
    image: redis:7
    ports:
      - "6379:6379"

  api:
    image: my-spring-app-amd
    ports:
      - "8080:8080"
    environment:
      SPRING_DATA_REDIS_HOST: redis
      SPRING_DATA_REDIS_PORT: 6379
    depends_on:
      - redis
```

---

## 🧪 Testing

**On server:**
```bash
curl http://localhost:8080/api/login
```

**External:**
```bash
curl http://SERVER_IP:8080/api/login
```

---

## 🙋 Troubleshooting

**API logs:**
```bash
docker compose logs -f api
```

**Redis logs:**
```bash
docker compose logs redis
```

---

## 🎉 Summary

**Start everything:**
```bash
docker compose up -d
```

**Stop everything:**
```bash
docker compose down
```