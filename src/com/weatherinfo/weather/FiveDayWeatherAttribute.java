package com.weatherinfo.weather;

import java.io.Serializable;

public class FiveDayWeatherAttribute implements Serializable{
	public String[] weekArray = new String[6];
	public String[] dayArray = new String[6];
	public String[] dayStatusArray = new String[6];
	public String[] nightStatusArray = new String[6];
	public String[] maxTempArray = new String[6];
	public String[] minTempArray = new String[6];
}
