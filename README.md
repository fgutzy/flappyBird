# 🐦 Flappy Bird Game

<div align="center">

A modern implementation of Flappy Bird built with JavaFX, featuring online multiplayer capabilities, user authentication, and leaderboard tracking powered by Spring Boot and Redis.

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=flat&logo=java&logoColor=white)](https://openjfx.io/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen?style=flat&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Redis](https://img.shields.io/badge/Redis-7-red?style=flat&logo=redis&logoColor=white)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue?style=flat&logo=docker&logoColor=white)](https://www.docker.com/)
[![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=flat&logo=apachemaven&logoColor=white)](https://maven.apache.org/)

</div>

---

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Technologies Used](#-technologies-used)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [Building the Project](#-building-the-project)
- [Creating a Standalone Executable](#-creating-a-standalone-executable)
- [Deploying the API Service](#-deploying-the-api-service)
- [Usage](#-usage)
- [API Endpoints](#-api-endpoints)
- [Troubleshooting](#-troubleshooting)

---

## ✨ Features

- 🎮 **Classic Flappy Bird Gameplay** - Smooth physics and responsive controls
- 🔐 **User Authentication** - Secure login system with encrypted credentials
- 🏆 **Global Leaderboard** - Compete with players worldwide
- 💾 **Persistent Storage** - High scores and user data stored in Redis
- 🌐 **RESTful API** - Spring Boot backend for game data management
- 🐳 **Docker Support** - Easy deployment with Docker Compose
- 🎨 **Modern JavaFX UI** - Clean and responsive interface
- 📦 **Standalone Executables** - Create platform-specific installers with jpackage

---

## 🏗️ Architecture

This project follows a client-server architecture:

```
┌─────────────────────┐          ┌──────────────────────┐
│                     │   HTTP   │                      │
│   JavaFX Client     │ ◄──────► │   Spring Boot API    │
│   (Game Client)     │  REST    │   (Backend Service)  │
│                     │          │                      │
└─────────────────────┘          └──────────┬───────────┘
                                            │
                                            │ Redis
                                            │ Protocol
                                            ▼
                                  ┌──────────────────────┐
                                  │                      │
                                  │   Redis Database     │
                                  │   (Data Storage)     │
                                  │                      │
                                  └──────────────────────┘
```

---

## 🛠️ Technologies Used

### Frontend (Game Client)
- **JavaFX 21** - UI framework and game rendering
- **JavaFX FXML** - Declarative UI layout
- **JavaFX Media** - Sound and audio effects
- **Gson** - JSON serialization/deserialization
- **Java HTTP Client** - RESTful API communication

### Backend (API Service)
- **Spring Boot 3.5.7** - Application framework
- **Spring Data Redis** - Redis integration
- **Spring Web** - REST API endpoints
- **Jakarta Validation** - Request validation
- **Java 17** - Backend runtime

### Infrastructure
- **Redis 7** - In-memory data store
- **Docker & Docker Compose** - Containerization and orchestration
- **Maven** - Build automation and dependency management

---

## 📁 Project Structure

```
flappyBird/
├── gameDirectory/              # JavaFX Game Client
│   ├── src/main/java/
│   │   └── org/example/gamedirectory/
│   │       ├── FlappyBirdGame.java        # Main game logic
│   │       ├── AuthenticationScreen.java   # Login/Register UI
│   │       ├── HttpClientGame.java         # API communication
│   │       └── SSLHelper.java              # SSL configuration
│   ├── src/main/resources/
│   └── pom.xml
│
├── apiService/                 # Spring Boot Backend
│   ├── src/main/java/
│   │   └── org/example/apiservice/
│   │       ├── ApiServiceApplication.java  # Spring Boot entry point
│   │       ├── controller/                 # REST controllers
│   │       ├── dto/                        # Data transfer objects
│   │       ├── model/                      # Domain models
│   │       └── repo/                       # Redis repositories
│   ├── src/main/resources/
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── pom.xml
│
└── pom.xml                     # Parent POM
```

---

## 📦 Prerequisites

### For Running the Game Client
- **Java Development Kit (JDK) 21** or higher
- **Maven 3.6+** (or use included Maven wrapper)

### For Running the API Service
- **Docker** and **Docker Compose** (recommended)
- OR **Java 17+** and **Redis 7** (for manual setup)

Check your installations:
```bash
java -version
mvn -version
docker compose version
```

---

## 🚀 Getting Started

### 1️⃣ Clone the Repository
```bash
git clone <repository-url>
cd flappyBird
```

### 2️⃣ Start the API Service (Docker Method - Recommended)
```bash
cd apiService
docker compose up -d
```

This will start:
- Redis on `localhost:6379`
- API Service on `localhost:8080`

### 3️⃣ Build and Run the Game Client
```bash
cd gameDirectory
mvn clean javafx:run
```

Or use the Maven wrapper:
```bash
./mvnw clean javafx:run
```

---

## 🔨 Building the Project

### Build Everything (Parent + All Modules)
```bash
mvn clean install
```

### Build Only the Game Client
```bash
cd gameDirectory
mvn clean package
```

This creates a fat JAR with all dependencies:
```
gameDirectory/target/gameDirectory-1.0-SNAPSHOT-shaded.jar
```

### Build Only the API Service
```bash
cd apiService
mvn clean package
```

This creates a Spring Boot executable JAR:
```
apiService/target/apiService-0.0.1-SNAPSHOT.jar
```

---

## 📱 Creating a Standalone Executable

You can create a native executable with a custom JRE using `jpackage`. This bundles the application with the Java runtime, so users don't need Java installed.

### Prerequisites
1. **Download JavaFX jmods** from [Gluon](https://gluonhq.com/products/javafx/)
2. Extract to a known location (e.g., `/path/to/javafx-jmods-21`)

### Build the Fat JAR
```bash
cd gameDirectory
mvn clean package
```

### Create the Executable

#### macOS
```bash
jpackage \
  --input gameDirectory/target \
  --main-jar gameDirectory-1.0-SNAPSHOT-shaded.jar \
  --module-path /path/to/javafx-jmods-21 \
  --add-modules javafx.controls,javafx.fxml,javafx.media,jdk.crypto.ec \
  --name FlappyBird \
  --app-version 1.0 \
  --type dmg \
  --icon gameDirectory/src/main/resources/icon.icns
```

#### Windows
```bash
jpackage ^
  --input gameDirectory\target ^
  --main-jar gameDirectory-1.0-SNAPSHOT-shaded.jar ^
  --module-path C:\path\to\javafx-jmods-21 ^
  --add-modules javafx.controls,javafx.fxml,javafx.media,jdk.crypto.ec ^
  --name FlappyBird ^
  --app-version 1.0 ^
  --type exe ^
  --icon gameDirectory\src\main\resources\icon.ico ^
  --win-menu ^
  --win-shortcut
```

#### Linux
```bash
jpackage \
  --input gameDirectory/target \
  --main-jar gameDirectory-1.0-SNAPSHOT-shaded.jar \
  --module-path /path/to/javafx-jmods-21 \
  --add-modules javafx.controls,javafx.fxml,javafx.media,jdk.crypto.ec \
  --name FlappyBird \
  --app-version 1.0 \
  --type deb \
  --icon gameDirectory/src/main/resources/icon.png
```

### Parameters Explained
- `--input` - Directory containing the JAR file
- `--main-jar` - Your fat JAR with all dependencies
- `--module-path` - Path to JavaFX jmods directory
- `--add-modules` - Required JavaFX and JDK modules
  - `javafx.controls` - UI controls
  - `javafx.fxml` - FXML support
  - `javafx.media` - Audio playback
  - `jdk.crypto.ec` - Elliptic curve cryptography for HTTPS
- `--name` - Application name
- `--type` - Package type (dmg, exe, deb, rpm, etc.)

The generated installer will be in the current directory.

---

## 🐳 Deploying the API Service

### Using Docker Compose (Recommended)

The `apiService/docker-compose.yml` defines both the API and Redis:

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

**Start the services:**
```bash
cd apiService
docker compose up -d
```

**Check status:**
```bash
docker compose ps
```

**View logs:**
```bash
docker compose logs -f api
```

**Stop the services:**
```bash
docker compose down
```

### Deploying to a Remote Server

**1. Build and save the Docker image:**
```bash
cd apiService
docker build -t my-spring-app-amd .
docker save -o my-spring-app-amd.tar my-spring-app-amd
```

**2. Copy to server:**
```bash
scp my-spring-app-amd.tar user@SERVER_IP:/home/user/
scp docker-compose.yml user@SERVER_IP:/home/user/
```

**3. Load and start on server:**
```bash
ssh user@SERVER_IP
docker load -i my-spring-app-amd.tar
docker compose up -d
```

**4. Test the deployment:**
```bash
curl http://SERVER_IP:8080/api/login
```

---

## 🎮 Usage

### Starting the Game

1. **Ensure API service is running** (see [Getting Started](#-getting-started))
2. **Launch the game client:**
   ```bash
   cd gameDirectory
   mvn javafx:run
   ```
3. **Create an account or login**
4. **Play the game!** Click or press space to flap

### Controls
- **Click** or **Space** - Make the bird flap
- **ESC** - Pause/Menu (if implemented)

---

## 🌐 API Endpoints

The Spring Boot API exposes the following endpoints:

### Authentication
```http
POST /api/register
Content-Type: application/json

{
  "username": "player1",
  "password": "securepass123"
}
```

```http
POST /api/login
Content-Type: application/json

{
  "username": "player1",
  "password": "securepass123"
}
```

### Leaderboard
```http
GET /api/leaderboard
```

### High Score
```http
POST /api/score
Content-Type: application/json

{
  "username": "player1",
  "score": 42
}
```

```http
GET /api/score/{username}
```

### Health Check
```http
GET /actuator/health
```

**Base URL:**
- Local: `http://localhost:8080`
- Remote: `http://YOUR_SERVER_IP:8080`

---

## 🔧 Troubleshooting

### Game Won't Connect to API

**Check if API is running:**
```bash
curl http://localhost:8080/actuator/health
```

**Check Docker containers:**
```bash
docker compose ps
docker compose logs api
```

**Verify network connectivity:**
- Ensure no firewall blocking port 8080
- Check API URL in game client configuration

### JavaFX Runtime Error

**Error:** `Error: JavaFX runtime components are missing`

**Solution:** Ensure you're using Java 21 with JavaFX included, or run via Maven:
```bash
mvn javafx:run
```

### Redis Connection Error

**Error:** `Unable to connect to Redis at localhost:6379`

**Solution:**
```bash
# Check if Redis is running
docker compose ps

# Restart Redis
docker compose restart redis

# Check Redis logs
docker compose logs redis
```

### jpackage Not Found

**Error:** `jpackage: command not found`

**Solution:** Ensure you're using JDK 14+ (jpackage is included):
```bash
java -version  # Should show version 14 or higher
```

### SSL/HTTPS Issues

**Error:** `SSL handshake failed` or `Certificate validation failed`

**Note:** The project includes `SSLHelper.java` which may disable SSL verification for development. For production, configure proper SSL certificates.

---

## 📝 License

This project is for educational purposes.

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

## 📧 Contact

For questions or support, please open an issue on the repository.

---

<div align="center">

**Made with ☕ and JavaFX**

</div>
