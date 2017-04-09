package myhexaville.com.androidfirebase;

import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Query;
import com.algolia.search.saas.Query.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import myhexaville.com.androidfirebase.databinding.ActivityMainBinding;
import myhexaville.com.androidfirebase.retrofit.FirebaseApi;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.widget.Toast.LENGTH_SHORT;
import static myhexaville.com.androidfirebase.Constants.BOSTON_MA;
import static myhexaville.com.androidfirebase.Constants.FORT_LAUDERDALE_FL;
import static myhexaville.com.androidfirebase.Constants.NEW_YORK;
import static myhexaville.com.androidfirebase.Util.parseJsonList;


public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";
    public static final LatLng CURRENT_LOCATION = FORT_LAUDERDALE_FL;
    private ActivityMainBinding binding;

    private DatabaseReference database;
    private Client client = new Client("", "");
    private List<User> users = new ArrayList<>();

    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        FirebaseMessaging.getInstance().subscribeToTopic("all");

        setupToolbar();

        setupList();

        database = FirebaseDatabase.getInstance().getReference();

        fetchUsers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_user_new_york:
                createUser(Constants.NEW_YORK);
                return true;
            case R.id.create_user_boston:
                createUser(Constants.BOSTON_MA);
                return true;
            case R.id.create_user_fort_lauderdale:
                createUser(Constants.FORT_LAUDERDALE_FL);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);

        if (CURRENT_LOCATION == FORT_LAUDERDALE_FL) {
            getSupportActionBar().setTitle("Fort Lauderdale, FL");
        } else if (CURRENT_LOCATION == BOSTON_MA) {
            getSupportActionBar().setTitle("Boston, MA");
        } else if (CURRENT_LOCATION == NEW_YORK) {
            getSupportActionBar().setTitle("New York");
        }
    }

    private void fetchUsers() {
        Query query = new Query().setAroundLatLng(CURRENT_LOCATION)
                .setAroundRadius(900000000);

        client.getIndex("users").searchAsync(query, (jsonObject, e) -> {
            parseJsonList(jsonObject, users, User.class);
            adapter.notifyDataSetChanged();
        });
    }

    private void setupList() {
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter(this, users);
        binding.list.setAdapter(adapter);
    }

    /*
    * Cloud Functions Trigger. Geofire populated using Cloud Functions Trigger on backend side
    * */
    private void createUser(LatLng location) {
        DatabaseReference user = database.child("users").push();
        User u = User.Companion.randomUser(location);
        user.setValue(u);

        saveToAlgolia(user, u);
    }

    private void saveToAlgolia(DatabaseReference user, User u) {
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(u);
        try {
            JSONObject j = new JSONObject(json);
            j.put("_geoloc", new JSONObject().put("lat", u.getLatitude()).put("lng", u.getLongitude()));
            client.getIndex("users")
                    .saveObjectAsync(j, user.getKey(), (jsonObject, e) -> {
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //todo Here's two more different ways of saving data

//     /**
//     * Cloud Functions API. Same for Geofire, but with no Android Firebase SDK usage
//       More abstraction = better
//     */
//    private void createUser(GeoLocation location) {
//        User user = User.Companion.randomUser(location);
//        FirebaseApi.getInstance().addUser(user).enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                if (response.isSuccessful()) {
//                    Log.d(LOG_TAG, "onResponse: Success");
//                } else {
//                    Log.d(LOG_TAG, "onResponse: Unsuccessful");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Log.e(LOG_TAG, "onFailure: ", t);
//            }
//        });
//    }

//    /**
//     * Android Firebase SDK + RxJava. Not new. Pretty bad
//     */
//    private void createUser(GeoLocation location) {
//        Flowable.just(1)
//                .map(ignore -> {
//                    DatabaseReference user = database.child("users").push();
//                    user.setValue(User.Companion.randomUser(location));
//                    return user.getKey();
//                })
//                .flatMap(userId -> Flowable.create(
//                        e -> geofire.setLocation(userId, location,
//                                (key, error) -> {
//                                    e.onNext(key);
//                                    e.onComplete();
//                                }), DROP))
//                .subscribe();
//}
//    }

    public void sendNotification(View view) {
        // todo Or replace "all" with registration token which you save to your backend for each user
        // todo retreived in MyFirebaseInstanceIDService

        Toast.makeText(this, "Sent notification to all users", LENGTH_SHORT).show();
        FirebaseApi.getInstance()
                .sendNotification("all", "This is title", "This is body")
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });

    }
}
