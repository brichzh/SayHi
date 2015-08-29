package com.startup.sayhi.app;


import java.util.List;

import com.startup.sayhi.app.R;
import com.startup.sayhi.model.Reply;

import android.content.Context;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

/**
 * 回复适配器
 * 
 * @author jiangyue
 * 
 */
public class ReplyAdapter extends BaseAdapter implements OnClickListener {

	private Context context;

	private List<Reply> list;

	private Reply reply;

	public ReplyAdapter(Context context) {
		this.context = context;
	}

	public void setData(List<Reply> list) {
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
					R.layout.friend_reply_item, null);
			holder = getViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (list != null) {
			reply = list.get(position);
			bindData(holder);
		}
		return convertView;
	}

	/**
	 * 绑定数据
	 * 
	 * @param holder
	 */
	private void bindData(ViewHolder holder) {
		if (reply == null)
			return;
		holder.sendName.setText(reply.sendName);
		holder.replyName.setText(reply.replyName);
//		Spannable span ="";// SmileUtils.getSmiledText(context,reply.content);
		// 设置内容
//		holder.content.setText(span,BufferType.SPANNABLE);
		holder.content.setText(reply.content);
		if (isEmpty(reply.replyName)) {
			holder.replyName.setVisibility(View.GONE);
		} else {
			holder.replyName.setVisibility(View.VISIBLE);
		}
		if (isEmpty(reply.replyName)) {
			holder.replyTemp.setVisibility(View.GONE);
		} else {
			holder.replyTemp.setVisibility(View.VISIBLE);
		}
		holder.sendName.setOnClickListener(this);
		holder.replyName.setOnClickListener(this);

	}

	/**
	 * 初始化viewHolder
	 * 
	 * @param convertView
	 * @return
	 */
	private ViewHolder getViewHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.replyName = (TextView) convertView.findViewById(R.id.reply_name);
		holder.sendName = (TextView) convertView.findViewById(R.id.send_name);
		holder.content = (TextView) convertView.findViewById(R.id.content);
		holder.replyTemp = (TextView) convertView.findViewById(R.id.reply_temp);
		return holder;
	}

	private class ViewHolder {
		public TextView replyName;// 回复的名字
		public TextView sendName;// 发送的名字
		public TextView content;// 回复的内容
		public TextView replyTemp;// 回复文字
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.send_name:
			Toast.makeText(context, "去到他的首页", Toast.LENGTH_SHORT).show();
			break;
		case R.id.reply_name:
			Toast.makeText(context, "去到他的首页", Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
	}
	/**
	 * 判断指定的字符串是否是 正确的（不为“”、null 、“null”）
	 * 
	 * @param str
	 * @return
	 */
	private boolean isEmpty(String str) {
		if (str != null && !"".equals(str) && !"null".equals(str))
			return false;
		return true;
	}
}
