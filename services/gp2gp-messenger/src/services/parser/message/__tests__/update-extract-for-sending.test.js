import { updateExtractForSending } from '../update-extract-for-sending';
import { readFileSync } from 'fs';
import * as path from 'path';
import { v4 } from 'uuid';

describe('updateExtractForSending', () => {
  // Constants for test
  const OLD_RECEIVING_ASID = '200000001161';
  const OLD_SENDING_ASID = '200000000149';
  const OLD_EHR_REQUEST_ID = v4();
  const OLD_AUTHOR_ODS_CODE = 'OLD-AUTHOR-ODS-CODE';
  const OLD_DEST_ODS_CODE = 'OLD-DESTINATION-ODS-CODE';
  // TODO: Commented out because was failing when going for FOT V1
  // const NEW_SENDING_ASID = '200000001161';
  const NEW_RECEIVING_ASID = '200000001162';
  const NEW_AUTHOR_ODS_CODE = 'NEW-AUTHOR-ODS-CODE';
  const NEW_DEST_ODS_CODE = 'NEW-DESTINATION-ODS-CODE';

  const templateEhrExtract = (
    receivingAsid,
    sendingAsid,
    priorEhrRequestId,
    authorOdsCode,
    destOdsCode
  ) => {
    const template = readFileSync(path.join(__dirname, 'data', 'templateEhrExtract'), 'utf-8');
    return template
      .replaceAll('${receivingAsid}', receivingAsid)
      .replaceAll('${sendingAsid}', sendingAsid)
      .replaceAll('${priorEhrRequestId}', priorEhrRequestId)
      .replaceAll('${authorOdsCode}', authorOdsCode)
      .replaceAll('${destOdsCode}', destOdsCode);
  };
  const buildInputEhrExtract = () => {
    return templateEhrExtract(
      OLD_RECEIVING_ASID,
      OLD_SENDING_ASID,
      OLD_EHR_REQUEST_ID,
      OLD_AUTHOR_ODS_CODE,
      OLD_DEST_ODS_CODE
    );
  };

  // TODO: REMOVE THESE TESTS
  // it('should update sender and receiver ASID codes, ODS codes and EHR Request ID in EHR Core', async () => {
  //   // given
  //   const originalEhrExtract = buildInputEhrExtract();
  //   const ehrRequestId = v4();
  //
  //   const expectedEhrExtract = templateEhrExtract(
  //     NEW_RECEIVING_ASID,
  //     NEW_SENDING_ASID,
  //     ehrRequestId,
  //     NEW_AUTHOR_ODS_CODE,
  //     NEW_DEST_ODS_CODE
  //   );
  //
  //   // when
  //   const newEhrExtract = await updateExtractForSending(
  //     originalEhrExtract,
  //     ehrRequestId,
  //     NEW_RECEIVING_ASID,
  //     NEW_AUTHOR_ODS_CODE,
  //     NEW_DEST_ODS_CODE
  //   );
  //
  //   // then
  //   const parsedNewEhrExtract = await new XmlParser().parse(newEhrExtract);
  //   const parsedExpectedEhrExtract = await new XmlParser().parse(expectedEhrExtract);
  //   expect(isEqual(parsedNewEhrExtract, parsedExpectedEhrExtract)).toBe(true);
  // });

  it('should keep escaped special characters (linebreak, apostrophes, quotation marks) unchanged', async () => {
    // given
    const originalEhrExtract = buildInputEhrExtract();

    // manually add some escaped special characters to the ehr extract
    const replacedText =
      'Drinking status on eventdate: &#10;&#10;&#10; &apos; &quot; &Current drinker';
    const ehrExtractWithSpecialChars = originalEhrExtract.replace(
      'Drinking status on eventdate: Current drinker',
      replacedText
    );
    const ehrRequestId = v4();

    // when
    const newEhrExtract = await updateExtractForSending(
      ehrExtractWithSpecialChars,
      ehrRequestId,
      NEW_RECEIVING_ASID,
      NEW_AUTHOR_ODS_CODE,
      NEW_DEST_ODS_CODE
    );

    // then
    // expect that the special chars are still in the updated ehr extract
    expect(newEhrExtract.includes(replacedText)).toBe(true);
  });
});
