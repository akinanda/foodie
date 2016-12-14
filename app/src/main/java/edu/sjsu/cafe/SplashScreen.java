package edu.sjsu.cafe;

/**
 * Created by Akshay on 12/4/2016.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.webkit.URLUtil;
import android.widget.LinearLayout;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import edu.sjsu.cafe.messages.NotificationMessageActivity;
import edu.sjsu.cafe.messages.NotificationMessageManager;
import edu.sjsu.cafe.offers.Offer;
import edu.sjsu.cafe.offers.OfferManager;

public class SplashScreen extends Activity {
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    /**
     * Called when the activity is first created.
     */
    Thread splashTread;
    SharedPreferences settings;
    Boolean fcm_registered = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // Instantiate Iconify for the 1st time
        Iconify.with(new FontAwesomeModule());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spashscreen);

        registerWithFCM();
        handleSysemTrayNotifications();
    }

    private void registerWithFCM() {
        //Subscribe to be notified
        //Firebase.setAndroidContext(getApplicationContext());
        String PREFS_NAME = getResources().getString(R.string.database_file);
        settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        try {
            if (settings != null) {
                fcm_registered = settings.contains("fcm_registered");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!fcm_registered) {
            settings.edit().putBoolean("fcm_registered", true).commit();
            FirebaseMessaging.getInstance().subscribeToTopic("cafe");
            FirebaseInstanceId.getInstance().getToken();
        }
    }


    private void handleSysemTrayNotifications() {
        Intent intent = getIntent();
        if (intent != null) {
            Bundle intentData = intent.getExtras();
            if (intentData != null) {
                String messageTitle = AppUtility.sanitizeString(AppUtility.escapeHTML(intentData.getString("title"))).trim();
                String messageBody = AppUtility.sanitizeString(AppUtility.escapeHTML(intentData.getString("message"))).trim();
                String url = intentData.getString("url");
                String offerCode = AppUtility.sanitizeString(AppUtility.escapeHTML(intentData.getString("offer"))).trim().toUpperCase();
                String offerDetails = AppUtility.sanitizeString(AppUtility.escapeHTML(intentData.getString("offerDetails"))).trim();
                String offerExpiry = AppUtility.sanitizeString(AppUtility.escapeHTML(intentData.getString("offerExpiry"))).trim();

                if (!AppUtility.isNullOrEmpty(messageTitle)
                        || !AppUtility.isNullOrEmpty(messageBody)
                        || !AppUtility.isNullOrEmpty(url)
                        || !AppUtility.isNullOrEmpty(offerCode)) {

                    Intent notification_intent = new Intent(this, MainActivity.class);
                    notification_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    if (!AppUtility.isNullOrEmpty(url) && URLUtil.isValidUrl(url.trim())) {
                        notification_intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.trim()));
                    } else if (!AppUtility.isNullOrEmpty(offerCode)) {
                        offerCode = offerCode.toUpperCase();
                        if (AppUtility.isNullOrEmpty(offerDetails)) {
                            offerDetails = getString(R.string.offer_validity);
                        }

                        if (AppUtility.isNullOrEmpty(offerExpiry)) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            offerExpiry = sdf.format(new Date());
                            Calendar c = Calendar.getInstance();
                            try {
                                c.setTime(sdf.parse(offerExpiry));
                            } catch (Exception ex) {
                                // date parse exception
                                ex.printStackTrace();
                            }
                            try {
                                int offerLength = Integer.parseInt(getString(R.string.default_offer_validity_days));
                                c.add(Calendar.DATE, offerLength); // if no expiry date set, then default offer validity
                            } catch (Exception ex) {
                                c.add(Calendar.DATE, 1);  // if no expiry date set, then offer valid for 1 day
                            }

                            offerExpiry = sdf.format(c.getTime());
                        }

                        OfferManager offerManager = new OfferManager(this);
                        offerManager.open();
                        offerManager.insertOffer(offerCode, offerDetails, offerExpiry);
                        notification_intent = new Intent(this, Offer.class);
                        notification_intent.putExtra("trayNotification", "true");
                        notification_intent.putExtra("offerCode", offerCode);
                        notification_intent.putExtra("offerDesc", offerDetails);
                        notification_intent.putExtra("expiryDate", offerExpiry);
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String today = sdf.format(new Date());
                        NotificationMessageManager notificationMessageManager = new NotificationMessageManager(this);
                        notificationMessageManager.open();
                        notificationMessageManager.insertMessage(messageTitle, messageBody, today);

                        notification_intent = new Intent(this, NotificationMessageActivity.class);
                        notification_intent.putExtra("trayNotification", "true");
                        notification_intent.putExtra("title", messageTitle);
                        notification_intent.putExtra("message", messageBody);
                    }

                    startActivity(notification_intent);
                } else {
                    startAnimations();
                }
            } else {
                startAnimations();
            }
        } else {
            startAnimations();
        }
    }

    private void startAnimations() {

        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.splash_background_veil);

        AlphaAnimation animation = new AlphaAnimation(1.0F, 0.0F);
        animation.setInterpolator(new AccelerateInterpolator());
        animation.setDuration(2500);
        animation.setStartOffset(100);
        animation.setFillAfter(true);
        animation.setRepeatCount(0);
        linearLayout.startAnimation(animation);

        splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    // Splash screen pause time
                    while (waited < 5000) {
                        sleep(100);
                        waited += 100;
                    }
                    Intent intent = new Intent(SplashScreen.this,
                            MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    SplashScreen.this.finish();
                } catch (InterruptedException e) {
                    // do nothing
                } finally {
                    SplashScreen.this.finish();
                }

            }
        };
        splashTread.start();

    }
}
