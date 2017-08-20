package com.ssr.alarmproject;

public class AlarmData {
	public int hh;
	public int mm;
	public int reqCode;
	public String location;
//, String location
	public AlarmData(int hh, int mm, int reqCode) {
		this.hh = hh;
		this.mm = mm;
		this. reqCode = reqCode;
		this.location = location;
	}
	
	@Override
	public String toString() {
		return hh+":"+mm +" and requestCode : "+reqCode;
	}


}

