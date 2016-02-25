package agi.hackday.lobot;

import android.net.Uri;

/**
 * Created by ltrempe on 2/25/16.
 */
public class UrlConfig {

    public static final String HOST =  "http://cloudcityadmin.io/lobot";

    public static final String REGISTRATION = HOST + "/register";

    public static final String BADGER = HOST + "/badger";

    public static final Uri getRegistrationUrl(String token) {
        return Uri.parse(REGISTRATION).buildUpon()
                .appendQueryParameter("os", "g")
                .appendQueryParameter("username", "ltrempe")
                .appendQueryParameter("registration_id", token)
                .build();
    }
}
