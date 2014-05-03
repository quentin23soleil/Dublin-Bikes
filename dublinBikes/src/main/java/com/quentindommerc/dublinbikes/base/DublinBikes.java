package com.quentindommerc.dublinbikes.base;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.loopj.android.http.RequestParams;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.PushService;
import com.parse.SaveCallback;
import com.quentindommerc.dublinbikes.R;
import com.quentindommerc.dublinbikes.activity.AlertNotification;
import com.quentindommerc.dublinbikes.activity.Home;
import com.quentindommerc.dublinbikes.interfaces.OnApiFinished;
import com.quentindommerc.dublinbikes.utils.Api;
import com.quentindommerc.dublinbikes.utils.Utils;

/**
 * Created by kentin on 30/04/14.
 */
public class DublinBikes extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "Q4YKMk5SO64YBZIiUaWI0miLtgA9GcsFP0XlrmPJ", "r88o69d5z57zQOJ52kE28V2ucie8fg8vuZFRmhGV");
        PushService.setDefaultPushCallback(this, AlertNotification.class, R.drawable.ic_cycle_notif);
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        if (Utils.getEmail(this) != null)
            installation.put("email", Utils.getEmail(this));
        installation.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                String deviceToken = (String) ParseInstallation.getCurrentInstallation().get("deviceToken");
                if (Utils.getEmail(DublinBikes.this) != null)
                    sendRegistrationIdToBackend(deviceToken);

            }
        });
        Crashlytics.start(this);
    }

    private void sendRegistrationIdToBackend(String token) {
        if (Utils.getBooleanSharedPref(this, "registered"))
            return;
        Utils.setBooleanSharedPref(this, "registered", true);
        RequestParams params = new RequestParams();
        params.put("method", "registerId");
        params.put("parseId", token);
        params.put("email", Utils.getEmail(this));
        new Api().execute("dublin.php", params, new OnApiFinished() {

            @Override
            public void success(String json) {
                Utils.log("REGISTERED ON SERVER ? " + json);
            }

            @Override
            public void error(String error) {
            }
        });
    }

}
