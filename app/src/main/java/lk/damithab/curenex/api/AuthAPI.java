package lk.damithab.curenex.api;

import lk.damithab.curenex.dto.LoginRequestDTO;
import lk.damithab.curenex.dto.TokenDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthAPI {
    @POST("auth/refresh")
    Call<TokenDTO> refreshAccessToken(@Body TokenDTO tokenDTO);

    @POST("auth/login")
    Call<TokenDTO> userLogin(@Body LoginRequestDTO requestDTO);
}
