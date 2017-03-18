package myhexaville.com.androidfirebase;

import android.databinding.DataBindingUtil;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Flowable;
import myhexaville.com.androidfirebase.databinding.ActivityMainBinding;

import static android.widget.Toast.LENGTH_SHORT;
import static io.reactivex.BackpressureStrategy.DROP;
import static myhexaville.com.androidfirebase.Constants.BOSTON_MA;
import static myhexaville.com.androidfirebase.Constants.FORT_LAUDERDALE_FL;
import static myhexaville.com.androidfirebase.Constants.NEW_YORK;


public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";
    public static final GeoLocation CURRENT_LOCATION = FORT_LAUDERDALE_FL;
    private ActivityMainBinding mBinding;

    private DatabaseReference mDatabase;
    private GeoFire mGeofire;
    private Set<GeoQuery> mGeoQueries;

    private List<User> mUsers;
    private ValueEventListener mUserValueListener;
    private boolean mFetchedUserIds;
    private Set<String> mUserIdsWithListeners = new HashSet<>();

    private Adapter mAdapter;
    private int mInitialListSize;
    private int mIterationCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setupToolbar();

        mGeoQueries = new HashSet();
        mUsers = new ArrayList<>();

        setupFirebase();

        setupList();

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

    @Override
    protected void onDestroy() {
        removeListeners();
        super.onDestroy();
    }

    private void setupToolbar() {
        setSupportActionBar(mBinding.toolbar);

        if (CURRENT_LOCATION == FORT_LAUDERDALE_FL) {
            getSupportActionBar().setTitle("Fort Lauderdale, FL");
        } else if (CURRENT_LOCATION == BOSTON_MA) {
            getSupportActionBar().setTitle("Boston, MA");
        } else if (CURRENT_LOCATION == NEW_YORK) {
            getSupportActionBar().setTitle("New York");
        }
    }

    private void fetchUsers() {
        List<String> userIds = new ArrayList<>();
        GeoQuery geoQuery = mGeofire.queryAtLocation(CURRENT_LOCATION, 50);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d(LOG_TAG, "onKeyEntered: ");
                if (!mFetchedUserIds) {
                    userIds.add(key);
                } else {
                    addUserListener(key);
                }
            }

            @Override
            public void onKeyExited(String key) {
                Log.d(LOG_TAG, "onKeyExited: ");
                if (mUserIdsWithListeners.contains(key)) {
                    int position = getUserPosition(key);
                    mUsers.remove(position);
                    mAdapter.notifyItemRemoved(position);
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d(LOG_TAG, "onKeyMoved: ");
            }

            @Override
            public void onGeoQueryReady() {
                Log.d(LOG_TAG, "onGeoQueryReady: ");
                mInitialListSize = userIds.size();
                mIterationCount = 0;
                for (String userId : userIds) {
                    addUserListener(userId);
                }
            }

            private void addUserListener(String userId) {
                mDatabase.child("users").child(userId)
                        .addValueEventListener(mUserValueListener);

                mUserIdsWithListeners.add(userId);
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e(LOG_TAG, "onGeoQueryError: ", error.toException());
            }
        });

        mGeoQueries.add(geoQuery);
    }

    private void setupList() {
        mBinding.list.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new Adapter(this, mUsers);
        mBinding.list.setAdapter(mAdapter);
    }

    private void setupFirebase() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mGeofire = new GeoFire(mDatabase.child("geofire"));

        setupListeners();
    }

    private void setupListeners() {
        mUserValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                u.setId(dataSnapshot.getKey());

                if (mUsers.contains(u)) {
                    userUpdated(u);
                } else {
                    newUser(u);
                }
            }

            private void newUser(User u) {
                Log.d(LOG_TAG, "onDataChange: new user");
                mIterationCount++;
                mUsers.add(0, u);
                if (!mFetchedUserIds && mIterationCount == mInitialListSize) {
                    mFetchedUserIds = true;
                    mAdapter.setUsers(mUsers);
                } else {
                    mAdapter.notifyItemInserted(0);
                }
            }

            private void userUpdated(User u) {
                Log.d(LOG_TAG, "onDataChange: update");
                int position = getUserPosition(u.getId());
                mUsers.set(position, u);
                mAdapter.notifyItemChanged(position);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(LOG_TAG, "onCancelled: ", databaseError.toException());
            }
        };
    }

    private void removeListeners() {
        for (GeoQuery geoQuery : mGeoQueries) {
            geoQuery.removeAllListeners();
        }

        for (String userId : mUserIdsWithListeners) {
            mDatabase.child("users").child(userId)
                    .removeEventListener(mUserValueListener);
        }
    }

    /**
     * Saves user to database and his location to geofire using RxJava 2
     */
    private void createUser(GeoLocation location) {
        Flowable.just(1)
                .map(ignore -> {
                    DatabaseReference user = mDatabase.child("users").push();
                    user.setValue(User.Companion.randomUser(location));
                    return user.getKey();
                })
                .flatMap(userId -> Flowable.create(
                        e -> mGeofire.setLocation(userId, location,
                                (key, error) -> {
                                    e.onNext(key);
                                    e.onComplete();
                                }), DROP))
                .subscribe();
    }

    private int getUserPosition(String id) {
        for (int i = 0; i < mUsers.size(); i++) {
            if (mUsers.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public void readRandom(View view) {
        int randomPosition = (int) (Math.random() * mUsers.size());
        User user = mAdapter.getUser(randomPosition);
        Toast.makeText(
                this,
                "Location of " + user.getName() + " is " + user.getLatitude() + " " + user.getLongitude()
                , LENGTH_SHORT).show();
    }
}
