
Create API Key
POST https://s1rsnvhtl3.execute-api.us-east-2.amazonaws.com/configuration/apiKeys
{
  "name": "My API Key"
}
Response:
{
    "apiKey": "DezDxw3ocmFGMZQUfcATxivIiyTq3hC35N39PcJzkI7F6uyZAkT"
}

1. within the `server-side-proxy` directory, run `npm install`
2. use the FormKiQ Console (or the FormKIQ API) to create an API key
3. add values for {apiKey} and {keyAuthApiUrl} in app.js
4. `mkdir uploads` (creates a place to store file uploads)
5. run the application: `node src/app.js`
6. upload a document: `curl -F "document=@<DOCUMENT_PATH>" http://localhost:3001/upload`
