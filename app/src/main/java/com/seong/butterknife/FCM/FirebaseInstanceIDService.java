package com.seong.butterknife.FCM;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by hans on 2018-03-08.
 */

public class FirebaseInstanceIDService extends FirebaseInstanceIdService{

    private static final String TAG = "FirebaseIIDService";

    @Override
    public void onTokenRefresh() {
        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        Log.i(TAG, "Refreshed Token: " + refreshToken);
    }
}
