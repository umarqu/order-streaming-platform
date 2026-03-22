# E-Commerce Order Management System

A microservices-based e-commerce platform demonstrating Kubernetes-ready architecture with order processing, event streaming, and containerized services.

## Project Overview

This application consists of:

- **Backend** — Spring Boot REST API for order management (Java)
- **Client** — Node.js order generation client (single and continuous modes)
- **Infrastructure** — MySQL database, Kafka message broker, Zookeeper
- **Containerization** — Docker & Docker Compose for local development, ready for Kubernetes deployment

### Tech Stack

| Component | Technology |
|-----------|------------|
| API | Spring Boot 3.3.3 (Java 23) |
| Client | Node.js 20 |
| Database | MySQL 8.4 |
| Message Broker | Kafka 7.5.0 |
| Orchestration | Docker Compose / Kubernetes |

---

## Quick Start

### Option 1: Run Everything with Docker Compose (Recommended)

Brings up MySQL, Kafka, backend, and client with one command.

```bash
docker-compose up
```

This will:
- Start Zookeeper and Kafka
- Initialize MySQL with schema and sample data
- Build and run the Spring Boot backend on `http://localhost:8080`
- Build and run the Node.js client in continuous mode (sends orders every 3 seconds)

View logs:
```bash
docker-compose logs -f backend
docker-compose logs -f client
docker-compose logs -f kafka
```

Stop everything:
```bash
docker-compose down
```

---

### Option 2: Run Locally (Without Containers)

#### Prerequisites

- Java 23 (JDK)
- Node.js 20+
- MySQL 8.4 running locally
- Kafka + Zookeeper (optional — app degrades gracefully)

#### Setup MySQL

```bash
# Via Docker (easiest)
docker run --name mysql-ecommerce \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -e MYSQL_DATABASE=ecommerce \
  -e MYSQL_USER=ecommerce_user \
  -e MYSQL_PASSWORD=ecommerce_pass \
  -p 3306:3306 \
  mysql:8.4

# Or install locally and create database manually
mysql -u root -p < backend/src/main/java/ecommerce/backend/scripts/schema.sql
mysql -u root -p < backend/src/main/java/ecommerce/backend/scripts/insert-data.sql
```

#### Start Kafka (Optional)

```bash
# Using Docker
docker run -d --name zookeeper -p 2181:2181 \
  -e ZOOKEEPER_CLIENT_PORT=2181 \
  confluentinc/cp-zookeeper:7.5.0

docker run -d --name kafka -p 9092:9092 \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT \
  -e KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka:7.5.0
```

#### Start Backend

```bash
cd backend
mvn spring-boot:run
```

Backend will start on `http://localhost:8080`

Verify:
```bash
curl http://localhost:8080/api/orders
# Should return: []
```

#### Start Client

```bash
cd client
npm install

# Single order
npm run single

# Continuous mode (orders every 3 seconds)
npm run continuous
```

---

## API Endpoints

### Products

```bash
# Get all products
GET /api/products

# Get product by ID
GET /api/products/{id}

# Create product
POST /api/products
Content-Type: application/json
{ "name": "Laptop", "price": 999.99 }
```

### Customers

```bash
# Get all customers
GET /api/customers

# Get customer by ID
GET /api/customers/{id}

# Create customer
POST /api/customers
Content-Type: application/json
{ "name": "John Doe", "email": "john@example.com" }
```

### Orders

```bash
# Get all orders
GET /api/orders

# Create order
POST /api/orders
Content-Type: application/json
{ "customerId": 1, "productId": 2, "quantity": 3 }
```

---

## Testing the Application

### Test with Docker Compose

The client is automatically started in continuous mode. Check the logs:

```bash
docker-compose logs -f client
```

You should see orders being created every 3 seconds.

### Test Manually

With everything running locally, generate an order:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "productId": 1,
    "quantity": 2
  }'
```

Expected response:
```json
{
  "id": 4,
  "customerId": 1,
  "productId": 1,
  "quantity": 2,
  "orderDate": "2026-03-22T20:30:45.123456"
}
```

### Monitor Kafka Events

If Kafka is running, orders are published to the `orders` topic. Listen to events:

```bash
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic orders \
  --from-beginning
```

---

## Project Structure

```
ecomerce/
├── backend/                          # Spring Boot backend
│   ├── src/main/java/ecommerce/
│   │   ├── model/                   # JPA entities (Product, Customer, Order)
│   │   ├── dto/                     # Data transfer objects
│   │   ├── controller/              # REST endpoints
│   │   ├── service/                 # Business logic
│   │   ├── repository/              # Database access (JPA)
│   │   └── exceptions/              # Custom exceptions
│   ├── src/main/resources/
│   │   └── application.yml          # Spring Boot config
│   ├── src/main/java/ecommerce/backend/scripts/
│   │   ├── schema.sql               # Database schema
│   │   └── insert-data.sql          # Sample data
│   ├── pom.xml                      # Maven dependencies
│   └── Dockerfile                   # Container image
│
├── client/                           # Node.js order client
│   ├── src/
│   │   ├── index.js                 # Entry point (single/continuous modes)
│   │   ├── orderClient.js           # HTTP client for backend
│   │   ├── kafkaProducer.js         # Kafka event publishing
│   │   └── config.js                # Configuration management
│   ├── package.json
│   ├── Dockerfile
│   └── README.md                    # Client-specific documentation
│
├── docker-compose.yml               # All services orchestration
└── README.md                        # This file
```

---

## Configuration

### Backend (Spring Boot)

Configure via environment variables or `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/ecommerce
    username: ecommerce_user
    password: ecommerce_pass
  kafka:
    bootstrap-servers: kafka:29092
```

Environment variables (override `application.yml`):
- `SPRING_DATASOURCE_URL` — MySQL connection string
- `SPRING_DATASOURCE_USERNAME` — DB user
- `SPRING_DATASOURCE_PASSWORD` — DB password
- `KAFKA_BOOTSTRAP_SERVERS` — Kafka broker addresses

### Client (Node.js)

Configure via environment variables:

```bash
BACKEND_URL=http://localhost:8080 \
KAFKA_BROKERS=localhost:9092 \
MODE=continuous \
INTERVAL=2000 \
CUSTOMER_ID=1 \
PRODUCT_ID=2 \
QUANTITY=5 \
node src/index.js
```

See `client/README.md` for all options.

---

## Architecture

```
┌──────────────┐
│  Order Client│ (Node.js)
│  (orders every N ms)
└───────┬──────┘
        │ HTTP POST /api/orders
        │
┌───────▼──────────────┐
│  Backend API         │ (Spring Boot)
│  ├─ Create Order     │
│  └─ Query Orders     │
└───────┬──────────────┘
        │ Persists Order
        │
┌───────▼──────────┐
│  MySQL Database  │
│  ├─ customers    │
│  ├─ products     │
│  └─ orders       │
└──────────────────┘
        │
        │ Publishes order.created event
        │
┌───────▼──────────┐
│  Kafka Broker    │ (Event Stream)
│  Topic: orders   │
└──────────────────┘
```

---

## Scaling & Kubernetes

This project is designed to run on Kubernetes:

- Each service has a `Dockerfile` for containerization
- Services communicate via well-defined APIs (HTTP, Kafka)
- Configuration via environment variables (12-factor app)
- Stateless services (state stored in MySQL/Kafka)
- Ready for Kubernetes manifests (deployment, service, configmap)

Example Kubernetes deployment:
```bash
kubectl apply -f k8s-manifests/mysql.yaml
kubectl apply -f k8s-manifests/kafka.yaml
kubectl apply -f k8s-manifests/backend.yaml
kubectl apply -f k8s-manifests/client.yaml
```

---

## Cleanup

### Stop Docker Compose

```bash
docker-compose down
```

Remove volumes (deletes all data):
```bash
docker-compose down -v
```

### Stop Local Services

```bash
# Kill MySQL container
docker stop mysql-ecommerce

# Kill Kafka containers
docker stop kafka zookeeper
```

---

## Troubleshooting

### Backend won't connect to MySQL

Check if MySQL is running:
```bash
docker ps | grep mysql
```

Verify connection string in `application.yml` or `SPRING_DATASOURCE_URL`.

### Client can't reach backend

Check backend is running:
```bash
curl http://localhost:8080/api/orders
```

If using Docker Compose, backend is at `http://backend:8080` (from within containers).

### Kafka events not publishing

Kafka is optional. The app will log a warning but continue. To verify Kafka is running:
```bash
docker ps | grep kafka
```

---

## Next Steps

- [ ] Add Kubernetes manifests (deployment, service, ingress)
- [ ] Add request validation and error handling
- [ ] Add authentication/authorization
- [ ] Add order status tracking (pending → shipped → delivered)
- [ ] Add inventory management
- [ ] Add metrics and monitoring (Prometheus, Grafana)
- [ ] Add distributed tracing (Jaeger)

---

## License

MIT
