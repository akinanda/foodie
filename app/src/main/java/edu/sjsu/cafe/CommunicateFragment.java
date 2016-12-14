package edu.sjsu.cafe;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.joanzapata.iconify.widget.IconTextView;

import static edu.sjsu.cafe.AppUtility.PERMISSIONS_REQUEST_PHONE_CALL;


/**
 * A simple {@link Fragment} subclass.
 */
public class CommunicateFragment extends Fragment {

    Activity act;
    IconTextView call_button, website_button, share_button;
    private String phoneNumberToCall = "";

    public CommunicateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_communicate, container, false);

        call_button = (IconTextView)view.findViewById(R.id.communicate_call);
        call_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                call(getString(R.string.phone));
            }
        });

        website_button = (IconTextView)view.findViewById(R.id.communicate_website);
        website_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website)));
                startActivity(browserIntent);
            }
        });

        share_button = (IconTextView)view.findViewById(R.id.communicate_share);
        share_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.play_store_url));
                sendIntent.setType("text/plain");
                startActivity(sendIntent);

            }
        });

        return view;
    }



    public void call(String number) {
        phoneNumberToCall = number;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && CommunicateFragment.this.getActivity().checkSelfPermission(android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CALL_PHONE}, AppUtility.PERMISSIONS_REQUEST_PHONE_CALL);
        } else {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            try {
                startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(CommunicateFragment.this.getActivity(),"Please call " + number, Toast.LENGTH_LONG);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_PHONE_CALL) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                call(phoneNumberToCall);
            } else {
                Toast.makeText(CommunicateFragment.this.getActivity(), "Permission to call denied!", Toast.LENGTH_LONG).show();
            }
        }
    }

}
