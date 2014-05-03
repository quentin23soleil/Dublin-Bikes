package com.quentindommerc.dublinbikes.interfaces;

public interface OnApiFinished {
	public void success(String json);

	public void error(String error);
}
