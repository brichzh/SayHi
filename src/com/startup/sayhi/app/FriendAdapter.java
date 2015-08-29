package com.startup.sayhi.app;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.startup.sayhi.app.R;
import com.startup.sayhi.model.Friend;
import com.startup.sayhi.model.Photos;
import com.startup.sayhi.model.Reply;
import com.startup.sayhi.view.DragLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 朋友圈适配器
 * 
 * @author jiangyue
 * 
 */
public class FriendAdapter extends BaseAdapter implements OnClickListener {
	private static final String TAG = "FriendAdapter";
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Context context;

	private PopupWindow window;

	private MyListView replyList;

	private ReplyAdapter adapter;


	private List<Friend> list;
	private Friend friend;

	private DragLayout topLayout;

	private FlushListView flush;

	private AlertDialog.Builder builder;
	private DisplayImageOptions options;
	
	public FriendAdapter(Context context) {
		this.context = context;
		topLayout = (DragLayout) LayoutInflater.from(context).inflate(
				R.layout.friend_group_activity, null);
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

		holder.sendDate.setText(handTime(friend.sendDate));
		options = new DisplayImageOptions.Builder()
					.cacheInMemory(true)
					.cacheOnDisk(true).considerExifParams(true)
					.bitmapConfig(Bitmap.Config.RGB_565).build();
		
		ImageLoader.getInstance().displayImage(friend.photo, holder.photo, options, null);
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
								ArrayList<String> maxList = new ArrayList<String>();
								for (Photos photos : list) {
									maxList.add(photos.max);
								}
								imageBrowser(position, maxList);
							}
						});
			}
		}
		
		holder.name.setOnClickListener(this);
		holder.company.setOnClickListener(this);
		holder.photo.setOnClickListener(this);
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

	/**
	 * 初始化ViewHolder
	 * 
	 * @param convertView
	 * @return
	 */
	private ViewHolder getHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.photo = (ImageView) convertView.findViewById(R.id.photo);
		holder.name = (TextView) convertView.findViewById(R.id.name);
		holder.company = (TextView) convertView.findViewById(R.id.company);
		holder.contentText = (TextView) convertView.findViewById(R.id.content_text);
		holder.sendDate = (TextView) convertView.findViewById(R.id.date);
		holder.viewCnt = (TextView) convertView.findViewById(R.id.viewcnt);
		holder.commonCnt = (TextView) convertView.findViewById(R.id.commcnt);
		holder.images = (MyGridView) convertView.findViewById(R.id.image_content);
		return holder;
	}

	private class ViewHolder {
		public ImageView photo;// 头像
		public TextView name;// 名字
		public TextView company;// 公司
		public TextView contentText;// 文字内容
		public TextView sendDate;// 发表时间
		public TextView viewCnt; // 阅读次数
		public TextView commonCnt;// 评论次数
		public MyGridView images;// 发表的图片 最多9张
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.photo:
		case R.id.name:
		case R.id.company://进入profile页面
			Log.d(TAG, "show profile!");
			break;

		default:
			Log.d(TAG, "ON CLICK!");
			break;
		}
	}
	private void imageBrowser(int position, ArrayList<String> urls) {
		Intent intent = new Intent(context, ImagePagerActivity.class);
		
		intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_URLS, urls);
		intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_INDEX, position);
		context.startActivity(intent);
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
