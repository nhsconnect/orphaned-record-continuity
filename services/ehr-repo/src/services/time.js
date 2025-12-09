import moment from 'moment-timezone';

export const getNow = () => new Date();

export const getUKTimestamp = () => {
  return moment().tz('Europe/London').format('YYYY-MM-DDTHH:mm:ssZ');
};
export const TIMESTAMP_REGEX = /\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\+\d{2}:00/;

export const getEpochTimeInSecond = (datetime) => {
  return moment(datetime).unix();
};
