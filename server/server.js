const https = require('https');
const fs = require('fs');
const WebSocket = require('ws');

const serverOptions = {
  key: fs.readFileSync('catFishServer.key'), 
  cert: fs.readFileSync('catFishServer.crt'), 
};

const server = https.createServer(serverOptions);
const wss = new WebSocket.Server({ server });

let fishState = { x: 0, y: 0, visible: true };

wss.on('connection', (ws) => {
  console.log("WebSocket connection established...");

  // Handle incoming messages from the client
  ws.on('message', (message) => {
    const data = JSON.parse(message);

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

server.listen(8081, () => {
  console.log("Secure WebSocket server is listening on port 8081...");
});
