package uk.nhs.prm.repo.re_registration.ehr_repo;

public class EhrRepoServerException extends RuntimeException {
    public EhrRepoServerException(String message, Throwable ex) {
        super(message, ex);
    }
}