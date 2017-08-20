package com.ssr.alarmproject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class AlarmTestForHaruActivity extends Activity {
    /** Called when the activity is first created. */
	static final int REQUEST_ENABLE_BT = 10;
	private AlarmManager alarmManager;
	private Context mContext;
	public static final int DEFAULT_ALARM_REQUEST = 800;
	private static final int GPS_ACTIVITY = 11;

	BluetoothAdapter mBluetoothAdapter;
	TimePicker timePickerAlarmTime;
	Button btnAddAlarm,btnTest;
	ListView listViewAlarm;
	ArrayList<AlarmData> arrayListAlarmTimeItem = new ArrayList<AlarmData>(); 
	
//	GregorianCalendar currentCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+09:00"));
	GregorianCalendar currentCalendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+09:00"));

	private SharedPreferences sharedPref;
	private SharedPreferences.Editor sharedEditor;
	private SharedPreferences sharedPref_size;
	private SharedPreferences.Editor sharedEditor_size;
	private LocationManager locationManager;
	
	AdapterAlarm arrayAdapterAlarmList;

	private GPSInfo gpsInfo;
	static Double nowlati = 0.0, nowlong = 0.0;

	String addressString = "지역정보를 가져오지 못하였습니다.";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			//alertCheckGPS();
			//moveConfigGPS();
			showSettingsAlert();
		}

		initGPS(true);

         mContext = getApplicationContext();
        
         timePickerAlarmTime = (TimePicker)findViewById(R.id.timePickerAlarmTime);
    	 btnAddAlarm = (Button)findViewById(R.id.btnAddAlarm);
		 btnTest = (Button)findViewById(R.id.button);
    	 listViewAlarm	= (ListView)findViewById(R.id.listViewAlarm);
    	 
    	 sharedPref = getSharedPreferences("alarm", Context.MODE_PRIVATE);
 		 sharedPref_size = getSharedPreferences("size", Context.MODE_PRIVATE);
    	 sharedEditor = sharedPref.edit();
		sharedEditor_size = sharedPref_size.edit();
    	 
    	 timePickerAlarmTime.setIs24HourView(false);
    	 
    	 arrayAdapterAlarmList = new AdapterAlarm(mContext, arrayListAlarmTimeItem);
    	 listViewAlarm.setAdapter(arrayAdapterAlarmList);
        
    	 alarmManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
    	 //alarmManager.setTimeZone("GMT+09:00");
        
		btnTest.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent intent = new Intent(AlarmTestForHaruActivity.this, ActivityAlarmedTimeShow.class);
				startActivity(intent);

			}
		});
        
		btnAddAlarm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				Toast.makeText(getApplicationContext(),Double.toString(nowlati), Toast.LENGTH_LONG).show();

				if (gpsInfo.isGetLocation()) {
					/*
					nowlati = gpsInfo.getLatitude();
					nowlong = gpsInfo.getLongitude();
					//Toast.makeText(getApplicationContext(),Double.toString(nowlati), Toast.LENGTH_LONG).show();
					if(nowlati==0.0||nowlong==0.0)
					{
						Toast.makeText(getApplicationContext(),"잠시 후에 다시 시도해주세요.", Toast.LENGTH_LONG).show();
						gpsInfo = new GPSInfo(getApplicationContext(), 1);
						return;
					}
					*/
				} else {
						Toast.makeText(getApplicationContext(),"위치 설정을 해주세요", Toast.LENGTH_LONG).show();
						gpsInfo = new GPSInfo(getApplicationContext(), 1);
						return;
				}

				//setLocation(nowlati,nowlong);

				int hh = timePickerAlarmTime.getCurrentHour();
				int mm = timePickerAlarmTime.getCurrentMinute();
				int reqCode = DEFAULT_ALARM_REQUEST+arrayListAlarmTimeItem.size();
				int i =arrayListAlarmTimeItem.size();
				//, addressString
				arrayListAlarmTimeItem.add(new AlarmData(hh, mm, reqCode));
				arrayAdapterAlarmList.notifyDataSetChanged();
				
				sharedEditor.putInt("list" + i + "hh", hh);
				sharedEditor.putInt("list"+i+"mm", mm);
				sharedEditor.putInt("list" + i + "reqCode", reqCode);
				//sharedEditor.putString("list"+i+"location", addressString);
				sharedEditor_size.putInt("size", i + 1);
				sharedEditor.commit();
				sharedEditor_size.commit();
				//Toast.makeText(mContext, "size : "+i, 0).show();
				GregorianCalendar gregorianCalendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+09:00"));
				
				int currentYY = currentCalendar.get(Calendar.YEAR);
				int currentMM = currentCalendar.get(Calendar.MONTH);
				int currentDD = currentCalendar.get(Calendar.DAY_OF_MONTH);
				
				gregorianCalendar.set(currentYY, currentMM, currentDD, hh, mm,00);

				if(gregorianCalendar.getTimeInMillis() < currentCalendar.getTimeInMillis()){
					gregorianCalendar.set(currentYY, currentMM, currentDD+1, hh, mm,00);
					Log.i("TAG",gregorianCalendar.getTimeInMillis()+":");
				}
					
				
				Intent intent = new Intent(AlarmTestForHaruActivity.this, ActivityAlarmedTimeShow.class);
				intent.putExtra("time", hh+":"+mm);
				intent.putExtra("data", "데이터: " + currentCalendar.getTime().toLocaleString());
				intent.putExtra("reqCode", reqCode);
				
				PendingIntent pi = PendingIntent.getActivity(AlarmTestForHaruActivity.this, reqCode, intent,PendingIntent.FLAG_UPDATE_CURRENT );
				alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, gregorianCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);

				//Toast.makeText(getApplicationContext(),Double.toString(nowlati), Toast.LENGTH_LONG).show();
			}
		});

		checkBluetooth();
    }

	public void showSettingsAlert(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(AlarmTestForHaruActivity.this);

		alertDialog.setTitle("GPS 사용유무셋팅");
		alertDialog.setMessage("GPS 셋팅이 되지 않았을수도 있습니다. 설정창으로 가시겠습니까?");

		// OK 를 누르게 되면 설정창으로 이동합니다.
		alertDialog.setPositiveButton("Settings",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(intent);
					}
				});
		// Cancle 하면 종료 합니다.
		alertDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		alertDialog.show();
	}

	private void moveConfigGPS() {
		Intent gpsOptionsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivityForResult(gpsOptionsIntent, GPS_ACTIVITY);
	}

	public void setLocation(Double lati, Double longit)
	{
		Geocoder gc = new Geocoder(AlarmTestForHaruActivity.this, Locale.KOREAN);

		try {

			List<Address> addresses = gc.getFromLocation(lati, longit, 1);

			StringBuilder sb = new StringBuilder();

			if (addresses.size() > 0) {

				Address address = addresses.get(0);

				for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
					sb.append(address.getAddressLine(i)).append("\n");
				}

				if (address.getAdminArea() != null)
					sb.append(address.getAdminArea()).append(" ");      // 시

				if (address.getLocality() != null)
					sb.append(address.getLocality()).append(" ");

				if (address.getSubLocality() != null)
					sb.append(address.getSubLocality()).append(" ");     // 구

				if (address.getThoroughfare() != null)
					sb.append(address.getThoroughfare()).append(" ");   // 길

				addressString = sb.toString();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initGPS(boolean init) {

		gpsInfo = new GPSInfo(getApplicationContext(), 1);

		if (gpsInfo.isGetLocation()) {

			nowlati = gpsInfo.getLatitude();
			nowlong = gpsInfo.getLongitude();
			//Toast.makeText(getApplicationContext(),Double.toString(nowlati), Toast.LENGTH_LONG).show();
		} else {
			if (!init) {
				Toast.makeText(getApplicationContext(),"위치 설정을 해주세요", Toast.LENGTH_LONG).show();
			}
		}

	}

    @Override
    protected void onResume() {
    	super.onResume();
    	arrayListAlarmTimeItem.clear();
    	int size = sharedPref_size.getInt("size", 0);

    	if(size !=0)
    	for(int i = 0 ; i < size; i ++ ){
    		int hh = sharedPref.getInt("list"+i+"hh", 0);
			int mm = sharedPref.getInt("list"+i+"mm", 0);
			int reqCode = sharedPref.getInt("list" +i+"reqCode", 0);
			//String location = sharedPref.getString("list"+i+"location", "지역정보를 가져오지 못하였습니다.");
			arrayListAlarmTimeItem.add(new AlarmData(hh, mm, reqCode));
    	}

    	arrayAdapterAlarmList.notifyDataSetChanged();
    }

	void checkBluetooth() {
		/**
		 * getDefaultAdapter() : 만일 폰에 블루투스 모듈이 없으면 null 을 리턴한다.
		 이경우 Toast를 사용해 에러메시지를 표시하고 앱을 종료한다.
		 */
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null ) {  // 블루투스 미지원
			Toast.makeText(getApplicationContext(), "기기가 블루투스를 지원하지 않습니다.", Toast.LENGTH_LONG).show();
			finish();  // 앱종료
		}
		else { // 블루투스 지원
			/** isEnable() : 블루투스 모듈이 활성화 되었는지 확인.
			 *               true : 지원 ,  false : 미지원
			 */
			if(!mBluetoothAdapter.isEnabled()) { // 블루투스 지원하며 비활성 상태인 경우.
				Toast.makeText(getApplicationContext(), "현재 블루투스가 비활성 상태입니다.", Toast.LENGTH_LONG).show();
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				// REQUEST_ENABLE_BT : 블루투스 활성 상태의 변경 결과를 App 으로 알려줄 때 식별자로 사용(0이상)
				/**
				 startActivityForResult 함수 호출후 다이얼로그가 나타남
				 "예" 를 선택하면 시스템의 블루투스 장치를 활성화 시키고
				 "아니오" 를 선택하면 비활성화 상태를 유지 한다.
				 선택 결과는 onActivityResult 콜백 함수에서 확인할 수 있다.
				 */
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
			//else // 블루투스 지원하며 활성 상태인 경우.
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// startActivityForResult 를 여러번 사용할 땐 이런 식으로 switch 문을 사용하여 어떤 요청인지 구분하여 사용함.
		switch(requestCode) {
			case REQUEST_ENABLE_BT:
				if(resultCode == RESULT_OK) { // 블루투스 활성화 상태
				}
				else if(resultCode == RESULT_CANCELED) { // 블루투스 비활성화 상태 (종료)
					Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(i, REQUEST_ENABLE_BT);
					//Toast.makeText(getApplicationContext(), "블루투수를 사용할 수 없어 프로그램을 종료합니다", Toast.LENGTH_LONG).show();
					//finish();
				}
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}