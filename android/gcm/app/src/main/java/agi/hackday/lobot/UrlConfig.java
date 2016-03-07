package agi.hackday.lobot;

import android.net.Uri;

/**
 * Created by ltrempe on 2/25/16.
 */
public class UrlConfig {

    public static final String HOST =  "http://cloudcityadmin.io";

    public static final String REGISTRATION = HOST + "/lobot/register";

    public static final String BADGER = HOST + "/badger";

    public static final Uri getRegistrationUrl(CharSequence userName, String token) {
        return Uri.parse(REGISTRATION).buildUpon()
                .appendQueryParameter("os", "g")
                .appendQueryParameter("username", userName.toString())
                .appendQueryParameter("registration_id", token)
                .build();
    }

    public static final Uri getClearBadgesUrl(CharSequence userName) {
        return Uri.parse(BADGER).buildUpon()
                .appendPath("clear")
                .appendQueryParameter("username", userName.toString())
                .appendQueryParameter("badge_type", "1")
                .build();
    }
}
