package edu.sjsu.cafe.offers;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import edu.sjsu.cafe.R;
import edu.sjsu.cafe.database.DatabaseHelper;


/**
 * A simple {@link Fragment} subclass.
 */
public class OffersFragment extends Fragment {

    private LayoutInflater inflaterObj;
    private ViewGroup containerObj;
    private OfferManager offerManager;
    private ListView listView;
    private SimpleCursorAdapter adapter;


    final String[] from = new String[] {
            DatabaseHelper.ID,
            DatabaseHelper.OFFER_CODE,
            DatabaseHelper.OFFER_DESC,
            DatabaseHelper.OFFER_EXPIRY };

    final int[] to = new int[] {
            R.id.offer_id,
            R.id.offer_code,
            R.id.offer_desc,
            R.id.expiry_date };



    public OffersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflaterObj = inflater;
        containerObj = container;
        View view = inflaterObj.inflate(R.layout.fragment_offers, containerObj, false);

        offerManager = new OfferManager(OffersFragment.this.getContext());
        offerManager.open();
        Cursor cursor = offerManager.getOffers();

        listView = (ListView)view.findViewById(R.id.list_view);
        listView.setEmptyView(view.findViewById(R.id.empty));

        adapter = new SimpleCursorAdapter(OffersFragment.this.getContext(), R.layout.offer_layout, cursor, from, to, 0);
        adapter.notifyDataSetChanged();

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long viewId) {
                TextView titleTextView = (TextView) view.findViewById(R.id.offer_code);
                TextView descTextView = (TextView) view.findViewById(R.id.offer_desc);
                TextView expiryTextView = (TextView) view.findViewById(R.id.expiry_date);

                String title = titleTextView.getText().toString();
                String desc = descTextView.getText().toString();
                String expiryDate = expiryTextView.getText().toString();

                Intent offer_intent = new Intent(OffersFragment.this.getActivity().getApplicationContext(), Offer.class);
                offer_intent.putExtra("offerCode", title);
                offer_intent.putExtra("offerDesc", desc);
                offer_intent.putExtra("expiryDate", expiryDate);
                startActivity(offer_intent);
            }
        });

        return view;
    }
}
