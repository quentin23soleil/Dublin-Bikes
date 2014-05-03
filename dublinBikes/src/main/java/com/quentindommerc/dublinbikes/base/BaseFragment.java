package com.quentindommerc.dublinbikes.base;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.quentindommerc.dublinbikes.utils.Utils;

public abstract class BaseFragment extends Fragment {

    public abstract String getTitle();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.playServicesCheck(getActivity());
        getActivity().getActionBar().setTitle(this.getTitle());
    }
}
