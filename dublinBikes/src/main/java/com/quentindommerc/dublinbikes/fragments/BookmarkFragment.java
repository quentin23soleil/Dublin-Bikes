package com.quentindommerc.dublinbikes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.quentindommerc.dublinbikes.R;
import com.quentindommerc.dublinbikes.base.BaseFragment;
import com.quentindommerc.dublinbikes.bdd.BookmarkBdd;
import com.quentindommerc.dublinbikes.bean.Station;
import com.quentindommerc.dublinbikes.interfaces.OnApiFinished;
import com.quentindommerc.dublinbikes.utils.Api;
import com.quentindommerc.dublinbikes.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class BookmarkFragment extends BaseFragment {

    private static BookmarkFragment fragment;
    private Api api;
    private BookmarkBdd bdd;
    private ArrayList<Station> stations;

    public static BookmarkFragment newInstance() {
        fragment = new BookmarkFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.f_want_a_bike, container, false);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = new Api();

    }
    public void closeDb() {
        bdd.close();
    }

    @Override
    public void onPause() {
        bdd.close();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        bdd.close();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        bdd = new BookmarkBdd(getActivity());
        bdd.open();
        getBookmarks();
    }

    public void getBookmarks() {
         updateBikesFree(bdd.getStationIds());
    }

    private void updateBikesFree(String ids) {
        if (stations == null)
            stations = new ArrayList<Station>();
        else
            stations.clear();
        api.execute("dublin.php?method=getWithIdxs&idxs=" + ids, null, new OnApiFinished() {

            @Override
            public void success(String json) {
                try {
                    JSONArray arr = new JSONArray(json);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        Station s = new Station();
                        s.setFree(obj.getInt("free"));
                        s.setBikes(obj.getInt("bikes"));
                        s.setName(obj.getString("name"));
                        s.setTimestamp(obj.getString("timestamp"));
                        s.setLatitude(obj.getDouble("lat") / 1000000);
                        s.setLongitude(obj.getDouble("lng") / 1000000);
                        s.setId(obj.getInt("idx"));
                        s.setIdx(obj.getInt("idx"));
                        stations.add(s);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Bundle b = new Bundle();
                b.putParcelableArrayList("stations", stations);
                b.putInt("mode", Constants.BOOKMARK_MODE);
                ListFragment frag = new ListFragment();
                frag.setArguments(b);
                getChildFragmentManager().beginTransaction().replace(R.id.frame, frag).commit();

            }

            @Override
            public void error(String error) {
            }
        });

    }
    @Override
    public String getTitle() {
        if (getActivity()!= null)
            return getString(R.string.add_to_bookmark);
        return "Dublin Bikes";
    }

}
