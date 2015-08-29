package com.startup.sayhi.model;

public class Comment {
	private String commentId;
	private String userId;
	private String replyUId;
	private String content;
	
	public String getCommentId() {
		return commentId;
	}
	public void setCommentId(String commentId) {
		this.commentId = commentId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getReplyUId() {
		return replyUId;
	}
	public void setReplyUId(String replyUId) {
		this.replyUId = replyUId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
