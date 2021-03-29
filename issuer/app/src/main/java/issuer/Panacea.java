package issuer;

import org.medibloc.panacea.*;
import org.medibloc.panacea.domain.TxResponse;
import org.medibloc.panacea.encoding.message.BroadcastReq;
import org.medibloc.panacea.encoding.message.MsgCreateDid;
import org.medibloc.panacea.encoding.message.StdFee;
import org.medibloc.panacea.encoding.message.StdTx;
import org.medibloc.panacea.encoding.message.did.DidDocument;
import org.medibloc.panacea.encoding.message.did.DidVerificationMethod;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Panacea {
    private static final String PANACEA_ENDPOINT = "https://testnet-api.gopanacea.org";
    private static final String PANACEA_ACCOUNT_ADDR = "panacea111111111111111111";
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
}
