package com.thomasmore.ezgreen.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.thomasmore.ezgreen.R;

public class AboutFragment extends Fragment {

    public static final String TAG = AboutFragment.class.getSimpleName();

    private ImageView ivWebTomas, ivInTomas, ivMailTomas;
    private ImageView ivWebNeel, ivInNeel, ivMailNeel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        container.removeAllViews();
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        initViews(view);

        return view;
    }

    private void initViews(View v) {
        ivWebTomas = (ImageView) v.findViewById(R.id.web_tomas);
        ivInTomas = (ImageView) v.findViewById(R.id.in_tomas);
        ivMailTomas = (ImageView) v.findViewById(R.id.mail_tomas);

        ivWebNeel = (ImageView) v.findViewById(R.id.web_neel);
        ivInNeel = (ImageView) v.findViewById(R.id.in_neel);
        ivMailNeel = (ImageView) v.findViewById(R.id.mail_neel);

        ivWebTomas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToUrl(Uri.parse("http://www.tomasmilio.com"));
            }
        });

        ivInTomas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToUrl(Uri.parse("http://www.linkedin.com/in/tomasmilio"));
            }
        });

        ivMailTomas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail("contact@tomasmilio.com", "Hey Tomas,");
            }
        });

        ivWebNeel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToUrl(Uri.parse("http://www.neelborghs.com"));
            }
        });

        ivInNeel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToUrl(Uri.parse("http://www.linkedin.com/in/neelborghs/"));
            }
        });

        ivMailNeel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail("info@neelborghs.com", "Hey Neel,");
            }
        });
    }

    private void goToUrl(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void sendEmail(String email, String body) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{email});
        i.putExtra(Intent.EXTRA_SUBJECT, "Contact from EZgreen application");
        i.putExtra(Intent.EXTRA_TEXT   , body);
        try {
            startActivity(Intent.createChooser(i, "Send email"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
