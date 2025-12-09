import { jsObjectToXmlString, xmlStringToJsObject } from '../message-utilities';

describe('message-utilities', () => {
  it('should keep escaped linebreak symbols unchanged', async () => {
    // given
    const inputXml =
      '<text>Drinking status on eventdate: &#10;&#10;&#10;   &#13;&#10; Current drinker</text>';

    // when
    const jsObjectAfterParse = xmlStringToJsObject(inputXml);
    const xmlStringAfterRebuild = jsObjectToXmlString(jsObjectAfterParse);

    // then
    expect(xmlStringAfterRebuild).toEqual(inputXml);
  });

  it('should keep leading and trailing whitespaces in tags unchanged', async () => {
    // given
    const inputXml =
      '<text>     Drinking status on eventdate: &#10;&#10;&#10;   &#13;&#10; Current drinker    </text>';

    // when
    const jsObjectAfterParse = xmlStringToJsObject(inputXml);
    const xmlStringAfterRebuild = jsObjectToXmlString(jsObjectAfterParse);

    // then
    expect(xmlStringAfterRebuild).toEqual(inputXml);
  });

  it('should keep escaped greater than (>) and less than (<) symbols unchanged', async () => {
    // given
    const inputXml =
      '<text>Drinking status on eventdate: &lt; &gt; &gt; &lt; Current drinker</text>';

    // when
    const jsObjectAfterParse = xmlStringToJsObject(inputXml);
    const xmlStringAfterRebuild = jsObjectToXmlString(jsObjectAfterParse);

    // then
    expect(xmlStringAfterRebuild).toEqual(inputXml);
  });

  it(`should keep escaped apostrophes (') and quotation marks (") unchanged`, async () => {
    // given
    const inputXml =
      '<agentPerson classCode="PSN" determinerCode="INSTANCE"><name><family>DR &quot;DK NANDI&apos;S &quot;PRACTICE</family></name></agentPerson>';

    // when
    const jsObjectAfterParse = xmlStringToJsObject(inputXml);
    const xmlStringAfterRebuild = jsObjectToXmlString(jsObjectAfterParse);

    // then
    expect(xmlStringAfterRebuild).toEqual(inputXml);
  });

  it(`should not remove leading 0s in the inner text`, async () => {
    // given
    const inputXml = '<Version xsi:type="xsd:string">01</Version>';

    // when
    const jsObjectAfterParse = xmlStringToJsObject(inputXml);
    const xmlStringAfterRebuild = jsObjectToXmlString(jsObjectAfterParse);

    // then
    expect(xmlStringAfterRebuild).toEqual(inputXml);
  });
});
