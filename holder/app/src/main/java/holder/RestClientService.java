package holder;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RestClientService {
    @GET("vc/did/{did}")
    Call<String> issueVc(@Path("did") String did);
}
