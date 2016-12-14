package edu.sjsu.cafe;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Akshay on 12/6/2016.
 */

public class FirebaseTokenService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseIDService";
    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        markTokenReceived();
    }

    private void markTokenReceived() {
        String PREFS_NAME = getResources().getString(R.string.database_file);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        try {
            if (settings != null) {
                if(!settings.contains("fcm_registered")) {
                    settings.edit().putBoolean("fcm_registered", true).commit();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
