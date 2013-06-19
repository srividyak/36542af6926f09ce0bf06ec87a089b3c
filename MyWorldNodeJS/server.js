var http = require('http');
var config = require('./config');
var middleware = require('./middleware');
var queryParser = require('./queryParser');

var cfgObj = new config.config();
function start(port) {
  function onRequest(request, response) {
    console.log("Request received");
    new queryParser.queryParser(request, function(queryParams) {
      if(queryParams) {
        request[request.method.toLowerCase() + 'Params'] = queryParams;
      }
      new middleware.middleware(request, response, cfgObj);
    });
  }

  http.createServer(onRequest).listen(port);
}

cfgObj.getConfig(null, function(data) {
  start(data.port);
});
