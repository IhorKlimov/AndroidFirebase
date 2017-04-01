package myhexaville.com.androidfirebase.retrofit;

import myhexaville.com.androidfirebase.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by ihor on 2017-03-22.
 */

public interface FirebaseService {
    @POST("addUser")
    Call<ResponseBody> addUser(@Body User user);

    @POST("sendNotification")
    Call<ResponseBody> sendNotification(
            @Query("to") String to,
            @Query("title") String title,
            @Query("body") String body);
}
