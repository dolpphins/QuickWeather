package com.data.weather;

public class CityAttribute {
	public String cityName;
	public String province;
	public String cityId;
	public String district;
	public CityAttribute(String province,String cityName,String district,String cityId)
	{
		this.province = province;
		this.cityName = cityName;
		this.district = district;
		this.cityId = cityId;
	}
	public CityAttribute(){}
}
