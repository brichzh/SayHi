package com.startup.sayhi.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class MyProgressDialog{
	ProgressDialog dialog;
	public MyProgressDialog(Context context) {
		dialog=new ProgressDialog(context);
	}
	public void show(){
			dialog.setMessage("数据加载中，请稍后...");
		dialog.show();
	}
	public void show(String msg){
		dialog.setMessage(msg);
		dialog.show();
	}
	public void setTouch(Boolean touch){
		dialog.setCanceledOnTouchOutside(touch);// 设置点击屏幕Dialog不消失  
	}
	public void dismiss(){
		dialog.dismiss();
	}
}
