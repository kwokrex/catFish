const WebSocket = require('ws');
const wss = new WebSocket.Server({ noServer: true });

// ... Rest of your code ...

// Create an HTTP server and combine it with the WebSocket server
const httpServer = require('http').createServer((req, res) => {
  // Handle HTTP requests if needed
});

httpServer.on('upgrade', (request, socket, head) => {
  wss.handleUpgrade(request, socket, head, (ws) => {
    wss.emit('connection', ws, request);
  });
});

// Start both the HTTP server and WebSocket server on port 8080
httpServer.listen(8080, () => {
  console.log('HTTP and WebSocket servers are listening on port 8080');
});
