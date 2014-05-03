package com.quentindommerc.dublinbikes.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class BaseActivity extends FragmentActivity {

	public boolean isActive;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		isActive = true;
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onPause() {
		isActive = false;
		super.onPause();
	}

	@Override
	protected void onResume() {
		isActive = true;
		super.onResume();
	}

	@Override
	protected void onStop() {
		isActive = false;
		super.onStop();
	}
}
