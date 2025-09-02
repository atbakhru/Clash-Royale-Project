module.exports = {
  webpack: {
    configure: (webpackConfig, { env, paths }) => {
      return webpackConfig;
    }
  },
  devServer: {
    host: 'localhost',
    port: 3000,
    hot: true,
    historyApiFallback: true,
    allowedHosts: 'all',
    headers: {
      'Access-Control-Allow-Origin': '*',
    }
  }
};
