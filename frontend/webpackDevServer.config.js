const path = require('path');

module.exports = {
  devServer: {
    allowedHosts: ['localhost', '.localhost'],
    host: 'localhost',
    port: 3000,
    hot: true,
    historyApiFallback: true,
    client: {
      webSocketURL: 'ws://localhost:3000/ws',
    },
    headers: {
      'Access-Control-Allow-Origin': '*',
    }
  }
};