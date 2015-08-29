package com.startup.sayhi.app;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;





import com.startup.sayhi.app.R;
import com.startup.sayhi.model.Friend;
import com.startup.sayhi.model.Photos;
import com.startup.sayhi.model.Reply;
import com.startup.sayhi.view.DragLayout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 朋友圈适配器
 * 
 * @author jiangyue
 * 
 */
public class TopicDetail extends BaseAdapter implements OnClickListener {
	private static final String TAG = "FriendAdapter";
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Reply reply;// 临时引用
	private Context context;

	private PopupWindow window;

	private MyListView replyList;

	private ReplyAdapter adapter;


	private List<Friend> list;
	private Friend friend;

	private PopupWindow editWindow;

	private EditText replyEdit;
	private Button sendBtn;

	private DragLayout topLayout;

	private FlushListView flush;

	private MyDialog delDialog;// 删除提示框
	private AlertDialog.Builder builder;

	public TopicDetail(Context context, PopupWindow window,
			PopupWindow editWindow,FlushListView flush) {
		this.context = context;
		this.window = window;
		// dialog = new ListDialog(context);
		this.flush=flush;
		this.editWindow = editWindow;
		replyEdit = (EditText) editWindow.getContentView().findViewById(
				R.id.reply);
		sendBtn = (Button) editWindow.getContentView().findViewById(
				R.id.send_msg);
		topLayout = (DragLayout) LayoutInflater.from(context).inflate(
				R.layout.friend_group_activity, null);
	}

	private void initDialog() {
		builder = new AlertDialog.Builder(context);
		builder.setTitle("提示");
		builder.setMessage("确定删除吗?");
	}

	public void setData(List<Friend> list) {
		this.list = list;
	}

	@Override
	public int getCount() {
		return list == null ? 0 : list.size();
	}

	@Override
	public Object getItem(int position) {
		return list == null ? null : list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.friend_items, null);
			holder = getHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (list != null) {
			friend = list.get(position);
			friend.position = position;
			convertView.setId(friend.id);
			bindData(holder);
		}
		return convertView;
	}

	/**
	 * 绑定数据
	 * 
	 * @param holder
	 */

	private List<Reply> replys;// 临时引用

	private void bindData(ViewHolder holder) {
		if (friend == null)
			return;
		holder.name.setText(friend.name);

		// 判断是否有回复
		if (isEmpty(friend.contentText)) {
			holder.contentText.setVisibility(View.GONE);
		} else {
			holder.contentText.setVisibility(View.VISIBLE);
		}
		holder.contentText.setText(friend.contentText);

		// 判断是否有分享链接
		if (isEmpty(friend.linkUrl)) {
			holder.linkContent.setVisibility(View.GONE);
		} else {
			holder.linkContent.setVisibility(View.GONE);
			holder.linkIcon.setImageResource(R.drawable.icon);
			holder.linkDescription.setText("我只是做个测试");
		}

		holder.sendDate.setText(handTime(friend.sendDate));

		// 判断是否有点赞
		if (isEmpty(friend.favourName)) {
			holder.favourTemp.setVisibility(View.GONE);
		} else {
			holder.favourTemp.setVisibility(View.VISIBLE);
			if (listIsEmpty(friend.replyList)) {// 只有点赞 则不显示分隔线
				holder.praiseLine.setVisibility(View.GONE);
			} else {
				holder.praiseLine.setVisibility(View.VISIBLE);
			}
		}

		// 判断点赞 与回复是否都没有内容
		if (isEmpty(friend.favourName) && listIsEmpty(friend.replyList)) {
			holder.replyContent.setVisibility(View.GONE);
		} else {
			holder.replyContent.setVisibility(View.VISIBLE);
		}

		// 判断是否有回复内容
		if (listIsEmpty(friend.replyList)) {
			holder.replyList.setVisibility(View.GONE);
		} else {
			holder.replyList.setVisibility(View.VISIBLE);
		}
		holder.favourName.setText(friend.favourName);

		// 分享类型
		holder.shareType.setVisibility(View.GONE);
		// 判断是否有发表图片
		if (isEmpty(friend.images)) {
			holder.images.setVisibility(View.GONE);
		} else {
			holder.images.setVisibility(View.VISIBLE);
			final ArrayList<Photos> list = getPhotos(friend.images);
			 if (list != null) {
			holder.images.setAdapter(new ImageAdapter(context, list, false));
			holder.images.setOnItemClickListener(new OnItemClickListener() {// 设置监听器
						// ，点击进入大图
						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
						}
					});
			 }
		}
		adapter = new ReplyAdapter(context);
		replys = friend.replyList;
		// =========判断是否有更多回复=========
		checkMoreReply(holder);
		// ==============================

		// ==================评论==================
		adapter.setData(replys);
		holder.replyList.setAdapter(adapter);
		holder.replyList.setTag(replys);
		holder.replyList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				replys = (List<Reply>) parent.getTag();
				reply = replys.get(position);
				flush.handReply(reply);// 处理评论
			}
		});
		// ==================评论end==================
		holder.delText.setTag(friend.id);
		holder.delText.setOnClickListener(this);
//		flush.showDel(holder.delText, friend.userId);
		holder.replyIcon.setTag(friend);
		holder.replyIcon.setOnClickListener(this);
	}

	/**
	 * 处理图片数据
	 * 
	 * @param photo
	 * @return
	 */
	private ArrayList<Photos> getPhotos(String photo) {
		if (!photo.contains("[")) {
			return null;
		}
		ArrayList<Photos> phs = new ArrayList<Photos>();
		try {
			JSONArray array = new JSONArray(photo);
			int size = array.length();
			JSONObject obj;
			Photos ph;
			for (int i = 0; i < size; i++) {
				obj = array.getJSONObject(i);
				ph = new Photos();
				ph.min = obj.getString("urlMin");
				ph.max = obj.getString("url");
				phs.add(ph);
			}
			return phs;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void checkMoreReply(ViewHolder holder) {
		int replyCount = friend.replyCount;
		if (replyCount > 10 && replyCount != friend.replyList.size()) {// 最多显示10条
			holder.replyMore.setVisibility(View.VISIBLE);
			holder.replyMore.setTag(friend.friendId);
			holder.replyMore.setOnClickListener(this);
		} else {
			holder.replyMore.setVisibility(View.GONE);
		}
	}

	/**
	 * 初始化ViewHolder
	 * 
	 * @param convertView
	 * @return
	 */
	private ViewHolder getHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.shareType = (TextView) convertView.findViewById(R.id.share_type);
		holder.photo = (ImageView) convertView.findViewById(R.id.photo);
		holder.name = (TextView) convertView.findViewById(R.id.name);
		holder.contentText = (TextView) convertView
				.findViewById(R.id.content_text);
		holder.linkIcon = (ImageView) convertView.findViewById(R.id.link_icon);
		holder.linkDescription = (TextView) convertView
				.findViewById(R.id.link_description);
		holder.sendDate = (TextView) convertView.findViewById(R.id.date);
		holder.delText = (TextView) convertView.findViewById(R.id.delete);
		holder.replyIcon = (ImageButton) convertView
				.findViewById(R.id.reply_icon);
		holder.favourName = (TextView) convertView
				.findViewById(R.id.favuor_name);
		holder.replyList = (MyListView) convertView
				.findViewById(R.id.reply_list);
		holder.linkContent = (LinearLayout) convertView
				.findViewById(R.id.link_content);
		holder.replyContent = (LinearLayout) convertView
				.findViewById(R.id.reply_content);
		holder.favourTemp = (LinearLayout) convertView
				.findViewById(R.id.favour_temp);
		holder.replyMore = (TextView) convertView.findViewById(R.id.reply_more);
		holder.images = (MyGridView) convertView
				.findViewById(R.id.image_content);
		holder.praiseLine = convertView.findViewById(R.id.praise_line);
		return holder;
	}

	private class ViewHolder {
		public TextView shareType;// 分享类型
		public ImageView photo;// 头像
		public TextView name;// 名字
		public TextView contentText;// 文字内容
		public ImageView linkIcon;// 链接图标
		public TextView linkDescription;// 链接描述
		public TextView sendDate;// 发表时间
		public TextView delText;// 删除文字
		public ImageButton replyIcon;// 回复icon
		public TextView favourName;// 点赞的名字
		public MyListView replyList;// 回复的listView
		public LinearLayout linkContent;// 链接内容
		public LinearLayout replyContent;// 回复布局
		public LinearLayout favourTemp;// 点赞布局
		public TextView replyMore;// 更多回复
		public MyGridView images;// 发表的图片 最多8张

		public View praiseLine;// 点赞下面的线
	}

	/**
	 * 显示评论弹窗
	 * 
	 * @param view
	 */
	private void showDialog(View view) {
		int width = view.getWidth();
		friend = (Friend) view.getTag();
		flush.showCancle(friend);// 显示或者隐藏赞
		int[] location = new int[2];
		view.getLocationInWindow(location);
		int x = location[0] - dip2px(context, width) - width - 80;
		int y = location[1] - 20;
		View v = window.getContentView();
		TextView discuss = (TextView) v.findViewById(R.id.discuss);
		TextView favuor = (TextView) v.findViewById(R.id.favuor);
		TextView favuorCancle = (TextView) v.findViewById(R.id.favuor_cancle);
		favuorCancle.setOnClickListener(this);
		discuss.setOnClickListener(this);
		favuor.setOnClickListener(this);
		discuss.setTag(view.getTag());
		favuor.setTag(view.getTag());
		favuorCancle.setTag(view.getTag());
		window.showAtLocation(view, Gravity.NO_GRAVITY, x, y);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.reply_icon:// 显示评论窗口
			showDialog(v);
			break;
		case R.id.delete:// 删除 说说
			delDialog.show();
			break;
		case R.id.discuss:// 评论 说说
			flush.showDiscussDialog(v);
			break;
		case R.id.favuor_cancle:// 取消点赞
			friend = (Friend) v.getTag();
			flush.delParise(String.valueOf(friend.id));
			window.dismiss();
			break;
		case R.id.favuor:// 点赞 说说
			friend = (Friend) v.getTag();
			flush.addTrendParise(String.valueOf(friend.id));
			window.dismiss();
			break;
		case R.id.reply_more:// 点赞 说说
			flush.getReplyByTrendId(v.getTag());
			break;
		default:
			Log.d(TAG, "ON CLICK!");
			break;
		}
	}

	/**
	 * 弹出删除提示框
	 */
	private Dialog dialog;

	private void showDeletedialog(final String trendId) {

		builder.setNegativeButton("否", new Dialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setPositiveButton("是", new Dialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				flush.delTrendById(trendId);
				dialog.dismiss();
			}
		});
		dialog = builder.create();
		dialog.show();
		// delDialog.setLeftOnclick(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// delDialog.dismiss();
		// }
		// });
		// delDialog.setRightOnclick(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// flush.delTrendById(trendId);
		// delDialog.dismiss();
		// }
		// });
		// delDialog.show();
	}

	/**
	 * 显示回复窗口
	 * 
	 * @param type
	 */

	/**
	 * 显示评论窗
	 * 
	 * @param type
	 * @param v
	 */
	@Deprecated
	private void showDiscuss(View v) {
		friend = (Friend) v.getTag();
		final int id = friend.id;
		// getViewPosition(friend.position);// 获取view的位置
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
		final InputMethodManager manager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		manager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		editWindow.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				manager.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
			}
		});
		sendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				reply = new Reply();
				// reply.sendName =userInfo.getUsername();
				reply.content = replyEdit.getEditableText().toString();
				reply.friendId = id;
				// db.save(reply);
				flush.saveReply(reply);
				// flush.flush();
				editWindow.dismiss();
				replyEdit.setText("");
			}
		});

		window.dismiss();
	}

	/**
	 * 获取view的位置
	 * 
	 * @param position
	 */
	private void getViewPosition(int position) {
		int[] location = new int[2];
		View view = null;// = views.get(position);
		view.getLocationInWindow(location);
		int x = location[0];
		int y = location[1];
		String str = "x=" + x + "                   y=" + y;
		toast(str);
	}

	/**
	 * 回调接口 实现数据刷新
	 * 
	 * @author jiangyue
	 * 
	 */
	public interface FlushListView {
		public void flush();// 刷新数据

		public void showDiscussDialog(View v);// 显示评论对话框

		public void getReplyByTrendId(Object tag);// 根据动态id获取评论回复

		public void getViewPosition(int position);

		public void delParise(String valueOf);// 删除点赞

		public void showCancle(Friend friend);// 显示或者隐藏赞

		public void saveReply(Reply reply);// 保存回复信息

		public void addTrendParise(String trendId);// 添加点赞

		public void delTrendById(String trendId);// 根据id删除动态

		public void showDel(TextView view, String userId);// 显示删除按钮

		public void handReply(Reply reply);// 处理评论

	}

	/**
	 * 判断指定的字符串是否是 正确的（不为“”、null 、“null”）
	 * 
	 * @param str
	 * @return
	 */
	private boolean isEmpty(String str) {
		if (str != null && !"".equals(str) && !"null".equals(str)
				&& !"[]".equals(str))
			return false;
		return true;
	}

	/**
	 * 判断集合是否是需要的格式 （不为null size>0）
	 * 
	 * @param list
	 * @return
	 */
	private boolean listIsEmpty(List list) {
		if (list != null && list.size() > 0)
			return false;
		return true;

	}

	private void toast(String str) {
		Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 处理时间
	 * 
	 * @param string
	 * @return
	 */
	private String handTime(String time) {
		if (time == null || "".equals(time.trim())) {
			return "";
		}
		try {
			Date date = format.parse(time);
			long tm = System.currentTimeMillis();// 当前时间戳
			long tm2 = date.getTime();// 发表动态的时间戳
			long d = (tm - tm2) / 1000;// 时间差距 单位秒
			if ((d / (60 * 60 * 24)) > 0) {
				return d / (60 * 60 * 24) + "天前";
			} else if ((d / (60 * 60)) > 0) {
				return d / (60 * 60) + "小时前";
			} else if ((d / 60) > 0) {
				return d / 60 + "分钟前";
			} else {
				// return d + "秒前";
				return "刚刚";
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	private class FriendRunnable implements Runnable {
		private ViewHolder holder;

		public FriendRunnable(ViewHolder holder) {
			this.holder = holder;
		}
		@Override
		public void run() {
			if (holder != null)
				bindData(holder);
		}
	}
}
