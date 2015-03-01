package com.weatherinfo.weather;

import java.util.Calendar;

import org.json.JSONObject;

import com.main.weather.MainActivity;

import android.content.Context;
import android.util.Log;

public class ForecastFiveDayRequest {
	private static String tag="ForecastFiveDayRequest"; 
	public static FiveDayWeatherAttribute getFiveDayWeatherInfor(String cityId,Context context)
	{
		String rawResult = WeatherRequest.getWeatherInfor(cityId,context);
		if(rawResult==null) 
		{
			return null;
		}
		try
		{
			FiveDayWeatherAttribute fiveDayWeatherAttribute=new FiveDayWeatherAttribute();
			JSONObject jsonObject = new JSONObject(rawResult).getJSONObject("forecast");
			String tomorrow = jsonObject.getString("week");
			Calendar calendar = Calendar.getInstance();
			int index = calendar.get(Calendar.DAY_OF_WEEK);
			String[] week={"����","��һ","�ܶ�","����","����","����","����"};
			//��������
			setWeek(week[index-1],fiveDayWeatherAttribute.weekArray);
			jsonObject = new JSONObject(rawResult).getJSONObject("today");
			String[] today = jsonObject.getString("date").split("-");
			//��������
			setDate(today,fiveDayWeatherAttribute.dayArray);
			
			
			//��������״�����¶�
			jsonObject = new JSONObject(rawResult).getJSONObject("realtime");
			String[] time=jsonObject.getString("time").split(":");
			jsonObject = new JSONObject(rawResult).getJSONObject("yestoday");
			if(Integer.parseInt(time[0])<18)
			{
				//�����ȡ��json����
				jsonObject = new JSONObject(rawResult).getJSONObject("forecast");
				fiveDayWeatherAttribute.dayStatusArray[1]=jsonObject.getString("img_title2");
				fiveDayWeatherAttribute.dayStatusArray[2]=jsonObject.getString("img_title4");
				fiveDayWeatherAttribute.dayStatusArray[3]=jsonObject.getString("img_title6");
				fiveDayWeatherAttribute.dayStatusArray[4]=jsonObject.getString("img_title8");
				fiveDayWeatherAttribute.dayStatusArray[5]=jsonObject.getString("img_title10");
				fiveDayWeatherAttribute.nightStatusArray[0]=jsonObject.getString("img_title1");
				fiveDayWeatherAttribute.nightStatusArray[1]=jsonObject.getString("img_title3");
				fiveDayWeatherAttribute.nightStatusArray[2]=jsonObject.getString("img_title5");
				fiveDayWeatherAttribute.nightStatusArray[3]=jsonObject.getString("img_title7");
				fiveDayWeatherAttribute.nightStatusArray[4]=jsonObject.getString("img_title9");
				fiveDayWeatherAttribute.nightStatusArray[5]=jsonObject.getString("img_title11");
				jsonObject = new JSONObject(rawResult).getJSONObject("yestoday");
				fiveDayWeatherAttribute.dayStatusArray[0]=jsonObject.getString("weatherStart");
				
				String[] temp=new String[12];
				temp[0]=jsonObject.getString("tempMax")+"��c";
				temp[1]=jsonObject.getString("tempMin")+"��c";
				jsonObject = new JSONObject(rawResult).getJSONObject("forecast");
				temp[2]=jsonObject.getString("temp1").split("~")[0];
				temp[3]=jsonObject.getString("temp1").split("~")[1];
				temp[4]=jsonObject.getString("temp2").split("~")[0];
				temp[5]=jsonObject.getString("temp2").split("~")[1];
				temp[6]=jsonObject.getString("temp3").split("~")[0];
				temp[7]=jsonObject.getString("temp3").split("~")[1];
				temp[8]=jsonObject.getString("temp4").split("~")[0];
				temp[9]=jsonObject.getString("temp4").split("~")[1];
				temp[10]=jsonObject.getString("temp5").split("~")[0];
				temp[11]=jsonObject.getString("temp5").split("~")[1];
				fiveDayWeatherAttribute.maxTempArray[0]=temp[0];
				fiveDayWeatherAttribute.maxTempArray[1]=temp[2].substring(0, temp[2].length()-1)+"��c";
				fiveDayWeatherAttribute.maxTempArray[2]=temp[4].substring(0, temp[4].length()-1)+"��c";
				fiveDayWeatherAttribute.maxTempArray[3]=temp[6].substring(0, temp[6].length()-1)+"��c";
				fiveDayWeatherAttribute.maxTempArray[4]=temp[8].substring(0, temp[8].length()-1)+"��c";
				fiveDayWeatherAttribute.maxTempArray[5]=temp[10].substring(0, temp[10].length()-1)+"��c";
				fiveDayWeatherAttribute.minTempArray[0]=temp[1];
				fiveDayWeatherAttribute.minTempArray[1]=temp[3].substring(0, temp[3].length()-1)+"��c";
				fiveDayWeatherAttribute.minTempArray[2]=temp[5].substring(0, temp[5].length()-1)+"��c";
				fiveDayWeatherAttribute.minTempArray[3]=temp[7].substring(0, temp[7].length()-1)+"��c";
				fiveDayWeatherAttribute.minTempArray[4]=temp[9].substring(0, temp[9].length()-1)+"��c";
				fiveDayWeatherAttribute.minTempArray[5]=temp[11].substring(0, temp[11].length()-1)+"��c";

			}
			else
			{
				//ҹ���ȡ��json����
				jsonObject = new JSONObject(rawResult).getJSONObject("yestoday");
				fiveDayWeatherAttribute.dayStatusArray[0]=jsonObject.getString("weatherStart");
				fiveDayWeatherAttribute.nightStatusArray[0]=jsonObject.getString("weatherEnd");
				jsonObject = new JSONObject(rawResult).getJSONObject("today");
				fiveDayWeatherAttribute.dayStatusArray[1]=jsonObject.getString("weatherStart");
				jsonObject = new JSONObject(rawResult).getJSONObject("forecast");
				fiveDayWeatherAttribute.nightStatusArray[1]=jsonObject.getString("img_title1");
				fiveDayWeatherAttribute.dayStatusArray[2]=jsonObject.getString("img_title2");
				fiveDayWeatherAttribute.nightStatusArray[2]=jsonObject.getString("img_title3");
				fiveDayWeatherAttribute.dayStatusArray[3]=jsonObject.getString("img_title4");
				fiveDayWeatherAttribute.nightStatusArray[3]=jsonObject.getString("img_title5");
				fiveDayWeatherAttribute.dayStatusArray[4]=jsonObject.getString("img_title6");
				fiveDayWeatherAttribute.nightStatusArray[4]=jsonObject.getString("img_title7");
				fiveDayWeatherAttribute.dayStatusArray[5]=jsonObject.getString("img_title8");
				fiveDayWeatherAttribute.nightStatusArray[5]=jsonObject.getString("img_title9");
				
				String[] temp=new String[12];
				jsonObject = new JSONObject(rawResult).getJSONObject("yestoday");
				temp[0]=jsonObject.getString("tempMax")+"��c";
				temp[1]=jsonObject.getString("tempMin")+"��c";
				jsonObject = new JSONObject(rawResult).getJSONObject("today");
				temp[2]=jsonObject.getString("tempMax")+"��c";
				jsonObject = new JSONObject(rawResult).getJSONObject("forecast");
				temp[3]=jsonObject.getString("temp1").split("~")[0];
				temp[4]=jsonObject.getString("temp1").split("~")[1];
				temp[5]=jsonObject.getString("temp2").split("~")[0];
				temp[6]=jsonObject.getString("temp2").split("~")[1];
				temp[7]=jsonObject.getString("temp3").split("~")[0];
				temp[8]=jsonObject.getString("temp3").split("~")[1];
				temp[9]=jsonObject.getString("temp4").split("~")[0];
				temp[10]=jsonObject.getString("temp4").split("~")[1];
				temp[11]=jsonObject.getString("temp5").split("~")[0];
				fiveDayWeatherAttribute.maxTempArray[0]=temp[0];
				fiveDayWeatherAttribute.maxTempArray[1]=temp[2];
				fiveDayWeatherAttribute.maxTempArray[2]=temp[4].substring(0, temp[4].length()-1)+"��c";
				fiveDayWeatherAttribute.maxTempArray[3]=temp[6].substring(0, temp[6].length()-1)+"��c";
				fiveDayWeatherAttribute.maxTempArray[4]=temp[8].substring(0, temp[8].length()-1)+"��c";
				fiveDayWeatherAttribute.maxTempArray[5]=temp[10].substring(0, temp[10].length()-1)+"��c";
				fiveDayWeatherAttribute.minTempArray[0]=temp[1];
				fiveDayWeatherAttribute.minTempArray[1]=temp[3].substring(0, temp[3].length()-1)+"��c";
				fiveDayWeatherAttribute.minTempArray[2]=temp[5].substring(0, temp[5].length()-1)+"��c";
				fiveDayWeatherAttribute.minTempArray[3]=temp[7].substring(0, temp[7].length()-1)+"��c";
				fiveDayWeatherAttribute.minTempArray[4]=temp[9].substring(0, temp[9].length()-1)+"��c";
				fiveDayWeatherAttribute.minTempArray[5]=temp[11].substring(0, temp[11].length()-1)+"��c";
				
			}
			
			return fiveDayWeatherAttribute;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	public static void setWeek(String todayWeek,String[] str) throws Exception
	{
		str[0]="����";str[1]="����";
		if("��һ".equals(todayWeek))
		{
			str[2]="�ܶ�";str[3]="����";str[4]="����";str[5]="����";
		}
		else if("�ܶ�".equals(todayWeek))
		{
			str[2]="����";str[3]="����";str[4]="����";str[5]="����";
		}
		else if("����".equals(todayWeek))
		{
			str[2]="����";str[3]="����";str[4]="����";str[5]="����";
		}
		else if("����".equals(todayWeek))
		{
			str[2]="����";str[3]="����";str[4]="����";str[5]="��һ";
		}
		else if("����".equals(todayWeek))
		{
			str[2]="����";str[3]="����";str[4]="��һ";str[5]="�ܶ�";
		}
		else if("����".equals(todayWeek))
		{
			str[2]="����";str[3]="��һ";str[4]="�ܶ�";str[5]="����";
		}
		else if("����".equals(todayWeek))
		{
			str[2]="��һ";str[3]="�ܶ�";str[4]="����";str[5]="����";
		}
		else
		{
			throw new Exception();
		}
	}
	public static void setDate(String[] today,String[] str) throws Exception
	{
		String[] yesterdayArray=getPrevDay(today);
		str[0]=yesterdayArray[1]+"/"+yesterdayArray[2];//�õ���������
		String[] todayArray=getToday(today);
		str[1]=todayArray[1]+"/"+todayArray[2];//�õ���������
		String[] tomorrowArray=getNextDay(todayArray);
		str[2]=tomorrowArray[1]+"/"+tomorrowArray[2];//�õ���������
		String[] afterTomorrowArray=getNextDay(tomorrowArray);
		str[3]=afterTomorrowArray[1]+"/"+afterTomorrowArray[2];//�õ���������
		String[] dahoutianArray=getNextDay(afterTomorrowArray);
		str[4]=dahoutianArray[1]+"/"+dahoutianArray[2];//�õ����������
		String[] dadahoutianArray=getNextDay(dahoutianArray);
		str[5]=dadahoutianArray[1]+"/"+dadahoutianArray[2];//�õ�����������
	}
	private static String[] getNextDay(String[] today) throws Exception
	{
		int year=Integer.parseInt(today[0]);
		int month=Integer.parseInt(today[1]);
		int day=Integer.parseInt(today[2]);
		boolean isLeap=(year%4==0&&year%100!=0||year%400==0);
		if((month==1||month==3||month==5||month==7||month==8||month==10||month==12)&&day==31)
		{
			if(month==12)
			{
				return new String[]{(year+1)+"","1","1"};
			}
			else
			{
				return new String[]{year+"",(month+1)+"","1"};
			}
		}
		else if((month==4||month==6||month==9||month==11)&&day==30)
		{
			return new String[]{year+"",(month+1)+"","1"};
		}
		else if(isLeap&&month==2&&day==29)
		{
			return new String[]{year+"","3","1"};
		}
		else if(!isLeap&&month==2&&day==28)
		{
			return new String[]{year+"","3","1"};
		}
		else
		{
			return new String[]{year+"",month+"",(day+1)+""};
		}
	}
	private static String[] getToday(String[] today)
	{
		int year=Integer.parseInt(today[0]);
		int month=Integer.parseInt(today[1]);
		int day=Integer.parseInt(today[2]);
		return new String[]{year+"",month+"",day+""};
	}
	private static String[] getPrevDay(String[] today) throws Exception
	{
		int year=Integer.parseInt(today[0]);
		int month=Integer.parseInt(today[1]);
		int day=Integer.parseInt(today[2]);
		boolean isLeap=year%4==0&&year%100!=0||year%400==0;
		if(day==1)
		{
			if(month==1)
			{
				return new String[]{(year-1)+"","12","31"};
			}
			else if(month==2||month==4||month==6||month==8||month==9||month==11)
			{
				return new String[]{year+"",(month-1)+"","31"};
			}
			else if(month==5||month==7||month==10||month==12)
			{
				return new String[]{year+"",(month-1)+"","30"};
			}
			else if(month==3&&isLeap)
			{
				return new String[]{year+"","2"+"","29"};
			}
			else if(month==3&&!isLeap)
			{
				return new String[]{year+"","2"+"","28"};
			}
		}
		else
		{
			return new String[]{year+"",month+"",(day-1)+""};
		}
		throw new Exception();
	}
}
