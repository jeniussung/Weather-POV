package com.ssr.alarmproject;

import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.ssr.alarmproject.LinearLayoutSingleAlarmItem.OnRemoveButtonClickListner;

public class AdapterAlarm extends BaseAdapter {
	
	Context mContext;
	ArrayList<String> mData;
	LayoutInflater mInflate;
	ArrayList<AlarmData> arrayListAlarmDatas;
	private SharedPreferences sharedPref;
	private SharedPreferences.Editor sharedEditor;
	private SharedPreferences sharedPref_size;
	private SharedPreferences.Editor sharedEditor_size;

	public AdapterAlarm(Context context, ArrayList<AlarmData> arrayListAlarmDatas) {
		mContext = context;
		this.arrayListAlarmDatas = arrayListAlarmDatas;
		mInflate = LayoutInflater.from(mContext);
		sharedPref  = context.getSharedPreferences("alarm", Context.MODE_PRIVATE);
		sharedEditor = sharedPref.edit();
		sharedPref_size = context.getSharedPreferences("size", Context.MODE_PRIVATE);
		sharedEditor_size = sharedPref_size.edit();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return arrayListAlarmDatas.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return arrayListAlarmDatas.get(position).reqCode;
	}
	
	public boolean removeData(int position){
		arrayListAlarmDatas.remove(position);
		notifyDataSetChanged();
		return false;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayoutSingleAlarmItem layoutSingleAlarmItem = (LinearLayoutSingleAlarmItem) convertView;
		if (layoutSingleAlarmItem == null) {
			layoutSingleAlarmItem = new LinearLayoutSingleAlarmItem(mContext);
			layoutSingleAlarmItem.setOnRemoveButtonClickListner(onRemoveButtonClickListner);
		}
		layoutSingleAlarmItem.setData(arrayListAlarmDatas.get(position), position);
		return layoutSingleAlarmItem;
	}
	
	OnRemoveButtonClickListner onRemoveButtonClickListner = new OnRemoveButtonClickListner() {
		//, String location
		@Override
		public void onClicked(int hh, int mm, int reqCode, int position) {
			sharedEditor.remove("list"+position+"hh");
			sharedEditor.remove("list"+position+"mm");
			sharedEditor.remove("list" + position + "reqCode");
			//sharedEditor.remove("list"+position+"location");
			sharedEditor_size.putInt("size",sharedPref_size.getInt("size", 0)-1);
			sharedEditor_size.commit();
			//sharedEditor.remove("size");
			sharedEditor.commit();
			//Toast.makeText(mContext, "position : "+position+1 + " reqCode :"+reqCode, 0).show();
			 AlarmManager alarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
//			    Intent i = new Intent(mContext ,AlarmTestForHaruActivity.class);
			 	Intent intent = new Intent(mContext, ActivityAlarmedTimeShow.class);
			 //l   Toast.makeText(mContext, "reqCode : "+reqCode, 0).show();
			    PendingIntent pi = PendingIntent.getActivity(mContext, reqCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			    alarmManager.cancel(pi);
			removeData(position);
		}
	};

}
