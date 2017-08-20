package com.ssr.alarmproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LinearLayoutSingleAlarmItem extends LinearLayout {
	Context mContext;
	TextView textViewTime, textViewLocation;
	Button btnSingleAlarmItemCancel;
	private SharedPreferences sharedPref;
	AlarmData alarmData;
	private int position;
	
	public LinearLayoutSingleAlarmItem(Context context) {
		super(context);
		mContext = context;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.single_alarm_data_layout, this);
		textViewTime = (TextView)layout.findViewById(R.id.textViewTime);
		textViewLocation = (TextView)layout.findViewById(R.id.textViewLocation);
		btnSingleAlarmItemCancel = (Button)findViewById(R.id.btnSingleAlarmItemCancel);

		btnSingleAlarmItemCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(onRemoveButtonClickListner != null)
				onRemoveButtonClickListner.onClicked(alarmData.hh, alarmData.mm, alarmData.reqCode, position);
				//, alarmData.location
			}
		});
	}
	
	public interface OnRemoveButtonClickListner{
//, String location
		void onClicked(int hh, int mm, int reqCode, int position);

	}
	
	OnRemoveButtonClickListner onRemoveButtonClickListner;
	
	void setOnRemoveButtonClickListner(OnRemoveButtonClickListner onRemoveButtonClickListner){

		this.onRemoveButtonClickListner = onRemoveButtonClickListner;

	}
	
	public boolean setData(AlarmData alarmData, int position){
		
		this.alarmData = alarmData;
		this.position = position;
		
		textViewTime.setText("알람시각 : "+alarmData.hh+":"+alarmData.mm);
		//textViewLocation.setText(alarmData.location);
		
		return true;
	}
	
	

}
