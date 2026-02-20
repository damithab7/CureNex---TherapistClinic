package lk.damithab.curenex.client;

import android.content.Context;

import lk.damithab.curenex.interceptor.AuthInterceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;
    public static final String BASE_URL="http://10.77.241.155:8080/api/v1/"; ///emulators -> 10.0.2.2:8080
    /// physical device -> ipv4 address

    public static Retrofit getInstance(Context context){
        if(retrofit ==null){
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))
                    .build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
