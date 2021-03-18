package issuer;

import org.medibloc.panacea.*;
import org.medibloc.panacea.domain.TxResponse;
import org.medibloc.panacea.encoding.message.BroadcastReq;
import org.medibloc.panacea.encoding.message.MsgCreateDid;
import org.medibloc.panacea.encoding.message.StdFee;
import org.medibloc.panacea.encoding.message.StdTx;
import org.medibloc.panacea.encoding.message.did.DidDocument;
import org.medibloc.panacea.encoding.message.did.DidSignable;
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
    private static final String PANACEA_ENDPOINT = "https://testnet-api.gopanacea.org";
    private static final String PANACEA_ACCOUNT_ADDR = "panacea111111111111111111111";
    private static final String PANACEA_MNEMONIC = "MY_MNOMENIC";

    private final DidWallet didWallet;
    private final DidDocument didDocument;

    public Issuer() throws PanaceaApiException, NoSuchAlgorithmException, IOException {
        this.didWallet = DidWallet.createRandomWallet();
        this.didDocument = registerDid(this.didWallet);
    }

    public DidWallet getDidWallet() {
        return didWallet;
    }

    public DidDocument getDidDocument() {
        return didDocument;
    }

    private static DidDocument registerDid(DidWallet didWallet) throws NoSuchAlgorithmException, IOException, PanaceaApiException {
        PanaceaApiRestClient panaceaClient = PanaceaApiClientFactory.newInstance().newRestClient(PANACEA_ENDPOINT);

        DidDocument doc = DidDocument.create(didWallet);
        doc.getVerificationMethods().get(0).getId();

        MsgCreateDid msg = new MsgCreateDid(doc.getId(), doc, PANACEA_ACCOUNT_ADDR);
        msg.sign(doc.getVerificationMethods().get(0).getId(), didWallet, DidSignable.INITIAL_SEQUENCE);

        Wallet wallet = Wallet.createWalletFromMnemonicCode(PANACEA_MNEMONIC, "panacea", 0);
        wallet.ensureWalletIsReady(panaceaClient);

        StdTx tx = new StdTx(msg, new StdFee("umed", "10000", "200000"), "");
        tx.sign(wallet);
        wallet.increaseAccountSequence();

        TxResponse res = panaceaClient.broadcast(new BroadcastReq(tx, "block"));
        assert res.getCode() == 0;

        return doc;
    }

    public VerifiableCredential issueVc(String issuerDid, String userDid, ECPrivateKey privateKey, String keyId) throws VerifiableCredentialException, MalformedURLException {
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
                privateKey
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
