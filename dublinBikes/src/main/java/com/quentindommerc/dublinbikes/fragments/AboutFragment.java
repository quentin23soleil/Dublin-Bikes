package com.quentindommerc.dublinbikes.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.analytics.tracking.android.EasyTracker;
import com.quentindommerc.dublinbikes.R;
import com.quentindommerc.dublinbikes.base.BaseFragment;

public class AboutFragment extends BaseFragment {

	private static AboutFragment fragment;


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

    public static AboutFragment newInstance() {
		if (fragment == null)
			fragment = new AboutFragment();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.f_about, null);
		return v;
	}
    @Override
    public String getTitle() {
        return getString(R.string.about);
    }

}
