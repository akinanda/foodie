package edu.sjsu.cafe;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.ArrayMap;
import android.webkit.URLUtil;

import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import edu.sjsu.cafe.messages.NotificationMessageActivity;
import edu.sjsu.cafe.messages.NotificationMessageManager;
import edu.sjsu.cafe.offers.Offer;
import edu.sjsu.cafe.offers.OfferManager;

/**
 * Created by Akshay on 12/6/2016.
 */

public class FirebaseMessageService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        showNotification(remoteMessage);
    }

    private void showNotification(RemoteMessage message) {
        String messageTitle = getString(R.string.app_name);
        String messageBody = "";
        String url = "";
        String offerCode = "";
        String offerExpiry = "";
        String offerDetails = "";

        Map<String, String> dataMap = new ArrayMap<String, String>();

        if (message != null) {
            if (message.getNotification() != null) {
                if (!AppUtility.isNullOrEmpty(message.getNotification().getTitle())) {
                    messageTitle = AppUtility.sanitizeString(AppUtility.escapeHTML(message.getNotification().getTitle().trim()));
                }

                if (!AppUtility.isNullOrEmpty(message.getNotification().getBody())) {
                    messageBody = AppUtility.sanitizeString(AppUtility.escapeHTML(message.getNotification().getBody().trim()));
                }

                if (message.getData() != null && !message.getData().isEmpty()) {
                    dataMap = message.getData();
                    url = dataMap.get("url");
                    offerCode = AppUtility.sanitizeString(AppUtility.escapeHTML(dataMap.get("offer"))).trim().toUpperCase();
                    offerDetails = AppUtility.sanitizeString(AppUtility.escapeHTML(dataMap.get("offerDetails"))).trim();
                    offerExpiry = AppUtility.sanitizeString(AppUtility.escapeHTML(dataMap.get("offerExpiry"))).trim();
                }
            }
        }

        Intent notification_intent = new Intent(this, MainActivity.class);
        notification_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //dummy action so that params are not dropped
        notification_intent.setAction(Long.toString(System.currentTimeMillis()));

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
            notification_intent.putExtra("offerCode", offerCode);
            notification_intent.putExtra("offerDesc", offerDetails);
            notification_intent.putExtra("expiryDate", offerExpiry);
            //startActivity(notification_intent);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String today = sdf.format(new Date());
            NotificationMessageManager notificationMessageManager = new NotificationMessageManager(this);
            notificationMessageManager.open();
            notificationMessageManager.insertMessage(messageTitle, messageBody, today);

            notification_intent = new Intent(this, NotificationMessageActivity.class);
            notification_intent.putExtra("date", today);
            notification_intent.putExtra("title", messageTitle);
            notification_intent.putExtra("message", messageBody);
        }

        int requestCode = new Random().nextInt();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, notification_intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setSmallIcon(R.drawable.logo)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(requestCode, builder.build());

    }
}
