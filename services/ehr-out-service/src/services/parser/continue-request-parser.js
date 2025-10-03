import { validateFieldsHaveSuccessfullyParsed } from './parsing-validation';
import { INTERACTION_IDS } from '../../constants/interaction-ids';
import { jsonParseMessage } from './parsing-utilities';
import { XmlParser } from './xml-parser/xml-parser';

export const parseContinueRequestMessage = async message => {
  const messageParts = {
    payload: await new XmlParser().parse(jsonParseMessage(message).payload),
    ebxml: await new XmlParser().parse(jsonParseMessage(message).ebXML)
  };

  const odsCode =
    messageParts.payload?.['data']?.[INTERACTION_IDS.CONTINUE_REQUEST]?.['ControlActEvent']?.[
      'subject'
    ]?.['PayloadInformation']?.['value']?.['Gp2gpfragment']?.['From'];

  const messageId =
    messageParts.ebxml?.['data']?.['Envelope']?.['Header']?.['MessageHeader']?.['MessageData']?.[
      'MessageId'
    ];

  const parsedFields = { odsCode, messageId };

  validateFieldsHaveSuccessfullyParsed(parsedFields);

  return parsedFields;
};
