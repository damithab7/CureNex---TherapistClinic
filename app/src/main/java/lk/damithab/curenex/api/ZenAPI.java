package lk.damithab.curenex.api;

import java.util.List;

import lk.damithab.curenex.dto.QuoteDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;

public interface ZenAPI {
    @GET("https://zenquotes.io/api/today")
    Call<List<QuoteDTO>> getQuote();
}
