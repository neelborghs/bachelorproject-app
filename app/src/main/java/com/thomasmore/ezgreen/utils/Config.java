package com.thomasmore.ezgreen.utils;

import java.util.HashMap;

/**
 * Created by Tomas-Laptop on 4/05/2017.
 */

public class Config {

    public static HashMap getConfig() {
        HashMap config = new HashMap();
        config.put("cloud_name", ""); //fill in your own credentials
        config.put("api_key", "");
        config.put("api_secret", "");

        return config;
    }
}
