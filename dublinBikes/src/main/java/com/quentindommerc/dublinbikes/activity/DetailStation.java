package com.quentindommerc.dublinbikes.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.quentindommerc.dublinbikes.R;
import com.quentindommerc.dublinbikes.bdd.BookmarkBdd;
import com.quentindommerc.dublinbikes.bean.Station;
import com.quentindommerc.dublinbikes.interfaces.OnApiFinished;
import com.quentindommerc.dublinbikes.utils.Api;
import com.quentindommerc.dublinbikes.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class DetailStation extends Activity {

    private TextView spots;
    private TextView bikes;
    private TextView name;
    private Station station;
    private GoogleMap gMap;
    private TextView updated;
    private MapFragment mapFrag;
    private FrameLayout mapFrame;
    private boolean expanded;

    private Api api;
    private BookmarkBdd bdd;
    private SwipeRefreshLayout mPullToRefreshLayout;
    private MenuItem mBookmarkMenuItem;

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this); // Add this method.

        Tracker tracker = GoogleAnalytics.getInstance(this).getTracker("UA-45191498-1");

        HashMap<String, String> hitParameters = new HashMap<String, String>();
        hitParameters.put(Fields.EVENT_LABEL, station.getName());
        hitParameters.put(Fields.SCREEN_NAME, "Detail station");

        tracker.send(hitParameters);
    }

    @Override
    public void onStop() {
        super.onStop();
        bdd.close();
        EasyTracker.getInstance(this).activityStop(this); // Add this method.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail, menu);
        mBookmarkMenuItem = menu.findItem(R.id.bookmark);

        if (bdd.isBookmark(station))
            mBookmarkMenuItem.setIcon(R.drawable.ic_star_full);
        else
            mBookmarkMenuItem.setIcon(R.drawable.ic_star);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.alert:
//                addAlert(null);
//                break;
            case R.id.refresh:
                refresh();
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.bookmark:
                bookmark();
                break;
            default:
                break;
        }
        return true;
    }

    private void bookmark() {
        if (bdd.isBookmark(station)) {
            mBookmarkMenuItem.setIcon(R.drawable.ic_star);
            bdd.removeStation(station);
        }
        else {
            mBookmarkMenuItem.setIcon(R.drawable.ic_star_full);
            bdd.addBookmark(station);
        }
    }

    private void refresh() {
        mPullToRefreshLayout.setRefreshing(true);
        api.execute("dublin.php?method=getWithIdx&idx=" + station.getId(), null,
                new OnApiFinished() {

                    @Override
                    public void success(String json) {
                        try {
                            JSONObject obj = new JSONObject(json);
                            station.setBikes(obj.optInt("bikes"));
                            station.setFree(obj.optInt("free"));
                            station.setTimestamp(obj.optString("timestamp"));
                            refreshView(station);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void error(String error) {
                    }
                });
    }

    protected void refreshView(Station s) {
        mPullToRefreshLayout.setRefreshing(false);
        name.setText(station.getName());
        spots.setText(Utils.getQuantityString(R.plurals.spots, R.string.spot_zero,
                station.getFree(), DetailStation.this));
        bikes.setText(Utils.getQuantityString(R.plurals.bikes, R.string.bike_zero,
                station.getBikes(), DetailStation.this));
        name.setText(station.getName());
        updated.setText(Utils.updateTimeToString(station.getTimestamp(), "dd MMMM yyyy hh:mm:ss",
                DetailStation.this));
        gMap.clear();
        MarkerOptions m = new MarkerOptions().position(new LatLng(station.getLatitude(), station
                .getLongitude()));
        setSnippet(station, m);
        m.title(station.getName());
        gMap.addMarker(m);
    }

    private MarkerOptions setSnippet(Station station, MarkerOptions m) {
        m.snippet(Utils.getQuantityString(R.plurals.bikes_available, R.string.bikes_no,
                station.getBikes(), DetailStation.this)
                + " & "
                + Utils.getQuantityString(R.plurals.free_spots, R.string.spot_zero,
                station.getFree(), DetailStation.this));
        return m;
    }

    @Override
    protected void onResume() {
        super.onResume();
        bdd.open();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_station);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mPullToRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.ptr_layout);
        mPullToRefreshLayout.setEnabled(false);

        api = new Api();
        bdd = new BookmarkBdd(this);
        name = (TextView) findViewById(R.id.station_name);
        spots = (TextView) findViewById(R.id.spots);
        bikes = (TextView) findViewById(R.id.bikes);
        updated = (TextView) findViewById(R.id.updated);
        station = getIntent().getExtras().getParcelable("station");
        getActionBar().setTitle(station.getName());
        if (getIntent().getExtras().getBoolean("notification", false)) {
            SharedPreferences.Editor editor = getSharedPreferences("Bikes", Context.MODE_PRIVATE)
                    .edit();
            editor.remove("clearNotif");
            editor.commit();
        }

        name.setText(station.getName());
        spots.setText(Utils.getQuantityString(R.plurals.spots, R.string.spot_zero,
                station.getFree(), DetailStation.this));
        bikes.setText(Utils.getQuantityString(R.plurals.bikes, R.string.bike_zero,
                station.getBikes(), DetailStation.this));
        name.setText(station.getName());
        updated.setText(Utils.updateTimeToString(station.getTimestamp(), "dd MMMM yyyy HH:mm:ss",
                DetailStation.this));
        mapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        gMap = mapFrag.getMap();
        mapFrame = (FrameLayout) findViewById(R.id.mapFrame);
        CameraUpdate position = CameraUpdateFactory.newLatLngZoom(new LatLng(station.getLatitude(),
                station.getLongitude()), 15);
        gMap.animateCamera(position);
        gMap.setMyLocationEnabled(true);
        gMap.getUiSettings().setZoomControlsEnabled(false);

        MarkerOptions m = new MarkerOptions().position(new LatLng(station.getLatitude(), station
                .getLongitude()));
        setSnippet(station, m);
        m.title(station.getName());
        gMap.addMarker(m);

    }

    public void expandB(View v) {
        mapFrag.getView().setVisibility(View.INVISIBLE);
        if (expanded == false)
            expand(mapFrame);
        else
            collapse(mapFrame);
    }

    public void expand(final View v) {
        expanded = true;
        v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        final int targtetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                // Animation :
                // v.getLayoutParams().height = interpolatedTime == 1 ?
                // LayoutParams.WRAP_CONTENT
                // : (int) (targtetHeight * interpolatedTime);

                v.getLayoutParams().height = LayoutParams.WRAP_CONTENT; // no
                // animation
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int) (targtetHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mapFrag.getView().setVisibility(View.VISIBLE);
                gMap.getUiSettings().setZoomControlsEnabled(true);
            }
        });
        v.startAnimation(a);
    }

    public void collapse(final View v) {
        expanded = false;
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = Utils.pxToDp(getApplicationContext(), getResources().getInteger(R.integer.map_size));
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mapFrag.getView().setVisibility(View.VISIBLE);
                gMap.getUiSettings().setZoomControlsEnabled(false);
            }
        });
        v.startAnimation(a);

    }

    public void addAlert(View v) {
//        if (Utils.getEmail(this).equals("dommer.q@gmail.com")) {
            Intent intent = new Intent(this, AddAlert.class);
            intent.putExtra("station", station);
            startActivity(intent);
//        } else {
//            Toast.makeText(this, "This feature is not ready yet. Stay tuned.", Toast.LENGTH_LONG)
//                    .show();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        bdd.close();
    }

    public void addToBookmark(View v) {
        if (bdd.isBookmark(station)) {
            bdd.removeStation(station);
        } else {
            bdd.addBookmark(station);
        }
    }
}
