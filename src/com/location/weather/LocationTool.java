package com.location.weather;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.util.Log;

public class LocationTool {
	private static HttpClient httpClient;
	private static HttpGet httpGet;
	private static HttpResponse httpResponse;
	//通过百度反地址解析
	public static AddressAttribute fromLLToAddress(double longitude,double latitude)
	{
		try
		{
			httpClient = new DefaultHttpClient();
			HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000); // 设置连接超时
			HttpConnectionParams.setSoTimeout(httpClient.getParams(), 10000); // 设置数据读取时间超时
			HttpClientParams.setCookiePolicy(httpClient.getParams(), CookiePolicy.BROWSER_COMPATIBILITY);
			ConnManagerParams.setTimeout(httpClient.getParams(), 10000); // 设置从连接池中取连接超时
			String url = "http://api.map.baidu.com/geocoder/v2/?location="+latitude+","+longitude+"&output=json&ak=2565b50d0e68a921715390c425794b56&callback=showLocation";
			httpGet = new HttpGet(url);
			httpResponse = httpClient.execute(httpGet);	
			if(httpResponse.getStatusLine().getStatusCode()==200)
			{
				String result = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
				AddressAttribute address = reverseParseAddress(result);
				return address;
			}
			else return null;
		} 
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (IOException e) 
		{
			e.printStackTrace();
			return null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	private static AddressAttribute reverseParseAddress(String result)
	{
		if(result == null) return null;
		try
		{
			int start = result.indexOf("{\"status\"");
			int end = result.indexOf(")");
			if(start == -1 || end == -1) return null;
			result = result.substring(start,end);
			JSONObject jsonObject = new JSONObject(result);
			String status = jsonObject.getString("status");
			if(!"0".equals(status)) return null;
			jsonObject = new JSONObject(result).getJSONObject("result").getJSONObject("addressComponent");
			String city = jsonObject.getString("city");
			String district = jsonObject.getString("district");
			if(city==null&&district==null) return null;
			int end1 = city.indexOf("市");
			if(end1!=-1) city = city.substring(0, end1);
			int end2 = district.indexOf("县");
			if(end2!=-1) district = district.substring(0, end2);
			else
			{
				int end3 = district.indexOf("区");
				if(end3!=-1) district = district.substring(0, end3);
			}
			return new AddressAttribute(city,district);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
