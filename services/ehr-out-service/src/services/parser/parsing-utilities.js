import { XmlParser } from './xml-parser/xml-parser';
import { validateFieldsHaveSuccessfullyParsed } from './parsing-validation';
import { ParsingError } from '../../errors/errors';

export const jsonParseMessage = message => {
  if (typeof message != 'object') {
    try {
      return JSON.parse(message);
    } catch (error) {
      throw new ParsingError(error);
    }
  } else {
    return message;
  }
};

export const parseInteractionId = async message => {
  const ebxmlAsJson = await new XmlParser().parse(jsonParseMessage(message).ebXML);

  const parsedFields = {
    interactionId: ebxmlAsJson?.['data']?.['Envelope']?.['Header']?.['MessageHeader']?.['Action']
  };

  validateFieldsHaveSuccessfullyParsed(parsedFields);

  return parsedFields.interactionId;
};

export const parseConversationId = async message => {
  const ebxmlAsJson = await new XmlParser().parse(jsonParseMessage(message).ebXML);

  const parsedFields = {
    conversationId:
      ebxmlAsJson?.['data']?.['Envelope']?.['Header']?.['MessageHeader']?.['ConversationId']
  };

  validateFieldsHaveSuccessfullyParsed(parsedFields);

  return parsedFields.conversationId;
};

export const parseMessageId = async message => {
  const ebxmlAsJson = await new XmlParser().parse(jsonParseMessage(message).ebXML);

  const parsedFields = {
    messageId:
      ebxmlAsJson?.['data']?.['Envelope']?.['Header']?.['MessageHeader']?.['MessageData']?.[
        'MessageId'
      ]
  };

  validateFieldsHaveSuccessfullyParsed(parsedFields);

  return parsedFields.messageId;
};

export const extractReferencedFragmentMessageIds = async message => {
  const ebXmlAsJson = await new XmlParser().parse(jsonParseMessage(message).ebXML);

  const parsedFields = {
    reference: ebXmlAsJson?.['data']?.['Envelope']?.['Body']?.['Manifest']?.['Reference']
  };

  validateFieldsHaveSuccessfullyParsed(parsedFields);

  // Ensure we got an array before continue, as parser might yield the field 'Reference'
  // as an array or single object, depends on how many references in input file.
  const referencesList = Array.isArray(parsedFields.reference)
    ? parsedFields.reference
    : [parsedFields.reference];

  const fragmentReferences = referencesList.filter(reference => reference?.href.startsWith('mid:'));

  return fragmentReferences.map(reference => reference.href.replace('mid:', ''));
};
