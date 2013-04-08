var fs = require('fs');
var middleware = function(request, response, cfg) {
  var url = request.url;
  fs.readFile('./router.json', 'utf-8', function(err, data) {
    if(err) {
      console.log(err);
    } else {
      var routerJson = JSON.parse(data);
      var uiRoutes = routerJson.uiUrls;
      for(var i in uiRoutes) {
        var regexp = new RegExp(i);
        if(regexp.test(url)) {
          var uiroute = uiRoutes[i];
          try {
            var page = require('./pages/' + uiroute);
            new page.init(request, response, cfg);
          } catch(e) {
            console.log("unable to find module:" + uiroute);
          }
          break;
        }
      }
    }
  });
}

exports.middleware = middleware;
