const https = require('https');
const fs = require('fs');
const WebSocket = require('ws');

const privateKey = fs.readFileSync('server.key');
const certificate = fs.readFileSync('server.crt');

const credentials = { key: privateKey, cert: certificate };

const server = https.createServer(credentials, (req, res) => {

const wss = new WebSocket.Server({ server });

let fishState = { x: 0, y: 0, visible: true };

wss.on('connection', (ws) => {
  console.log("WebSocket connection established...");

  // Handle incoming messages from the client
  ws.on('message', (message) => {
    const data = JSON.parse(message);
    console.log("message incoming......");

    // Check if the message contains screen dimensions
    if (data.screenWidth && data.screenHeight) {
      // Update fishState with the provided screen dimensions
      fishState.screenWidth = data.screenWidth;
      fishState.screenHeight = data.screenHeight;
      console.log(fishState.screenWidth);

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
  //ws.send(JSON.stringify(fishState));
});
});
server.listen(8081, () => {
  console.log("Secure WebSocket server is listening on port 8081...");
});


//TODO
// use reverse proxy to  eliminate the need to make changes to the Android device's security settings or trust self-signed certificates.