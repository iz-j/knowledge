var event = {
  Records: [{
    Sns: {
      Type: 'Notification',
      MessageId: 'f3d182b2-89f1-5ec3-98c6-d19fcbffba14',
      TopicArn: 'arn:aws:sns:ap-northeast-1:274527156693:dynamodb',
      Subject: 'ALARM: "AccountByEmail-ReadCapacityUnitsLimit-BasicAlarm" in Asia Pacific (Tokyo)',
      Message: '{"AlarmName":"AccountByEmail-ReadCapacityUnitsLimit-BasicAlarm","AlarmDescription":null,"AWSAccountId":"274527156693","NewStateValue":"ALARM","NewStateReason":"initializing","StateChangeTime":"2018-03-22T05:31:58.268+0000","Region":"Asia Pacific (Tokyo)","OldStateValue":"OK","Trigger":{"MetricName":"ConsumedReadCapacityUnits","Namespace":"AWS/DynamoDB","StatisticType":"Statistic","Statistic":"SUM","Unit":null,"Dimensions":[{"name":"TableName","value":"AccountByEmail"}],"Period":60,"EvaluationPeriods":5,"ComparisonOperator":"GreaterThanOrEqualToThreshold","Threshold":144.00000000000003,"TreatMissingData":"","EvaluateLowSampleCountPercentile":""}}',
      Timestamp: '2018-03-22T05:31:58.319Z',
      SignatureVersion: '1',
      Signature: 'V8TK2873VBZxcqxaHZhcW/h4X4oMCz2jyUmjDmajHF5o0mZJq5QYJswtsNQtQkY4aAnVtYb77ewMfQzpT/gV0cHCJ/6wZ+tlRT68tkoyyPXanv1Y5GSsV1Z/K/5CaT1v6L7/MV0hEoHh19VfbYhcs5z2VBkBTZgURMAKTds33hle+gXadR4jHSGN5hB/prIW1s8j7ueAnWJRqmLxaxKvgxEYStON02Ger67y2Zw/4j/99qvuj+NaTnuKV8oAnpw2B6BGqW3aoVZJ7q/y0kDhn6vbdh/QKs53v4XW0cNDK9im5rTaSiuPLjdP8UnS7Fj1tbOAZdeiwdkp4/WAh698EQ==',
      SigningCertUrl: 'https://sns.ap-northeast-1.amazonaws.com/SimpleNotificationService-433026a4050d206028891664da859041.pem',
      UnsubscribeUrl: 'https://sns.ap-northeast-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:ap-northeast-1:274527156693:dynamodb:8cda5365-5032-4761-81dc-62a47cc294ff',
      MessageAttributes: {}
    }
  }]
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