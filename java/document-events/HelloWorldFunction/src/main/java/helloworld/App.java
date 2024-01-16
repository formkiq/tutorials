package helloworld;

import com.formkiq.client.api.DocumentTagsApi;
import com.formkiq.client.invoker.ApiClient;
import com.formkiq.client.invoker.ApiException;
import com.formkiq.client.model.AddDocumentTag;
import com.formkiq.client.model.AddDocumentTagsRequest;

import java.util.Map;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<SQSEvent, Void> {

    public Void handleRequest(final SQSEvent input, final Context context) {

        DocumentTagsApi documentTagsApi = getDocumentTagsApi();

        // Loop through all the SQS Messages
        for(SQSMessage msg : input.getRecords()){

            String body = msg.getBody();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            Map<String, String> bodyMap = gson.fromJson(body, Map.class);
            Map<String, String> documentEvent = gson.fromJson(bodyMap.get("Message"), Map.class);

            String siteId = documentEvent.get("siteId");
            String documentId = documentEvent.get("documentId");

            context.getLogger().log("processing siteId: " + siteId + " documentId: " + documentId);

            AddDocumentTagsRequest req = new AddDocumentTagsRequest();
            req.addTagsItem(new AddDocumentTag().key("somekey").value("somevalue"));
            
            try {
                documentTagsApi.addDocumentTags(documentId, req, siteId, null);
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private DocumentTagsApi getDocumentTagsApi() {

        // Environment Variable Defining the FormKiQ API URL 
        // that uses IAM authorization
        String url = System.getenv("IAM_API_URL");
        
        // Create API Client and set the signing credentials
        ApiClient apiClient = new ApiClient().setReadTimeout(0).setBasePath(url);
        apiClient.setAWS4Configuration(System.getenv("AWS_ACCESS_KEY_ID"),
            System.getenv("AWS_SECRET_ACCESS_KEY"), System.getenv("AWS_SESSION_TOKEN"),
            System.getenv("AWS_REGION"), "execute-api");

        // Create FormKiQ DocumentTags API
        return new DocumentTagsApi(apiClient);
    }
}
