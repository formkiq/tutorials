// importing the dependencies
const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const fs = require('fs');
const multer = require('multer');
const path = require('path');
const axios = require('axios');

//const apiKey = "<API_KEY>";
//const keyApiUrl = "<KEY_API_URL>";


const apiKey = "WyGERUCU8G8jaL1hI2jGaOvJ02BowtKmFs916zuMGGSoBTAsEqJ";
const keyApiUrl = "https://gf7ty26nel.execute-api.ca-central-1.amazonaws.com";

// Set up Multer storage for handling file uploads
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, 'uploads/');
  },
  filename: function (req, file, cb) {
    const ext = path.extname(file.originalname);
    cb(null, Date.now() + ext);
  },
});

const upload = multer({ storage: storage });

// defining the Express app
const app = express();

// adding Helmet to enhance your API's security
app.use(helmet());

// using bodyParser to parse JSON bodies into JS objects
app.use(bodyParser.json());

// enabling CORS for all requests
app.use(cors());

// adding morgan to log HTTP requests
app.use(morgan('combined'));

// defining an endpoint to return a "hello, world"
app.get('/', (req, res) => {
  res.send([
    {title: 'Hello, world!'}
  ]);
});

const axiosConfig = {
  headers: {
    'Authorization': `${apiKey}`,
    'Content-Type': 'application/json',
  },
};

// define an endpoint for file uploads
app.post('/upload', upload.single('document'), (req, res) => {

  // TODO: is this user authorized to upload documents?

  // Handle the uploaded file
  if (!req.file) {
    return res.status(400).json({ message: 'No file uploaded' });
  }

  const fileName = req.file.filename;
  const filePath = path.join(__dirname, '../uploads', fileName);
  const fileBuffer = fs.readFileSync(filePath);

  const uploadMetadata = {
    tags: [
      {
        "key":"approvalRequired",
        "value":"true"
      },
      {
        "key":"submittedByUser",
        "value":"user@mycompany.com"
      },
    ]
  };

  axios.post(keyApiUrl + "/documents/upload", uploadMetadata, axiosConfig)
  .then((response) => {
    // Handle the response from the API
    console.log('Response from the API:');

    const awsPresignedUrl = response.data.url;

    const putConfig = {
      headers: {
        'Content-Type': req.file.mimetype,
      },
    };

    // upload document content
    axios.put(awsPresignedUrl, fileBuffer, putConfig)
    .then((response) => {
      console.log('File uploaded to FormKiQ.');
      console.log('Response:', response);
    })
    .catch((error) => {
      console.error('Error uploading file:', error.message);
    });

    res.json({ message: 'File uploaded successfully!', documentId: response.data.documentId});

    // delete temporary file from uploads
    fs.unlinkSync(filePath)

  })
  .catch((error) => {
    // Handle any errors that occurred during the request
    console.error('Error sending POST request:', error.message);
  });
});

app.get('/documents', (req, res) => {
  const searchData = {
    query:
    {
      "tag":{
        "eq": "true",
        "key": "approvalRequired"
      }
    }
  };
  axios.post(keyApiUrl + "/search", searchData, axiosConfig)
  .then((response) => {
    // Handle the response from the API
    res.send(response.data);
  })
  .catch((error) => {
    // Handle any errors that occurred during the request
    console.error('Error sending POST request:', error.message);
  });
});

// starting the server
app.listen(3001, () => {
  console.log('listening on port 3001');
});
