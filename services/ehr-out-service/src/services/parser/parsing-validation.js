import { ParsingError } from '../../errors/errors';

export const validateFieldsHaveSuccessfullyParsed = parsedFields => {
  const undefinedFields = Object.entries(parsedFields).filter(([key, value]) => {
    if (value === undefined) {
      return key;
    }
  });

  if (undefinedFields.length > 0) {
    throw new ParsingError(`The following fields have parsed as undefined: ${undefinedFields}`);
  }
};
