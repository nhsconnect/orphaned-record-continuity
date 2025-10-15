# suspension-service

Handles processing of patients and their assignment to a managing organisation (MOF).

Maintained by the Patient Record Migrations (PRM) Repository team

## Instances

There are currently 2 instances of this Java application currently running:

- suspensions service;
- end of transfer service.

These are the main differences between them:

- inbound and outbound queues are different;
- suspension service spins up out of working our, end of transfer service is expected to be running all time.
- when invoking pds adapter, end of transfer service sets MOF to the previous GP.

The difference above are reflected in:

- the infrastructure code: each instance has its specific terraform state;
- a very few differences in the application logic, handled with toggles.