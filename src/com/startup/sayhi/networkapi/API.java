package com.startup.sayhi.networkapi;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.startup.sayhi.storage.Storage;
import com.startup.sayhi.volley.Response;
import com.startup.sayhi.networkapi.BaseHttp;

import java.util.HashMap;

/**
 * Created by aidi on 15-06-20.
 */
public class API  {
	private static final String TAG = "API";
	private BaseHttp baseHttp;
	private Storage storage = null;

	//可以使用单例
	public API(Context context) {
		baseHttp = new BaseHttp(context);
		storage = new Storage(context, "account");

	}
	
	public Storage getStorage() {
		return storage;
	}
	
	public BaseHttp getBaseHttp() {
		return baseHttp;
	}
	
	public void get(final String url, HashMap<String, String> params,
			final Response.Listener<String> listener,
			final Response.ErrorListener errorListener) {
		Log.d(TAG, "Get url="+url);
		if (!TextUtils.isEmpty(storage.get("auth"))) {
			baseHttp.addCookie("auth", storage.get("auth"),
					storage.get("domain"));
		}
		baseHttp.get(url, params, listener, errorListener);
	}
	
	
	public void post(final String url, HashMap<String, String> params,
			final Response.Listener<String> listener,
			final Response.ErrorListener errorListener) {
		if (!TextUtils.isEmpty(storage.get("auth"))) {
			baseHttp.addCookie("auth", storage.get("auth"),
					storage.get("domain"));
		}
		
		baseHttp.post(url, params, listener, errorListener);
	}

}
