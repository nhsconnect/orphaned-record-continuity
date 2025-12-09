Query to SDS, to lookup ASID of the TPP practise:
```
ldapsearch -L -v -x  -H ldaps://ldap.nis1.national.ncrs.nhs.uk:636 -b ou=services,o=nhs \
 '(&(objectclass=nhsAS) (nhsIDCode=M85019) (nhsASSvcIA=urn:nhs:names:services:gp2gp:RCMR_IN010000UK05))' \
 '+' '*'
```

Response:
```
ldap_initialize( ldaps://ldap.nis1.national.ncrs.nhs.uk:636/??base )
filter: (&(objectclass=nhsAS) (nhsIDCode=M85019) (nhsASSvcIA=urn:nhs:names:services:gp2gp:RCMR_IN010000UK05))
requesting: + *
version: 1

#
# LDAPv3
# base <ou=services,o=nhs> with scope subtree
# filter: (&(objectclass=nhsAS) (nhsIDCode=M85019) (nhsASSvcIA=urn:nhs:names:services:gp2gp:RCMR_IN010000UK05))
# requesting: + *
#

# 200000000149, Services, nhs
dn: uniqueIdentifier=200000000149,ou=Services,o=nhs
objectClass: nhsas
objectClass: top
nhsProductKey: 10830
nhsMhsManufacturerOrg: YGM24
nhsRequestorURP: uniqueidentifier=555008543106,uniqueidentifier=555008542105,u
 id=134935459518,ou=people, o=nhs
nhsMHSPartyKey: YGM24-820388
nhsDateApproved: 20150302145732
nhsProductVersion: 6.01 Core + EPS2 + GP2GP (AS)
nhsProductName: SystmOne
nhsDateRequested: 20150302141231
nhsAsClient: M85019
nhsApproverURP: uniqueidentifier=062238671515,uniqueidentifier=356526849514,ui
 d=279851860510,ou=people, o=nhs
nhsAsSvcIA: urn:nhs:names:services:lrsquery:GET_RESOURCE_PERMISSIONS_INUK01
nhsAsSvcIA: urn:nhs:names:services:lrsquery:GET_RESOURCE_PERMISSIONS_RESPONSE_
 INUK01
nhsAsSvcIA: urn:nhs:names:services:lrsquery:MCCI_IN010000UK13
nhsAsSvcIA: urn:nhs:names:services:lrsquery:HAS_RESOURCE_PERMISSIONS_INUK01
nhsAsSvcIA: urn:nhs:names:services:lrsquery:HAS_RESOURCE_PERMISSIONS_RESPONSE_
 INUK01
nhsAsSvcIA: urn:nhs:names:services:lrs:MCCI_IN010000UK13
nhsAsSvcIA: urn:nhs:names:services:lrs:SET_RESOURCE_PERMISSIONS_INUK01
nhsAsSvcIA: urn:nhs:names:services:ebs:MCCI_IN010000UK13
nhsAsSvcIA: urn:nhs:names:services:ebs:PRPA_IN010000UK07
nhsAsSvcIA: urn:nhs:names:services:ebs:PRPA_IN020000UK06
nhsAsSvcIA: urn:nhs:names:services:ebs:PRPA_IN030000UK08
nhsAsSvcIA: urn:oasis:names:tc:ebxml-msg:service:Acknowledgment
nhsAsSvcIA: urn:oasis:names:tc:ebxml-msg:service:MessageError
nhsAsSvcIA: urn:nhs:names:services:mm:MCCI_IN010000UK12
nhsAsSvcIA: urn:nhs:names:services:mm:PORX_IN020101UK04
nhsAsSvcIA: urn:nhs:names:services:mm:PORX_IN020102UK04
nhsAsSvcIA: urn:nhs:names:services:mm:PORX_IN250101UK04
nhsAsSvcIA: urn:nhs:names:services:mm:MCCI_IN010000UK13
nhsAsSvcIA: urn:nhs:names:services:mm:PORX_IN020101UK31
nhsAsSvcIA: urn:nhs:names:services:mm:PORX_IN020102UK31
nhsAsSvcIA: urn:nhs:names:services:mm:PORX_IN030101UK32
nhsAsSvcIA: urn:nhs:names:services:mm:PORX_IN050101UK31
nhsAsSvcIA: urn:nhs:names:services:mm:PORX_IN050102UK32
nhsAsSvcIA: urn:nhs:names:services:psis:MCCI_IN010000UK13
nhsAsSvcIA: urn:nhs:names:services:psis:REPC_IN150015UK05
nhsAsSvcIA: urn:nhs:names:services:psis:REPC_IN150016UK05
nhsAsSvcIA: urn:nhs:names:services:psis:REPC_IN150017UK02
nhsAsSvcIA: urn:nhs:names:services:psis:REPC_IN150018UK02
nhsAsSvcIA: urn:nhs:names:services:psisquery:MCCI_IN010000UK13
nhsAsSvcIA: urn:nhs:names:services:psisquery:QUPC_IN180000UK04
nhsAsSvcIA: urn:nhs:names:services:psisquery:QUPC_IN190000UK04
nhsAsSvcIA: urn:nhs:names:services:psisquery:QUPC_IN200000UK04
nhsAsSvcIA: urn:nhs:names:services:psisquery:QUPC_IN210000UK04
nhsAsSvcIA: urn:nhs:names:services:psisquery:QUQI_IN010000UK14
nhsAsSvcIA: urn:nhs:names:services:gp2gp:MCCI_IN010000UK13
nhsAsSvcIA: urn:nhs:names:services:gp2gp:RCMR_IN010000UK05
nhsAsSvcIA: urn:nhs:names:services:gp2gp:RCMR_IN030000UK06
nhsAsSvcIA: urn:nhs:names:services:gpes:COMT_IN000001GB01
nhsAsSvcIA: urn:nhs:names:services:gpes:COMT_IN000002GB01
nhsAsSvcIA: urn:nhs:names:services:gpes:COMT_IN000003GB01
nhsAsSvcIA: urn:nhs:names:services:gpes:COMT_IN000004GB01
nhsAsSvcIA: urn:nhs:names:services:gpes:MCCI_IN010000UK13
nhsAsSvcIA: urn:nhs:names:services:pat:MCCI_IN010000UK13
nhsAsSvcIA: urn:nhs:names:services:pat:POLB_IN020005UK01
nhsAsSvcIA: urn:nhs:names:services:pat:POLB_IN020006UK01
nhsAsSvcIA: urn:nhs:names:services:pds:MCCI_IN010000UK13
nhsAsSvcIA: urn:nhs:names:services:pds:PRPA_IN040000UK30
nhsAsSvcIA: urn:nhs:names:services:pds:PRPA_IN060000UK30
nhsAsSvcIA: urn:nhs:names:services:pds:PRPA_IN150000UK30
nhsAsSvcIA: urn:nhs:names:services:pds:PRPA_IN160000UK30
nhsAsSvcIA: urn:nhs:names:services:pdsquery:MCCI_IN010000UK13
nhsAsSvcIA: urn:nhs:names:services:pdsquery:QUPA_IN010000UK32
nhsAsSvcIA: urn:nhs:names:services:pdsquery:QUPA_IN020000UK31
nhsAsSvcIA: urn:nhs:names:services:pdsquery:QUPA_IN030000UK32
nhsAsSvcIA: urn:nhs:names:services:pdsquery:QUPA_IN040000UK32
nhsAsSvcIA: urn:nhs:names:services:pdsquery:QUPA_IN050000UK32
nhsAsSvcIA: urn:nhs:names:services:pdsquery:QUQI_IN010000UK14
nhsAsSvcIA: urn:nhs:names:services:pds:PRPA_IN100000UK01
nhsAsSvcIA: urn:nhs:names:services:pds:QUPA_IN060000UK01
nhsAsSvcIA: urn:nhs:names:services:pds:QUPA_IN070000UK02
nhsAsSvcIA: urn:nhs:names:services:pdsquery:QUPA_IN060000UK30
nhsAsSvcIA: urn:nhs:names:services:pdsquery:QUPA_IN070000UK30
nhsAsSvcIA: urn:nhs:names:services:psisquery:QUPC_IN160101UK05
nhsAsSvcIA: urn:nhs:names:services:psisquery:QUPC_IN160102UK05
nhsAsSvcIA: urn:nhs:names:services:psisquery:QUPC_IN160103UK05
nhsAsSvcIA: urn:nhs:names:services:psisquery:QUPC_IN160104UK05
nhsAsSvcIA: urn:nhs:names:services:psisquery:QUPC_IN160107UK05
nhsAsSvcIA: urn:nhs:names:services:psisquery:QUPC_IN160108UK05
nhsAsSvcIA: urn:nhs:names:services:psisquery:QUPC_IN160109UK05
nhsAsSvcIA: urn:nhs:names:services:psisquery:QUPC_IN160110UK05
nhsAsSvcIA: urn:nhs:names:services:alerts:COMT_IN000001UK01
nhsAsSvcIA: urn:nhs:names:services:alerts:MCCI_IN010000UK13
uniqueIdentifier: 200000000149
nhsIDCode: M85019
entryUUID: e1b20fa5-8857-4189-93ab-f2f7552d7060
creatorsName: cn=Directory Manager,cn=Root DNs,cn=config
createTimestamp: 20150302145733Z
etag: 00000000baf46b3e
structuralObjectClass: nhsAs
numSubordinates: 0
hasSubordinates: false
subschemaSubentry: cn=schema
entryDN: uniqueidentifier=200000000149,ou=services,o=nhs

# search result

# numResponses: 2
# numEntries: 1
```
