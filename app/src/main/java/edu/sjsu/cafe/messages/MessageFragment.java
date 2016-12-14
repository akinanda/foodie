package edu.sjsu.cafe.messages;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.joanzapata.iconify.widget.IconTextView;

import edu.sjsu.cafe.R;
import edu.sjsu.cafe.database.DatabaseHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends Fragment {

    private LayoutInflater inflaterObj;
    private ViewGroup containerObj;
    private NotificationMessageManager notificationMessageManager;
    private ListView listView;
    private SimpleCursorAdapter adapter;
    private Cursor cursor;
    private IconTextView removeAllMessageBtn;
    private LinearLayout clearMessagesParent;

    final String[] from = new String[]{
            DatabaseHelper.ID,
            DatabaseHelper.MESSAGE_TITLE,
            DatabaseHelper.MESSAGE_BODY,
            DatabaseHelper.MESSAGE_DATE};

    final int[] to = new int[]{
            R.id.message_id,
            R.id.message_title,
            R.id.message_body,
            R.id.message_date};

    public MessageFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflaterObj = inflater;
        containerObj = container;
        View view = inflaterObj.inflate(R.layout.fragment_message, containerObj, false);

        notificationMessageManager = new NotificationMessageManager(MessageFragment.this.getContext());
        notificationMessageManager.open();
        cursor = notificationMessageManager.getMessages();

        removeAllMessageBtn = (IconTextView) view.findViewById(R.id.remove_all_messages);
        clearMessagesParent = (LinearLayout) removeAllMessageBtn.getParent();

        if (cursor != null && cursor.getCount() < 1) {
            clearMessagesParent.setVisibility(View.GONE);
        }

        removeAllMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificationMessageManager.deleteAll();
                cursor = notificationMessageManager.getMessages();
                adapter = new SimpleCursorAdapter(MessageFragment.this.getContext(), R.layout.message_layout, cursor, from, to, 0);
                adapter.notifyDataSetChanged();
                listView.setAdapter(adapter);
                clearMessagesParent.setVisibility(View.GONE);
            }
        });


        listView = (ListView) view.findViewById(R.id.list_view);
        listView.setEmptyView(view.findViewById(R.id.empty));

        adapter = new SimpleCursorAdapter(MessageFragment.this.getContext(), R.layout.message_layout, cursor, from, to, 0);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long viewId) {
                TextView titleTextView = (TextView) view.findViewById(R.id.message_title);
                TextView bodyTextView = (TextView) view.findViewById(R.id.message_body);
                TextView dateTextView = (TextView) view.findViewById(R.id.message_date);

                String title = titleTextView.getText().toString();
                String body = bodyTextView.getText().toString();
                String date = dateTextView.getText().toString();

                Intent message_intent = new Intent(MessageFragment.this.getActivity().getApplicationContext(), NotificationMessageActivity.class);
                message_intent.putExtra("message", body);
                message_intent.putExtra("date", date);
                message_intent.putExtra("title", title);
                startActivity(message_intent);
            }
        });

        return view;
    }

}
