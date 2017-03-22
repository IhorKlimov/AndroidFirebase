package myhexaville.com.androidfirebase.retrofit;

import myhexaville.com.androidfirebase.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by ihor on 2017-03-22.
 */

public interface FIrebaseService {
    @POST("addUser")
    Call<ResponseBody> addUser(@Body User user);
}
