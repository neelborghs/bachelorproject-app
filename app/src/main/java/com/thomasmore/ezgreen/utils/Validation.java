package com.thomasmore.ezgreen.utils;

import android.text.TextUtils;
import android.util.Patterns;

/**
 * Created by Tomas-Laptop on 24/04/2017.
 */

public class Validation {

    public static boolean validateFields(String name) {
        if(TextUtils.isEmpty(name)) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean validateEmail(String email) {
        if(TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false;
        } else {
            return true;
        }
    }
}
