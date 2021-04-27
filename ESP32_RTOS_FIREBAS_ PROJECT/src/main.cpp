#include <Arduino.h>
#include <Wire.h>
#include "TSl2581.h"
#include <esp32DHT.h>
#include <SPI.h>
#include <WiFi.h>
#include <Preferences.h>
#include "esp32-hal-ledc.h"
#include <PietteTech_DHT.h> 
#include <IOXhop_FirebaseESP32.h>
//================================================================================================================
#define WIFI_SSID             "coznanie"
#define WIFI_PASSWORD         "timur1972"
#define FIREBASE_HOST         "fir-smartbox.firebaseio.com"
#define FIREBASE_AUTH         "hfcdglIyq0IUd8je0vdZbxwleBjoUjANuTCeXiBY"
#define ROOT_PATH             "/usersInfoData"
#define USER_CONTROL_PATH     "/userControl"
#define USER_DATA_PATH        "/userData"
#define USER_ID               "/ADBQqlaM7Sa7zC6lP22xhjVP5VA2"
#define HUMIDITY              "/humidity"
#define TEMPERATURA           "/temperatura"
#define LIGHT                 "/light"
#define PWM_LEVEL             "/pwmLevel"
#define IS_CHECKED            "/isCheked"
//================================================================================================================
#define DHTTYPE         DHT22 // Sensor type DHT11/21/22/AM2301/AM2302
#define DHTPIN          15     // Digital pin for communications
#define SDA             21
#define SCL             22
#define PWM_PIN         26
#define PWM_RESOLUTION  16
#define PWM_FREQ        250
#define PWM_RES_TO_WIDE 65535
#define EXT_INT_PIN     13
//================================================================================================================
TaskHandle_t        TSL_task;
TaskHandle_t        DHT_task;
TaskHandle_t        wifi_task;
QueueHandle_t       xQueueTH;
QueueHandle_t       xQueueTSL;
SemaphoreHandle_t   xSemaphore = NULL;
//================================================================================================================
struct AMessage
{
  float temperatura;
  float humidity;
  char error_message[12];
} xMessageTH = {0, 0, " "};
//================================================================================================================
//declaration
void dht_wrapper(); // must be declared before the lib initialization
// Lib instantiate
PietteTech_DHT DHT(DHTPIN, DHTTYPE, dht_wrapper);
//TSL PIN SCL 22 SDA 21
//DTH PIN 15
//PWM PIN 26
//================================================================================================================
int TSL2581_INT = 17;
WaveShare_TSL2581 tsl = WaveShare_TSL2581();
//DHT22 sensor;
//================================================================================================================
Preferences preferences;
float TSL_data = 0;
uint16_t pwmLevel = 0, inputPwmLevel = 0;
bool previousSwitchState = false;
uint8_t switchData = 0;
//===============================================================================================================
void read_id(void)
{
  int id;
  int a;
  id = tsl.TSL2581_Read_ID();
  a = id & 0xf0;   //The lower four bits are the silicon version number
  if (!(a == 144)) //ID = 90H = 144D
    {
      Serial.println("false ");
    }
  else
    {
      Serial.print("I2C DEV is working ,id = ");
      Serial.println(id);
    }
}
//================================================================================================================
void Read_gpio_interrupt(uint16_t mindata, uint16_t maxdata)
{
  tsl.SET_Interrupt_Threshold(mindata, maxdata);
  int val = digitalRead(TSL2581_INT);
  if (val == 1)
  {
    //Serial.print("interrupt = 1 \n");
  }
  else
  {
    //Serial.print("interrupt = 0 \n");
    tsl.Reload_register();
  }
}
//================================================================================================================
void readDHT()
{
  //sensor.read();
}
//================================================================================================================
void TSL_task_func(void *parameter)
{
  unsigned long Lux;
  while (true)
  {
    //if (xSemaphoreTake(xSemaphore, (TickType_t)portMAX_DELAY))
    {
      tsl.TSL2581_Read_Channel();
      Lux = tsl.calculateLux(2, NOM_INTEG_CYCLE);
      Read_gpio_interrupt(2000, 50000);
      //Serial.print("Lux = ");
      //Serial.println(Lux);
      //Serial.print("TSL_task running on core ");
      //  "Блок loop() выполняется на ядре "
      //Serial.println(xPortGetCoreID());
      //xSemaphoreGive(xSemaphore);
      if (xQueueTSL != NULL)
      {
        // Отправить указатель на объект struct AMessage. Не блокируйте, если
        //очередь уже заполнена.
        TSL_data = Lux;
        xQueueSend(xQueueTSL, (void *)&TSL_data, portMAX_DELAY);
      }
    }
    vTaskDelay(pdMS_TO_TICKS(1000));
  }
}
//================================================================================================================
bool cpuStatus = false;
void DHT_task_func(void *parameter)
{
  while (true)
  {
    if(cpuStatus==false){
      cpuStatus = true;
      Serial.print("CPU FREQ = ");
      uint32_t freq = getCpuFrequencyMhz();
      Serial.println(freq);
      freq = getApbFrequency();
      Serial.print("APB FREQ = ");
      Serial.println(freq);
    }
    //if (xSemaphoreTake(xSemaphore, (TickType_t)portMAX_DELAY))
    {
      //  readDHT();
      //  sensor.onData([](float humidity, float temperatura) {
      //   xMessageTH.humidity = humidity;
      //   xMessageTH.temperatura = temperatura;
      //   Serial.printf("Temp: %gC\nHumid: %g%%\n",  xMessageTH.temperatura,  xMessageTH.humidity);
      // });
      // sensor.onError([](uint8_t error) {
      //   //Serial.printf("Sensor error: %s\n", sensor.getError());
      //   strcpy(xMessageTH.error_message, sensor.getError());
      // });
      int result = DHT.acquireAndWait(0);
#ifdef debug
      switch (result)
      {
      case DHTLIB_OK:
        Serial.println("Measuring DHT OK");
        break;
      case DHTLIB_ERROR_CHECKSUM:
        Serial.println("Error\n\r\tChecksum error");
        break;
      case DHTLIB_ERROR_ISR_TIMEOUT:
        Serial.println("Error\n\r\tISR time out error");
        break;
      case DHTLIB_ERROR_RESPONSE_TIMEOUT:
        Serial.println("Error\n\r\tResponse time out error");
        break;
      case DHTLIB_ERROR_DATA_TIMEOUT:
        Serial.println("Error\n\r\tData time out error");
        break;
      case DHTLIB_ERROR_ACQUIRING:
        Serial.println("Error\n\r\tAcquiring");
        break;
      case DHTLIB_ERROR_DELTA:
        Serial.println("Error\n\r\tDelta time to small");
        break;
      case DHTLIB_ERROR_NOTSTARTED:
        Serial.println("Error\n\r\tNot started");
        break;
      default:
        Serial.println("Unknown error");
        break;
      }
#endif
#ifdef debug
      Serial.print("Humidity (%): ");
#endif
      xMessageTH.humidity = DHT.getHumidity();
#ifdef debug
      Serial.println(xMessageTH.humidity, 2);
      Serial.print("Temperature (oC): ");
#endif
      xMessageTH.temperatura = DHT.getCelsius();
#ifdef debug
      Serial.println(xMessageTH.temperatura, 2);
#endif
      //Serial.print("DHT_task running on core ");
      //  "Блок loop() выполняется на ядре "
      //Serial.println(xPortGetCoreID());
      if (xQueueTH != NULL)
      {
        // Отправить указатель на объект struct AMessage. Не блокируйте, если
        //очередь уже заполнена.
        xQueueSend(xQueueTH, (void *)&xMessageTH, portMAX_DELAY);
      }
    }
    vTaskDelay(pdMS_TO_TICKS(1000));
  }
}
//================================================================================================================
void readMeasuredData()
{
  if (xQueueTH != NULL)
  {
    if (xQueueReceive(xQueueTH, &xMessageTH, pdMS_TO_TICKS(100)) == pdPASS)
    {
      #ifdef debug
       Serial.printf("Temp: %.1fC\nHumid: %.1f%%\n", xMessageTH.temperatura, xMessageTH.humidity);
      Serial.printf("Sensor status  %S\n", xMessageTH.error_message);
      #endif
    }
  }

  if (xQueueTSL != NULL)
  {
    if (xQueueReceive(xQueueTSL, &(TSL_data), pdMS_TO_TICKS(100)) == pdPASS)
    {
      #ifdef debug
      Serial.printf("TSL value: %.1f Lux \n", TSL_data);
      #endif
    }
  }
}
//============================================================================================================
void SavePwmData(uint16_t data)
{
  preferences.begin("data", false);
  preferences.putUShort("pwmLevel", data);
  preferences.end();
  Serial.print("Save pwmLevel to flash ");
  Serial.println(data);
}
//============================================================================================================
uint16_t ReadPwmData()
{
  uint16_t tmp = 0;
  preferences.begin("data", false);
  tmp = (uint8_t)preferences.getUShort("pwmLevel");
  preferences.end();
  return tmp;
}
//============================================================================================================
long convert(long x, long in_min, long in_max, long out_min, long out_max)
{
   return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}
//============================================================================================================
//#define debug
void WritePwmToOut(uint32_t pwmLevel)
{  
  uint32_t tmp = convert(pwmLevel,0,100,0,PWM_RES_TO_WIDE);
#ifdef debug
  Serial.print("WritePwmToOut: ");
  Serial.println(tmp);
#endif
  ledcWrite(0, tmp);
}

//=============================================================================================================
#define UNKNOWN 0
#define CLICK_SWITCH_ON 1
#define CLICK_SWITCH_OFF 2
uint8_t PWM_LEVEL_STATE = UNKNOWN;
int status = WL_IDLE_STATUS;
//=============================================================================================================
String sumChar(const char *first,const char *second,const char *third ,const char *fourth){
     
    return ( String(first)+ String(second) + String(third) + String(fourth));
}
//================================================================================================================
void connectToFireBase(){
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    Serial.print("Connecting to Wi-Fi");
    while (WiFi.status() != WL_CONNECTED)
    {
      Serial.print(".");
      vTaskDelay(pdMS_TO_TICKS(500));
    }
    Serial.println();
    Serial.print("Connected with IP: ");
    Serial.println(WiFi.localIP());
    Serial.println();
    Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
}
//=============================================================================================================
void writeDataToFireBase()
{
  String tmp = "";
  char buff[12] = "";
  tmp = "";
  tmp = sumChar(ROOT_PATH, USER_ID, USER_DATA_PATH, HUMIDITY);
  if(xMessageTH.humidity < 0){
    xMessageTH.humidity = 0.0;
  }
  snprintf (buff, sizeof(buff), "%.1f", xMessageTH.humidity);
  Firebase.setString(tmp, buff);
  #ifdef debug
  Serial.print("Humidity ");
  Serial.println(buff);
  #endif
  tmp = "";
  tmp = sumChar(ROOT_PATH, USER_ID, USER_DATA_PATH, TEMPERATURA);
  if(xMessageTH.temperatura < 0){
    xMessageTH.temperatura = 0.0;
  }
  snprintf (buff, sizeof(buff), "%.1f", xMessageTH.temperatura);
  Firebase.setString(tmp, buff);
  #ifdef debug
  Serial.print("Temperatura ");
  Serial.println(buff);
  #endif
  tmp = "";
  tmp = sumChar(ROOT_PATH, USER_ID, USER_DATA_PATH, LIGHT);
  if(TSL_data < 0){
    TSL_data = 0.0;
  }
  snprintf (buff, sizeof(buff), "%.1f", TSL_data);
  Firebase.setString(tmp,buff );
  #ifdef debug
  Serial.print("Light ");
  Serial.println(buff);
  #endif
  //vTaskDelay(pdMS_TO_TICKS(100));
}
//=============================================================================================================

void readDataFromFireBase()
{
  String tmp = "";
  String pwmData = "";
  char buff[12] = "";
  tmp = "";
  pwmData = "";
  tmp = sumChar(ROOT_PATH, USER_ID, USER_CONTROL_PATH, PWM_LEVEL);
  pwmData = Firebase.getString(tmp);
  pwmLevel = pwmData.toInt();
  #ifdef debug
  Serial.print("pwmLevel ");
  Serial.println(pwmLevel);
  #endif
  tmp = "";
  pwmData = "";
  tmp = sumChar(ROOT_PATH, USER_ID, USER_CONTROL_PATH, IS_CHECKED);
  pwmData = Firebase.getString(tmp);
  if (pwmData.equals("checked"))
  {
    switchData = 1;
  }else if (pwmData.equals("unchecked"))
  {
    switchData = 0;   
  }
  #ifdef debug
  Serial.print("isChecked ");
  Serial.println(switchData);
  #endif
  switch (PWM_LEVEL_STATE)
  {
  case UNKNOWN:
    if (switchData == 1)
    {
      PWM_LEVEL_STATE = CLICK_SWITCH_ON;
    }
    break;
  case  CLICK_SWITCH_ON:
    if (switchData == 0)
    {
      PWM_LEVEL_STATE = CLICK_SWITCH_OFF;
    }
    break;
  case CLICK_SWITCH_OFF:
    SavePwmData(pwmLevel);
    PWM_LEVEL_STATE = UNKNOWN;
    break;
  }
  tmp = "";
  tmp = sumChar(ROOT_PATH, USER_ID, USER_CONTROL_PATH, PWM_LEVEL);
}
//=============================================================================================================
void wifiTask_func(void *pvParam)
{
  connectToFireBase();
  while (WiFi.status() == WL_CONNECTED)
  {
    readMeasuredData();
    writeDataToFireBase();
    readDataFromFireBase();
    WritePwmToOut(pwmLevel);
  }
}
//================================================================================================================
void dht_wrapper()
{
  DHT.isrCallback();
}
//================================================================================================================

//================================================================================================================
#define RXD2 16
#define TXD2 17

void setup(void)
{   
  //Serial2.begin(9600, SERIAL_8N1, RXD2, TXD2);
  pinMode(PWM_PIN, OUTPUT);
  ledcSetup(0, PWM_FREQ, PWM_RESOLUTION); //0 - channel, 250 - HZ, 8 - rsolution
  ledcAttachPin(PWM_PIN, 0);
  Serial.begin(115200);
  Serial.print("Read data from flash...  ");
  pwmLevel = (uint16_t)ReadPwmData();
  Serial.print("PWM LEVEL =  ");
  Serial.println(pwmLevel);/* Use 1st timer of 4 */
  WritePwmToOut(pwmLevel);
  //sensor.setup(15);            // pin 2 is DATA, RMT channel defaults to channel 0 and 1
  Wire.begin(SDA, SCL);          //i2c config
  pinMode(TSL2581_INT, INPUT); // sets the digital pin 7 as input
  /* Setup the sensor power on */
  tsl.TSL2581_power_on();
  //  /* Setup the sensor gain and integration time */
  tsl.TSL2581_config();
  read_id();    

  xTaskCreatePinnedToCore(
      TSL_task_func, /* Функция, содержащая код задачи */
      "TSL_task",    /* Название задачи */
      4096,          /* Размер стека в словах */
      NULL,          /* Параметр создаваемой задачи */
      0,             /* Приоритет задачи */
      &TSL_task,     /* Идентификатор задачи */
      0);            /* Ядро, на котором будет выполняться задача */
  xTaskCreatePinnedToCore(
      DHT_task_func, /* Функция, содержащая код задачи */
      "DHT_task",    /* Название задачи */
      4096,          /* Размер стека в словах */
      NULL,          /* Параметр создаваемой задачи */
      0,             /* Приоритет задачи */
      &DHT_task,     /* Идентификатор задачи */
      0);            /* Ядро, на котором будет выполняться задача */
  xTaskCreatePinnedToCore(
      wifiTask_func, /* Функция, содержащая код задачи */
      "wifi_task",   /* Название задачи */
      8096,          /* Размер стека в словах */
      NULL,          /* Параметр создаваемой задачи */
      0,             /* Приоритет задачи */
      &wifi_task,    /* Идентификатор задачи */
      1);            /* Ядро, на котором будет выполняться задача */
  xQueueTSL = xQueueCreate(10, sizeof(TSL_data));
  xQueueTH = xQueueCreate(10, sizeof(xMessageTH));
  //xSemaphore = xSemaphoreCreateCounting(2, 0);
}
//================================================================================================================
void loop(void)
{
  vTaskDelete(NULL);
}
//================================================================================================================