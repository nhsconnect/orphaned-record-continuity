package uk.nhs.prm.deductions.pdsadaptor.model.pdsresponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdentifierPeriod {
    private LocalDate start;
    private LocalDate end;
}
