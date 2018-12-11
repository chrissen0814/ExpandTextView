package com.chrissen.expandtextview;

import android.app.Application;

/**
 * Function:
 * <br/>
 * Describe:
 * <br/>
 * Author: chris on 2018/5/30.
 * <br/>
 * Email: sunqirui@jiuhuar.com
 */


public class App extends Application {

    private static Application sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
    }

    public static Application getContext() {
        return sContext;
    }
}
