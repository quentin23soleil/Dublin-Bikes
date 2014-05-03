package com.quentindommerc.dublinbikes.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.doomonafireball.betterpickers.recurrencepicker.EventRecurrence;
import com.doomonafireball.betterpickers.timepicker.TimePickerBuilder;
import com.doomonafireball.betterpickers.timepicker.TimePickerDialogFragment;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.loopj.android.http.RequestParams;
import com.quentindommerc.dublinbikes.R;
import com.quentindommerc.dublinbikes.base.BaseActivity;
import com.quentindommerc.dublinbikes.bean.Station;
import com.quentindommerc.dublinbikes.interfaces.OnApiFinished;
import com.quentindommerc.dublinbikes.utils.Api;
import com.quentindommerc.dublinbikes.utils.EventRecurrenceFormatter;
import com.quentindommerc.dublinbikes.utils.Utils;
import com.quentindommerc.dublinbikes.views.RecurrencePickerDialog;

import java.util.HashMap;

public class AddAlert extends BaseActivity {

    private static final int REFERENCE_START = 0;
    private static final int REFERENCE_END = 1;
    private static final String FRAG_TAG_RECUR_PICKER = "recurrencePickerDialogFragment";
    private Api api;
    public static Button start_time;
    public static Button end_time;
    private Button min_bikes;
    public static boolean START;
    public static String start;
    public static String end;
    private Station station;
    private String days;
    private Button repeat;
    private EventRecurrence mEventRecurrence = new EventRecurrence();
    private String mRrule;
    private int mMinBikes = -1;

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);

        Tracker tracker = GoogleAnalytics.getInstance(this).getTracker("UA-45191498-1");

        HashMap<String, String> hitParameters = new HashMap<String, String>();
        hitParameters.put(Fields.EVENT_LABEL, station.getName());
        hitParameters.put(Fields.SCREEN_NAME, "Add Alert");

        tracker.send(hitParameters);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this); // Add this method.
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alert);
        getActionBar().setTitle(getString(R.string.add_alert_title));
        getActionBar().setDisplayHomeAsUpEnabled(true);
        api = new Api();
        days = "";
        station = (Station) getIntent().getExtras().get("station");
        start_time = (Button) findViewById(R.id.start_time);
        end_time = (Button) findViewById(R.id.end_time);
        repeat = (Button) findViewById(R.id.repeat);
        min_bikes = (Button) findViewById(R.id.number_bikes);

        if (!Utils.getBooleanSharedPref(this, "help_alert"))
            showHelp();

        start_time.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                TimePickerBuilder dpb = new TimePickerBuilder().setFragmentManager(getSupportFragmentManager())
                        .setStyleResId(R.style.BetterPickersDialogFragment).setReference(REFERENCE_START).addTimePickerDialogHandler(new TimePickerDialogFragment.TimePickerDialogHandler() {
                            @Override
                            public void onDialogTimeSet(int reference, int hourOfDay, int minute) {
                                start = String.format("%02d", hourOfDay) + ":"
                                        + String.format("%02d", minute);
                                start_time.setText(getString(R.string.start_f,start));
                            }
                        });
                dpb.show();
            }
        });

        end_time.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                TimePickerBuilder dpb = new TimePickerBuilder().setFragmentManager(getSupportFragmentManager())
                        .setStyleResId(R.style.BetterPickersDialogFragment).setReference(REFERENCE_END).addTimePickerDialogHandler(new TimePickerDialogFragment.TimePickerDialogHandler() {
                            @Override
                            public void onDialogTimeSet(int reference, int hourOfDay, int minute) {
                                end = String.format("%02d", hourOfDay) + ":"
                                        + String.format("%02d", minute);
                                end_time.setText(getString(R.string.end_f, end));
                            }
                        });
                dpb.show();
            }
        });

        repeat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                Bundle b = new Bundle();
                Time t = new Time();
                t.setToNow();
                b.putLong(RecurrencePickerDialog.BUNDLE_START_TIME_MILLIS, t.toMillis(false));
                b.putString(RecurrencePickerDialog.BUNDLE_TIME_ZONE, t.timezone);
                b.putString(RecurrencePickerDialog.BUNDLE_RRULE, "FREQ=WEEKLY");

                RecurrencePickerDialog rpd = (RecurrencePickerDialog) fm.findFragmentByTag(
                        FRAG_TAG_RECUR_PICKER);
                if (rpd != null) {
                    rpd.dismiss();
                }
                rpd = new RecurrencePickerDialog();
                rpd.setArguments(b);
                rpd.setOnRecurrenceSetListener(new RecurrencePickerDialog.OnRecurrenceSetListener() {
                    @Override
                    public void onRecurrenceSet(String rrule) {
                        mRrule = rrule;
                        if (mRrule != null) {
                            mEventRecurrence.parse(mRrule);
                        }
                        populateRepeats();
                    }
                });
                rpd.show(fm, FRAG_TAG_RECUR_PICKER);
            }
        });

        min_bikes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                NumberPickerBuilder npb = new NumberPickerBuilder().setFragmentManager(getSupportFragmentManager()).addNumberPickerDialogHandler(new NumberPickerDialogFragment.NumberPickerDialogHandler() {
                    @Override
                    public void onDialogNumberSet(int i, int i2, double v, boolean b, double v2) {
                        min_bikes.setText(getString(R.string.nb_bikes_f, i2));
                        mMinBikes = i2;
                    }
                }).setStyleResId(R.style.BetterPickersDialogFragment);
                npb.show();
            }
        });

    }

    private void showHelp() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setTitle(getString(R.string.add_alert_title));
        alertDialogBuilder
                .setMessage(getString(R.string.add_alert_help))
                .setCancelable(true)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        Utils.setBooleanSharedPref(this, "help_alert", true);
    }


    private void populateRepeats() {
        Resources r = getResources();
        String repeatString = "";
        boolean enabled;
        if (!TextUtils.isEmpty(mRrule)) {
            repeatString = EventRecurrenceFormatter.getRepeatString(this, r, mEventRecurrence, true);
        }
        days = repeatString;
        repeat.setText(repeatString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_alert, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.validate:
                if (start_time.getText().equals(getString(R.string.start))
                        || end_time.getText().equals(getString(R.string.end))
                        || mMinBikes == -1
                        || days.length() == 0) {
                    Toast.makeText(this,
                            getString(R.string.fill_all),
                            Toast.LENGTH_LONG).show();
                    return false;
                }
                RequestParams params = new RequestParams();
                params.put("method", "addAlertWithRepeat");
                params.put("email", Utils.getEmail(this));
                params.put("station_id", station.getIdx() + "");
                params.put("hour_start", Utils.timeToParisTime(start) + ":00");
                params.put("hour_end", Utils.timeToParisTime(end) + ":00");
                params.put("min_bikes", String.valueOf(mMinBikes));
                params.put("repeatDays", days);
                api.execute("dublin.php", params, new OnApiFinished() {

                    @Override
                    public void success(String json) {
                        if (json.contains("Success")) {
                            Toast.makeText(AddAlert.this,
                                    getString(R.string.alert_success),
                                    Toast.LENGTH_LONG).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(AddAlert.this, "Error. Investigating.",
                                    Toast.LENGTH_LONG).show();
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    }

                    @Override
                    public void error(String error) {
                        Toast.makeText(AddAlert.this, "Error. Investigating.",
                                Toast.LENGTH_LONG).show();
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;

            default:
                break;
        }

        return true;
    }
}
