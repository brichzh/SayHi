package com.startup.sayhi.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.startup.sayhi.app.R;
import com.startup.sayhi.model.Friend;
import com.startup.sayhi.view.DragLayout;
import com.startup.sayhi.view.DragLayout.DragListener;
import com.startup.sayhi.view.XListView;

import android.app.Activity;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author 
 * 
 */
public class TopicsMainActivity extends Activity implements OnClickListener {
	private static final String TAG = "FriendActivity";
	private XListView listView;
	private PopupWindow window;// 评论window
	private PopupWindow editWindow;// 回复window

	private ListDialog dialog;
	private TextView me;// 弹出我
	private TextView post;// 弹出发表

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
		initImageLoader();
	}
	private void initImageLoader() {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.build();
		ImageLoader.getInstance().init(config);
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
		
		listView = (XListView) findViewById (R.id.friend_list);
		// ===============刷新

		// 如果有新消息
		headView = LayoutInflater.from(this).inflate(R.layout.notification, null);
		newsNum = (TextView) headView.findViewById(R.id.news_tip);
		listView.addHeaderView(headView);
		
		adpter = new FriendAdapter(this);// 初始化适配器
		adpter.setData(list);
		listView.setAdapter(adpter);
		me = (TextView) findViewById(R.id.me);
		post = (TextView) findViewById(R.id.post);
		me.setOnClickListener(this);
		post.setOnClickListener(this);
		//侧边栏
		sideMenus = findViewById(R.id.sidemenus);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Log.d(TAG, "click position="+position);
			}
		});
	}
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.me:
			dl.open();
			break;
		case R.id.post:
			Log.d(TAG, "Post new topic!");
			break;
		case R.id.smail:
			break;
		default:
			break;
		}
	}
	public void onMsgCenterClicked(View view) {
		Log.d(TAG, "onMsgCenterClicked");
		
	}
	public void onShareWXClicked(View view) {
		Log.d(TAG, "onShareWXClicked");
		
	}
	public void onAboutUsClicked(View view) {
		Log.d(TAG, "onAboutUsClicked");
		
	}
	public void onLogoutClicked(View view) {
		Log.d(TAG, "onLogoutClicked");
		
	}
	public void onShowProfile(View view) {
		Log.d(TAG, "onShowProfile");
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
	
	/**
	 * 弹出信息
	 * 
	 * @param str
	 */
	private void toast(String str) {
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}
}
