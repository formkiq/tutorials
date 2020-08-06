
var aws = require('aws-sdk');
var ses = new aws.SES();

/**
 *
 * Listens for a FormKiQ DocumentEvent from the Document SNS Topics.
 * 
 * Expected Event:
 * 
 * {
 *   "Records": [
 *       {
 *           "messageId": "bf4e101c-7734-49aa-a819-b289f2de4677",
 *           "receiptHandle": "....",
 *           "body": "{\"documentId\":\"4741ea5f-edbd-4fe7-b808-165d897673bf\",\"s3key\":\"4741ea5f-edbd-4fe7-b808-165d897673bf\",\"s3bucket\":\"formkiq-stacks-document-prod-XXXXXXXXXXXX-documents\",\"type\":\"create\"}",
 *           "attributes": {
 *               "ApproximateReceiveCount": "1",
 *               "SentTimestamp": "1596469659190",
 *               "SenderId": "AIDAIT2UOQQY3AUEKVGXU",
 *               "ApproximateFirstReceiveTimestamp": "1596469659192"
 *           },
 *           "messageAttributes": {},
 *           "md5OfBody": "cd74f64eb9cc72b9dc19ab6c07d47e03",
 *           "eventSource": "aws:sqs",
 *           "eventSourceARN": "arn:aws:sqs:us-east-1:XXXXXXXXXXXX:sam-app-HellowWorldQueue-1PHBYFIE8USTK",
 *           "awsRegion": "us-east-1"
 *       }
 *   ]
 * }
 * 
 */
exports.lambdaHandler = async (event, context) => {

    var toEmail = "<email>";
    var fromEmail = toEmail;
    var body = "";
    
    if (event.Records != null) {
         
        event.Records.forEach(function(record) {
            if (record.body) {
                let bodyRecord = JSON.parse(record.body);
                body += bodyRecord.type + " event for document " + bodyRecord.documentId + " located in s3 bucket " + bodyRecord.s3bucket + " and s3 key " + bodyRecord.s3key + "\n";
            }
        });
    }

    var params = {
        Source: fromEmail,
        Destination: { ToAddresses: [toEmail] },
        Message: {
            Subject: { Data: 'FormKiQ Document Event'
        },
        Body: { Text: { Data: body } } }
    };
  
    return ses.sendEmail(params).promise().then(data => {
      console.log("sent message " + data.MessageId);
    }).catch(console.error.bind(console));
};