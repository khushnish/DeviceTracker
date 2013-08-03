package com.indianic.devicetracker;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NumberAdapter extends ArrayAdapter<Object> {
	
	private Typeface typeface;
	private ArrayList<Object> numbers;
	
	public NumberAdapter(Context context, int resource, int textViewResourceId,
			ArrayList<Object> numbers) {
		super(context, resource, textViewResourceId, numbers);
		this.numbers = numbers;
		typeface = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
	}
	
	@Override
	public int getCount() {
		return numbers.size();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if ( convertView == null ) {
			
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.row_activity_main, null);
			
			viewHolder = new ViewHolder();
			viewHolder.txtNumber = (TextView) convertView.findViewById(R.id.row_activity_main_txt_number);
			viewHolder.txtNumber.setTypeface(typeface, Typeface.BOLD);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		viewHolder.txtNumber.setText(String.valueOf(numbers.get(position)));
		return convertView;
	}
	
	static class ViewHolder {
		TextView txtNumber;
	}
}