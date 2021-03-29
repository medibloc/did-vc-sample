package verifier;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RestClientService {
    @GET("vp/verifier/{verifier}/nonce/{nonce}")
    Call<String> getVp(@Path("verifier") String verifier, @Path("nonce") String nonce);
}
