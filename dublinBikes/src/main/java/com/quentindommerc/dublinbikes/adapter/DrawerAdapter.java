package com.quentindommerc.dublinbikes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.quentindommerc.dublinbikes.R;
import com.quentindommerc.dublinbikes.bean.MenuItem;

public class DrawerAdapter extends ArrayAdapter<MenuItem> {

	private final LayoutInflater mInflater;

    public final static int MENU_LIST = 0;
//    public final static int MENU_TAKE = 1;
//    public final static int MENU_LEAVE = 2;
    public final static int MENU_MAP = 1;
    public final static int MENU_BOOKMARK = 2;
    public final static int MENU_ABOUT = 3;

	public DrawerAdapter(Context ct) {
		super(ct, 0);
		this.mInflater = LayoutInflater.from(ct);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ViewHolder holder;
		if (v == null) {
			v = mInflater.inflate(R.layout.r_menu, null);
			holder = new ViewHolder();
			holder.title = (TextView) v.findViewById(R.id.title);
			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}
		holder.title.setText(getItem(position).getTitle());
//		holder.title.setCompoundDrawablesWithIntrinsicBounds(getItem(position)
//				.getIcon(), 0, 0, 0);
		return v;
	}

	static class ViewHolder {
		public TextView title;
	}
}
