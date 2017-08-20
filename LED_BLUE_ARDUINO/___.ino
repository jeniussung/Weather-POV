#include <SoftwareSerial.h> // 블루투스 통신을 위한 SoftwareSerial 라이브러리를 불러온다.

int snow1_1[] = {B10010001, B01010011, B00110001, B11110001, B00001001, B00000101, B01000011, B11111111, B01000011, B00000101, B00001001, B11110001, B00110001, B01010011, B10010001, B00000000};
int snow1_2[] = {B00010010, B10010100, B00011000, B00011110, B00100000, B01000000, B10000100, B11111110, B10000100, B01000000, B00100000, B00011110, B00011000, B10010100, B00010010, B00000000};




int sun_red1[] = {B01000000, B00100000, B00010000, B00001000, B10000001, B10000011, B10000111, B11110111, B10000111, B10000011, B10000001, B00001000, B00010000, B00100000, B01000000, B00000000};
int sun_red2[] = {B11000000, B01000001, B01000010, B01000100, B01100000, B01110000, B01111000, B11111011, B01111000, B01110000, B01100000, B00000100, B01000010, B01000001, B11000000, B00000000};


int rain5[] = {B00000001,B00000011,B00000111,B00001111,B00011111,B00011111,B00011111,B11111111,B00011111,B00011111,B00011111,B00001111,B00000111,B00000011,B00000001, B00000000}; //진짜 비
int rain6[] = {B10000000,B10000000,B10000000,B10000000,B10001100,B10000010,B10000010,B11111100,B10000000,B10000000,B10000000,B10000000,B10000000,B10000000,B10000000, B00000000};

int cloud1[] = {B00000000, B00000001, B00001010, B00010100, B00100010, B01000100, B01001000, B01001000, B00101000, B00010100, B00010010, B00100001, B00100011, B00010100, B00001000, B00000000};
int cloud2[] = {B11000000, B00100000, B00010000, B00010000, B00100000, B00010000, B00010000, B00001000, B00010000, B00100000, B01000000, B00100000, B01000000, B10000000, B00000000, B00000000};

SoftwareSerial BTSerial(2, 3); // SoftwareSerial(TX, RX)
byte buffer[1024]; // 데이터를 수신 받을 버퍼
int bufferPosition; // 버퍼에 데이타를 저장할 때 기록할 위치
boolean temp = 0;
int curdata = 0;
int SER = 8;
int LAT = 9;
int CLK = 10;

void setup() {
  // put your setup code here, to run once:
  BTSerial.begin(9600); 
  Serial.begin(9600); 
  pinMode(13, OUTPUT);
  pinMode(SER, OUTPUT);
  pinMode(LAT, OUTPUT);
  pinMode(CLK, OUTPUT);
  bufferPosition = 0; // 버퍼 위치 초기화
  digitalWrite(LAT,LOW);
  shiftOut(SER, CLK, MSBFIRST,B00000000); //파랑 위
  shiftOut(SER, CLK, MSBFIRST, B00000000); //파랑 아래
  shiftOut(SER, CLK, LSBFIRST, B00000000); //빨강 아래
  shiftOut(SER, CLK, LSBFIRST, B00000000); //빨강 위
  digitalWrite(LAT,HIGH);    
}
void weather(int letter[], int letter2[])
{
  for(int yy=0;yy<16;yy++)
  {
   digitalWrite(LAT,LOW);
   shiftOut(SER, CLK, MSBFIRST, letter[yy]); //파랑 위
   shiftOut(SER, CLK, MSBFIRST, letter2[yy]); //파랑 아래
   shiftOut(SER, CLK, LSBFIRST, B00000000); //빨강 아래
   shiftOut(SER, CLK, LSBFIRST, B00000000); //빨강 위
   digitalWrite(LAT,HIGH);      
   delay(4);
  }
  digitalWrite(LAT,LOW);
  shiftOut(SER, CLK, MSBFIRST, B00000000); //파랑 위
  shiftOut(SER, CLK, MSBFIRST, B00000000); //파랑 아래
  shiftOut(SER, CLK, LSBFIRST, B00000000); //빨강 아래
  shiftOut(SER, CLK, LSBFIRST, B00000000); //빨강 위
  digitalWrite(LAT,HIGH);    
  delay(19);
}


void red_weather(int letter[], int letter2[])
{
  for(int yy=0;yy<16;yy++)
  {
    digitalWrite(LAT,LOW);
    shiftOut(SER, CLK, MSBFIRST, B00000000); //파랑 위
    shiftOut(SER, CLK, MSBFIRST, B00000000); //파랑 아래
    shiftOut(SER, CLK, LSBFIRST, letter2[yy]); //빨강 아래
    shiftOut(SER, CLK, LSBFIRST, letter[yy]); //빨강 위
    digitalWrite(LAT,HIGH);    
    delay(4);
  }
  digitalWrite(LAT,LOW);
  shiftOut(SER, CLK, MSBFIRST, B00000000); //파랑 위
  shiftOut(SER, CLK, MSBFIRST, B00000000); //파랑 아래
  shiftOut(SER, CLK, LSBFIRST, B00000000); //빨강 아래
  shiftOut(SER, CLK, LSBFIRST, B00000000); //빨강 위
  digitalWrite(LAT,HIGH);    
  delay(19);
}
void snowy()
{
  weather(snow1_1,snow1_2);
}
void sun_red()
{
  red_weather(sun_red1,sun_red2);
} 
void rain()
{
  weather(rain5,rain6);
}
void cloudy()
{
  weather(cloud1, cloud2); 
}
void loop() 
{



  
  if (BTSerial.available()){ // 블루투스로 데이터 수신
    byte data = BTSerial.read(); // 수신 받은 데이터 저장
    Serial.write(data); // 수신된 데이터 시리얼 모니터로 출력
    buffer[bufferPosition++] = data; // 수신 받은 데이터를 버퍼에 저장
  
    if(data == '1')// 블루투스를 통해 '1' 이 들어오면  
    {     
      curdata = 1;
    }
    else if(data == '2') // 블루투스를 통해 '1' 이 들어오면
    { 
      curdata = 2;
    }
    else if(data == '3') // 블루투스를 통해 '1' 이 들어오면
    { 
      curdata = 3;
    }
    else if(data == '4') // 블루투스를 통해 '1' 이 들어오면
    { 
      curdata = 4;
    }
    if(data == '\n'){ // 문자열 종료 표시
      buffer[bufferPosition] = '\0';
      bufferPosition = 0;
    }  
  }

  if(curdata == 1)// 블루투스를 통해 '1' 이 들어오면  
    {     
      sun_red();
    }
    else if(curdata == 2) // 블루투스를 통해 '1' 이 들어오면
    { 
      rain();
    }
    else if(curdata == 3) // 블루투스를 통해 '1' 이 들어오면
    { 
      cloudy();
    }
    else if(curdata == 4) // 블루투스를 통해 '1' 이 들어오면
    { 
      snowy();
    }
    
}
