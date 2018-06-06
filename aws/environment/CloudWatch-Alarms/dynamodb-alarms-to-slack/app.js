var url = require('url');
var https = require('https');

const HOOK_URL = 'https://hooks.slack.com/services/~~~~/~~~~/~~~~';

var postMessage = (payload, callback) => {
  let body = JSON.stringify(payload);
  let options = url.parse(HOOK_URL);
  options.method = 'POST';
  options.headers = {
    'Content-Type': 'application/json',
    'Content-Length': Buffer.byteLength(body),
  };

  let postReq = https.request(options, (res) => {
    let chunks = [];
    res.setEncoding('utf8');
    res.on('data', (chunk) => {
      return chunks.push(chunk);
    });
    res.on('end', () => {
      let body = chunks.join('');
      callback({
        body: body,
        statusCode: res.statusCode,
        statusMessage: res.statusMessage
      });
    });
    return res;
  });

  postReq.write(body);
  postReq.end();
};

exports.handler = function (event, context) {
  let message = JSON.parse(event.Records[0].Sns.Message);

  let text = `${message.AlarmName}\n${message.OldStateValue} -> ${message.NewStateValue}\nreason: ${message.NewStateReason}`;

  let payload = {
    channel: '#~~~~_notice-e',
    username: 'Cloudwatch Alarm',
    icon_emoji: ':aws_cloudwatch-alarm:',
    attachments: [{
      color: 'warning',
      title: '<!channel> DynamoDB alert!',
      text: text
    }]
  };

  postMessage(payload, (response) => {
    if (response.statusCode < 400) {
      context.succeed(`Message posted!`);
    } else if (response.statusCode < 500) {
      context.succeed(`4xx error occured when processing message: ${response.statusCode} - ${response.statusMessage}`);
    } else {
      // Retry Lambda func when got 5xx errors
      context.fail(`Server error when processing message: ${response.statusCode} - ${response.statusMessage}`);
    }
  });
};
