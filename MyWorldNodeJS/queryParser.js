var qs = require('querystring');

var queryParser = function(request, callback) {
  if(request.method == 'POST') {
    var body = '';

    request.on('data', function(data) {
      body += data;
    });

    request.on('end', function() {
      var postParams = qs.parse(body);
      callback(postParams);
    });
  } else {
    callback(null);
  }
}

exports.queryParser = queryParser;
