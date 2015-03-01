package com.weatherinfo.weather;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.util.Log;

import org.json.*;

import com.main.weather.MainActivity;

import android.widget.Toast;

public class WeatherRequest {
	private static String tag="WeatherRequest";
	private static HttpClient httpClient;
	private static HttpGet httpget;
	//��ȡԭʼjson����
	public static String getWeatherInfor(String cityId,Context context)
	{
		//�ж������Ƿ����
		if(context!=null)
		{
			ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netWorkInfor=null;
			if(connectivityManager!=null) 
			{
				netWorkInfor=connectivityManager.getActiveNetworkInfo();
			}
			if(netWorkInfor==null)
			{
				return null;
			}
			else
			{
				if(!netWorkInfor.isAvailable()) 
				{
					return null;
				}
				else
				{
					httpClient = new DefaultHttpClient(); // �½�HttpClient����
					HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000); // �������ӳ�ʱ
					HttpConnectionParams.setSoTimeout(httpClient.getParams(), 10000); // �������ݶ�ȡʱ�䳬ʱ
					ConnManagerParams.setTimeout(httpClient.getParams(), 10000); // ���ô����ӳ���ȡ���ӳ�ʱ
			
					String url="http://weatherapi.market.xiaomi.com/wtr-v2/weather?cityId="+cityId+"&imei=e32c8a29d0e8633283737f5d9f381d47&device=HM2013023&miuiVersion=JHBCNBD16.0&modDevice=&source=miuiWeatherApp";
				    httpget = new HttpGet(url); // ��ȡ����
				    try {
						HttpResponse response = httpClient.execute(httpget); // ִ�����󣬻�ȡ��Ӧ���
						if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) { // ��Ӧͨ��
							String result = EntityUtils.toString(response.getEntity(),"UTF-8");
							return result;
						} else {
							return null;
						}
					} catch (ClientProtocolException e) {
						e.printStackTrace();
						return null;
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
				}
			}
		}
		return null;
	}
	public static WeatherAttribute parseJson(String rawResult)
	{
		WeatherAttribute tempWeatherAttribute=new WeatherAttribute();
		try {
			JSONObject jsonObject=new JSONObject(rawResult).getJSONObject("forecast");
			if(jsonObject.has("city")) 
			{
				tempWeatherAttribute.cityName=jsonObject.getString("city");
			}
			else 
			{
				tempWeatherAttribute.cityName="������";
			}
			if(jsonObject.has("cityid")) 
			{
				tempWeatherAttribute.cityId=jsonObject.getString("cityid");
			}
			else 
			{
				tempWeatherAttribute.cityId="������";
			}
			if(jsonObject.has("temp1")) 
			{
				tempWeatherAttribute.temperatureRange=jsonObject.getString("temp1");
			}
			else 
			{
				tempWeatherAttribute.temperatureRange="������";
			}
			jsonObject=new JSONObject(rawResult).getJSONObject("realtime");
			if(jsonObject.has("temp")) 
			{
				tempWeatherAttribute.realtimeTemperature=jsonObject.getString("temp")+"��c";
			}
			else 
			{
				tempWeatherAttribute.realtimeTemperature="������";
			}
			if(jsonObject.has("weather")) 
			{
				tempWeatherAttribute.status=jsonObject.getString("weather");
			}
			else 
			{
				tempWeatherAttribute.status="������";
			}
			if(jsonObject.has("SD")) 
			{
				tempWeatherAttribute.humidity=jsonObject.getString("SD");
			}
			else 
			{
				tempWeatherAttribute.humidity="������";
			}
			
			if(jsonObject.has("WS")) 
			{
				tempWeatherAttribute.windPower=jsonObject.getString("WS");
			}
			else 
			{
				tempWeatherAttribute.windPower="������";
			}
			if(jsonObject.has("WD")) 
			{
				tempWeatherAttribute.windDirection=jsonObject.getString("WD");
			}
			else 
			{
				tempWeatherAttribute.windDirection="������";
			}
			if(jsonObject.has("time"))
			{
				tempWeatherAttribute.updateTime=jsonObject.getString("time");
			}
			else 
			{
				tempWeatherAttribute.updateTime="������";
			}
			jsonObject=new JSONObject(rawResult).getJSONObject("aqi");
			if(jsonObject.has("aqi")) 
			{
				tempWeatherAttribute.airQuality=jsonObject.getString("aqi");
			}
			else 
			{
				tempWeatherAttribute.airQuality="������";
			}
			if(jsonObject.has("pm25")) 
			{
				tempWeatherAttribute.PM=jsonObject.getString("pm25");
			}
			else 
			{
				tempWeatherAttribute.PM="������";
			}
			jsonObject=new JSONObject(rawResult).getJSONObject("today");			
			if(jsonObject.has("date")) 
			{
				tempWeatherAttribute.date=jsonObject.getString("date");
			}
			else 
			{
				tempWeatherAttribute.date="������";
			}
			return tempWeatherAttribute;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
