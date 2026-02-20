package lk.damithab.curenex.interceptor;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;

import lk.damithab.curenex.api.AuthAPI;
import lk.damithab.curenex.client.RetrofitClient;
import lk.damithab.curenex.dto.TokenDTO;
import lk.damithab.curenex.manager.TokenManager;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthInterceptor implements Interceptor {
    private Context context;

    private static final String AUTHORIZATION = "Authorization";
    private static final int UNAUTHORIZED = 401;
    private lk.damithab.curenex.api.AuthAPI authAPI;
    public AuthInterceptor(Context context){
        this.context = context.getApplicationContext();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RetrofitClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        authAPI = retrofit.create(AuthAPI.class);
    }
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String url = originalRequest.url().encodedPath();
        if(url.contains("/auth/login") || url.contains("/auth/refresh")){
            return chain.proceed(originalRequest);
        }

        /// attach request header ===> authorization
        String accessToken = TokenManager.retrieveAccessToken(context);
        Request request = originalRequest.newBuilder()
                .header(AuthInterceptor.AUTHORIZATION, "Bearer " + accessToken)
                .build();
        Response response = chain.proceed(request);
        if(response.code()==AuthInterceptor.UNAUTHORIZED){
                /// refresh logic
                response.close();
                synchronized (this){
                    String newAccess = fetchRefreshToken();
                    if(newAccess != null){
                        Request newRequest = originalRequest.newBuilder()
                                .header(AuthInterceptor.AUTHORIZATION, "Bearer " + newAccess)
                                .build();
                        return chain.proceed(newRequest);
                    }
                }
        }
        return response;
    }

    private String fetchRefreshToken(){
        try {
            /// Retrieving refresh token from the shared preference
            String refreshToken = TokenManager.retrieveRefreshToken(context);
            TokenDTO dto = new TokenDTO();
            dto.setRefreshToken(refreshToken);
            /// make the call event from authAPI
            Call<TokenDTO> tokenDTOCall = authAPI.refreshAccessToken(dto);
            /// synchronized call ===> stop other execution | execute request and get response
            retrofit2.Response<TokenDTO> response = tokenDTOCall.execute();

            if(response.isSuccessful()){
                TokenDTO tokenDTO = response.body();
                if(tokenDTO != null){
                    /// save the retrieved token again in shared preferences
                    TokenManager.saveTokens(context, tokenDTO);
                    /// returning new access token
                    return tokenDTO.getAccessToken();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        TokenManager.clearToken(context);
        return null;
    }
}
