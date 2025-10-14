# prm-deductions-nems-event-processor

This component is a Java service that receives NEMS events when there is a change of GP for a patient in PDS (Personal Demographics Service). It processes each event to filter for relevant use cases and extracts important information from each, and forwards this information to another queue for further use.

