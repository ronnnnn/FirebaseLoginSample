package com.ronnnnn.firebaseloginsample;

import android.app.Application;

import com.goodpatch.feedbacktool.sdk.Balto;

/**
 * This class is a subclass of Application and initializes Balto.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Balto.init(this);
    }
}
