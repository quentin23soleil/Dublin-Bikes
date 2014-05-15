package com.quentindommerc.dublinbikes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.quentindommerc.dublinbikes.R;
import com.quentindommerc.dublinbikes.base.BaseFragment;
import com.quentindommerc.dublinbikes.bean.Station;
import com.quentindommerc.dublinbikes.interfaces.OnApiFinished;
import com.quentindommerc.dublinbikes.utils.Api;
import com.quentindommerc.dublinbikes.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListModeFragment extends BaseFragment {

    private static ListModeFragment fragment;
    private ArrayList<Station> stations;
    private boolean mShowingBack;
    private MapFragment mapFrag;
    private Api mApi;

    public static ListModeFragment newInstance() {
        fragment = new ListModeFragment();
        return fragment;
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        if (!mShowingBack)
//            inflater.inflate(R.menu.want_a_bike, menu);
//        else
//            inflater.inflate(R.menu.want_a_bike_list, menu);
//        super.onCreateOptionsMenu(menu, inflater);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_map:
//                flip();
//                break;
//            case R.id.menu_list:
//                mShowingBack = true;
//                flip();
//                break;
//            default:
//                break;
//        }
//        return true;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stations = new ArrayList<Station>();
        setHasOptionsMenu(true);
        mApi = new Api();
        getStations();
    }

    private void getStations() {
        mApi.execute("dublin.json", null, new OnApiFinished() {

            @Override
            public void success(String json) {
                try {
                    JSONArray array = new JSONArray(json);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject stationJson = array.optJSONObject(i);
                        Station s = new Station();
                        s.setName(stationJson.optString("name"));
                        s.setTimestamp(stationJson.optString("timestamp"));
                        s.setNumber(stationJson.optInt("number"));
                        s.setFree(stationJson.optInt("free"));
                        s.setBikes(stationJson.optInt("bikes"));
                        s.setLatitude(stationJson.optDouble("lat") / 1000000);
                        s.setLongitude(stationJson.optDouble("lng") / 1000000);
                        s.setId(stationJson.optString("id"));
                        s.setStationUrl(stationJson.optString("station_url"));
                        stations.add(s);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                getFragmentManager().beginTransaction().replace(R.id.frame, ListFragment.newInstance(stations, Constants.LIST_MODE)).commit();
            }

            @Override
            public void error(String error) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.f_want_a_bike, container, false);
        return v;
    }

    private void flip() {
        getActivity().invalidateOptionsMenu();
        if (mShowingBack) {
            mShowingBack = false;
            getFragmentManager().popBackStack();
            return;
        }
        mShowingBack = true;

        if (mapFrag == null) {
            mapFrag = new MapFragment();
            Bundle b = new Bundle();
            b.putParcelableArrayList("stations", stations);
            b.putInt("mode", Constants.LIST_MODE);
            mapFrag.setArguments(b);
        }
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                .replace(R.id.frame, mapFrag).addToBackStack(null).commit();
    }
    @Override
    public String getTitle() {
        return getString(R.string.station_list);
    }
}
