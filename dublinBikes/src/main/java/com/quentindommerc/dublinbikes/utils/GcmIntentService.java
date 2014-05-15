package com.quentindommerc.dublinbikes.utils;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quentindommerc.dublinbikes.R;
import com.quentindommerc.dublinbikes.activity.DetailStation;
import com.quentindommerc.dublinbikes.bean.Station;
import com.quentindommerc.dublinbikes.utils.DeleteNotification;
import com.quentindommerc.dublinbikes.utils.Utils;

public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	private Station station;
	private static ArrayList<Station> stations;

	NotificationCompat.Builder builder;

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (stations == null
				|| !getSharedPreferences("Bikes", Context.MODE_PRIVATE)
						.getBoolean("clearNotif", false)) {

			SharedPreferences.Editor editor = getSharedPreferences("Bikes",
					Context.MODE_PRIVATE).edit();
			editor.putBoolean("clearNotif", true);
			editor.commit();
			stations = new ArrayList<Station>();
		}
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {
			station = new Station();
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {
				Utils.log(extras.getString("message"));
				try {
					JSONObject obj = new JSONObject(extras.getString("message"));
					station.setName(obj.optString("name"));
					station.setTimestamp(obj.optString("timestamp"));
					station.setId(obj.optString("id"));
					station.setIdx(obj.optString("idx"));
					station.setBikes(obj.optInt("bikes"));
					station.setFree(obj.optInt("free"));
					station.setLatitude(obj.optDouble("lat") / 1000000);
					station.setLongitude(obj.optDouble("lng") / 1000000);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				sendNotification(station);
			}
		}
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void sendNotification(Station s) {
		stations.add(s);
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(this, DetailStation.class);
		intent.putExtra("station", s);
		intent.putExtra("notification", true);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent intent2 = new Intent(this, DeleteNotification.class);
		PendingIntent deleteIntent = PendingIntent.getBroadcast(this, 0,
				intent2, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_cycle_notif).setContentTitle(
				"Dublin Bikes alert");

		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
		inboxStyle.setBigContentTitle("Dublin Bikes alerts");
		inboxStyle.setSummaryText(getString(R.string.move_now));
		if (stations.size() > 1) {
			mBuilder.setContentTitle(stations.size()
					+ getApplicationContext().getResources().getQuantityString(
							R.plurals.alerts_nb, stations.size()));
			mBuilder.setContentText(s.getName());
		} else {
			mBuilder.setContentTitle(station.getName());
			mBuilder.setContentText(Utils.getQuantityString(R.plurals.notif,
					R.string.notif, station.getBikes(), this));
		}
		for (Station station : stations) {
			inboxStyle.addLine(station.getName()
					+ " : "
					+ Utils.getQuantityString(R.plurals.notif, R.string.notif,
							station.getBikes(), this));
		}
		mBuilder.setStyle(inboxStyle);
		mBuilder.setAutoCancel(true);
		mBuilder.setDeleteIntent(deleteIntent);
		mBuilder.setContentIntent(contentIntent);
		mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);

		Uri alarmSound = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		mBuilder.setSound(alarmSound);

		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}
