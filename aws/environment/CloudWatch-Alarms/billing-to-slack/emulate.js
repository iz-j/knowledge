var message = {};

var event = {
};

var context = {
  invokeid: 'test',
  succeed: (message) => {
    console.log(message);
    return;
  },
  fail: (message) => {
    console.error(message);
    return;
  }
};

var lambda = require("./app");
lambda.handler(event, context);