const { v4: uuid } = require('uuid');
const dateFormat = require('dateformat');
const generateEhrRequestQuery = require('../src/templates/ehr-request-template');
const generateUpdatePdsRequest = require('../src/templates/generate-update-ods-request');
const generatePdsRetrievalQuery = require('../src/templates/pds-retrieval-template');
const generateContinueRequest = require('../src/templates/generate-continue-request');

// receiving_asid_2: '200000000631',
const emisPatient = {
  title: 'Miss',
  gender: 'Female',
  firstName: 'Emma',
  lastName: 'Fergus',
  dob: '20170104',
  nhsNumber: '9651221577',
  pdsId: '273B24A9',
  conversationId: 'hhh'
};

// receiving_asid_2: '200000000631',
const tppPatient = {
  gender: 'Female',
  firstName: 'Justice',
  lastName: 'Sadare',
  dob: '19701202',
  nhsNumber: '9442964410',
  pdsId: 'cppz',
  conversationId: '',
  serialChaneNumber: '138' // + 1 each time
};

const emisPractise = {
  name: 'Walton Village R',
  odsCode: 'N82668',
  asid: '200000000205'
};

const tppPractise = {
  name: '',
  odsCode: 'M85019',
  asid: ''
};

const mhs = {
  name: 'Deductions MHS',
  odsCode: 'B86041',
  asid: '200000001161'
};

const pds = {
  name: 'PDS',
  odsCode: 'YES',
  asid: '928942012545'
};

//   pdsASID:
//   conversationId: '6A073022-182F-4106-92CC-86E933F71A3E'

console.log(dateFormat(Date.now(), 'yyyymmddHHMMss'));

const advancePdsRequest = generateAdvancePdsRequest(
  uuid(),
  dateFormat(Date.now(), 'yyyymmddHHMMss'),
  patient.mhsASID,
  patient.pdsASID,
  genderCode(patient.patientGender),
  patient.patientTitle,
  patient.patientFirstName,
  patient.patientLastName,
  patient.patientDOB
);

const gp2gpRequest = generateEhrRequestQuery({
  id: uuid(),
  timestamp: dateFormat(Date.now(), 'yyyymmddHHMMss'),
  patient: {
    nhsNumber: emisPatient.nhsNumber
  },
  receivingService: {
    asid: emisPractise.asid,
    odsCode: emisPractise.odsCode
  },
  sendingService: {
    asid: mhs.asid,
    odsCode: mhs.odsCode
  }
});

const pdsUpdateRequestToUs = generateUpdatePdsRequest({
  id: uuid(),
  timestamp: dateFormat(Date.now(), 'yyyymmddHHMMss'),
  receivingService: { asid: pds.asid },
  sendingService: { asid: mhs.asid, odsCode: mhs.odsCode },
  patient: {
    nhsNumber: tppPatient.nhsNumber,
    pdsId: tppPatient.pdsId,
    pdsUpdateChangeNumber: tppPatient.serialChaneNumber
  }
});

const pdsRetrevalQuery = generatePdsRetrievalQuery({
  id: uuid(),
  timestamp: dateFormat(Date.now(), 'yyyymmddHHMMss'),
  receivingService: { asid: pds.asid },
  sendingService: { asid: mhs.asid },
  patient: { nhsNumber: tppPatient.nhsNumber }
});

const gp2gpContinueMessage = generateContinueRequest(
  uuid(),
  dateFormat(Date.now(), 'yyyymmddHHMMss'),
  patient.receiving_asid_2,
  patient.mhsASID,
  patient.conversationId,
  patient.practiseODSCode,
  patient.mhsODSCode
);

//console.log('AdvancePDS:\n', advancePdsRequest);
//console.log('GP2GP Request:\n', gp2gpRequest);
//console.log('Retreival querr PDS:\n', pdsRetrevalQuery);
console.log('PDS Update GP -> Us:\n', pdsUpdateRequestToUs);
//console.log('Continue Message:\n', gp2gpContinueMessage);
