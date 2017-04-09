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

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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


public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";
    public static final GeoLocation CURRENT_LOCATION = FORT_LAUDERDALE_FL;
    private ActivityMainBinding binding;

    private DatabaseReference database;
    private GeoFire geofire;
    private Set<GeoQuery> geoQueries = new HashSet<>();

    private List<User> users = new ArrayList<>();
    private ValueEventListener userValueListener;
    private boolean fetchedUserIds;
    private Set<String> userIdsWithListeners = new HashSet<>();

    private Adapter adapter;
    private int initialListSize;
    private int iterationCount;
    private Location from;
    private Map<String, Location> userIdsToLocations = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        FirebaseMessaging.getInstance().subscribeToTopic("all");

        setupToolbar();

        setupFirebase();

        setupList();

        fetchUsers();

        from = new Location("from");
        from.setLatitude(CURRENT_LOCATION.latitude);
        from.setLongitude(CURRENT_LOCATION.longitude);

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

    @Override
    protected void onDestroy() {
        removeListeners();
        super.onDestroy();
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
        GeoQuery geoQuery = geofire.queryAtLocation(CURRENT_LOCATION, 50000000);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Location to = new Location("to");
                to.setLatitude(location.latitude);
                to.setLongitude(location.longitude);
                if (!fetchedUserIds) {
                    userIdsToLocations.put(key, to);
                } else {
                    addUserListener(key);
                }
            }

            @Override
            public void onKeyExited(String key) {
                Log.d(LOG_TAG, "onKeyExited: ");
                if (userIdsWithListeners.contains(key)) {
                    int position = getUserPosition(key);
                    users.remove(position);
                    adapter.notifyItemRemoved(position);
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d(LOG_TAG, "onKeyMoved: ");
            }

            @Override
            public void onGeoQueryReady() {
                Log.d(LOG_TAG, "onGeoQueryReady: ");
                initialListSize = userIdsToLocations.size();
                iterationCount = 0;

                userIdsToLocations.keySet().forEach(this::addUserListener);
            }

            private void addUserListener(String userId) {
                database.child("users").child(userId)
                        .addValueEventListener(userValueListener);

                userIdsWithListeners.add(userId);
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e(LOG_TAG, "onGeoQueryError: ", error.toException());
            }
        });

        geoQueries.add(geoQuery);
    }

    private void setupList() {
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter(this, users);
        binding.list.setAdapter(adapter);
    }

    private void setupFirebase() {
        database = FirebaseDatabase.getInstance().getReference();
        geofire = new GeoFire(database.child("geofire"));

        setupListeners();
    }

    private void setupListeners() {
        userValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                u.setId(dataSnapshot.getKey());
                Location location = userIdsToLocations.get(dataSnapshot.getKey());
                u.setLatitude(location.getLatitude());
                u.setLongitude(location.getLongitude());

                if (users.contains(u)) {
                    userUpdated(u);
                } else {
                    newUser(u);
                }
            }

            private void newUser(User u) {
                Log.d(LOG_TAG, "onDataChange: new user");
                iterationCount++;
                users.add(0, u);
                if (!fetchedUserIds && iterationCount == initialListSize) {
                    fetchedUserIds = true;

                    sortByDistanceFromMe();

                    adapter.setUsers(users);
                } else {
//                    adapter.notifyItemInserted(0);
                    sortByDistanceFromMe();
                    adapter.notifyDataSetChanged();
                }
            }

            private void userUpdated(User u) {
                Log.d(LOG_TAG, "onDataChange: update");
                int position = getUserPosition(u.getId());
                users.set(position, u);
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(LOG_TAG, "onCancelled: ", databaseError.toException());
            }
        };
    }

    private void sortByDistanceFromMe() {
        Collections.sort(users, (u1, u2) -> {
            Location first = new Location("");
            first.setLatitude(u1.getLatitude());
            first.setLongitude(u1.getLongitude());

            Location second = new Location("");
            second.setLatitude(u2.getLatitude());
            second.setLongitude(u2.getLongitude());

            if (from.distanceTo(first) > from.distanceTo(second)) {
                return 1;
            } else if (from.distanceTo(first) < from.distanceTo(second)) {
                return -1;
            } else {
                return 0;
            }
        });

        for (User user : users) {
            Location location = new Location("");
            location.setLatitude(user.getLatitude());
            location.setLongitude(user.getLongitude());

            Log.d(LOG_TAG, "newUser: distance "+ from.distanceTo(location));
        }
    }

    private void removeListeners() {
        for (GeoQuery geoQuery : geoQueries) {
            geoQuery.removeAllListeners();
        }

        for (String userId : userIdsWithListeners) {
            database.child("users").child(userId)
                    .removeEventListener(userValueListener);
        }
    }

    /*
    * Cloud Functions Trigger. Geofire populated using Cloud Functions Trigger on backend side
    * */
    private void createUser(GeoLocation location) {
        DatabaseReference user = database.child("users").push();
        user.setValue(User.Companion.randomUser(location));
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

    private int getUserPosition(String id) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

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
