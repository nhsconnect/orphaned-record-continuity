const MOCKED_UUID = '00000000-0000-4000-a000-000000000000';
const MOCKED_UUID_V1 = '9adcb64c-28c0-11eb-adc1-0242ac120002';
const v4 = jest.fn().mockImplementation(() => MOCKED_UUID);
const v1 = jest.fn().mockImplementation(() => MOCKED_UUID_V1);
export { v4, MOCKED_UUID, v1, MOCKED_UUID_V1 };
