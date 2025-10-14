package uk.nhs.prm.deductions.pdsadaptor.client.auth;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.deductions.pdsadaptor.client.exceptions.SignedJwtException;

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class SignedJWTGenerator {
    private final String jwtPrivateKey;
    private final String jwtApiKey;
    private final String accessTokenEndpoint;
    private final String jwtKeyId;
    private final JwtSigner jwtSigner;

    public SignedJWTGenerator(@Value("${jwtPrivateKey}") String jwtPrivateKey,
                              @Value("${jwtApiKey}") String jwtApiKey,
                              @Value("${accessTokenEndpoint}") String accessTokenEndpoint,
                              @Value("${jwtKeyId}") String jwtKeyId,
                              JwtSigner jwtSigner) {

        this.jwtPrivateKey = jwtPrivateKey;
        this.jwtApiKey = jwtApiKey;
        this.accessTokenEndpoint = accessTokenEndpoint;
        this.jwtKeyId = jwtKeyId;
        this.jwtSigner = jwtSigner;
    }

    public String createSignedJWT() {
        PrivateKey rsaJWK = getPrivateKey(jwtPrivateKey);
        JWSSigner signer = new RSASSASigner(rsaJWK);
        JWTClaimsSet claimsSet = getClaimsSet();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS512).keyID(jwtKeyId).type(JOSEObjectType.JWT).build(), claimsSet);
        signJwt(signer, signedJWT);
        return signedJWT.serialize();
    }

    private void signJwt(JWSSigner signer, SignedJWT signedJWT) {
        log.info("Signing JWT Token");
        try {
            jwtSigner.signIt(signer, signedJWT);
        } catch (JOSEException e) {
            throw new SignedJwtException(e);
        }
    }

    private JWTClaimsSet getClaimsSet() {
        return new JWTClaimsSet.Builder()
                .subject(jwtApiKey)
                .issuer(jwtApiKey)
                .jwtID(UUID.randomUUID().toString())
                .audience(accessTokenEndpoint)
                .expirationTime(new Date(new Date().getTime() + 60 * 1000))
                .build();
    }

    private PrivateKey getPrivateKey(String privateKeyString) {
        try {
            log.info("Parsing RSA Private key");
            PEMParser pemParser = new PEMParser(new StringReader(privateKeyString));
            Security.addProvider(new BouncyCastleProvider());
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            Object object = pemParser.readObject();
            KeyPair kp = converter.getKeyPair((PEMKeyPair) object);
            return kp.getPrivate();
        }
        catch (IOException e) {
            throw new PdsAdaptorMisconfigurationException("Private key configuration unusable", e);
        }
    }
}
