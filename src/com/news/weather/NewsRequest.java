package com.news.weather;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class NewsRequest {
	private static String tag = "NewsRequest";
	public static String getNewsListHtml(String urlString)
	{
		try {
			URL url=new URL(urlString);
			HttpURLConnection con=(HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setReadTimeout(10000);
			InputStream is = con.getInputStream();
			byte[] data = readInputStream(is);
			return new String(data);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (ProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	private static byte[] readInputStream(InputStream is) throws IOException
	{
		ByteArrayOutputStream byteArrayOs = new ByteArrayOutputStream();
		byte[] buffer=new byte[1024];
		int len=0;
		while((len=is.read(buffer))!=-1)
		{
			byteArrayOs.write(buffer, 0, len);
		}
		return byteArrayOs.toByteArray();
	}
	public static ArrayList<NewsAttribute> parseListHtml(String rawHtml)
	{
		try
		{
			if(rawHtml==null) return null;
			ArrayList<NewsAttribute> arrayList=new ArrayList<NewsAttribute>();
			String html="";
    		Pattern p=Pattern.compile("(<ul class=\"newList\"><li>(.*)</li></ul>)");
    		Matcher m=p.matcher(rawHtml);
    		if(m.find()) html=m.group();
    		else throw new Exception();
			Pattern pTitle=null,pUrl=null,pDate=null;
			Matcher mTitle=null,mUrl=null,mDate=null;
			//提取标题正则表达式
			pTitle=Pattern.compile("(?<=title=\")[[\\w-\\s][^x00-xff][：][:]]+(?=\")");
			mTitle=pTitle.matcher(html);
			//提取链接正则表达式
			pUrl=Pattern.compile("(?<=href=\")(.*?)(?=\")");
			mUrl=pUrl.matcher(html);
			//提取发布时间正则表达式
			pDate=Pattern.compile("(?<=<span>)(.*?)(?=</span>)");
			mDate=pDate.matcher(html);
			while(mTitle.find()&&mUrl.find()&&mDate.find())
			{
				NewsAttribute newsAttribute = new NewsAttribute();
				newsAttribute.title=mTitle.group();
				newsAttribute.url=mUrl.group();
				newsAttribute.updateTime=mDate.group();
				arrayList.add(newsAttribute);
			}
			return arrayList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	public static String getContentHtml(NewsAttribute newsAttribute)
	{
		try
		{
			String url=newsAttribute.url;
			String contentHtml=getNewsListHtml(url);
			return contentHtml;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	public static String getContentHtml(String url)
	{
		try
		{
			String contentHtml=getNewsListHtml(url);
			return contentHtml;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	public static String parseContentHtml(String contentHtml)
	{
		try
		{
			String str1=contentHtml;
			int start1=str1.indexOf("<div class=\"content_doc\">");
			int end1=str1.indexOf("<div class=\"content_pages\">");
			if(start1==-1||end1==-1) return null;
			String str2=str1.substring(start1, end1);
			int start2=str2.indexOf("<dl class=\"doctools\">");
			int end2=str2.indexOf("</dl>");
			if(start2==-1||end2==-1) return null;
			String str3=str2.substring(start2, end2);
			String str4=str2.replaceAll(str3, "");//去掉字体设置
			Pattern p=Pattern.compile("href=\"(.*?)\"");
	        Matcher m=p.matcher(str4);
	        while(m.find()) str4=str4.replaceAll(m.group(),"");
	        return str4;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	public static String parsePictureHtml(String contentHtml)
	{
		try
		{
			int start = contentHtml.indexOf("<div class=\"box\">");
			int end = contentHtml.indexOf("<div class=\"shensuo\">");
			if(start==-1||end==-1) return null;
			return contentHtml.substring(start, end);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	public static String getSource(String content)
	{
		try
		{
			if(content==null) return null;
			String str1=content;
			Pattern p=Pattern.compile("(?<=title=\")[[\\w-\\s][^x00-xff]]+(?=\")");
			Matcher m=p.matcher(str1);
			String str2=null;
			if(m.find()) str2=m.group();
			return str2;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	public static String getCurrentNeteorkTime()
	{
		try
		{
			URL url=new URL("http://www.baidu.com");
			URLConnection con = url.openConnection();
			con.connect();
			long d = con.getDate();//得到时间戳
			SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//m必须大写，h大写表示24小时制
			return s.format(d);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	public static String transformPublishTime(String currentNetworkTime,String rawPublishTime)
	{
		try
		{
			SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date d = s.parse(currentNetworkTime);
			long l = d.getTime();
			//如果没有网络时间直接返回
			if(currentNetworkTime==null) return rawPublishTime;
			//提取网络时间
			String[] network = currentNetworkTime.split(" ");
			String[] networkDate = network[0].split("-");
			String[] networkTime = network[1].split(":");
			int networkYear = Integer.parseInt(networkDate[0]);
			int networkMonth = Integer.parseInt(networkDate[1]);
			int networkDay = Integer.parseInt(networkDate[2]);
			int networkHour = Integer.parseInt(networkTime[0]);
			int networkMiniute = Integer.parseInt(networkTime[1]);
			int networkSecond = Integer.parseInt(networkTime[2]);
			//提取发布时间
			String[] publish = rawPublishTime.split(" ");
			String[] publishDate = publish[0].split("-");
			String[] publishTime = publish[1].split(":");
			int publishYear = Integer.parseInt(publishDate[0]);
			int publishMonth = Integer.parseInt(publishDate[1]);
			int publishDay = Integer.parseInt(publishDate[2]);
			int publishHour = Integer.parseInt(publishTime[0]);
			int publishMiniute = Integer.parseInt(publishTime[1]);
			int publishSecond = Integer.parseInt(publishTime[2]);
			if(networkDay-publishDay>3)
			{
				return publish[0];
			}
			else if(networkDay-publishDay>0)
			{
				return networkDay-publishDay+"天前";
			}
			else
			{
				if(networkHour!=publishHour) return networkHour-publishHour+"小时前";
				else return networkMiniute-publishMiniute+"分钟前";
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return rawPublishTime;
		}
	}
	public static String getTitle(String html)
	{
		try
		{
			int start = html.indexOf("<h3>");
			int end = html.indexOf("</a>");
			if(start==-1||end==-1) return null;
			return html.substring(start, end);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	public static String getContent(String html)
	{
		try
		{
			String title = getTitle(html);
			if(title==null) return null;
			String content = html.replaceAll(title, "");
			return content;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	public static String getPictureTitle(String html)
	{
		try
		{
			int start = html.indexOf("<div class=\"dList\">");
			int end = html.indexOf("</a></span></p>");
			if(start==-1||end==-1) return null;
			return html.substring(start, end);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	public static String getPictureContent(String html)
	{
		try
		{
			String title = getPictureTitle(html);
			if(title==null) return null;
			String content = html.replaceAll(title, "");
			return content;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
















