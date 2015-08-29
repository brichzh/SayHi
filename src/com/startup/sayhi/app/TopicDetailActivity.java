package com.startup.sayhi.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.startup.sayhi.networkapi.API;
import com.startup.sayhi.networkapi.APIURL;
import com.startup.sayhi.share.WXShareManager;
import com.startup.sayhi.storage.Storage;
import com.startup.sayhi.util.MyToast;
import com.startup.sayhi.view.XListView;
import com.startup.sayhi.view.XListView.IXListViewListener;
import com.startup.sayhi.volley.Response;
import com.startup.sayhi.volley.VolleyError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopicDetailActivity extends Activity implements IXListViewListener{
	private static final String TAG = "NoteActivity";
	
	private API api;
	private XListView listView;
	private Storage storage;
	private String uid;
	private String topicId;
	private String replyUid;
	private String topicContent;
	private String commPageIndex;
	private String replyRowId;
	private ProgressDialog dialog;
	private SimpleAdapter adapter;
	final List<Map<String, Object>> commentListMap = new ArrayList<Map<String, Object>>();
	private List<String> topicOwnerRowlist = new ArrayList<String>();
	private TextView comment;
	private TextView viewCount;
	private TextView commCount;
	private TextView content;
	private TextView commTip;
	private RelativeLayout relativeLayout;
	private ProgressDialog progressDialog;
	private String myId;
	private boolean isMyTopic = false;
	private String myCommId;
	private boolean isMyComm = false;
	private int commRowId = 0;
	private boolean isFromNotify = false;
	private boolean isLoadMore = false;
	private boolean isRequesting = false;
	
	private String viewNum;
	private String commentNum;
	private String shareUrl;
	
	final String[] arrayFruit1 = new String[] { "分享给微信好友", "分享到朋友圈", "删除", "取消" };
	final String[] arrayFruit2 = new String[] { "分享给微信好友", "分享到朋友圈", "举报", "取消" };
	final String[] arrayFruit3 = new String[] { "删除", "取消" };
	final String[] arrayFruit4 = new String[] { "举报", "取消" };
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.topic_comment);
		api = new API(getApplicationContext());
		storage = new Storage(getApplicationContext(), "note");
		comment = (TextView) findViewById(R.id.comment);
		View header1 = getLayoutInflater().inflate(R.layout.comment_tip, null);
		commTip = (TextView) header1.findViewById(R.id.comment_tip);
		commTip.setVisibility(View.GONE);
		
		relativeLayout = (RelativeLayout) findViewById(R.id.banner);
		
		
		relativeLayout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
						TopicDetailActivity.this.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);  
				return false;
			}
		});
		
		adapter = new SimpleAdapter(getBaseContext(), commentListMap, R.layout.comment_list,
				new String[] { "content", "top" }, new int[] { R.id.content, R.id.top });
		listView = (XListView) findViewById(R.id.listview);
		listView.setPullLoadEnable(true);
		listView.setPullRefreshEnable(false);
		
		View header2 = getLayoutInflater().inflate(R.layout.topic_detail, null);
		content = (TextView) header2.findViewById(R.id.note_detail_content);
		commCount = (TextView) header2.findViewById(R.id.note_detail_comment_num);
		viewCount = (TextView) header2.findViewById(R.id.note_detail_view_num);
		
		listView.addHeaderView(header2);
		listView.addHeaderView(header1);
		
		listView.setAdapter(adapter);
		listView.setXListViewListener(this);
		listView.setVerticalScrollBarEnabled(false);
		
		header2.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
						TopicDetailActivity.this.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);  
				return false;
			}
		});
		listView.setItemsCanFocus(true);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// MyToast.show(getBaseContext(),commentListMap.get(position).get("top").toString());
				Log.d(TAG, "setOnItemClickListener position="+position);
				if(position < 3){
					return;
				}
				replyTheCommment(position-3);
			}
		});
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				Log.d(TAG, "setOnItemLongClickListener position="+arg2);
				if(arg2 < 3) {
					return false;
				}
				commentDialog(arg2-3);
				return true;
			}
			
		});
		
		Intent it = getIntent();
		if (it != null) {
			if (it.getStringExtra("fromNotify") != null) {
				isFromNotify = true;
				topicId = it.getStringExtra("topicId");
				commRowId = Integer.parseInt(it.getStringExtra("rowId"));
				topicContent = it.getStringExtra("topicContent");
				content.setText(topicContent);
				loadComment(false, false);
				
			} else {
				uid = it.getStringExtra("uid");
				myId = it.getStringExtra("myId");
				topicId = it.getStringExtra("topicId");
				viewNum = it.getStringExtra("viewCnt");
				topicContent = it.getStringExtra("content");
				commentNum = it.getStringExtra("commentCnt");
				Integer viewC = Integer.parseInt(viewNum)+1;
				viewCount.setText(viewC.toString());
				commCount.setText(commentNum);
				content.setText(topicContent);
				
				if (uid.equals(myId)) {
					isMyTopic = true;
				}
				loadComment(false, false);
			}
		}
		
		findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				if (comment.getText().length() < 3) {
//					MyToast.show(getBaseContext(), "评论长度不够");
//					return;
//				}
				if (progressDialog == null || !progressDialog.isShowing()) {
					progressDialog = ProgressDialog.show(TopicDetailActivity.this, "", "正在提交...");
					progressDialog.setCancelable(true);
					progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
						public void onCancel(DialogInterface dialog) {
							progressDialog.cancel();
						}
					});
				}
				if (isRequesting) {
					return;
				}
				isRequesting = true;
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("c", "topic");
				params.put("a", "comment");
				params.put("topicId", topicId);
				
				if (replyUid != null) {
//					params.put("replyCommentId", replyCommentId);
					params.put("replyUid", replyUid);
					params.put("replyRowId", replyRowId);
				} else {
					params.put("replyRowId", "0");
				}
				params.put("comment", comment.getText().toString());
				//reset input
				comment.setText("");
				((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
						TopicDetailActivity.this.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);  
				replyUid = null;
				comment.setHint(R.string.note_activity_comment);
				
				api.post(APIURL.TOPIC_COMMENT, params, new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						isRequesting = false;
						if ( progressDialog != null ) {
							progressDialog.cancel();
						}
						try {
							JSONObject resp = new JSONObject(response);
							JSONObject baseResp = resp.getJSONObject("baseResp");
							int ret = baseResp.getInt("status");
							if (ret == 0) {
								MyToast.show(TopicDetailActivity.this, baseResp.getString("msg"));
							} else if (ret == 100001) {//重新登陆
								Intent intent = new Intent(TopicDetailActivity.this, LoginActivity.class);
								startActivity(intent);
								finish();
							} else {
								MyToast.show(TopicDetailActivity.this,
										baseResp.getString("msg"));
								return;
							}
	
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						loadComment(true,false);
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						isRequesting = false;
						if ( progressDialog != null ) {
							progressDialog.cancel();
						}
						MyToast.show(TopicDetailActivity.this, error.getMessage());
					}
				});

			}
		});
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	private boolean replyTheCommment(int itemPos) {
		replyUid = commentListMap.get(itemPos).get("replyUid").toString();
		replyRowId = commentListMap.get(itemPos).get("rowId").toString();
		if (topicOwnerRowlist.contains(commentListMap.get(itemPos).get("rowId"))) {
			comment.setHint("回复楼主");
		} else {
			comment.setHint("回复" + commentListMap.get(itemPos).get("rowId") + "F");
		}
		((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		comment.requestFocus();
		
		return true;
	}
	private boolean reportThisTopic(){
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("c", "report");
		params.put("a", "reportTopic");
		params.put("topicId", topicId);
		api.get(APIURL.NEWROOT, params, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d(TAG, response);
				JSONObject res;
				try {
					res = new JSONObject(response);
					JSONObject baseResp = res.getJSONObject("baseResp");
					if (baseResp.getInt("status") == 0) {
						MyToast.show(TopicDetailActivity.this, baseResp.getString("msg"));
						finish();
					} else if (baseResp.getInt("status") == 100001) {//重新登陆
						Intent intent = new Intent(TopicDetailActivity.this, LoginActivity.class);
						startActivity(intent);
						finish();
					} else {
						MyToast.show(TopicDetailActivity.this,
								baseResp.getString("msg"));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

			}
		});
		return true;
	}
	
	private boolean removeThisTopic(){
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("c", "topic");
		params.put("a", "deleteTopic");
		params.put("topicId", topicId);
		
		api.get(APIURL.NEWROOT, params, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					Log.d(TAG, response);
					JSONObject res = new JSONObject(response);
					JSONObject baseResp = res.getJSONObject("baseResp");
					if (baseResp.getInt("status") == 0) {
						MyToast.show(TopicDetailActivity.this, baseResp.getString("msg"));
						finish();
					} else if (baseResp.getInt("status") == 100001) {//重新登陆
						Intent intent = new Intent(TopicDetailActivity.this, LoginActivity.class);
						startActivity(intent);
						finish();
					} else {
						MyToast.show(TopicDetailActivity.this,
								baseResp.getString("msg"));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

			}
		});
		finish();
		return true;
	}
	private boolean reportThisComment(String commID) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("c", "report");
		params.put("a", "reportComment");
		params.put("topicId", topicId);
		
		params.put("commentId", commID);
		api.getJson(TopicDetailActivity.this, APIURL.NEWROOT, params, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				try {
					JSONObject baseResp = response.getJSONObject("baseResp");
					if (baseResp.getInt("status") == 0) {
						MyToast.show(TopicDetailActivity.this, baseResp.getString("msg"));
						//MyToast.show(getBaseContext(), "已经提交举报");
						loadComment(false, false);
					} else if (baseResp.getInt("status") == 100001) {//重新登陆
						Intent intent = new Intent(TopicDetailActivity.this, LoginActivity.class);
						startActivity(intent);
						finish();
					} else {
						MyToast.show(TopicDetailActivity.this,
								baseResp.getString("msg"));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}, null);
		
		return true;
	}
	private boolean removeThisComment(String commID){
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("c", "topic");
		params.put("a", "deleteComment");
		params.put("topicId", topicId);
		params.put("commentId", commID);
		
		api.get(APIURL.NEWROOT, params, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					Log.d(TAG, response);
					JSONObject res = new JSONObject(response);
					JSONObject baseResp = res.getJSONObject("baseResp");
					if (baseResp.getInt("status") == 0) {
						MyToast.show(TopicDetailActivity.this, baseResp.getString("msg"));
						Integer commC = Integer.parseInt(commCount.getText().toString());
						commC -= 1;
						commCount.setText(commC.toString());
						loadComment(false,true);
					} else if (baseResp.getInt("status") == 100001) {//重新登陆
						Intent intent = new Intent(TopicDetailActivity.this, LoginActivity.class);
						startActivity(intent);
						finish();
					} else {
						MyToast.show(TopicDetailActivity.this,
								baseResp.getString("msg"));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

			}
		});
		return true;
	}

	void loadComment(final boolean toBottom, boolean isRefresh) {
		HashMap<String, String> params = new HashMap<String, String>();
		if (isRefresh) {
			commPageIndex = "0";
			commentListMap.clear();
		}
		params.put("c", "topic");
		params.put("a", "getCommentList");
		params.put("pageIndex", commPageIndex==null?"0":commPageIndex);
		params.put("topicId", topicId);
		
		api.getJson(TopicDetailActivity.this, APIURL.NEWROOT, params, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject res) {
				try {
					Log.d(TAG, "comment json="+res.toString());
					
					JSONObject baseResp = res.getJSONObject("baseResp");
					int status = baseResp.getInt("status");

					if (status == 100001) {//重新登陆
						Intent intent = new Intent(TopicDetailActivity.this, LoginActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
						startActivity(intent);
						finish();
					} 
					JSONObject topic = res.getJSONObject("topicInfo");
					viewNum = topic.getString("viewCnt");
					commentNum = topic.getString("commentCnt");
					viewCount.setText(viewNum);
					commCount.setText(commentNum);
					
					shareUrl = res.getString("shareUrl");
					
					if (isFromNotify) {//从notify跳转过来的
						uid = topic.getString("uid");
						myId = topic.getString("myId");
						
						if (uid.equals(myId)) {
							isMyTopic = true;
						}
					}
					JSONArray commentList = res.getJSONArray("commentList");
					
					if (!res.getString("pageIndex").equals("0") && commentList.length() > 0) {
						commPageIndex = res.getString("pageIndex");
					} else if (commentList.length() == 0) {
						listView.stopLoadMore();
					}
					
					myCommId = res.getString("myId");
//					commentListMap.clear();
					if (commentList != null && commentList.length() > 0) {
						for (int i = 0; i < commentList.length(); i++) {
							Log.d(TAG, "index "+i);
							JSONObject comment = commentList.getJSONObject(i);
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("content", comment.getString("commentContent"));
							map.put("commentId", comment.getString("commentId"));
							map.put("replyUid", comment.getString("uid"));
							map.put("rowId", comment.getString("rowId"));
							
							if (uid.equals(comment.getString("uid"))) {
								map.put("top", "楼主");
								topicOwnerRowlist.add(comment.getString("rowId"));
							} else {
								map.put("top", comment.getString("rowId") + "F");
							}
							String replyRowid = comment.optString("replyRowId");
							if (!TextUtils.isEmpty(replyRowid) && Integer.parseInt(replyRowid) != 0) {
								
								if (topicOwnerRowlist.contains(replyRowid)) {
									map.put("content", "回复楼主: "+comment.getString("commentContent"));
								} else {
									map.put("content", "回复" + replyRowid + "F: "+comment.getString("commentContent"));
								}
								//map.put("top", map.get("top") + "回复" + comment.optString("replyRowId") + "F");
							}

							commentListMap.add(map);
						}
						Log.d(TAG, "load finish!" );
						adapter.notifyDataSetChanged();
						
						if (toBottom) {
							listView.setSelection(adapter.getCount());
						} else if (commRowId != 0) {//调到相应的comment位置
							Log.d(TAG, "commRowId="+commRowId);
							listView.post(new Runnable() {

						        @Override
						        public void run() {
						        	listView.setSelection(commRowId);
						        }
						    });
							//listView.setSelection(commRowId);
						}
						
						commTip.setVisibility(View.GONE);
					} else if (commentListMap.size() == 0) {
						Log.d(TAG, "no comment");
						commTip.setVisibility(View.VISIBLE);
					}
					listView.stopLoadMore();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				listView.stopLoadMore();
				if (commentListMap.size() == 0) {
					commTip.setVisibility(View.VISIBLE);
					commTip.setText(R.string.comment_list_failed);
				}
				MyToast.show(TopicDetailActivity.this, error.getMessage());
			}
		});
	}
	public void refreshCommlist(View view) {
		loadComment(false, true);
	}
	public void commentDialog(final int itemPos) {
		Log.d(TAG, "itemPos="+itemPos);
		isMyComm = false;
		//is my comment ?
		if(commentListMap.get(itemPos).get("replyUid").toString().equals(myCommId)){
			isMyComm = true;
		}
		String[] showChoices = isMyComm ? arrayFruit3 : arrayFruit4;
		
		Dialog alertDialog = new AlertDialog.Builder(this).setTitle("请选择操作")
				.setItems(showChoices, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							if (isMyComm) {//删除评论
								if (itemPos > commentListMap.size()) {
									MyToast.show(getBaseContext(), "不能删除该评论");
									return;
								}
								
								removeThisComment(commentListMap.get(itemPos).get("commentId").toString());
							} else {//举报评论
								if (itemPos > commentListMap.size()) {
									MyToast.show(getBaseContext(), "不能举报该评论");
									return;
								}
								reportThisComment(commentListMap.get(itemPos).get("commentId").toString());
							}
							
							break;
						default:
							break;
						}
					}
				}).create();

		alertDialog.show();
	}
	
	public void onMoreClicked(View view) {
//		backgroundAlpha(0.5f);
//		noteMorePopupWindow = new NoteMorePopupWindow();
//		noteMorePopupWindow.popAwindow(view, this, popupWinClickListener, new poponDismissListener(), true);
		String[] showTitles = isMyTopic ? arrayFruit1 : arrayFruit2; 
		Dialog alertDialog = new AlertDialog.Builder(this).setTitle("请选择操作")
				.setItems(showTitles, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case 0:
								WXShareManager.getInstance(TopicDetailActivity.this).shareH5ToWX(shareUrl, topicContent, topicContent, false);
								break;
							case 1:
								
								WXShareManager.getInstance(TopicDetailActivity.this).shareH5ToWX(shareUrl, topicContent, topicContent, true);
								break;
							case 2:
								if (isMyTopic) {
									new AlertDialog.Builder(TopicDetailActivity.this)
									.setTitle("提醒")
									.setMessage("确定删除该帖子？")
									.setPositiveButton("确定", new OnClickListener() {
										@Override
										public void onClick(DialogInterface arg0, int arg1) {
											// TODO Auto-generated method stub
											removeThisTopic();
										}
									})
									.setNegativeButton("取消", new OnClickListener() {
										@Override
										public void onClick(DialogInterface arg0, int arg1) {
											// TODO Auto-generated method stub
											return;
										}
									})
									.create()
									.show();
								} else {
									reportThisTopic();
								}
								break;
							default:
								break;
						}
					}
				}).create();

		alertDialog.show();
	}
	  
	private void share() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_SUBJECT, "分享"); 
		List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(intent, 0);
		if (!resInfo.isEmpty()) {
	        List<Intent> targetedShareIntents = new ArrayList<Intent>();
	        for (ResolveInfo info : resInfo) {
	            Intent targeted = new Intent(Intent.ACTION_SEND);
	            targeted.setType("image/*");
	            ActivityInfo activityInfo = info.activityInfo;
	            Log.d(TAG, "app pakage name "+activityInfo.packageName);
	            // judgments : activityInfo.packageName, activityInfo.name, etc.
	            if (activityInfo.packageName.contains("tencent.mm")) {
	            	Log.d(TAG, "tencent app pakage name "+activityInfo.packageName+" app name "+activityInfo.name);
	            	targeted.setPackage(activityInfo.packageName);
	            	targeted.putExtra(Intent.EXTRA_REFERRER, "分享自同事说:\n"+"");
		            targetedShareIntents.add(targeted);
	            }
	        }

	        Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "分享到");
	        if (chooserIntent == null) {
	            return;
	        }
	        //chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[] {}));
	        try {
	            startActivity(chooserIntent);
	        } catch (android.content.ActivityNotFoundException ex) {
	            Toast.makeText(this, "Can't find share component to share", Toast.LENGTH_SHORT).show();
	        }
	    }
	}

	public void backgroundAlpha(float bgAlpha) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.alpha = bgAlpha; // 0.0-1.0
		getWindow().setAttributes(lp);
	}

	class poponDismissListener implements PopupWindow.OnDismissListener {

		@Override
		public void onDismiss() {
			Log.d(TAG, "popup window dismiss!");
			backgroundAlpha(1f);
		}

	} 

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		loadComment(true, false);
		isLoadMore = true;
	}
}
