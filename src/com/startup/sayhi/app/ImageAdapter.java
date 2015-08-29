package com.startup.sayhi.app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.startup.sayhi.app.R;
import com.startup.sayhi.model.Photos;

import net.tsz.afinal.FinalBitmap;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
	private Context context;
	private List<Photos> paths;
	private boolean localtion;// true是本地图片 false 是网络图片
	private FinalBitmap finalBit;

	private AddImage add;
	private DisplayImageOptions options;
	
	public ImageAdapter(Context context, List<Photos> paths, boolean localtion) {
		this.context = context;
		this.paths = paths;
		this.localtion = localtion;
		finalBit = FinalBitmap.create(context);
		options = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565).build();
	}

	public void setAddImage(AddImage add) {
		this.add = add;
	}


	public void setData(List<Photos> paths) {
		this.paths = paths;
	}

	@Override
	public int getCount() {
		return paths == null ? 0 : paths.size();
	}

	@Override
	public Object getItem(int position) {
		return paths == null ? null : paths.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = new Holder();
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.friend_images, null);
			holder.imageView = (ImageView) convertView.findViewById(R.id.image);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		String defaulUrl = "http://g.hiphotos.baidu.com/image/pic/item/960a304e251f95caff8f2fa5cb177f3e670952ae.jpg";
		
		ImageLoader.getInstance().displayImage(paths.get(position).min, holder.imageView, options, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			}
		}, new ImageLoadingProgressListener() {
			@Override
			public void onProgressUpdate(String imageUri, View view, int current, int total) {
			}
		});
		return convertView;
	}

	private class Holder {
		public ImageView imageView;
	}

	/**
	 * 加载图片接口
	 * 
	 * @author jiangyue
	 * 
	 */
	public interface AddImage {
		public void addImage(ImageView view, String path);
	}
}
