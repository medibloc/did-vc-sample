package verifier;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RestClientService {
    @GET("vp/nonce/{nonce}")
    Call<String> getVp(@Path("nonce") String nonce);
}
