package agi.hackday.lobot;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

/**
 * Created by ltrempe on 2/25/16.
 */
public class CloudCity extends Fragment {

    public static final String TAG = "CloudCity";

    private static final String PREFERENCE_REGISTRATION_STATUS = "preferenceRegistrationStatus";

    public static final String REGIRATION_COMPLETE = "registrationComplete";

    public static final String EXTRA_REGISTRATION_STATUS = "cc_extra_registration_status";

    public enum Status {
        CONNECTED,
        DISCONNECTED
    }

    public enum Message {
        SOS(1, R.drawable.lobot_icon),
        GROWTH(2, R.drawable.lobot_icon),
        HANDS_DOWN(3, R.drawable.lobot_icon),
        HEART_ATTACK(4, R.drawable.lobot_icon),
        ORDER_45(5, R.drawable.lobot_icon);

        public final int imageResId;

        public final int id;

        Message(int id, int imageResId) {
            this.id = id;
            this.imageResId = imageResId;
        }
    }

    public static int imageResourceIdForMessage(int messageId) {
        int id = R.drawable.lobot_icon;
        for (Message message : Message.values()) {
            if (messageId == message.id) {
                id = message.imageResId;
                Log.e(TAG, "MESSAGE MATCHED: " + message);
                break;
            }
        }
        return id;
    }

    private ViewGroup mRootView;

    private TextView mUserNameTextView;

    private ImageView mStatusConnected;

    private ImageView mStatusDisconnected;

    public CloudCity() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_cloud_city, container, false);
        mStatusConnected = (ImageView) mRootView.findViewById(R.id.status_connected);
        mStatusDisconnected = (ImageView) mRootView.findViewById(R.id.status_disconnected);
        mUserNameTextView = (TextView) mRootView.findViewById(R.id.main_user_name);
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (hasNetworkConnection()) {
            clearBadges();
        } else {
            updateStatus(Status.DISCONNECTED);
        }
    }

    public void register(String token) {
        if (!hasNetworkConnection()) {
            updateStatus(Status.DISCONNECTED);
            return;
        }
        if (isRegistered()) {
            Log.i(TAG, "Already registered with cloud.");
            updateStatus(Status.CONNECTED);
        } else {
            final RequestQueueSingleton requestQueue = RequestQueueSingleton.getInstance(getContext());
            final String url = UrlConfig.getRegistrationUrl(token).toString();
            Log.i(TAG, "Requesting " + url);
            final StringRequest stringRequest =
                    new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.e(TAG, "Registration success: " + response);
                            saveRegistrationStatus(Status.CONNECTED);
                            updateStatus(Status.CONNECTED);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "ERROR: registration " + error);
                            saveRegistrationStatus(Status.DISCONNECTED);
                            updateStatus(Status.DISCONNECTED);
                        }
                    });
            requestQueue.addToRequestQueue(stringRequest);
        }
    }

    private void updateStatus(Status status) {
        final Resources resources = getResources();
        mUserNameTextView.setText("Not connected to cloud city");
        if (status == Status.CONNECTED) {
            // lock edit text
            // save user name
            mUserNameTextView.setText("Connected to cloud city");
            mUserNameTextView.setEnabled(false);
            mUserNameTextView.setClickable(false);
            mStatusConnected.setColorFilter(resources.getColor(R.color.green));
            mStatusDisconnected.setColorFilter(resources.getColor(R.color.gray));
        } else {
            mStatusDisconnected.setColorFilter(resources.getColor(R.color.red));
            mStatusConnected.setColorFilter(resources.getColor(R.color.gray));
        }
    }

    public void clearBadges() {
        if (!isRegistered()) {
            return;
        }
        final RequestQueueSingleton requestQueue = RequestQueueSingleton.getInstance(getContext());
        final String url = UrlConfig.getClearBadgesUrl().toString();
        Log.i(TAG, "Requesting " + url);
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "Badges cleared. " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "ERROR: Badges not cleared." + error);
            }
        });
        requestQueue.addToRequestQueue(stringRequest);
    }

    private boolean isRegistered() {
        boolean registered = false;

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        if (sharedPreferences.contains(PREFERENCE_REGISTRATION_STATUS)) {
            // TODO add expiration
            registered = sharedPreferences.getBoolean(PREFERENCE_REGISTRATION_STATUS, false);
        }
        Log.e(TAG, "Registration status " + registered);
        return registered;
    }

    private void saveRegistrationStatus(Status status) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        Log.e(TAG, "Status : " + status);
        if (status == Status.CONNECTED) {
            sharedPreferences.edit().putBoolean(PREFERENCE_REGISTRATION_STATUS, true).commit();
        } else {
            sharedPreferences.edit().remove(PREFERENCE_REGISTRATION_STATUS);
        }
    }

    private boolean hasNetworkConnection() {
        final ConnectivityManager connectivityManager =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && (networkInfo.isConnected());
    }
}
