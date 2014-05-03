package com.quentindommerc.dublinbikes.bean;


public class MenuItem {

	private String mTitle;
	private int mIcon;

	public MenuItem(String title, int icon) {
		mTitle = title;
		mIcon = icon;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public int getIcon() {
		return mIcon;
	}

	public void setIcon(int icon) {
		mIcon = icon;
	}

}
