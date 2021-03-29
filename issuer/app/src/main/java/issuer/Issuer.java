package issuer;

import org.medibloc.panacea.DidWallet;
import org.medibloc.panacea.PanaceaApiException;
import org.medibloc.panacea.encoding.message.did.DidDocument;
import org.medibloc.vc.VerifiableCredentialException;
import org.medibloc.vc.model.Credential;
import org.medibloc.vc.model.CredentialSubject;
import org.medibloc.vc.verifiable.VerifiableCredential;
import org.medibloc.vc.verifiable.jwt.JwtVerifiableCredential;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class Issuer {
    private final DidWallet didWallet;
    private final DidDocument didDocument;

    public Issuer() throws PanaceaApiException, NoSuchAlgorithmException, IOException {
        this.didWallet = DidWallet.createRandomWallet();
        this.didDocument = Panacea.registerDid(this.didWallet);
        System.out.printf("DID created: %s\n", this.didDocument.getId());
    }

    public DidWallet getDidWallet() {
        return didWallet;
    }

    public DidDocument getDidDocument() {
        return didDocument;
    }

    public VerifiableCredential issueVc(String issuerDid, String userDid, ECPrivateKey privateKey, String keyId, String nonce) throws VerifiableCredentialException, MalformedURLException {
        Credential credential = Credential.builder()
                .contexts(Arrays.asList("https://www.w3.org/2018/credentials/v1"))
                .types(Arrays.asList("VerifiableCredential", "VaccineCredential"))
                .id(new URL("https://my-credential.com/1"))
                .issuer(new org.medibloc.vc.model.Issuer(issuerDid))
                .issuanceDate(new Date())
                .credentialSubject(issueCredentialSubject(userDid))
                .build();

        return new JwtVerifiableCredential(
                credential,
                "ES256K",
                keyId,
                privateKey,
                nonce
        );
    }

    private static CredentialSubject issueCredentialSubject(String userDid) {
        CredentialSubject subject = new CredentialSubject(userDid);
        subject.addClaim("hospital", "병원1");
        subject.addClaim("vaccine", new HashMap<String, Object>() {{
            put("name", "COVID-19 Vaccine");
            put("brand", "ZstraAeneca");
        }});
        return subject;
    }
}
