package com.ssr.alarmproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.io.InputStream;
import java.io.OutputStream;

// 알람 시 실행되는 액티비티
public class ActivityAlarmedTimeShow extends Activity {
	TextView textViewAlarmedTime,textViewLocation;
	SoundPool sound;
	String weather=null;
	Double nowlati,nowlong;
	private RelativeLayout back;
	private Button changeweather;
	private String SUNNY = "1";
	private String RAINY = "2";
	private String CLOUDY = "3";
	private String SNOWY = "4";
	int img_seq = 0;
	int soundId;
	int streamId;
	int seq = 0;
	private GPSInfo gpsInfo;
	private String addressString = "지역정보를 가져오지 못하였습니다.";
	//////////블루투스/////////////////////////////////
	// 사용자 정의 함수로 블루투스 활성 상태의 변경 결과를 App으로 알려줄때 식별자로 사용됨 (0보다 커야함)
	static final int REQUEST_ENABLE_BT = 10;
	int mPariedDeviceCount = 0;
	Set<BluetoothDevice> mDevices;
	// 폰의 블루투스 모듈을 사용하기 위한 오브젝트.
	BluetoothAdapter mBluetoothAdapter;
	/**
	 BluetoothDevice 로 기기의 장치정보를 알아낼 수 있는 자세한 메소드 및 상태값을 알아낼 수 있다.
	 연결하고자 하는 다른 블루투스 기기의 이름, 주소, 연결 상태 등의 정보를 조회할 수 있는 클래스.
	 현재 기기가 아닌 다른 블루투스 기기와의 연결 및 정보를 알아낼 때 사용.
	 */
	BluetoothDevice mRemoteDevie;
	// 스마트폰과 페어링 된 디바이스간 통신 채널에 대응 하는 BluetoothSocket
	BluetoothSocket mSocket = null;
	OutputStream mOutputStream = null;
	InputStream mInputStream = null;
	String mStrDelimiter = "\n";
	char mCharDelimiter =  '\n';

	Thread mWorkerThread = null;
	byte[] readBuffer;
	int readBufferPosition;

	EditText mEditReceive, mEditSend;
	Button mButtonSend;

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			sound();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarmed_time_show_layout);

		back = (RelativeLayout) findViewById(R.id.backshow);
		changeweather = (Button)findViewById(R.id.button2);
		textViewAlarmedTime = (TextView)findViewById(R.id.textViewAlarmedTime);
		textViewLocation = (TextView)findViewById(R.id.textViewLocation);
		Intent intent = getIntent();
		String time = intent.getStringExtra("time");
		String data = intent.getStringExtra("data");
		int reqCode = intent.getIntExtra("reqCode", 0);
		//textViewAlarmedTime.setText("일어나세요");
		Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		long[] pattern = {1000, 200, 1000, 2000, 1200};          // 진동, 무진동, 진동 무진동 숫으로 시간을 설정한다.
		//vibe.vibrate(pattern, 0);                                         // 패턴을 지정하고 반복횟수를 지정

		//gpsInfo = new GPSInfo(getApplicationContext(), 1);

		initGPS(true);

		if (gpsInfo.isGetLocation()) {

			while(nowlati==0.0 || nowlong==0.0) {
				nowlati = gpsInfo.getLatitude();
				nowlong = gpsInfo.getLongitude();
			}

		} else {
				Toast.makeText(getApplicationContext(),"위치 설정을 해주세요", Toast.LENGTH_LONG).show();
		}

		if(nowlati!=0.0 || nowlong!=0.0)
			setLocation(nowlati, nowlong);

		new ReceiveShortWeather().execute();
		vibe.vibrate(10000);



		//handler.sendEmptyMessageDelayed(0,0);

		sound();

		changeweather.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if(img_seq == 0)
				{
					back.setBackgroundResource(R.mipmap.sunny);
					img_seq++;
				}
				else if(img_seq == 1)
				{
					back.setBackgroundResource(R.mipmap.rainy);
					img_seq++;
				}
				else if(img_seq == 2)
				{
					back.setBackgroundResource(R.mipmap.cloudy);
					img_seq++;
				}
				else if(img_seq == 3)
				{
					back.setBackgroundResource(R.mipmap.snowy);
					img_seq = 0;
				}
			}
		});

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


	// 블루투스 장치의 이름이 주어졌을때 해당 블루투스 장치 객체를 페어링 된 장치 목록에서 찾아내는 코드.
	BluetoothDevice getDeviceFromBondedList(String name) {
		// BluetoothDevice : 페어링 된 기기 목록을 얻어옴.
		BluetoothDevice selectedDevice = null;
		// getBondedDevices 함수가 반환하는 페어링 된 기기 목록은 Set 형식이며,
		// Set 형식에서는 n 번째 원소를 얻어오는 방법이 없으므로 주어진 이름과 비교해서 찾는다.
		for(BluetoothDevice deivce : mDevices) {
			// getName() : 단말기의 Bluetooth Adapter 이름을 반환
			if(name.equals(deivce.getName())) {
				selectedDevice = deivce;
				break;
			}
		}
		return selectedDevice;
	}

	//  connectToSelectedDevice() : 원격 장치와 연결하는 과정을 나타냄.
	//        실제 데이터 송수신을 위해서는 소켓으로부터 입출력 스트림을 얻고 입출력 스트림을 이용하여 이루어 진다.
	void connectToSelectedDevice(String selectedDeviceName) {
		// BluetoothDevice 원격 블루투스 기기를 나타냄.
		mRemoteDevie = getDeviceFromBondedList(selectedDeviceName);
		// java.util.UUID.fromString : 자바에서 중복되지 않는 Unique 키 생성.
		UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

		try {
			// 소켓 생성, RFCOMM 채널을 통한 연결.
			// createRfcommSocketToServiceRecord(uuid) : 이 함수를 사용하여 원격 블루투스 장치와 통신할 수 있는 소켓을 생성함.
			// 이 메소드가 성공하면 스마트폰과 페어링 된 디바이스간 통신 채널에 대응하는 BluetoothSocket 오브젝트를 리턴함.
			mSocket = mRemoteDevie.createRfcommSocketToServiceRecord(uuid);
			mSocket.connect(); // 소켓이 생성 되면 connect() 함수를 호출함으로써 두기기의 연결은 완료된다.

			// 데이터 송수신을 위한 스트림 얻기.
			// BluetoothSocket 오브젝트는 두개의 Stream을 제공한다.
			// 1. 데이터를 보내기 위한 OutputStrem
			// 2. 데이터를 받기 위한 InputStream
			mOutputStream = mSocket.getOutputStream();
			mInputStream = mSocket.getInputStream();

		}catch(Exception e) { // 블루투스 연결 중 오류 발생
			Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
			//connectToSelectedDevice(selectedDeviceName);
			//finish();  // App 종료
		}
	}

	// 문자열 전송하는 함수(쓰레드 사용 x)
	void sendData(String msg) {
		msg += mStrDelimiter;  // 문자열 종료표시 (\n)
		try{
			// getBytes() : String을 byte로 변환
			// OutputStream.write : 데이터를 쓸때는 write(byte[]) 메소드를 사용함. byte[] 안에 있는 데이터를 한번에 기록해 준다.
			mOutputStream.write(msg.getBytes());  // 문자열 전송.
			try {
				//mSocket.close();
				//mInputStream.close();
				//mSocket = null;
			}catch (Exception e) {}
		}catch(Exception e) {  // 문자열 전송 도중 오류가 발생한 경우
			//sendData(msg);
			Toast.makeText(getApplicationContext(), "전송도중 오류 발생", Toast.LENGTH_LONG).show();
			//finish();  // App 종료
		}
	}

	// 블루투스 지원하며 활성 상태인 경우.
	void selectDevice() {
		// 블루투스 디바이스는 연결해서 사용하기 전에 먼저 페어링 되어야만 한다
		// getBondedDevices() : 페어링된 장치 목록 얻어오는 함수.
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mDevices = mBluetoothAdapter.getBondedDevices();
		mPariedDeviceCount = mDevices.size();

		if(mPariedDeviceCount == 0 ) { // 페어링된 장치가 없는 경우.
			Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
			finish(); // App 종료.
		}
		// 페어링된 장치가 있는 경우.
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("블루투스 장치 선택");

		// 각 디바이스는 이름과(서로 다른) 주소를 가진다. 페어링 된 디바이스들을 표시한다.
		List<String> listItems = new ArrayList<String>();
		for(BluetoothDevice device : mDevices) {
			// device.getName() : 단말기의 Bluetooth Adapter 이름을 반환.
			listItems.add(device.getName());
		}
		listItems.add("취소");  // 취소 항목 추가.


		// CharSequence : 변경 가능한 문자열.
		// toArray : List형태로 넘어온것 배열로 바꿔서 처리하기 위한 toArray() 함수.
		final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
		// toArray 함수를 이용해서 size만큼 배열이 생성 되었다.
		listItems.toArray(new CharSequence[listItems.size()]);
		connectToSelectedDevice(items[seq++].toString());
		/*
		sendData("1");
		connectToSelectedDevice(items[1].toString());
		sendData("1");
		*/
		/*
		try {
			mSocket.close();
			mInputStream.close();

		}catch (Exception e) {}

		try{
			mSocket.close();
			mInputStream.close();
		}catch(Exception e){}
*/

		/*
		builder.setItems(items, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int item) {
				// TODO Auto-generated method stub
				if(item == mPariedDeviceCount) { // 연결할 장치를 선택하지 않고 '취소' 를 누른 경우.
					Toast.makeText(getApplicationContext(), "연결할 장치를 선택하지 않았습니다.", Toast.LENGTH_LONG).show();
					finish();
				}
				else { // 연결할 장치를 선택한 경우, 선택한 장치와 연결을 시도함.
					connectToSelectedDevice(items[item].toString());
				}
			}

		});

		builder.setCancelable(false);  // 뒤로 가기 버튼 사용 금지.
		AlertDialog alert = builder.create();
		alert.show();
		*/
	}

	public void setLocation(Double lati, Double longit)
	{
		Geocoder gc = new Geocoder(ActivityAlarmedTimeShow.this, Locale.KOREAN);

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
				textViewLocation.setText(addressString);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		/*
		try{
			mSocket.close();
			mInputStream.close();
		}catch(Exception e){}
		*/
		sound.stop(streamId);
		super.onDestroy();

	}

	public void sound()
	{
		sound  = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);// maxStreams, streamType, srcQuality
		soundId = sound.load(this, R.raw.siren, 1);
		//streamId = sound.play(soundId, 1.0F, 1.0F, 1, -1, 1.0F);
		streamId = sound.play(soundId, 1,1,0,-1,-1);
		int waitLimit = 1000;
		int waitCounter = 0;
		int throttle = 10;
		/*
		while(sound.play(soundId, 1.f, 1.f, 1, 0, 1.f) == 0 && waitCounter < waitLimit){
			waitCounter++; SystemClock.sleep(throttle);
		}
		*/
	}

	public class ReceiveShortWeather extends AsyncTask<URL, Integer, Long> {

		ArrayList<ShortWeather> shortWeathers = new ArrayList<ShortWeather>();

		protected Long doInBackground(URL... urls) {

			String url = "http://www.kma.go.kr/wid/queryDFS.jsp?gridx="+Double.toString(nowlati)+"&gridy="+Double.toString(nowlong);

			OkHttpClient client = new OkHttpClient();

			Request request = new Request.Builder()
					.url(url)
					.build();

			Response response = null;

			try {
				response = client.newCall(request).execute();
				parseXML(response.body().string());
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		protected void onPostExecute(Long result) {
			String data = "";
			sound();
/*
			for(int i=0; i<shortWeathers.size(); i++) {
				data += shortWeathers.get(i).getHour() + " " +
						shortWeathers.get(i).getDay() + " " +
						shortWeathers.get(i).getTemp() + " " +
						shortWeathers.get(i).getWfKor() + " " +
						shortWeathers.get(i).getPop() + "\n";
			}
*/
			try {
				weather = shortWeathers.get(0).getWfKor();
				//weather = "맑음";
				data = weather + " " + shortWeathers.get(0).getTemp()+"℃";//shortWeathers.get(0).getWfKor() + " " + shortWeathers.get(0).getTemp()+"℃";
				textViewAlarmedTime.setText(data);

				if(weather.equals("맑음"))
				{
					back.setBackgroundResource(R.mipmap.sunny);
					selectDevice();
					sendData(SUNNY);
					selectDevice();
					sendData(SUNNY);
					seq =0;
				}
				else if(weather.equals("흐림"))
				{
					back.setBackgroundResource(R.mipmap.cloudy);
					selectDevice();
					sendData(CLOUDY);
					selectDevice();
					sendData(CLOUDY);
					seq =0;
				}
				else if(weather.equals("구름 조금"))
				{
					back.setBackgroundResource(R.mipmap.cloudy);
					selectDevice();
					sendData(CLOUDY);
					selectDevice();
					sendData(CLOUDY);
					seq =0;
				}
				else if(weather.equals("구름 많음"))
				{
					back.setBackgroundResource(R.mipmap.cloudy);
					selectDevice();
					sendData(CLOUDY);
					selectDevice();
					sendData(CLOUDY);
					seq =0;
				}
				else if(weather.equals("비"))
				{
					back.setBackgroundResource(R.mipmap.rainy);
					selectDevice();
					sendData(RAINY);
					selectDevice();
					sendData(RAINY);
					seq =0;
				}
				else if(weather.equals("눈"))
				{
					back.setBackgroundResource(R.mipmap.snowy);
					selectDevice();
					sendData(SNOWY);
					selectDevice();
					sendData(SNOWY);
					seq =0;
				}
				else if(weather.equals("비/눈"))
				{
					back.setBackgroundResource(R.mipmap.rainy);
					selectDevice();
					sendData(RAINY);
					selectDevice();
					sendData(RAINY);
					seq =0;
				}
				else if(weather.equals("눈/비"))
				{
					back.setBackgroundResource(R.mipmap.snowy);
					selectDevice();
					sendData(SNOWY);
					selectDevice();
					sendData(SNOWY);
					seq =0;
				}
			}catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}

		}

		void parseXML(String xml) {
			try {
				String tagName = "";
				boolean onHour = false;
				boolean onDay = false;
				boolean onTem = false;
				boolean onWfKor = false;
				boolean onPop = false;
				boolean onEnd = false;
				boolean isItemTag1 = false;
				int i = 0;

				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = factory.newPullParser();

				parser.setInput(new StringReader(xml));

				int eventType = parser.getEventType();

				while (eventType != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_TAG) {
						tagName = parser.getName();
						if (tagName.equals("data")) {
							shortWeathers.add(new ShortWeather());
							onEnd = false;
							isItemTag1 = true;
						}
					} else if (eventType == XmlPullParser.TEXT && isItemTag1) {
						if (tagName.equals("hour") && !onHour) {
							shortWeathers.get(i).setHour(parser.getText());
							onHour = true;
						}
						if (tagName.equals("day") && !onDay) {
							shortWeathers.get(i).setDay(parser.getText());
							onDay = true;
						}
						if (tagName.equals("temp") && !onTem) {
							shortWeathers.get(i).setTemp(parser.getText());
							onTem = true;
						}
						if (tagName.equals("wfKor") && !onWfKor) {
							shortWeathers.get(i).setWfKor(parser.getText());
							onWfKor = true;
						}
						if (tagName.equals("pop") && !onPop) {
							shortWeathers.get(i).setPop(parser.getText());
							onPop = true;
						}
					} else if (eventType == XmlPullParser.END_TAG) {
						if (tagName.equals("s06") && onEnd == false) {
							i++;
							onHour = false;
							onDay = false;
							onTem = false;
							onWfKor = false;
							onPop = false;
							isItemTag1 = false;
							onEnd = true;
						}
					}

					eventType = parser.next();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
