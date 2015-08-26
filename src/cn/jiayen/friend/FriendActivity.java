package cn.jiayen.friend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.jiayen.bean.Friend;
import cn.jiayen.bean.Reply;
import cn.jiayen.friend.FriendAdapter.FlushListView;
import cn.jiayen.friend.ListDialog.ListDialogItemOnclick;

/**
 * @author jiangyue
 * 
 */
public class FriendActivity extends Activity implements OnClickListener {

	private ListView listView;
	private PopupWindow window;// 评论window
	private PopupWindow editWindow;// 回复window

	private ListDialog dialog;
	private ImageButton back;// 返回按钮
	private ImageButton more;// 弹出更多

	private TextView discuss;// 评论
	private TextView favuor;// 赞
	private TextView favuorCancle;// 取消赞

	private RelativeLayout topLayout;

	// ==============回复==============
	private EditText replyEdit;// 回复框
	// ====================回复完结================

	private FriendAdapter adpter;

	private Button sendBtn;// 发送按钮


	private List<Friend> list;// 动态数据

	private InputMethodManager manager;

	private int lastPosition;// listView item最后所在的位置
	private int lastY;// listView item最后所在的y坐标
	/**
	 * 头部用户信息部分
	 */
	private View headView;// 头部view
	private ImageView userPhoto;// 头像头像
	private TextView userName;// 用户名字
	private TextView newsNum;// 最新动态数量
	private TextView favourNum;// 最新评论、点赞数量

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_group_activity);
		getData();
		initViews();
	}
	private  void getData() {
		try {
			InputStreamReader reader = new InputStreamReader(getResources().getAssets().open("friend.txt"));
			BufferedReader buf = new BufferedReader(reader);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line=buf.readLine())!=null) {
				sb.append(line);
			}
			buf.close();
			reader.close();
			BeanUtil util = BeanUtil.getInstance();
			list = util.getFriends(sb.toString());
			for (int i = 0; i <3; i++) {
				list.addAll(list);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 初始化控件
	 */
	private void initViews() {
		dialog = new ListDialog(this);
		initPopWindow();// 初始化弹出窗
		topLayout = (RelativeLayout) findViewById(R.id.friend_circle);
		listView = (ListView) findViewById(R.id.friend_list);
		// ===============刷新

		// ================
		headView = LayoutInflater.from(this).inflate(R.layout.friend_head_item,
				null);
		userPhoto = (ImageView) headView.findViewById(R.id.user_photo);
		userName = (TextView) headView.findViewById(R.id.user_name);
		newsNum = (TextView) headView.findViewById(R.id.news_num);
		favourNum = (TextView) headView.findViewById(R.id.favour_num);
		listView.addHeaderView(headView);
		adpter = new FriendAdapter(this, window, editWindow,new Myflush());// 初始化适配器
		adpter.setData(list);
		listView.setAdapter(adpter);
		listView.setFocusableInTouchMode(false);
		back = (ImageButton) findViewById(R.id.friend_back);
		more = (ImageButton) findViewById(R.id.friend_more);
		back.setOnClickListener(this);
		more.setOnClickListener(this);
	}


	

	/**
	 * 初始化popWindow
	 */
	private void initPopWindow() {
		View view = getLayoutInflater().inflate(R.layout.friend_reply, null);
		window = new PopupWindow(view, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, true);
		window.setAnimationStyle(R.style.reply_window_anim);
		discuss = (TextView) view.findViewById(R.id.discuss);
		favuor = (TextView) view.findViewById(R.id.favuor);
		favuorCancle = (TextView) view.findViewById(R.id.favuor_cancle);
		window.setBackgroundDrawable(getResources().getDrawable(R.color.black));
		window.setOutsideTouchable(true);
		discuss.setOnClickListener(this);
		favuor.setOnClickListener(this);
		favuorCancle.setOnClickListener(this);

		View editView = getLayoutInflater().inflate(
				R.layout.friend__replay_input, null);
		editWindow = new PopupWindow(editView, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, true);
		editWindow.setBackgroundDrawable(getResources().getDrawable(
				R.color.white));
		editWindow.setOutsideTouchable(true);
		replyEdit = (EditText) editView.findViewById(R.id.reply);
		sendBtn = (Button) editView.findViewById(R.id.send_msg);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.friend_back:
			this.finish();
			// startActivity(new Intent(this, IndexActivity.class));
			break;
		case R.id.friend_more:
			show();
			break;
		case R.id.smail:
			break;
		default:
			break;
		}
	}

	/**
	 * 显示回复评论框
	 * 
	 * @param reply
	 */
	private void showDiscuss() {
		replyEdit.setFocusable(true);
		replyEdit.requestFocus();

		// 设置焦点，不然无法弹出输入法
		editWindow.setFocusable(true);

		// 以下两句不能颠倒
		editWindow.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
		editWindow
				.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		editWindow.showAtLocation(topLayout, Gravity.BOTTOM, 0, 0);

		// 显示键盘
		manager = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
		manager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		editWindow.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				manager.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
			}
		});
		window.dismiss();
		
	}
	/**
	 * 去到发表页面
	 */
	private void show() {
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}


	@Override
	protected void onResume() {
		super.onResume();
	}



	private class Myflush implements FlushListView{

		@Override
		public void flush() {
			
		}

		@Override
		public void showDiscussDialog(View v) {
			Friend friend = (Friend) v.getTag();
			Reply reply = new Reply();
			reply.friendId = friend.id;
			reply.userId = "0";
			showDiscuss();
		}

		@Override
		public void getReplyByTrendId(Object tag) {
			
		}

		@Override
		public void getViewPosition(int position) {
			
		}

		@Override
		public void delParise(String valueOf) {
			
		}

		@Override
		public void showCancle(Friend friend) {
			
		}

		@Override
		public void saveReply(Reply reply) {
			
		}

		@Override
		public void addTrendParise(String trendId) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void delTrendById(String trendId) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void showDel(TextView view, String userId) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void handReply(Reply reply) {
			// TODO Auto-generated method stub

			String[] items= new String[] { "删除", "复制","回复" };
			dialog.init(items, new ListDialogItemOnclick() {
				@Override
				public void onClick(View view) {
					TextView v = (TextView) view;
					if ("删除".equals(v.getText())) {// 删除
					} else if ("复制".equals(v.getText())) {// 复制
						ClipboardManager c = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						toast("已复制到剪切板");
					} else if ("回复".equals(v.getText())) {// 回复
						showDiscuss();
					}
					dialog.dismiss();
				}
			});
			dialog.show();
		
		}
		
	}
	
	/**
	 * 弹出信息
	 * 
	 * @param str
	 */
	private void toast(String str) {
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}
}
