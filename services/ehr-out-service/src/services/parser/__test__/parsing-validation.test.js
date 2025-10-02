import { validateFieldsHaveSuccessfullyParsed } from '../parsing-validation';
import { ParsingError } from '../../../errors/errors';
import expect from 'expect';

describe('parsing-validation.js', () => {
  it('should throw a ParsingError if one or more fields are undefined', () => {
    // Given
    const parsedFields = {
      nhsNumber: undefined,
      odsCode: undefined
    };

    // Then
    expect(() => validateFieldsHaveSuccessfullyParsed(parsedFields)).toThrow(ParsingError);
  });

  it('should not throw a ParsingError if all fields are defined', () => {
    // Given
    const parsedFields = {
      nhsNumber: 1234567890,
      odsCode: 'B85002'
    };

    // Then
    expect(() => validateFieldsHaveSuccessfullyParsed(parsedFields)).not.toThrow(ParsingError);
  });
});
