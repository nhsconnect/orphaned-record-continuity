const axios = require('axios');

axios
  .patch(process.env.EHR_URL + '/health-record/1234/message/4321', { transferComplete: true })
  .then(response => {
    console.log(response);
    if (response.status !== 204) {
      process.exit(5);
    }
  })
  .catch(err => {
    console.log(err);
    process.exit(8);
  });
