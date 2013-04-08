var mustache = require('mustache');
var fs = require('fs');
var tunnel = require('tunnel');
var resultParser = require('resultParser');
var init = function(request, response, cfg) {
  //create user html
  try {
    if(request.postParams) {
      request.postParams.gender = (request.postParams.gender == 'true');
      cfg.getConfig('apiUrls', function(apiUrls) {
        new tunnel.tunnel('POST', apiUrls['createUser'], request.postParams, function(wsResult) {
          resultParser.resultParser(wsResult, function(massagedResult) {
            response.write(massagedResult.content);
            response.end();
          });
        });
      });
    } else {
      cfg.getConfig('baseDir', function(dir) {
        var template = fs.readFileSync(dir + '/templates/home.mu', "utf-8");
        response.write(template);
        response.end();
      });
    }
  } catch(e) {
    console.log(e);
  }
}

exports.init = init;
