package com.draw.weather;

public class Screen {
	public static int getTextSize(int width)
	{
		if(width>=720&&width<1080)
		{
			return 16;
		}
		return 16;
	}
	public static int getHighestY(int height)
	{
		return height/2;
	}
	public static int getLowestY(int height)
	{
		return (5*height)/64;
	}
	public static int getViewHeight(int height)
	{
		return getHighestY(height)-getLowestY(height);
	}
}
