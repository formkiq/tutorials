package helloworld;

import java.text.MessageFormat;
import java.util.UUID;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Handler for requests to Lambda function.
 */
public class S3Graalvm implements RequestHandler<Object, Object> {

  private S3ClientBuilder builder = S3Client.builder();

  static {
      System.setProperty("software.amazon.awssdk.http.service.impl",
        "software.amazon.awssdk.http.urlconnection.UrlConnectionSdkHttpService");
  }
  
  @Override
  public Object handleRequest(Object input, Context context) {
    String bucket = System.getenv("S3Bucket");
    String key = UUID.randomUUID().toString();

    try (S3Client client = builder.build()) {
      client.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(),
          RequestBody.fromString("This is a test"));
    }

    String msg = MessageFormat.format("Created S3 File {0} in bucket {1}", key, bucket);
    context.getLogger().log(MessageFormat.format("Created S3 File {0} in bucket {1}", key, bucket));
    return msg;
  }
}