package com.startup.sayhi.app;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.*;

import java.io.File;
import java.io.IOException;

/**
 * com.tencent.mm.app
 * WeChatKids
 * Created by harlliu on 14-5-8.
 */


public class HiApplication extends Application {

    private static final String TAG = "HiApplication";

    private static HiApplication m_instance;
    private static Context m_context;
    
    @Override
    public void onCreate() {
        super.onCreate();

        m_context = getApplicationContext();
        m_instance = this;
    }

    public static HiApplication getInstance() {
        return m_instance;
    }
	
    public static Context getContext() {
        return m_context;
    }
}
