const { Kafka, logLevel } = require('kafkajs');
const config = require('./config');

const kafka = new Kafka({
  clientId: 'order-client',
  brokers: config.kafkaBrokers,
  logLevel: logLevel.WARN,
});

const producer = kafka.producer();
let connected = false;

async function connect() {
  await producer.connect();
  connected = true;
}

async function publishOrderEvent(order) {
  if (!connected) return;

  await producer.send({
    topic: 'orders',
    messages: [
      {
        key: String(order.id),
        value: JSON.stringify({ event: 'order.confirmed', ...order }),
      },
    ],
  });
}

async function disconnect() {
  if (connected) {
    await producer.disconnect();
  }
}

module.exports = { connect, publishOrderEvent, disconnect };
