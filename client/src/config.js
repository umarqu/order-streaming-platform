module.exports = {
  backendUrl: process.env.BACKEND_URL || 'http://localhost:8080',
  kafkaBrokers: (process.env.KAFKA_BROKERS || 'localhost:9092').split(','),
  mode: process.env.MODE || 'single',
  intervalMs: parseInt(process.env.INTERVAL || '3000', 10),
  customerId: process.env.CUSTOMER_ID ? parseInt(process.env.CUSTOMER_ID, 10) : null,
  productId: process.env.PRODUCT_ID ? parseInt(process.env.PRODUCT_ID, 10) : null,
  quantity: process.env.QUANTITY ? parseInt(process.env.QUANTITY, 10) : null,
};
