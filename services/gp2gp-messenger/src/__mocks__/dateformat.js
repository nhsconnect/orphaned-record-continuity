import { when } from 'jest-when';
const dateFormat = jest.fn();
export const fakeDateNow = '20200403092516';
when(dateFormat).calledWith(expect.anything(), 'yyyymmddHHMMss').mockReturnValue(fakeDateNow);
export default dateFormat;
