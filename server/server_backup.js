const WebSocket = require('ws');
const wss = new WebSocket.Server({ host: '0.0.0.0', port: 8080 });

let fishState = { x: 0, y: 0, visible: true };
console.log("handling...")
wss.on('error', (error) => {
  console.error('WebSocket server error:', error);
});
wss.on('connection', (ws) => {
  // Handle incoming messages from the client
  console.log("handling...")
  ws.on('message', (message) => {
    const data = JSON.parse(message);
    console.log("handling...")

    // Check if the message contains screen dimensions
    if (data.screenWidth && data.screenHeight) {

      // Update fishState with the provided screen dimensions
      fishState.screenWidth = data.screenWidth;
      fishState.screenHeight = data.screenHeight;

      // Generate a random position within the provided dimensions
      fishState.x = Math.floor(Math.random() * data.screenWidth);
      fishState.y = Math.floor(Math.random() * data.screenHeight);

      // Broadcast the updated fishState to all clients
      wss.clients.forEach((client) => {
        if (client.readyState === WebSocket.OPEN) {
          client.send(JSON.stringify(fishState));
        }
      });
    }
  });

  // Send the initial fishState to the newly connected client
  ws.send(JSON.stringify(fishState));
});
