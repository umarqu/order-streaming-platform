const axios = require('axios');
const config = require('./config');

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

async function sendOrder() {
  const payload = {
    customerId: config.customerId || randomInt(1, 3),
    productId: config.productId || randomInt(1, 3),
    quantity: config.quantity || randomInt(1, 5),
  };

  const response = await axios.post(`${config.backendUrl}/api/orders`, payload);
  return response.data;
}

module.exports = { sendOrder };
