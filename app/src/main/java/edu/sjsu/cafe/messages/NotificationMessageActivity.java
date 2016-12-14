package edu.sjsu.cafe.messages;

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

public class NotificationMessageActivity extends Activity {

    TextView title, message, date;
    String title_text, message_text, message_date, show_date;
    IconTextView okBtn, deleteBtn;
    boolean trayNotification = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("New Message!");
        setContentView(R.layout.activity_notification_message);

        Intent intent = getIntent();

        try {
            trayNotification = Boolean.parseBoolean(intent.getStringExtra("trayNotification"));
        } catch (Exception ex) {

        }


        title_text = intent.getStringExtra("title");
        message_text = intent.getStringExtra("message");
        message_date = intent.getStringExtra("date");

        SimpleDateFormat sdfActual = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfTransformed = new SimpleDateFormat("dd-MM-yyyy");
        try {
            show_date = sdfTransformed.format(sdfActual.parse(message_date));
        } catch (Exception ex) {

        }

        title = (TextView) findViewById(R.id.message_title);
        message = (TextView) findViewById(R.id.message_content);
        date = (TextView) findViewById(R.id.message_date);

        title.setText(title_text);
        message.setText(message_text);
        date.setText(show_date);

        okBtn = (IconTextView) findViewById(R.id.ok_message);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        deleteBtn = (IconTextView) findViewById(R.id.delete_message);

        if (trayNotification) {
            deleteBtn.setVisibility(View.GONE);
        } else {
            deleteBtn.setVisibility(View.VISIBLE);
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NotificationMessageManager notificationMessageManager = new NotificationMessageManager(getApplicationContext());
                    notificationMessageManager.open();
                    notificationMessageManager.deleteMessage(title_text, message_text, message_date);
                    onBackPressed();
                }
            });
        }

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        if (!trayNotification) {
            intent.putExtra("reload_messages", "true");
        }
        startActivity(intent);
    }
}
