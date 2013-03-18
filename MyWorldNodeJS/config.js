var fs = require('fs');

var config = function() {

  //{Object} reference to the config json
  this.configInfo = null;

  this.init = function(callback) {

    fs.readFile('config.json', function(err,data){
      if(err){
        throw err;
      } else {
        var confObj = JSON.parse(data);
        callback(confObj);
      }
    });
  };

  this.getConfig = function(key, callback) {
    //{Object} storing this in local var
    var self = this;

    var get = function() {
      if(key) {
        if(self.configInfo && self.configInfo[key]) {
          callback(self.configInfo[key]);
        }
        callback(null);
      }
      callback(self.configInfo);
    };

    if(this.configInfo) {
      get();
    } else {
      this.init(function(data){
        self.configInfo = data;
        get();
      });
    }
  }

}

exports.config = config;
