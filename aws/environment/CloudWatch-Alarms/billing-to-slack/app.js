var AWS = require('aws-sdk');
var url = require('url');
var https = require('https');

const HOOK_URL = 'https://hooks.slack.com/services/~~~~/~~~~';

var cloudwatch = new AWS.CloudWatch({ region: 'us-east-1' });

var getBillingMetric = () => {
  let startTime = new Date();
  startTime.setDate(startTime.getDate() - 1);
  let params = {
    EndTime: new Date(),
    MetricName: 'EstimatedCharges',
    Namespace: 'AWS/Billing',
    StartTime: startTime,
    Dimensions: [{ 'Name': 'Currency', 'Value': 'USD' }],
    Statistics: ['Maximum'],
    Period: 864000
  };

  return new Promise((resolve, reject) => {
    cloudwatch.getMetricStatistics(params, (err, data) => {
      if (err) {
        console.error(err, err.stack);
        reject(err);
      } else {
        console.log(data);
        resolve(data['Datapoints'][0]['Maximum']);
      }
    });
  });
};

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
  Promise.resolve()
    .then(() => {
      console.log('Get billing metric statistics ...');
      return getBillingMetric();
    })
    .then(billing => {
      console.log(`Billing -> '${billing}$' ...`);

      let payload = {
        channel: '#~~~~',
        username: 'billing-estimation',
        icon_emoji: ':money_with_wings:',
        text: `AWS billing amount -> *${billing}$*`,
      };

      postMessage(payload, function (response) {
        if (response.statusCode < 400) {
          context.succeed(`Message posted!`);
        } else if (response.statusCode < 500) {
          context.succeed(`4xx error occured when processing message: ${response.statusCode} - ${response.statusMessage}`);
        } else {
          context.fail(`Server error when processing message: ${response.statusCode} - ${response.statusMessage}`);
        }
      });
    })
    .catch(err => context.fail(err));
};
