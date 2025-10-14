package uk.nhs.prm.deductions.pdsadaptor.model.pdspatchrequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PdsPatch {
    private String op;
    private String path;
    private PdsPatchValue value;
}
