var url = require('url');
var https = require('https');
var zlib = require('zlib');

const ADMIN_URL = 'https://~~~~.ap-northeast-1.elasticbeanstalk.com/';
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

var toURL = (data) => {
  var toBase64 = (str) => {
    let b64 = new Buffer(str, 'binary').toString('base64');
    return b64;
  };

  let groupURL = encodeURIComponent(toBase64(data.logGroup));
  let streamURL = encodeURIComponent(toBase64(data.logStream));
  return `${ADMIN_URL}/main/logs/${groupURL}/${streamURL}`;
};

exports.handler = function (event, context) {
  let base64Logs = new Buffer(event['awslogs']['data'], 'base64');
  zlib.gunzip(base64Logs, (err, binary) => {
    let decodedLogs = binary.toString('ascii');
    let data = JSON.parse(decodedLogs);
    let logURL = toURL(data);

    let text = `logGroup: ${data.logGroup}\nlogStream: <${logURL}|${data.logStream}>\nmessage: ${data.logEvents[0].message}`;

    let payload = {
      channel: '#~~~~_notice-e',
      username: 'Cloudwatch Alarm',
      icon_emoji: ':aws_cloudwatch-alarm:',
      attachments: [{
        color: 'danger',
        title: '<!channel> Error occurred!!!',
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
  });
};
