package com.quentindommerc.dublinbikes.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.quentindommerc.dublinbikes.R;
import com.quentindommerc.dublinbikes.activity.DetailStation;
import com.quentindommerc.dublinbikes.adapter.StationListAdapter;
import com.quentindommerc.dublinbikes.base.BaseFragment;
import com.quentindommerc.dublinbikes.bdd.BookmarkBdd;
import com.quentindommerc.dublinbikes.bean.Station;
import com.quentindommerc.dublinbikes.interfaces.LocationCallback;
import com.quentindommerc.dublinbikes.interfaces.OnApiFinished;
import com.quentindommerc.dublinbikes.utils.Api;
import com.quentindommerc.dublinbikes.utils.Constants;
import com.quentindommerc.dublinbikes.utils.Location;
import com.quentindommerc.dublinbikes.utils.Utils;
import com.quentindommerc.superlistview.SuperListview;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ListFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener {

    private static ListFragment fragment;
    private ArrayList<Station> stations;
    private int mode;
    private Location l;
    private android.location.Location location;
    private BookmarkBdd bdd;
    private SuperListview mList;


    private StationListAdapter mAdapter;

    private void updateList(boolean clean) {
        if (clean) {
            updateAdapter(Utils.updateStationsAdpater(location, mAdapter, getActivity()));
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mList != null && mAdapter != null) {
                    mList.setAdapter(mAdapter);
                }
                if (mList != null && mList.getSwipeToRefresh() != null || (mAdapter != null && mAdapter.getCount() == 0))
                    mList.getSwipeToRefresh().setRefreshing(false);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mList != null && mList.getSwipeToRefresh() != null)
            mList.getSwipeToRefresh().setRefreshing(true);
        bdd = new BookmarkBdd(getActivity());
        bdd.open();
        mAdapter = new StationListAdapter(getActivity());
        stations = new ArrayList<Station>();
        l = new Location(getActivity(), new LocationCallback() {

            @Override
            public void requestedLocationPlayServicesFailed() {
            }

            @Override
            public void playServicesConnectionStatus(int connectionStatus) {
                l.requestLocationUpdates(false, 1000, 100, true);
                if (l.getLastLocation() != null) {
                    location = l.getLastLocation();
                    if (mList.getSwipeToRefresh() != null)
                        mList.getSwipeToRefresh().setRefreshing(true);
                    if (stations != null && stations.size() > 0) {
                        updateList(true);
                    } else {
                        stations = new ArrayList<Station>();
                        if (mAdapter == null)
                            mAdapter = new StationListAdapter(getActivity());
                    }
                } else {
                }
            }

            @Override
            public void locationReceived(android.location.Location newLocation) {
                location = newLocation;
                if (mList != null && mList.getSwipeToRefresh() != null)
                    mList.getSwipeToRefresh().setRefreshing(true);
                if (stations != null && stations.size() > 0) {
                    updateList(true);
                } else {
                    stations = new ArrayList<Station>();
                    if (mAdapter == null)
                        mAdapter = new StationListAdapter(getActivity());
                }
            }
        });
    }

    private void updateAdapter(ArrayList<Station> updateStationsAdpater) {
        mAdapter.clear();
        for (int i = 0; i < updateStationsAdpater.size(); i++) {
            Station card = updateStationsAdpater.get(i);
            mAdapter.add(card);
        }
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        stations = args.getParcelableArrayList("stations");
        mode = args.getInt("mode");

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (stations != null) {
                    for (int i = 0; i < stations.size(); i++) {
                        if (stations.get(i).getFree() == 0 && mode == Constants.LEAVE_BIKE_MODE) {
                            stations.remove(i);
                        } else if (stations.get(i).getBikes() == 0 && mode == Constants.WANT_BIKE_MODE) {
                            stations.remove(i);
                        } else {
                        }
                        updateList(false);
                    }
                } else {
                    stations = new ArrayList<Station>();
                    mode = Constants.LIST_MODE;
                }
            }
        }).start();
    }

    private void tracker(String s) {
        Tracker tracker = GoogleAnalytics.getInstance(getActivity()).getTracker("UA-45191498-1");

        HashMap<String, String> hitParameters = new HashMap<String, String>();
        hitParameters.put(Fields.SCREEN_NAME, s);

        tracker.send(hitParameters);
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(getActivity()).activityStart(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();

        EasyTracker.getInstance(getActivity()).activityStop(getActivity());
    }

    private void getStations() {
        stations.clear();
        Api api = new Api();
        String url = "dublin.json";
        if (mode == Constants.BOOKMARK_MODE)
            url = "dublin.php?method=getWithIdxs&idxs=" + bdd.getStationIds();
        Utils.log(url);
        api.execute(url, null, new OnApiFinished() {

            @Override
            public void success(String json) {
                try {
                    JSONArray array = new JSONArray(json);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject stationJson = array.optJSONObject(i);
                        Station s = Api.parseStation(stationJson, mode);
                        if ((mode == Constants.LIST_MODE) || (mode == Constants.BOOKMARK_MODE)) {
                            stations.add(s);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mList.getSwipeToRefresh().setRefreshing(false);
                mAdapter.clear();
                mAdapter.addAll(stations);
                updateList(true);
            }

            @Override
            public void error(String error) {
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.f_list, null);
        mList = (SuperListview) v.findViewById(R.id.list);
        mList.setOnItemClickListener(this);
        mList.setRefreshListener(this);
        mAdapter = new StationListAdapter(getActivity());
        getStations();
        return v;
    }

    public static ListFragment newInstance(ArrayList<Station> stations, int listMode) {
        if (fragment == null) {
            fragment = new ListFragment();
            Bundle b = new Bundle();
            b.putParcelableArrayList("stations", stations);
            b.putInt("mode", listMode);
            fragment.setArguments(b);
        }
        return fragment;
    }

    @Override
    public void onPause() {
        if (l != null) {
            l.stopRequestingUpdates();
        }
        bdd.close();
        super.onPause();
    }

    @Override
    public void onResume() {
        getActivity().getActionBar().show();
        super.onResume();
        if (l != null) {
            l.requestLocationUpdates(true, 1000, 0, true);
        }
        bdd.open();


        switch (mode) {
            case Constants.LIST_MODE:
                tracker("stations list");
                break;
            case Constants.BOOKMARK_MODE:
                tracker("bookmarks list");
        }

    }

    @Override
    public String getTitle() {
        return getString(R.string.station_list);
    }

    @Override
    public void onRefresh() {
        getStations();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent i = new Intent(getActivity(), DetailStation.class);
        i.putExtra("station", mAdapter.getItem(position));
        startActivity(i);
    }
}
