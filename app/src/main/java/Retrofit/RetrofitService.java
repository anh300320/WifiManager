package Retrofit;

import Objects.Vendor;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RetrofitService {
    @GET("v1/macs/{mac}")
    Call<Vendor> getVendor(@Path("mac") String macAddress);
}
