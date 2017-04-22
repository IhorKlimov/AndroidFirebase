package myhexaville.com.androidfirebase;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;


public class MyJobService extends JobService {
    private static final String LOG_TAG = "MyJobService";
    @Override
    public boolean onStartJob(JobParameters job) {
        Log.d(LOG_TAG, "onStartJob: ");
        // do some work and reschedule this job for next Monday
//        scheduleJob(getApplicationContext());

        return false; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Log.d(LOG_TAG, "onStopJob: ");
        return false; // Answers the question: "Should this job be retried?"
    }
}