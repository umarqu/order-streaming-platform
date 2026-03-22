# Order Client

A Node.js client that generates orders against the e-commerce backend. Supports single-shot and continuous modes, and publishes confirmed orders to Kafka.

## Prerequisites

- Node.js 20+
- Backend running at `http://localhost:8080` (or override with `BACKEND_URL`)
- Kafka at `localhost:9092` (optional — client degrades gracefully if unavailable)

## Setup

```bash
npm install
```

## Running

### Single order

Sends one order and exits.

```bash
npm run single
# or
MODE=single node src/index.js
```

### Continuous orders

Sends orders on a repeating interval. Press `Ctrl+C` to stop.

```bash
npm run continuous
# or
MODE=continuous INTERVAL=2000 node src/index.js
```

## Configuration

All configuration is via environment variables:

| Variable        | Default                | Description                                      |
|-----------------|------------------------|--------------------------------------------------|
| `BACKEND_URL`   | `http://localhost:8080`| Base URL of the e-commerce backend               |
| `KAFKA_BROKERS` | `localhost:9092`       | Comma-separated Kafka broker addresses           |
| `MODE`          | `single`               | `single` — one order then exit; `continuous` — loop |
| `INTERVAL`      | `3000`                 | Milliseconds between orders in continuous mode   |
| `CUSTOMER_ID`   | random 1–3             | Pin orders to a specific customer ID             |
| `PRODUCT_ID`    | random 1–3             | Pin orders to a specific product ID              |
| `QUANTITY`      | random 1–5             | Fix quantity per order                           |

### Examples

```bash
# Send orders every second to a remote backend
BACKEND_URL=http://my-backend:8080 MODE=continuous INTERVAL=1000 node src/index.js

# Pin to customer 2, product 1, quantity 3
CUSTOMER_ID=2 PRODUCT_ID=1 QUANTITY=3 MODE=single node src/index.js

# Continuous mode with custom Kafka brokers
KAFKA_BROKERS=kafka1:9092,kafka2:9092 MODE=continuous node src/index.js
```

## How it works

1. **Connects to Kafka** on startup (warns and continues if unavailable).
2. **Sends an HTTP POST** to `POST /api/orders` on the backend with a JSON body:
   ```json
   { "customerId": 1, "productId": 2, "quantity": 3 }
   ```
3. **Publishes the confirmed order** to the Kafka topic `orders`:
   ```json
   { "event": "order.confirmed", "id": 42, "customerId": 1, "productId": 2, "quantity": 3, ... }
   ```
4. In **continuous mode**, repeats steps 2–3 every `INTERVAL` ms.

## Running with Docker

```bash
docker build -t order-client .

# Single mode
docker run --rm \
  -e BACKEND_URL=http://host.docker.internal:8080 \
  -e KAFKA_BROKERS=host.docker.internal:9092 \
  order-client

# Continuous mode
docker run --rm \
  -e BACKEND_URL=http://backend:8080 \
  -e KAFKA_BROKERS=kafka:29092 \
  -e MODE=continuous \
  -e INTERVAL=2000 \
  order-client
```

## Running with Docker Compose

From the project root:

```bash
docker-compose up
```

This starts MySQL, Zookeeper, Kafka, the backend, and the client together.
