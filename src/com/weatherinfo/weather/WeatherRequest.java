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
	//获取原始json数据
	public static String getWeatherInfor(String cityId,Context context)
	{
		//判断网络是否可用
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
					httpClient = new DefaultHttpClient(); // 新建HttpClient对象
					HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000); // 设置连接超时
					HttpConnectionParams.setSoTimeout(httpClient.getParams(), 10000); // 设置数据读取时间超时
					ConnManagerParams.setTimeout(httpClient.getParams(), 10000); // 设置从连接池中取连接超时
			
					String url="http://weatherapi.market.xiaomi.com/wtr-v2/weather?cityId="+cityId+"&imei=e32c8a29d0e8633283737f5d9f381d47&device=HM2013023&miuiVersion=JHBCNBD16.0&modDevice=&source=miuiWeatherApp";
				    httpget = new HttpGet(url); // 获取请求
				    try {
						HttpResponse response = httpClient.execute(httpget); // 执行请求，获取响应结果
						if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) { // 响应通过
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
				tempWeatherAttribute.cityName="无数据";
			}
			if(jsonObject.has("cityid")) 
			{
				tempWeatherAttribute.cityId=jsonObject.getString("cityid");
			}
			else 
			{
				tempWeatherAttribute.cityId="无数据";
			}
			if(jsonObject.has("temp1")) 
			{
				tempWeatherAttribute.temperatureRange=jsonObject.getString("temp1");
			}
			else 
			{
				tempWeatherAttribute.temperatureRange="无数据";
			}
			jsonObject=new JSONObject(rawResult).getJSONObject("realtime");
			if(jsonObject.has("temp")) 
			{
				tempWeatherAttribute.realtimeTemperature=jsonObject.getString("temp")+"°c";
			}
			else 
			{
				tempWeatherAttribute.realtimeTemperature="无数据";
			}
			if(jsonObject.has("weather")) 
			{
				tempWeatherAttribute.status=jsonObject.getString("weather");
			}
			else 
			{
				tempWeatherAttribute.status="无数据";
			}
			if(jsonObject.has("SD")) 
			{
				tempWeatherAttribute.humidity=jsonObject.getString("SD");
			}
			else 
			{
				tempWeatherAttribute.humidity="无数据";
			}
			
			if(jsonObject.has("WS")) 
			{
				tempWeatherAttribute.windPower=jsonObject.getString("WS");
			}
			else 
			{
				tempWeatherAttribute.windPower="无数据";
			}
			if(jsonObject.has("WD")) 
			{
				tempWeatherAttribute.windDirection=jsonObject.getString("WD");
			}
			else 
			{
				tempWeatherAttribute.windDirection="无数据";
			}
			if(jsonObject.has("time"))
			{
				tempWeatherAttribute.updateTime=jsonObject.getString("time");
			}
			else 
			{
				tempWeatherAttribute.updateTime="无数据";
			}
			jsonObject=new JSONObject(rawResult).getJSONObject("aqi");
			if(jsonObject.has("aqi")) 
			{
				tempWeatherAttribute.airQuality=jsonObject.getString("aqi");
			}
			else 
			{
				tempWeatherAttribute.airQuality="无数据";
			}
			if(jsonObject.has("pm25")) 
			{
				tempWeatherAttribute.PM=jsonObject.getString("pm25");
			}
			else 
			{
				tempWeatherAttribute.PM="无数据";
			}
			jsonObject=new JSONObject(rawResult).getJSONObject("today");			
			if(jsonObject.has("date")) 
			{
				tempWeatherAttribute.date=jsonObject.getString("date");
			}
			else 
			{
				tempWeatherAttribute.date="无数据";
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
