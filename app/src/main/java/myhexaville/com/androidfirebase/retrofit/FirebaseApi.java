package myhexaville.com.androidfirebase.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ihor on 2017-03-22.
 */

public class FirebaseApi {
    private static FirebaseService service;

    // todo replace baseUrl with yours
    public static FirebaseService getInstance() {
        if (service == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(getGsonConverter())
                    .baseUrl("https://us-central1-amber-torch-963.cloudfunctions.net/")
                    .build();
            service = retrofit.create(FirebaseService.class);
        }

        return  service;
    }

    private static GsonConverterFactory getGsonConverter() {
        Gson gson = new GsonBuilder()
                .create();
        return GsonConverterFactory.create(gson);
    }
}
