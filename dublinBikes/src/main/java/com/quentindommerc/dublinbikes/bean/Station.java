package com.quentindommerc.dublinbikes.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Station implements Parcelable {
	private String mName;
	private String mIdx;
	private String mTimestamp;
	private int mNumber;
	private int mFree;
	private int mBikes;
	private double mLatitude;
	private double mLongitude;
	private String mId;
	private String mStationUrl;
	private String mDistanceString;
	private Float mDistanceMeter;

	public Station() {
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getIdx() {
		return mIdx;
	}

	public void setIdx(String idx) {
		mIdx = idx;
	}

	public String getTimestamp() {
		return mTimestamp;
	}

	public void setTimestamp(String timestamp) {
		mTimestamp = timestamp;
	}

	public int getNumber() {
		return mNumber;
	}

	public void setNumber(int number) {
		mNumber = number;
	}

	public int getFree() {
		return mFree;
	}

	public void setFree(int free) {
		mFree = free;
	}

	public int getBikes() {
		return mBikes;
	}

	public void setBikes(int bikes) {
		mBikes = bikes;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public void setLatitude(double d) {
		mLatitude = d;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public void setLongitude(double d) {
		mLongitude = d;
	}

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		mId = id;
	}

	public String getStationUrl() {
		return mStationUrl;
	}

	public void setStationUrl(String stationUrl) {
		mStationUrl = stationUrl;
	}

	@Override
	public String toString() {
		return mName + " " + mIdx + " free :" + mFree + " / " + mNumber
				+ " lat : " + mLatitude + " lng : " + mLongitude;
	}

	protected Station(Parcel in) {
		mName = in.readString();
		mIdx = in.readString();
		mTimestamp = in.readString();
		mNumber = in.readInt();
		mFree = in.readInt();
		mBikes = in.readInt();
		mLatitude = in.readDouble();
		mLongitude = in.readDouble();
		mId = in.readString();
		mStationUrl = in.readString();
		mDistanceString = in.readString();
		mDistanceMeter = in.readFloat();
	}

	public void setDistance(String distanceString) {
		mDistanceString = distanceString;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mName);
		dest.writeString(mIdx);
		dest.writeString(mTimestamp);
		dest.writeInt(mNumber);
		dest.writeInt(mFree);
		dest.writeInt(mBikes);
		dest.writeDouble(mLatitude);
		dest.writeDouble(mLongitude);
		dest.writeString(mId);
		dest.writeString(mStationUrl);
		dest.writeString(mDistanceString);
		if (mDistanceMeter != null)
			dest.writeFloat(mDistanceMeter);
	}

	public static final Parcelable.Creator<Station> CREATOR = new Parcelable.Creator<Station>() {
		@Override
		public Station createFromParcel(Parcel in) {
			return new Station(in);
		}

		@Override
		public Station[] newArray(int size) {
			return new Station[size];
		}
	};

	public String getDistance() {
		return mDistanceString;
	}

	public Float getDistanceMeter() {
		return mDistanceMeter;
	}

	public void setDistanceMeter(Float f) {
		this.mDistanceMeter = f;
	}
}
