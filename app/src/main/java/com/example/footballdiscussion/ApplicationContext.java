package com.example.footballdiscussion;

import android.app.Application;
import android.content.Context;

public class ApplicationContext extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
