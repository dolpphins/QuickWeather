package com.weatherinfo.weather;

import java.io.Serializable;

public class WeatherAttribute implements Serializable{
	public String cityName;//城市名
	public String realtimeTemperature;//实时温度
	public String status;//天气状况
	public String temperatureRange;//温度范围
	public String windPower;//风力
	public String windDirection;//风向
	public String airQuality;//空气质量
	public String updateTime;//发布时间
	public String date;//当天日期
	public String PM;//PM2.5
	public String humidity;//湿度
	public String Id;
	public String province;
	public String cityId;
}
