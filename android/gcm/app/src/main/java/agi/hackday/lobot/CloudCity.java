package agi.hackday.lobot;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

/**
 * Created by ltrempe on 2/25/16.
 */
public class CloudCity extends Fragment {

    public static final String TAG = "CloudCity";

    public static final String IN_APP_MESSAGE = "CloudCity.IN_APP_MESSAGE";

    public static final String EXTRA_MESSAGE = "CloudCity.MESSAGE";

    public static final String EXTRA_ID = "CloudCity.ID";

    private static final String PREFERENCE_REGISTRATION_STATUS = "preferenceRegistrationStatus";

    private static final String PREFERENCE_USER_NAME = "preferenceUserName";

    private BroadcastReceiver mInAppMessageReceiver;

    public enum Status {
        CONNECTED,
        DISCONNECTED,
        IN_PROGRESS
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

    private ImageView mStatusInProgress;

    private TextView mInformationTextView;

    private View mRegistrationForm;

    private ProgressBar mRegistrationProgressBar;

    private String mToken = "";

    public CloudCity() {
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_cloud_city, container, false);
        mUserNameTextView = (TextView) mRootView.findViewById(R.id.main_user_name);
        mInformationTextView = (TextView) mRootView.findViewById(R.id.informationTextView);
        mRegistrationProgressBar = (ProgressBar) mRootView.findViewById(R.id.registrationProgressBar);
        mRegistrationForm = mRootView.findViewById(R.id.cloud_city_registration_form);

        mInAppMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra(EXTRA_MESSAGE);
                int id = intent.getIntExtra(EXTRA_ID, 0);
                showInAppMessage(message, id);
            }
        };
        mUserNameTextView.setText(getUserName());

        mRootView.findViewById(R.id.submit).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasNetworkConnection()) {
                    register(mToken);
                } else {
                    showNoNetworkAlert();
                }
            }
        });
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        final ViewGroup actionBarView = (ViewGroup) inflater.inflate(R.layout.action_bar, null);

        mStatusConnected = (ImageView) actionBarView.findViewById(R.id.status_connected);
        mStatusDisconnected = (ImageView) actionBarView.findViewById(R.id.status_disconnected);
        mStatusInProgress = (ImageView) actionBarView.findViewById(R.id.status_in_progress);
        actionBar.setCustomView(actionBarView);

        updateStatus(Status.IN_PROGRESS);

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity())
                             .registerReceiver(mInAppMessageReceiver, new IntentFilter(IN_APP_MESSAGE));
        if (hasNetworkConnection()) {
            clearBadges();
        } else {
            updateStatus(Status.DISCONNECTED);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mInAppMessageReceiver);
    }

    private void registerWithAlertDialog() {
        if (hasNetworkConnection()) {
            register(mToken);
        } else {
            showNoNetworkAlert();
        }
    }

    public void register(String token) {
        mToken = token;
        updateStatus(Status.IN_PROGRESS);

        if (!hasNetworkConnection()) {
            Log.i("TAG", "No network connection");
            updateStatus(Status.DISCONNECTED);
            return;
        }
        mRegistrationProgressBar.setVisibility(ProgressBar.VISIBLE);
        if (isRegistered()) {
            Log.i(TAG, "Already registered with cloud.");
            updateStatus(Status.CONNECTED);
        } else {
            final CharSequence userName = mUserNameTextView.getText();
            final String url = UrlConfig.getRegistrationUrl(userName, token).toString();
            Log.i(TAG, "Requesting " + url);
            final StringRequest stringRequest =
                    new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.e(TAG, "Registration success: " + response);
                            saveRegistrationStatus(Status.CONNECTED, userName);
                            updateStatus(Status.CONNECTED);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "ERROR: registration " + error);
                            saveRegistrationStatus(Status.DISCONNECTED, null);
                            updateStatus(Status.DISCONNECTED);
                        }
                    });
            makeRequest(stringRequest);
        }
    }

    private void updateStatus(Status status) {
        final Resources resources = getResources();
        mUserNameTextView.setText(getUserName());
        mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
        Log.i(TAG, "Updating status " + status);
        if (status == Status.CONNECTED) {
            showConnectionState(resources);
        } else if (status == Status.IN_PROGRESS) {
            showInProgressState(resources);
        } else {
            showDisconnectedState(resources);
        }
    }

    private void showInProgressState(Resources resources) {
        mStatusConnected.setColorFilter(resources.getColor(R.color.status_none));
        mStatusDisconnected.setColorFilter(resources.getColor(R.color.status_none));
        mStatusInProgress.setColorFilter(resources.getColor(R.color.status_progress));
        mInformationTextView.setText(getString(R.string.cloud_city_in_progress));
        mRegistrationForm.setVisibility(View.VISIBLE);
        mRegistrationProgressBar.setVisibility(View.VISIBLE);
    }

    private void showConnectionState(Resources resources) {
        mInformationTextView.setText(String.format("Connected to Cloud City as %s.", getUserName()));
        mRegistrationForm.setVisibility(View.GONE);
        mStatusConnected.setColorFilter(resources.getColor(R.color.status_ok));
        mStatusDisconnected.setColorFilter(resources.getColor(R.color.status_none));
        mStatusInProgress.setColorFilter(resources.getColor(R.color.status_none));
    }

    private void showDisconnectedState(Resources resources) {
        mStatusDisconnected.setColorFilter(resources.getColor(R.color.status_error));
        mStatusConnected.setColorFilter(resources.getColor(R.color.status_none));
        mStatusInProgress.setColorFilter(resources.getColor(R.color.status_none));
        mUserNameTextView.setVisibility(View.VISIBLE);
        mInformationTextView.setText(getString(R.string.cloud_city_not_connected));
        mRegistrationForm.setVisibility(View.VISIBLE);
        mUserNameTextView.setFocusable(true);
        mUserNameTextView.requestFocus();
        mUserNameTextView.setImeOptions(EditorInfo.IME_ACTION_SEND);
        mUserNameTextView.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                registerWithAlertDialog();
                return false;
            }
        });
    }

    public void clearBadges() {
        if (!isRegistered()) {
            return;
        }
        final CharSequence userName = mUserNameTextView.getText();
        final String url = UrlConfig.getClearBadgesUrl(userName).toString();
        Log.i(TAG, "Requesting " + url);
        mRegistrationProgressBar.setVisibility(ProgressBar.VISIBLE);
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
        makeRequest(stringRequest);
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


    private void saveRegistrationStatus(Status status, CharSequence userName) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        Log.e(TAG, "Status : " + status);
        if (status == Status.CONNECTED && !TextUtils.isEmpty(userName)) {
            final Editor editor = sharedPreferences.edit();
            editor.putBoolean(PREFERENCE_REGISTRATION_STATUS, true);
            editor.putString(PREFERENCE_USER_NAME, userName.toString());
            editor.commit();
            Log.e(TAG, "Saved preference");
        } else {
            sharedPreferences.edit().remove(PREFERENCE_REGISTRATION_STATUS);
        }
    }

    private void makeRequest(Request request) {
        request.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        final RequestQueueSingleton requestQueue = RequestQueueSingleton.getInstance(getContext());
        requestQueue.addToRequestQueue(request);
    }

    private boolean hasNetworkConnection() {
        final ConnectivityManager connectivityManager =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && (networkInfo.isConnected());
    }

    private String getUserName() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String userName = sharedPreferences.getString(PREFERENCE_USER_NAME, "");
        if (TextUtils.isEmpty(userName)) {
            userName = getDefaultUserName();
        }
        return userName;
    }

    private String getDefaultUserName() {
        final AccountManager manager = (AccountManager) getContext().getSystemService(Context.ACCOUNT_SERVICE);
        final Account[] list = manager.getAccounts();
        String gmail = "";

        for (Account account : list) {
            Log.e(TAG, "Account " + account.name);
            if (account.type.equalsIgnoreCase("com.google")) {
                gmail = account.name;
                if (gmail != null && gmail.contains("@")) {
                    gmail = gmail.substring(0, gmail.indexOf("@"));
                }
                break;
            }
        }
        return gmail;
    }

    public void showInAppMessage(String message, int id) {
        Log.e(TAG, "RECEIVED " + message + " " + id);
        if (!TextUtils.isEmpty(message)) {
            new AlertDialog.Builder(getContext()).setTitle("Lobot").setMessage(message).setPositiveButton("Ok", null)
                                                 .create().show();
        }

    }

    private void showNoNetworkAlert() {
        new AlertDialog.Builder(getContext()).setTitle("Error")
                                             .setMessage("Check your network connection and try again")
                                             .setPositiveButton("Ok", null).create().show();


    }
}
