package myhexaville.com.androidfirebase.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ihor on 2017-03-22.
 */

public class FirebaseApi {
    private static FIrebaseService service;

    public static FIrebaseService getInstance() {
        if (service == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(getGsonConverter())
                    .baseUrl("https://us-central1-amber-torch-963.cloudfunctions.net/")
                    .build();
            service = retrofit.create(FIrebaseService.class);
        }

        return  service;
    }

    private static GsonConverterFactory getGsonConverter() {
        Gson gson = new GsonBuilder()
                .create();
        return GsonConverterFactory.create(gson);
    }
}
