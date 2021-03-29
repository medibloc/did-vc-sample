package holder;

import org.medibloc.panacea.*;
import org.medibloc.panacea.domain.TxResponse;
import org.medibloc.panacea.encoding.message.BroadcastReq;
import org.medibloc.panacea.encoding.message.MsgCreateDid;
import org.medibloc.panacea.encoding.message.StdFee;
import org.medibloc.panacea.encoding.message.StdTx;
import org.medibloc.panacea.encoding.message.did.Did;
import org.medibloc.panacea.encoding.message.did.DidDocument;
import org.medibloc.panacea.encoding.message.did.DidDocumentWithMeta;
import org.medibloc.panacea.encoding.message.did.DidVerificationMethod;
import org.medibloc.vc.key.Curve;
import org.medibloc.vc.key.KeyDecoder;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;

public class Panacea {
    private static final String PANACEA_ENDPOINT = "https://testnet-api.gopanacea.org";
    private static final String PANACEA_ACCOUNT_ADDR = "panacea12222222222222222222222";
    private static final String PANACEA_MNEMONIC = "MY_MNOMENIC";

    static DidDocument registerDid(DidWallet didWallet) throws NoSuchAlgorithmException, IOException, PanaceaApiException {
        PanaceaApiRestClient panaceaClient = PanaceaApiClientFactory.newInstance().newRestClient(PANACEA_ENDPOINT);

        DidDocument doc = DidDocument.create(didWallet);
        DidVerificationMethod.Id veriMethodId = doc.getVerificationMethods().get(0).getId();

        MsgCreateDid msg = new MsgCreateDid(doc, veriMethodId, didWallet, PANACEA_ACCOUNT_ADDR);

        Wallet wallet = Wallet.createWalletFromMnemonicCode(PANACEA_MNEMONIC, "panacea", 0);
        wallet.ensureWalletIsReady(panaceaClient);

        StdTx tx = new StdTx(msg, new StdFee("umed", "10000", "200000"), "");
        tx.sign(wallet);
        wallet.increaseAccountSequence();

        TxResponse res = panaceaClient.broadcast(new BroadcastReq(tx, "block"));
        assert res.getCode() == 0;

        return doc;
    }

    static ECPublicKey getDidPublicKey(String did, String keyId) throws Exception {
        PanaceaApiRestClient panaceaClient = PanaceaApiClientFactory.newInstance().newRestClient(PANACEA_ENDPOINT);

        DidDocumentWithMeta didDocumentWithMeta = panaceaClient.getDidDocument(new Did(did));
        DidDocument doc = didDocumentWithMeta.getDocument();
        DidVerificationMethod verificationMethod = getDidVerificationMethod(doc, keyId);
        byte[] publicKey = verificationMethod.decodePublicKey();

        return KeyDecoder.ecPublicKey(publicKey, getCurve(verificationMethod));
    }

    private static DidVerificationMethod getDidVerificationMethod(DidDocument doc, String keyId) throws Exception {
        for (DidVerificationMethod method : doc.getVerificationMethods()) {
            if (method.getId().getValue().equals(keyId)) {
                return method;
            }
        }
        throw new Exception("DidVerificationMethod not found");
    }

    private static Curve getCurve(DidVerificationMethod verificationMethod) throws Exception {
        switch (verificationMethod.getType()) {
            case ES256K:
                return Curve.SECP256K1;
            default:
                throw new Exception("Invalid VerificationMethod type");
        }
    }
}
