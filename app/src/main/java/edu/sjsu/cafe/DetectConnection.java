package edu.sjsu.cafe;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by Akshay on 12/6/2016.
 */
public class DetectConnection {
    public static boolean isInternetAvailable(Context context) {

        ConnectivityManager con_manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (con_manager.getActiveNetworkInfo() != null
                && con_manager.getActiveNetworkInfo().isAvailable()
                && con_manager.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            return false;
        }
    }
}