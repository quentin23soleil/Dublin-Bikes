package com.quentindommerc.dublinbikes.utils;

import java.util.Comparator;

import com.quentindommerc.dublinbikes.bean.Station;

public class StationComparator implements Comparator<Station> {

	@Override
	public int compare(Station lhs, Station rhs) {
		return lhs.getDistanceMeter().compareTo(rhs.getDistanceMeter());
	}

}
