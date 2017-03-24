package myhexaville.com.androidfirebase.retrofit;

import myhexaville.com.androidfirebase.User;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by ihor on 2017-03-22.
 */

public interface FIrebaseService {
    @POST("addUser")
    Call<ResponseBody> addUser(@Body User user);
}
