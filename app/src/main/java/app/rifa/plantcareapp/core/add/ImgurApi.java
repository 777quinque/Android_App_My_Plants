package app.rifa.plantcareapp.core.add;

import android.util.Log;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ImgurApi {
    @Multipart
    @POST("image")
    Call<ImgurResponse> uploadImage(
            @Header("Authorization") String authHeader,
            @Part MultipartBody.Part image
    );
}

