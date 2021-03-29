package holder;

import org.medibloc.panacea.*;
import org.medibloc.panacea.domain.TxResponse;
import org.medibloc.panacea.encoding.message.BroadcastReq;
import org.medibloc.panacea.encoding.message.MsgCreateDid;
import org.medibloc.panacea.encoding.message.StdFee;
import org.medibloc.panacea.encoding.message.StdTx;
import org.medibloc.panacea.encoding.message.did.DidDocument;
import org.medibloc.panacea.encoding.message.did.DidVerificationMethod;
import org.medibloc.vc.key.Curve;
import org.medibloc.vc.key.KeyDecoder;
import org.medibloc.vc.model.Presentation;
import org.medibloc.vc.verifiable.VerifiableCredential;
import org.medibloc.vc.verifiable.VerifiablePresentation;
import org.medibloc.vc.verifiable.jwt.JwtVerifiableCredential;
import org.medibloc.vc.verifiable.jwt.JwtVerifiablePresentation;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Holder {
    private static final String ISSUER_ENDPOINT = "http://localhost:8888";

    private final DidWallet didWallet;
    private final DidDocument didDocument;
    private final List<VerifiableCredential> vcList = new ArrayList<>();

    public Holder() throws PanaceaApiException, NoSuchAlgorithmException, IOException {
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

    VerifiableCredential getVcFromIssuer() throws Exception {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ISSUER_ENDPOINT)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        RestClientService service = retrofit.create(RestClientService.class);

        Call<String> call = service.issueVc(this.didDocument.getId().getValue());
        Response<String> response = call.execute();
        if (response.code() != 200) {
            throw new Exception("status code: " + response.code());
        }
        String vcJwt = response.body();
        System.out.println(vcJwt);

        VerifiableCredential vc = new JwtVerifiableCredential(vcJwt);

        // Check if VC is not forged
        ECPublicKey publicKey = Panacea.getDidPublicKey(vc.getCredential().getIssuer().getId(), vc.getKeyId());
        vc.verify(publicKey);

        this.vcList.add(vc);

        return vc;
    }


    VerifiablePresentation createVerifiablePresentation() throws Exception {
        // In this example, pick the 1st VC.
        VerifiableCredential vc = this.vcList.get(0);

        Presentation presentation = Presentation.builder()
                .contexts(Arrays.asList("https://www.w3.org/2018/credentials/v1", "https://www.w3.org/2018/credentials/examples/v1"))
                .types(Arrays.asList("VerifiablePresentation", "CredentialManagerPresentation"))
                .id(new URL("http://my-presentation.com/1"))
                .verifiableCredentials(Collections.singletonList(vc))
                .holder(this.didDocument.getId().getValue())
                .build();

        DidVerificationMethod veriMethod = this.didDocument.getVerificationMethods().get(0);

        return new JwtVerifiablePresentation(
                presentation,
                "ES256K",
                veriMethod.getId().getValue(),
                KeyDecoder.ecPrivateKey(this.didWallet.getEcKey().getPrivKey(), getCurve(veriMethod))
        );
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
