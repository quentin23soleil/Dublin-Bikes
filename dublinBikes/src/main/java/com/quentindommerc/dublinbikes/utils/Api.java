package com.quentindommerc.dublinbikes.utils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.quentindommerc.dublinbikes.bean.Station;
import com.quentindommerc.dublinbikes.interfaces.OnApiFinished;

import org.json.JSONObject;

public class Api {

	private final AsyncHttpClient client;

	public Api() {
		client = new AsyncHttpClient();
	}

	public void execute(String url, RequestParams params,
			final OnApiFinished listener) {
		if (params == null)
			params = new RequestParams();
		url = Constants.BASE_URL + "/" + url;
		Utils.log(AsyncHttpClient.getUrlWithQueryString(true, url, params));

		client.get(url, params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int arg0, String arg1) {
				super.onSuccess(arg0, arg1);
//				Utils.log(arg1);
				listener.success(arg1);
			}

			@Override
			public void onFailure(Throwable arg0, String arg1) {
				super.onFailure(arg0, arg1);
				Utils.log("Error : " + arg1);
				listener.error(arg1);
			}
		});
	}

    public static Station parseStation(JSONObject stationJson, int mode) {
        Station s = new Station();
        s.setName(stationJson.optString("name"));
        s.setTimestamp(stationJson.optString("timestamp"));
        s.setNumber(stationJson.optInt("number"));
        s.setFree(stationJson.optInt("empty_slots"));
        s.setBikes(stationJson.optInt("free_bikes"));
        s.setLatitude(stationJson.optDouble("latitude"));
        s.setLongitude(stationJson.optDouble("longitude"));
        if (mode == Constants.BOOKMARK_MODE)
            s.setId(stationJson.optString("idx"));
        else
            s.setId(stationJson.optString("id"));
        s.setIdx(stationJson.optString("idx"));
        s.setStationUrl(stationJson.optString("station_url"));
        return s;
    }
}
