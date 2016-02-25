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
public class Lobot {

    private static final String REGISTRATION_STATUS = "registrationStatus";

    public interface Listener {
        void onSuccess();
        void onError();
    }

    private final Context mContext;

    public Lobot(Context context) {
        mContext = context;
    }

    private static String TAG = "Lobot";

    public void register(String token, final Listener listener) {
        final RequestQueueSingleton requestQueue = RequestQueueSingleton.getInstance(mContext);

        final String url = UrlConfig.getRegistrationUrl(token).toString();
        Log.i(TAG, "url " + url);
        final StringRequest stringRequest =
                new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
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
