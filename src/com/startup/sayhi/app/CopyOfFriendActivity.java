package com.startup.sayhi.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;

import com.nineoldandroids.view.ViewHelper;
import com.startup.sayhi.app.R;
import com.startup.sayhi.app.FriendAdapter.FlushListView;
import com.startup.sayhi.app.ListDialog.ListDialogItemOnclick;
import com.startup.sayhi.model.Friend;
import com.startup.sayhi.model.Reply;
import com.startup.sayhi.view.DragLayout;
import com.startup.sayhi.view.DragLayout.DragListener;
import com.startup.sayhi.view.XListView;

import android.app.Activity;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author 
 * 
 */
public class CopyOfFriendActivity extends Activity implements OnClickListener {
	private static final String TAG = "FriendActivity";
	private XListView listView;
	private PopupWindow window;// 评论window
	private PopupWindow editWindow;// 回复window

	private ListDialog dialog;
	private TextView me;// 弹出我
	private TextView post;// 弹出发表

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
	
	private DragLayout dl;
//	private ListView lv;
	private View sideMenus;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_group_activity);
		getData();
		initDragLayout();
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
	
	private void initDragLayout() {
		dl = (DragLayout) findViewById(R.id.friend_circle);
		dl.setDragListener(new DragListener() {
			@Override
			public void onOpen() {
//				lv.smoothScrollToPosition(new Random().nextInt(30));
			}

			@Override
			public void onClose() {
//				shake();
			}

			@Override
			public void onDrag(float percent) {
				ViewHelper.setAlpha(me, 1 - percent);
			}
		});
	}
	/**
	 * 初始化控件
	 */
	private void initViews() {
		dialog = new ListDialog(this);
		initPopWindow();// 初始化弹出
		
		listView = (XListView) findViewById (R.id.friend_list);
		// ===============刷新

		// 如果有新消息
		headView = LayoutInflater.from(this).inflate(R.layout.notification, null);
		newsNum = (TextView) headView.findViewById(R.id.news_tip);
		listView.addHeaderView(headView);
		
		//adpter = new TopicDetail(this, window, editWindow,new Myflush());// 初始化适配器
		adpter.setData(list);
		listView.setAdapter(adpter);
//		listView.setFocusableInTouchMode(true);
		me = (TextView) findViewById(R.id.me);
		post = (TextView) findViewById(R.id.post);
		me.setOnClickListener(this);
		post.setOnClickListener(this);
		//侧边栏
		sideMenus = findViewById(R.id.sidemenus);
		
//		lv = (ListView) findViewById(R.id.lv);
//		lv.setAdapter(new ArrayAdapter<String>(FriendActivity.this,
//				R.layout.item_text, new String[] { "消息中心", "邀请微信好友",
//						"关于我们", "退出登录"}));
//		lv.setOnItemClickListener(new OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1,
//					int position, long arg3) {
//				Toast.makeText(FriendActivity.this, "click "+position, Toast.LENGTH_SHORT).show();
//			}
//		});
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Log.d(TAG, "click position="+position);
			}
		});
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
		case R.id.me:
			dl.open();
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
