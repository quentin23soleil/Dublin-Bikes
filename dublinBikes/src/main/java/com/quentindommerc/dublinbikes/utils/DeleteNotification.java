package com.quentindommerc.dublinbikes.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class DeleteNotification extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences.Editor editor = context.getSharedPreferences("Bikes",
				Context.MODE_PRIVATE).edit();
		editor.remove("clearNotif");
		editor.commit();
	}

}
