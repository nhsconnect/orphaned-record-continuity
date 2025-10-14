package uk.nhs.prm.repo.suspension.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateManagingOrganisationRequest {
    private String previousGp;
    private String recordETag;
}
