package edu.sjsu.cafe;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.joanzapata.iconify.widget.IconTextView;

import static edu.sjsu.cafe.AppUtility.PERMISSIONS_REQUEST_PHONE_CALL;


/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {


    public AboutFragment() {
        // Required empty public constructor
    }

    IconTextView callBtn;
    //TextView callBtn;
    Activity act;
    private String phoneNumberToCall = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        FragmentManager manager = this.getActivity().getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.fragment_communicate, new CommunicateFragment()).commit();


        callBtn = (IconTextView) view.findViewById(R.id.about_call_button);
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                call(getString(R.string.phone));

            }
        });
        return view;
    }

    public void call(String number) {
        phoneNumberToCall = number;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && AboutFragment.this.getActivity().checkSelfPermission(android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CALL_PHONE}, AppUtility.PERMISSIONS_REQUEST_PHONE_CALL);
        } else {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            try {
                startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(AboutFragment.this.getActivity(),"Please call " + number, Toast.LENGTH_LONG);
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
                Toast.makeText(AboutFragment.this.getActivity(), "Permission to call denied!", Toast.LENGTH_LONG).show();
            }
        }
    }

}
