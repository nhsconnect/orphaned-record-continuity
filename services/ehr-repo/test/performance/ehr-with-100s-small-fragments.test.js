import { v4 } from 'uuid';
import axios from 'axios';

const headers = { Authorization: process.env.E2E_TEST_AUTHORIZATION_KEYS_FOR_EHR_REPO };

async function sendFragment(id, conversationId, fragmentMessageIds) {
  const fragment = {
    data: {
      type: 'messages',
      id,
      attributes: {
        conversationId,
        messageType: 'fragment',
        fragmentMessageIds
      }
    }
  };

  const response = await axios.post(`http://localhost:3000/messages`, fragment, {
    adapter: 'http',
    headers
  });
  // Assert 201 for each fragment
  expect(response.status).toEqual(201);
}

async function sendEhrExtract(ehrExtractMessageId, conversationId, nhsNumber, fragmentMessageIds) {
  const ehrExtract = {
    data: {
      type: 'messages',
      id: ehrExtractMessageId,
      attributes: {
        conversationId,
        nhsNumber,
        messageType: 'ehrExtract',
        fragmentMessageIds: fragmentMessageIds
      }
    }
  };

  const ehrResponse = await axios.post(`http://localhost:3000/messages`, ehrExtract, {
    adapter: 'http',
    headers
  });

  expect(ehrResponse.status).toEqual(201);
}

const timeAction = async (description, action) => {
  const before = new Date();
  await action();
  const after = new Date();

  // Assert it returns 201
  // Measure how long it took for ehr extract to be stored
  console.log(description, after - before, 'ms');
};

const testPerformance = async (
  smallFragmentMessageIds,
  largeNestedFragmentId,
  nestedFragmentIds
) => {
  const ehrExtractMessageId = v4().toUpperCase();
  const conversationId = v4().toUpperCase();
  const nhsNumber = '1234567890';

  let ehrFragmentMessageIds = [...smallFragmentMessageIds];
  if (largeNestedFragmentId) {
    ehrFragmentMessageIds = smallFragmentMessageIds.concat([largeNestedFragmentId]);
  }

  await timeAction('Time taken to store health record extract: ', () =>
    sendEhrExtract(ehrExtractMessageId, conversationId, nhsNumber, ehrFragmentMessageIds)
  );

  await timeAction('Time taken to store all fragments: ', async () => {
    if (largeNestedFragmentId) {
      await sendFragment(largeNestedFragmentId, conversationId, nestedFragmentIds);
    }

    const allSmallFragments = smallFragmentMessageIds.concat(nestedFragmentIds);

    for (const id of allSmallFragments) {
      await sendFragment(id, conversationId, []);
    }
  });

  // Retrieve ehr
  const retrieval = await axios.get(
    `http://localhost:3000/patients/${nhsNumber}/health-records/${conversationId}`,
    {
      headers,
      adapter: 'http'
    }
  );

  // Assert 200 and assert correct number of fragments
  expect(retrieval.status).toEqual(200);
};

describe('Performance of EHR', () => {
  it('Performance of EHR with 100 small fragments - within 20 seconds', async () => {
    //send ehr with 100 fragment to ehr repo -
    // POST /messages with type ehrExtract and 100 entries in the fragment list
    let fragmentMessageIds = [];

    const numberOfFragments = 100;
    for (let i = 0; i < numberOfFragments; i++) {
      fragmentMessageIds.push(v4().toUpperCase());
    }

    await testPerformance(fragmentMessageIds, null, []);
  }, 20000);

  it('Performance of EHR with 50 fragments, one of which is a large nested fragment - within 25 seconds', async () => {
    // Send ehr with 50 fragments to store in EHR repo database
    let fragmentMessageIds = [];

    const numberOfFragments = 50;
    for (let i = 0; i < numberOfFragments; i++) {
      fragmentMessageIds.push(v4().toUpperCase());
    }

    // Create one large nested fragment which is made up of 50 parts
    let nestedFragmentIds = [];
    const numberOfNestedFragments = 50;
    const largeNestedFragmentId = v4().toUpperCase();
    for (let i = 0; i < numberOfNestedFragments - 1; i++) {
      nestedFragmentIds.push(v4().toUpperCase());
    }

    // Run test
    await testPerformance(fragmentMessageIds, largeNestedFragmentId, nestedFragmentIds);
  }, 25000);
});
