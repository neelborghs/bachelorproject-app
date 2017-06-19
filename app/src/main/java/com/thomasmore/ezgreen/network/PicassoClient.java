package com.thomasmore.ezgreen.network;

import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.thomasmore.ezgreen.R;

/**
 * Created by Tomas-Laptop on 4/05/2017.
 */

public class PicassoClient {
    public static void downloadProfileImage(Context c, String url, ImageView img) {

        if(url != null && url.length() > 0) {
            Picasso.with(c).load(url).into(img);

        } else {
            Picasso.with(c).load(R.drawable.user).into(img);
        }
    }
}
