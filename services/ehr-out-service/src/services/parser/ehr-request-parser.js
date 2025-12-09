import { validateFieldsHaveSuccessfullyParsed } from './parsing-validation';
import { INTERACTION_IDS } from '../../constants/interaction-ids';
import { jsonParseMessage } from './parsing-utilities';
import { XmlParser } from './xml-parser/xml-parser';

export const parseEhrRequestMessage = async message => {
  const messageParts = {
    payload: await new XmlParser().parse(jsonParseMessage(message).payload)
  };

  const ehrRequestContent =
    messageParts.payload?.['data']?.[INTERACTION_IDS.EHR_REQUEST]?.['ControlActEvent']?.[
      'subject'
    ]?.['EhrRequest'];

  const parsedFields = {
    ehrRequestId: ehrRequestContent?.['id']?.['root'],
    nhsNumber: ehrRequestContent?.['recordTarget']?.['patient']?.['id']?.['extension'],
    odsCode:
      ehrRequestContent?.['author']?.['AgentOrgSDS']?.['agentOrganizationSDS']?.['id']?.[
        'extension'
      ]
  };

  validateFieldsHaveSuccessfullyParsed(parsedFields);

  return parsedFields;
};
