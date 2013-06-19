var fs = require('fs');
var reqUrl = require('url');
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
            return;
          } catch(e) {
            console.log("unable to find module:" + uiroute);
            return;
          }
          break;
        }
      }

      var apiUrls = cfg.getConfig('apiUrls', function(apiUrls) {
        var ajaxUrls = routerJson.ajaxUrls;
        for(var i in ajaxUrls) {
          var regexp = new RegExp(i);
          if(regexp.test(url)) {
            try {
              var url_parts = reqUrl.parse(request.url, true);
              var query = url_parts.query;
              var ajax = require('./ajax/' + ajaxUrls[i]);
              new ajax.init(request, response, cfg, query);
              return;
            } catch(e) {
              console.log("unable to find module:" + ajaxUrls[i]);
              return;
            }
            break;
          }
        }
      });
    }
  });
}

exports.middleware = middleware;
