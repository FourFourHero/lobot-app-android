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

    private static String TAG = "Lobot";

    public static void register(Context context, String token) {
        final RequestQueueSingleton requestQueue = RequestQueueSingleton.getInstance(context);

        final StringRequest stringRequest =
                new StringRequest(Request.Method.GET, UrlConfig.getRegistrationUrl(token).toString(),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.e(TAG, "SUCCESS: " + response);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "ERROR: " + error);
                    }
                });
        requestQueue.addToRequestQueue(stringRequest);
    }
}
