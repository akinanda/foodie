package edu.sjsu.cafe.offers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.joanzapata.iconify.widget.IconTextView;

import java.text.SimpleDateFormat;

import edu.sjsu.cafe.MainActivity;
import edu.sjsu.cafe.R;

/**
 * Created by Akshay on 12/8/2016.
 */

public class Offer extends Activity {

    private TextView offerCode, offerDesc, expiryDate;
    private String offer_code, offer_desc, offer_expiry;
    private IconTextView removeOfferBtn, okOfferBtn;
    boolean trayNotification = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("New Offer!");
        setContentView(R.layout.activity_offer);

        Intent intent = getIntent();
        offer_code = intent.getStringExtra("offerCode");
        offer_desc = intent.getStringExtra("offerDesc");
        offer_expiry = intent.getStringExtra("expiryDate");


        SimpleDateFormat sdfActual = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfTransformed = new SimpleDateFormat("dd-MM-yyyy");
        try {
            offer_expiry = sdfTransformed.format(sdfActual.parse(offer_expiry));
        } catch (Exception ex) {

        }

        try {
            trayNotification = Boolean.parseBoolean(intent.getStringExtra("trayNotification"));
        } catch (Exception ex) {

        }

        offerCode = (TextView) findViewById(R.id.offer_code);
        offerDesc = (TextView) findViewById(R.id.offer_desc);
        expiryDate = (TextView) findViewById(R.id.offer_expiry);

        offerCode.setText(offer_code);
        offerDesc.setText(offer_desc);
        expiryDate.setText(offer_expiry);

        removeOfferBtn = (IconTextView) findViewById(R.id.delete_offer);
        removeOfferBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OfferManager offerManager = new OfferManager(getApplicationContext());
                offerManager.open();
                offerManager.deleteOffer(offer_code);
                onBackPressed();
            }
        });

        okOfferBtn = (IconTextView) findViewById(R.id.ok_offer);
        okOfferBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        if (!trayNotification) {
            intent.putExtra("reload_offer", "true");
        }
        startActivity(intent);
    }
}
