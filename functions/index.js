const functions = require('firebase-functions');

const admin = require("firebase-admin");
admin.initializeApp();

exports.testFunc = functions
  .https.onCall(async (data, context) => {

    //to get data sent by the client
    //var requestName = data.name;
    //var requestID = data.id;
    

    return {
      name:"myName",
      age:54
    }


  });
