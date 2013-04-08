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
    /*
     * router(request.url);
     */
/*    var options = {
      hostname: 'localhost',
      port: 4080,
      path: '/MyWorldWebService/v1/user?uuid=672a1f98c258811a09741e033cabdf26',
      method: 'GET'
    };

    var req = http.request(options, function(res){
      console.log('STATUS: ' + res.statusCode);
      res.setEncoding('utf8');
      response.writeHead(200, {'Content-Type': 'application/json'});
      res.on('data', function(chunk){
        console.log(chunk);
        response.write(chunk);
      });
      res.on('end', function() {
        response.end();
      });
    });

    req.on('error', function(e){
      console.log('error: ' + e.message);
    });

    req.end();*/

  }

  http.createServer(onRequest).listen(port);
}

cfgObj.getConfig(null, function(data) {
  start(data.port);
});
