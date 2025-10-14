package uk.nhs.prm.deductions.pdsadaptor.client.auth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;

@Component
public class JwtSigner {
    public void signIt(JWSSigner signer, SignedJWT signedJWT) throws JOSEException {
        signedJWT.sign(signer);
    }
}
