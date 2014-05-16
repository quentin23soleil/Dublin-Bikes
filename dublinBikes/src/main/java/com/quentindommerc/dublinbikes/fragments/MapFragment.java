package com.quentindommerc.dublinbikes.fragments;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.quentindommerc.dublinbikes.R;
import com.quentindommerc.dublinbikes.activity.DetailStation;
import com.quentindommerc.dublinbikes.base.BaseFragment;
import com.quentindommerc.dublinbikes.bean.Station;
import com.quentindommerc.dublinbikes.interfaces.OnApiFinished;
import com.quentindommerc.dublinbikes.utils.Api;
import com.quentindommerc.dublinbikes.utils.Constants;
import com.quentindommerc.dublinbikes.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MapFragment extends BaseFragment {

    private ArrayList<Station> mStations;
    private int mMode = -1;
    private GoogleMap mGMap;
    protected Location mCurrentLocation;
    protected boolean mFirst;
    private ArrayList<Marker> mMarkers;
    private HashMap<String, Station> mStationMap;
    private static View mView;

    public static MapFragment newInstance(int mode) {
        Bundle b = new Bundle();
        b.putInt("mode", mode);
        MapFragment fragment = new MapFragment();
        fragment.setArguments(b);
        return fragment;
    }


    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(getActivity()).activityStart(getActivity());
        Tracker tracker = GoogleAnalytics.getInstance(getActivity()).getTracker("UA-45191498-1");

        HashMap<String, String> hitParameters = new HashMap<String, String>();
        hitParameters.put(Fields.SCREEN_NAME, "Map");

        tracker.send(hitParameters);
    }

    @Override
    public void onStop() {
        super.onStop();

        EasyTracker.getInstance(getActivity()).activityStop(getActivity());
    }



    private void getStations() {
        Api api = new Api();
        api.execute("dublin.json", null, new OnApiFinished() {

            @Override
            public void success(String json) {
                try {
                    JSONArray array = new JSONArray(json);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject stationJson = array.optJSONObject(i);
                        Station s = Api.parseStation(stationJson, mMode);
                        if (mMode == Constants.LIST_MODE)
                            mStations.add(s);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                updateMap();
            }

            @Override
            public void error(String error) {
            }
        });
    }

    protected void updateMap() {
        if (mGMap != null) {
            mGMap.clear();
            clearMap();
            mStationMap.clear();
            if (mStations != null)
                for (Station s : mStations) {
                    MarkerOptions m = new MarkerOptions();
                    m.position(new LatLng(s.getLatitude(), s.getLongitude()));
                    m.title(s.getName());
                    m = setSnippet(s, m);
                    m = setIcon(s, m);
                    mMarkers.add(mGMap.addMarker(m));
                    mStationMap.put(mMarkers.get(mMarkers.size() - 1).getId(), s);

                }
        }

    }

    private MarkerOptions setSnippet(Station station, MarkerOptions m) {
        switch (mMode) {
            case Constants.LIST_MODE:
                m.snippet(Utils.getQuantityString(R.plurals.bikes_available, R.string.bikes_no,
                        station.getBikes(), getActivity())
                        + " | "
                        + Utils.getQuantityString(R.plurals.free_spots, R.string.spot_no,
                        station.getFree(), getActivity()));
                break;
            default:
                break;
        }
        return m;
    }

    private MarkerOptions setIcon(Station station, MarkerOptions m) {
        switch (mMode) {
            case Constants.LIST_MODE:
                return setIcon(station.getBikes(), m);
            default:
                return setIcon(station.getFree(), m);
        }
    }

    private MarkerOptions setIcon(int nb, MarkerOptions m) {
        if (nb > 5 && nb <= 10)
            m.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_blue));
        else if (nb > 10)
            m.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_green));
        else
            m.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_pink));
        return m;
    }

    private void clearMap() {
        for (int i = 0; i < mMarkers.size(); i++) {
            mMarkers.get(i).remove();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirst = true;
        mMarkers = new ArrayList<Marker>();
        mStationMap = new HashMap<String, Station>();

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        container.removeAllViews();
        try {
            mView = inflater.inflate(R.layout.f_map, container, false);
            com.google.android.gms.maps.SupportMapFragment map = (com.google.android.gms.maps.SupportMapFragment) getActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mGMap = map.getMap();
            mGMap.setMyLocationEnabled(true);
            mGMap.setOnMyLocationChangeListener(locationChange);
            mGMap.getUiSettings().setMyLocationButtonEnabled(true);
            CameraUpdate dublin = CameraUpdateFactory.newLatLngZoom(new LatLng(53.344103999999,
                    -6.2674936999999), 14);
            mGMap.moveCamera(dublin);
            mGMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

                @Override
                public void onInfoWindowClick(Marker marker) {
                    Station s = getStationWithMarkerId(marker.getId());
                    Intent intent = new Intent(getActivity(), DetailStation.class);
                    intent.putExtra("station", s);
                    startActivity(intent);
                }
            });


            mStations = getArguments().getParcelableArrayList("stations");
            mMode = getArguments().getInt("mode");
            if (mStations != null) {
                for (int i = 0; i < mStations.size(); i++) {
                    if (mStations.get(i).getFree() == 0 && mMode == Constants.LEAVE_BIKE_MODE)
                        mStations.remove(i);
                    else if (mStations.get(i).getBikes() == 0 && mMode == Constants.WANT_BIKE_MODE) {
                        mStations.remove(i);
                    }
                }
                updateMap();
            } else {
                mStations = new ArrayList<Station>();
                mMode = Constants.LIST_MODE;
                getStations();
            }
        }catch  (InflateException e) {

        }


        return mView;
    }

    protected Station getStationWithMarkerId(String id) {
        return mStationMap.get(id);
    }

    OnMyLocationChangeListener locationChange = new OnMyLocationChangeListener() {

        @Override
        public void onMyLocationChange(Location location) {
            mCurrentLocation = location;
            if (mFirst) {
                mFirst = false;
                CameraUpdate zoom = CameraUpdateFactory
                        .newLatLngZoom(
                                new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation
                                        .getLongitude()), 15);
                mGMap.animateCamera(zoom);
            }
        }
    };
//
//	@Override
//	public void onSaveInstanceState(Bundle outState) {
//		outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
//		// super.onSaveInstanceState(outState);
//	}

//	@Override
//	public void onDestroyView() {
//        super.onDestroyView();
//        if (getActivity() != null && !getActivity().isFinishing() && getActivity() instanceof Home) {
//            android.support.v4.app.Fragment fragment = (getActivity().getSupportFragmentManager().findFragmentById(R.id.map));
//            android.support.v4.app.FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
//            ft.remove(fragment);
//            ft.commit();
//        }
//    }

    @Override
    public String getTitle() {
        if (getActivity() != null)
            return getString(R.string.show_me_map);
        return "Dublin Bikes";
    }

}
