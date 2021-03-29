package verifier;

import org.medibloc.panacea.PanaceaApiClientFactory;
import org.medibloc.panacea.PanaceaApiRestClient;
import org.medibloc.panacea.encoding.message.did.Did;
import org.medibloc.panacea.encoding.message.did.DidDocument;
import org.medibloc.panacea.encoding.message.did.DidDocumentWithMeta;
import org.medibloc.panacea.encoding.message.did.DidVerificationMethod;
import org.medibloc.vc.key.Curve;
import org.medibloc.vc.key.KeyDecoder;

import java.security.interfaces.ECPublicKey;

public class Panacea {
    private static final String PANACEA_ENDPOINT = "https://testnet-api.gopanacea.org";

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
