const WebSocket = require('ws');
const ws = new WebSocket('ws://localhost:8081');

ws.on('open', () => {
  console.log('WebSocket connection opened.');
});

ws.on('message', (message) => {
  console.log('Received message: ' + message);
});

ws.on('close', () => {
  console.log('WebSocket connection closed.');
});
