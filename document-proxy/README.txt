https://auth0.com/blog/node-js-and-express-tutorial-building-and-securing-restful-apis/

Create API Key
POST https://s1rsnvhtl3.execute-api.us-east-2.amazonaws.com/configuration/apiKeys
{
  "name": "My API Key"
}
Response:
{
    "apiKey": "DezDxw3ocmFGMZQUfcATxivIiyTq3hC35N39PcJzkI7F6uyZAkT"
}

1. mkdir document-proxy
2. npm init -y
3. mkdir src
4. npm install body-parser cors express helmet morgan multer axios
5. Upload API Key & HttpApiUrl in code
6. mkdir uploads (make place to store file uploads directory)
7. node src <-- to run application
8. curl -F "document=@/Users/mike/Downloads/onlineStatement.pdf" http://localhost:3001/upload
