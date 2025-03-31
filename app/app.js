// app.js - Aplikasi Node.js Express Sederhana
const express = require('express');
const os = require('os');
const app = express();
const port = process.env.PORT || 3000;
const appVersion = process.env.APP_VERSION || '1.0.0';
const environment = process.env.NODE_ENV || 'development';

// Middleware untuk logging request
app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
  next();
});

// Halaman utama
app.get('/', (req, res) => {
  res.send(`
    <html>
      <head>
        <title>GitOps Demo App</title>
        <style>
          body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f5f5f5;
            color: #333;
          }
          h1 {
            color: #0066cc;
            border-bottom: 1px solid #ddd;
            padding-bottom: 10px;
          }
          .info {
            background-color: white;
            border-left: 4px solid #0066cc;
            padding: 15px;
            margin: 20px 0;
            border-radius: 0 4px 4px 0;
          }
          .version {
            display: inline-block;
            background-color: #0066cc;
            color: white;
            padding: 5px 10px;
            border-radius: 20px;
            font-size: 14px;
            margin-left: 10px;
          }
          footer {
            margin-top: 30px;
            border-top: 1px solid #ddd;
            padding-top: 10px;
            font-size: 12px;
            color: #777;
          }
        </style>
      </head>
      <body>
        <h1>GitOps Demo Application <span class="version">v${appVersion}</span></h1>
        <p>This is a simple application to demonstrate GitOps deployment with Docker Swarm.</p>
        
        <div class="info">
          <h2>Server Information</h2>
          <ul>
            <li><strong>Hostname:</strong> ${os.hostname()}</li>
            <li><strong>Platform:</strong> ${os.platform()} ${os.release()}</li>
            <li><strong>Environment:</strong> ${environment}</li>
            <li><strong>Server Time:</strong> ${new Date().toLocaleString()}</li>
            <li><strong>Uptime:</strong> ${Math.floor(os.uptime() / 60)} minutes</li>
          </ul>
        </div>

        <footer>
          &copy; ${new Date().getFullYear()} GitOps Demo - Running on Docker Swarm
        </footer>
      </body>
    </html>
  `);
});

// API endpoint untuk healthcheck
app.get('/health', (req, res) => {
  res.status(200).json({ status: 'UP', version: appVersion });
});

// API endpoint untuk mendapatkan info server
app.get('/api/info', (req, res) => {
  res.json({
    hostname: os.hostname(),
    platform: os.platform(),
    release: os.release(),
    uptime: os.uptime(),
    memory: {
      total: os.totalmem(),
      free: os.freemem()
    },
    cpus: os.cpus().length,
    serverTime: new Date()
  });
});

// Error handler
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).send('Something broke!');
});

// Mulai server
app.listen(port, () => {
  console.log(`GitOps Demo App listening at http://localhost:${port}`);
  console.log(`Version: ${appVersion}, Environment: ${environment}`);
});
