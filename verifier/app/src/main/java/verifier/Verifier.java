package verifier;

import org.medibloc.vc.verifiable.VerifiableCredential;
import org.medibloc.vc.verifiable.VerifiablePresentation;
import org.medibloc.vc.verifiable.jwt.JwtVerifiablePresentation;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.security.interfaces.ECPublicKey;
import java.util.UUID;

public class Verifier {
    private static final String HOLDER_ENDPOINT = "http://localhost:9999";

    VerifiablePresentation getVpFromHolder() throws Exception {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOLDER_ENDPOINT)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        RestClientService service = retrofit.create(RestClientService.class);

        String verifierDid = "my-identifier";
        String nonce = UUID.randomUUID().toString();

        Call<String> call = service.getVp(verifierDid, nonce);
        Response<String> response = call.execute();
        if (response.code() != 200) {
            throw new Exception("status code: " + response.code());
        }
        String vpJwt = response.body();
        System.out.println(vpJwt);

        VerifiablePresentation vp = new JwtVerifiablePresentation(vpJwt);

        // Check if VP is not forged
        ECPublicKey holderPublicKey = Panacea.getDidPublicKey(vp.getPresentation().getHolder(), vp.getKeyId());
        vp.verify(holderPublicKey, verifierDid, nonce);

        // Check if VCs in the VP are not forged
        for (VerifiableCredential vc : vp.getPresentation().getVerifiableCredentials()) {
            ECPublicKey issuerPublicKey = Panacea.getDidPublicKey(vc.getCredential().getIssuer().getId(), vc.getKeyId());
            vc.verify(issuerPublicKey);
        }

        return vp;
    }

}
