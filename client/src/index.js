const config = require('./config');
const { sendOrder } = require('./orderClient');
const kafka = require('./kafkaProducer');

async function run() {
  console.log(`Starting order client [mode=${config.mode}, backend=${config.backendUrl}]`);

  try {
    await kafka.connect();
    console.log(`Kafka connected [brokers=${config.kafkaBrokers.join(',')}]`);
  } catch (err) {
    console.warn(`Kafka unavailable, continuing without it: ${err.message}`);
  }

  if (config.mode === 'continuous') {
    await runContinuous();
  } else {
    await runSingle();
  }
}

async function runSingle() {
  try {
    const order = await sendOrder();
    console.log('Order created:', JSON.stringify(order));
    await kafka.publishOrderEvent(order);
  } catch (err) {
    console.error('Failed to create order:', err.message);
  } finally {
    await kafka.disconnect();
  }
}

async function runContinuous() {
  console.log(`Sending orders every ${config.intervalMs}ms. Press Ctrl+C to stop.`);

  let orderCount = 0;

  async function sendNext() {
    try {
      const order = await sendOrder();
      orderCount++;
      console.log(`[${orderCount}] Order created:`, JSON.stringify(order));
      await kafka.publishOrderEvent(order);
    } catch (err) {
      console.error(`[${orderCount + 1}] Failed to create order:`, err.message);
    }
  }

  await sendNext();
  const interval = setInterval(sendNext, config.intervalMs);

  process.on('SIGINT', async () => {
    clearInterval(interval);
    console.log(`\nStopped. Total orders sent: ${orderCount}`);
    await kafka.disconnect();
    process.exit(0);
  });
}

run().catch((err) => {
  console.error('Fatal error:', err.message);
  process.exit(1);
});
