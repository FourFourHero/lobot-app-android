package agi.hackday.lobot;

import android.net.Uri;

/**
 * Created by ltrempe on 2/25/16.
 */
public class UrlConfig {

    public static final String HOST =  "http://cloudcityadmin.io";

    public static final String REGISTRATION = HOST + "/lobot/register";

    public static final String BADGER = HOST + "/badger";

    public static final Uri getRegistrationUrl(String token) {
        return Uri.parse(REGISTRATION).buildUpon()
                .appendQueryParameter("os", "g")
                .appendQueryParameter("username", "ltrempe")
                .appendQueryParameter("registration_id", token)
                .build();
    }

    public static final Uri getClearBadgesUrl() {
        return Uri.parse(BADGER).buildUpon()
                .appendPath("clear")
                .appendQueryParameter("username", "ltrempe")
                .appendQueryParameter("badge_type", "1")
                .build();
    }
}
