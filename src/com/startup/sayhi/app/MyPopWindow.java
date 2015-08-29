package com.startup.sayhi.app;

import com.startup.sayhi.app.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

public class MyPopWindow extends PopupWindow{

	public MyPopWindow(Context context) {
		super(context);
	}

	public void initPopWindow(View view,Context context){
		this.setContentView(view);
		this.setWidth(LayoutParams.MATCH_PARENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setBackgroundDrawable(context.getResources().getDrawable(R.color.black));
		this.setOutsideTouchable(true);
	}

	
}
