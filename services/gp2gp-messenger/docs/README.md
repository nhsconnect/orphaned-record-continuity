# GP2GP Messenger Documentation

1. diagrams - Contains plantuml scripts

## plantuml

You can view and export plantuml diagrams using:
 1. Plugin for IntelliJ
 2. [Plugin for Chrome Browser](https://chrome.google.com/webstore/detail/plantuml-viewer/legbfeljfbjgfifnkmpoajgpgejojooj?hl=en)
 3. [Plant Text Website](https://www.planttext.com/)
 
The NHS stylesheet for plantuml can be found [HERE](https://gist.github.com/fishey2/c9d9d7c7426d3701959789c10e96fdb0)

The following example is how use the stylesheet in plantuml:

```
@startuml

!include https://gist.githubusercontent.com/fishey2/c9d9d7c7426d3701959789c10e96fdb0/raw/2afa46ecf5e126ad563693a8dccfa3e7ee46a50c/nhs_stylesheet.iuml

@enduml
```

# Diagrams

1. GP2GP Process
    1. [PDS Retrieve Patient](./diagrams/plantuml/gp2gp_process/01-pdsquery_retrieve_patient.plantuml)
    1. [PDS Update Patient ODS Code](./diagrams/plantuml/gp2gp_process/02-pds_update_patient_ods_code.iuml)
    1. [GP2GP EHR Request](./diagrams/plantuml/gp2gp_process/03-gp2gp_ehr_request_started.plantuml)
1. MHS Interactions
    1. [Asynchronous Workflow](./diagrams/plantuml/mhs_interactions/01-mhs_interactions_async_workflow.plantuml)
1. Test Interfaces
    1. [Fake MHS Gateway](./diagrams/plantuml/test_interfaces/01-fake_mhs_gateway.plantuml)
        1. [messageHandler(message)](./diagrams/plantuml/test_interfaces/011-message_handler_with_message.plantuml)