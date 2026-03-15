package lk.damithab.curenex.api;

import java.util.List;

import lk.damithab.curenex.dto.QuoteDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;

public interface ZenAPI {
    @GET("today")
    Call<List<QuoteDTO>> getQuote();

    @GET("random")
    Call<List<QuoteDTO>> getRandomQuote();
}
