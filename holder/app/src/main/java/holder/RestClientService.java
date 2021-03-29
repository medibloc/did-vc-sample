package holder;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RestClientService {
    @GET("vc/did/{did}/nonce/{nonce}")
    Call<String> issueVc(@Path("did") String did, @Path("nonce") String nonce);
}
