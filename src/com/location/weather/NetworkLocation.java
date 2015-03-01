package com.location.weather;

import com.data.weather.CityAttribute;
import com.data.weather.MySQLite;
import com.main.weather.ChangeCityActivity;
import com.main.weather.MainActivity;
import com.weatherinfo.weather.FiveDayWeatherAttribute;
import com.weatherinfo.weather.ForecastFiveDayRequest;
import com.weatherinfo.weather.WeatherAttribute;
import com.weatherinfo.weather.WeatherRequest;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

public class NetworkLocation {
	private String tag = "NetworkLocation";
	private double longitude=-1;
	private double latitude=-1;
	private LocationManager locationManager;
	private Context context;
	private static WeatherAttribute currentWeatherAttibute = new WeatherAttribute();
	private FiveDayWeatherAttribute currentFiveDayWeatherAttibute = new FiveDayWeatherAttribute();
	private boolean notLock = true;//保证最多只能有一个子线程在获取天气数据
	public NetworkLocation(Context context)
	{
		this.context = context;
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}
	public double getLongitude() {return longitude;}
	public double getLatitude() {return latitude;}
	class MyLocation implements LocationListener
	{

		@Override
		public void onLocationChanged(Location location) {
			longitude = location.getLongitude();
			latitude = location.getLatitude();
			if(notLock&&longitude!=-1&&latitude!=-1) getLocalInfo();
		}

		@Override
		public void onProviderDisabled(String location) {
	
		}

		@Override
		public void onProviderEnabled(String location) {
	
		}

		@Override
		public void onStatusChanged(String location, int status, Bundle arg2) {

		}
	}
	public void startListenLocationChange()
	{
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, new MyLocation());
	}
	private void getLocalInfo()
    {
		new Thread(new Runnable(){
			public void run()
			{
				notLock = false;
				try
				{
					ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo netWorkInfor=null;
					if(connectivityManager!=null) 
					{
						netWorkInfor=connectivityManager.getActiveNetworkInfo();
					}
					if(netWorkInfor==null)
					{
						notLock = true;
						return;
					}
					else
					{
						if(!netWorkInfor.isAvailable()) 
						{
							notLock = true;
							return;
						}
						else
						{
							AddressAttribute address = LocationTool.fromLLToAddress(longitude, latitude);
							CityAttribute cityAttribute = MySQLite.getCity(context, address.city);
							//如果是当前位置就直接返回
							if(cityAttribute.cityName.equals(currentWeatherAttibute.cityName))
							{
								notLock = true;
								return;
							}
							String rawResult = WeatherRequest.getWeatherInfor(cityAttribute.cityId, context);
							WeatherAttribute result = WeatherRequest.parseJson(rawResult);
							FiveDayWeatherAttribute fiveday = ForecastFiveDayRequest.getFiveDayWeatherInfor(cityAttribute.cityId, context);
							//通知主程序自动定成功并获取天气信息成功
							if(result!=null&&fiveday!=null)
							{
								currentWeatherAttibute = result;
								currentFiveDayWeatherAttibute = fiveday;
								MainActivity.handler.sendEmptyMessage(25);
							}
							notLock = true;
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
					notLock = true;
				}
			}
		}).start();
    }
	public static WeatherAttribute getCurrentWeatherAttibute()
	{
		return currentWeatherAttibute;
	}
	public FiveDayWeatherAttribute getCurrentFiveDayWeatherAttibute()
	{
		return currentFiveDayWeatherAttibute;
	}
	
}
