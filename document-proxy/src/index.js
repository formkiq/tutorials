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

const apiKey = "<API KEY>";
const keyApiUrl = "<KEY API URL";

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

// defining an array to work as the database (temporary solution)
const ads = [
  {title: 'Hello, world (again)!'}
];

// adding Helmet to enhance your API's security
app.use(helmet());

// using bodyParser to parse JSON bodies into JS objects
app.use(bodyParser.json());

// enabling CORS for all requests
app.use(cors());

// adding morgan to log HTTP requests
app.use(morgan('combined'));

// defining an endpoint to return all ads
app.get('/', (req, res) => {
  res.send(ads);
});

// Set up a route for file uploads
app.post('/upload', upload.single('document'), (req, res) => {
  // Handle the uploaded file
  if (!req.file) {
    return res.status(400).json({ message: 'No file uploaded' });
  }

  const fileName = req.file.filename;
  const filePath = path.join(__dirname, '../uploads', fileName);
  const fileBuffer = fs.readFileSync(filePath);

  const axiosConfig = {
    headers: {
      'Authorization': `${apiKey}`,
      'Content-Type': 'application/json',
    },
  };

  const postData = {
    tags: [
    {
      "key":"approvalRequired",
      "value":"true"
    }
    ]
  };

  axios.post(keyApiUrl + "/documents/upload", postData, axiosConfig)
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
