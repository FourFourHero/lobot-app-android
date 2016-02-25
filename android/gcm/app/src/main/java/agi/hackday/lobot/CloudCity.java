package agi.hackday.lobot;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

/**
 * Created by ltrempe on 2/25/16.
 */
public class CloudCity {

    public static final String TAG = "CloudCity";

    private static final String REGISTRATION_STATUS = "registrationStatus";

    public static final String REGIRATION_COMPLETE = "registrationComplete";

    public static final String EXTRA_REGISTRATION_STATUS = "cc_extra_registration_status";

    public enum Status {
        CONNECTED,
        DISCONNECTED;
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


    public interface Listener {
        void onSuccess();

        void onError();
    }

    private final Context mContext;

    public CloudCity(Context context) {
        mContext = context;
    }

    public void register(String token, final Listener listener) {
        final RequestQueueSingleton requestQueue = RequestQueueSingleton.getInstance(mContext);

        final String url = UrlConfig.getRegistrationUrl(token).toString();
        Log.i(TAG, "url " + url);
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "SUCCESS: " + response);
                listener.onSuccess();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "ERROR: " + error);
                listener.onError();
            }
        });
        requestQueue.addToRequestQueue(stringRequest);
    }
}
