package com.startup.sayhi.model;

import java.util.List;

public class TopicModel {
	private int userId;
	private int companyId;
	private String content;
	private int viewCnt;
	private int commentCnt;
	private int reportCnt;
	private String timeStm;
	private List<String> pictures;
	
	public List<String> getPictures() {
		return pictures;
	}
	public void setPictures(List<String> pictures) {
		this.pictures = pictures;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public int getCompanyId() {
		return companyId;
	}
	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getViewCnt() {
		return viewCnt;
	}
	public void setViewCnt(int viewCnt) {
		this.viewCnt = viewCnt;
	}
	public int getCommentCnt() {
		return commentCnt;
	}
	public void setCommentCnt(int commentCnt) {
		this.commentCnt = commentCnt;
	}
	public int getReportCnt() {
		return reportCnt;
	}
	public void setReportCnt(int reportCnt) {
		this.reportCnt = reportCnt;
	}
	public String getTimeStm() {
		return timeStm;
	}
	public void setTimeStm(String timeStm) {
		this.timeStm = timeStm;
	}
	
}
