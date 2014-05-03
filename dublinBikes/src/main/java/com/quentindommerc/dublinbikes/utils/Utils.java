package com.quentindommerc.dublinbikes.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.location.Location;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.loopj.android.http.RequestParams;
import com.quentindommerc.dublinbikes.R;
import com.quentindommerc.dublinbikes.activity.Home;
import com.quentindommerc.dublinbikes.adapter.StationListAdapter;
import com.quentindommerc.dublinbikes.bean.Station;
import com.quentindommerc.dublinbikes.interfaces.OnApiFinished;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {

	static AlertDialog d;

	public static void log(String log) {
		if (Constants.DEBUG)
			Log.d("Bike", log);
	}

	public static String getEmail(Context context) {
		AccountManager accountManager = AccountManager.get(context);
		Account account = getAccount(accountManager);

		if (account == null) {
			return null;
		} else {
			return account.name;
		}
	}

	private static Account getAccount(AccountManager accountManager) {
		Account[] accounts = accountManager.getAccountsByType("com.google");
		Account account;
		if (accounts.length > 0) {
			account = accounts[0];
		} else {
			account = null;
		}
		return account;
	}

	public static int pxToDp(Context ct, int dp) {
		Resources r = ct.getResources();
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				r.getDisplayMetrics());
		return px;
	}

	public static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	public static boolean isSameDay(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		return isSameDay(cal1, cal2);
	}

	public static boolean isSameDay(Calendar cal1, Calendar cal2) {
		if (cal1 == null || cal2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
				&& cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1
					.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
	}

	public static Calendar toCalendar(final String iso8601string) throws ParseException {
		Calendar calendar = GregorianCalendar.getInstance();
		String s = iso8601string.replace("Z", "+00:00");
		try {
			s = s.substring(0, 22) + s.substring(23);
		} catch (IndexOutOfBoundsException e) {
			throw new ParseException("Invalid length", 0);
		}
		Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH).parse(s);
		calendar.setTime(date);
		return calendar;
	}

	public static String formatDate(String format, Date d) {
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Dublin"));
		return sdf.format(d);
	}

	public static String updateTimeToString(String time, String outFormat, Context ct) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date d = null;

		try {
			d = format.parse(time);
			if (isSameDay(d, new Date()))
				return ct.getResources().getString(R.string.server_update_today,
						formatDate("HH:mm:ss", d));
			return ct.getResources().getString(R.string.server_update, formatDate(outFormat, d));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "error getting updated time";
	}

	public static String isKmOrM(float distance) {
		if ((distance / 1000) >= 1.0f)
			return String.format("%.02f", (distance / 1000)) + " km";
		return String.format("%.02f", distance) + " m";
	}

	public static String getQuantityString(int resQ, int res, int qty, Context ct) {
		if (ct != null) {
			if (qty > 0) {
				return ct.getResources().getQuantityString(resQ, qty, qty);
			} else {
				if (res == R.string.bikes_no || res == R.string.spot_no || res == R.string.notif)
					return ct.getResources().getString(res);
				else
					return qty + " " + ct.getResources().getString(res);
			}
		}
		return "error";
	}

	public static String getDistance(double lat1, double lng1, double lat2, double lng2,
			boolean inMeter) {
		float distance = 0.0f;

		Location a = new Location("user");
		a.setLatitude(lat1);
		a.setLongitude(lng1);
		Location b = new Location("bike spot");
		b.setLatitude(lat2);
		b.setLongitude(lng2);
		distance = a.distanceTo(b);
		if (!inMeter)
			return isKmOrM(distance);
		return distance + "";
	}

	public static ArrayList<Station> updateStationsAdpater(android.location.Location newLocation,
			StationListAdapter adapter, final Context ct) {
		ArrayList<Station> stations = new ArrayList<Station>();
		for (int i = 0; i < adapter.getCount(); i++) {
			Station item = adapter.getItem(i);
			if (newLocation != null) {
				item.setDistance(Utils.getDistance(newLocation.getLatitude(),
						newLocation.getLongitude(), item.getLatitude(), item.getLongitude(), false));
				item.setDistanceMeter(Float.valueOf(Utils.getDistance(newLocation.getLatitude(),
						newLocation.getLongitude(), item.getLatitude(), item.getLongitude(), true)));
			} else {
				item.setDistance("Calculating...");
				item.setDistanceMeter(0.0f);

			}
			// Utils.log(item.getDistance());
			stations.add(item);
		}
		Collections.sort(stations, new StationComparator());
		return stations;
	}

	private static SharedPreferences getGCMPreferences(Context context) {
		return context.getSharedPreferences(Home.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	public static void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = Utils.getAppVersion(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Constants.PROPERTY_REG_ID, regId);
		editor.putInt(Constants.PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	public static String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(Constants.PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Utils.log("Registration not found.");
			return "";
		}
		int registeredVersion = prefs.getInt(Constants.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = Utils.getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Utils.log("App version changed.");
			return "";
		}
		return registrationId;
	}

	public static String timeToParisTime(String start) {
		Calendar c = Calendar.getInstance(TimeZone.getDefault());
		c.setTime(new Date());
		c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
				Integer.parseInt(start.substring(0, 2)), Integer.parseInt(start.substring(3, 5)), 0);
		Date startD = c.getTime();
		SimpleDateFormat paris = new SimpleDateFormat("HH:mm", Locale.FRANCE);
		paris.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
		return paris.format(startD);
	}

	public static void sendReport(final Context ct) {
		final View v = LayoutInflater.from(ct).inflate(R.layout.report_dialog, null);
		AlertDialog.Builder b = new AlertDialog.Builder(ct);
		b.setTitle("Report a bug");
		b.setView(v);
		b.setPositiveButton(ct.getString(R.string.send), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText input = (EditText) v.findViewById(R.id.editText1);
				Api api = new Api();
				RequestParams params = new RequestParams();
				params.put("method", "report");
				params.put("reported", input.getText().toString());
				params.put("regId", Utils.getRegistrationId(ct));
				api.execute("dublin.php", params, new OnApiFinished() {

					@Override
					public void success(String json) {
					}

					@Override
					public void error(String error) {
					}
				});
			}
		});
		if (d != null && !d.isShowing())
			d = b.show();
		if (d == null)
			d = b.show();
	}

    public static Boolean getBooleanSharedPref(Context ct, String key) {
        SharedPreferences pref = ct.getSharedPreferences("Dublin Bikes", Context.MODE_PRIVATE);
        return pref.getBoolean(key, false);
    }

    public static void setBooleanSharedPref(Context ct, String key, boolean value) {
        SharedPreferences.Editor pref = ct.getSharedPreferences("Dublin Bikes", Context.MODE_PRIVATE).edit();
        pref.putBoolean(key, value);
        pref.commit();
    }

    public static void playServicesCheck(final Activity ct) {
        int error = GooglePlayServicesUtil.isGooglePlayServicesAvailable(ct);
        GooglePlayServicesUtil.getErrorDialog(error, ct, 0, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(ct, ct.getString(R.string.sorry_services), Toast.LENGTH_LONG).show();
                ct.finish();
            }
        }).show();


    }
}
