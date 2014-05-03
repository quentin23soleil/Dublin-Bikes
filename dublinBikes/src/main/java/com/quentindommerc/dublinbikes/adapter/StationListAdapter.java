package com.quentindommerc.dublinbikes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.quentindommerc.dublinbikes.R;
import com.quentindommerc.dublinbikes.bean.Station;

import java.text.NumberFormat;

/*@Deprecated
 **
 **use CardAdapter instead.
 */
public class StationListAdapter extends ArrayAdapter<Station> {

    private final LayoutInflater inflater;

    public StationListAdapter(Context ct) {
        super(ct, 0);
        this.inflater = LayoutInflater.from(ct);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;
        if (v == null) {
            v = inflater.inflate(R.layout.r_list, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) v.findViewById(R.id.station_name);
            holder.distance = (TextView) v.findViewById(R.id.distance);
            holder.spots = (TextView) v.findViewById(R.id.spots_tv);
            holder.bikes = (TextView) v.findViewById(R.id.bikes_tv);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        Station s = getItem(position);
        holder.name.setText(s.getName());
        holder.distance.setText(s.getDistance());
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(2);
        holder.spots.setText(nf.format(s.getFree()));
        holder.bikes.setText(nf.format(s.getBikes()));
        return v;
    }

    static class ViewHolder {
        TextView name;
        TextView distance;
        TextView spots;
        TextView bikes;
    }

}
