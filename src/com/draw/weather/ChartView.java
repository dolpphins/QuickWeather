package com.draw.weather;

import com.main.weather.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChartView extends View{
	private static String tag="ChartView";
	private static int screenWidth;
	private static int screenHeight;
	public static int[] TempArray = new int[12];//保存温度
	private static boolean canDraw = false;//标记是否有数据可显示
	public ChartView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
	}
	public ChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}

	public ChartView(Context context) {
		super(context);
		
	}
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		if(canDraw)
		{
			canDraw=false;
			Paint paint=new Paint();
			paint.setColor(Color.argb(255, 255, 97, 0));
			paint.setStrokeWidth(4);
			paint.setAntiAlias(true);//设置抗锯齿
			int length=screenWidth/6;
			int x1=length/2;
			int x2=x1+length;
			int x3=x2+length;
			int x4=x3+length;
			int x5=x4+length;
			int x6=x5+length;
			int maxTemp=TempArray[0],minTemp=TempArray[0];
			for(int i=1;i<12;i++)
			{
				
				if(maxTemp<TempArray[i]) maxTemp=TempArray[i];
				if(minTemp>TempArray[i]) minTemp=TempArray[i];
			}
			//注意判断除数是否为0
			int unit=(Screen.getHighestY(screenHeight)-Screen.getLowestY(screenHeight)-160)/(maxTemp-minTemp);
			int[] y = new int[12];
			for(int i=0;i<12;i++)
			{
				y[i]=Screen.getHighestY(screenHeight)-80-Screen.getLowestY(screenHeight)-(TempArray[i]-minTemp)*unit;
			}
			//白天折线
			//画线
			canvas.drawLine(x1,y[0],x2,y[1],paint);
			canvas.drawLine(x2,y[1],x3,y[2],paint);
			canvas.drawLine(x3,y[2],x4,y[3],paint);
			canvas.drawLine(x4,y[3],x5,y[4],paint);
			canvas.drawLine(x5,y[4],x6,y[5],paint);
			//画点
			paint.setColor(Color.argb(255, 255, 40, 2));
			canvas.drawCircle(x1, y[0], 6, paint);
			canvas.drawCircle(x2, y[1], 6, paint);
			canvas.drawCircle(x3, y[2], 6, paint);
			canvas.drawCircle(x4, y[3], 6, paint);
			canvas.drawCircle(x5, y[4], 6, paint);
			canvas.drawCircle(x6, y[5], 6, paint);
			//添加温度值
			paint.setColor(Color.argb(255, 0, 0, 0));
			paint.setTextSize(25);
			canvas.drawText(TempArray[0]+"°c", x1-20, y[0]-30, paint);
			canvas.drawText(TempArray[1]+"°c", x2-20, y[1]-30, paint);
			canvas.drawText(TempArray[2]+"°c", x3-20, y[2]-30, paint);
			canvas.drawText(TempArray[3]+"°c", x4-20, y[3]-30, paint);
			canvas.drawText(TempArray[4]+"°c", x5-20, y[4]-30, paint);
			canvas.drawText(TempArray[5]+"°c", x6-20, y[5]-30, paint);
			//夜间折线
			//画线
			paint.setColor(Color.argb(255, 111, 32, 229));
			canvas.drawLine(x1,y[6],x2,y[7],paint);
			canvas.drawLine(x2,y[7],x3,y[8],paint);
			canvas.drawLine(x3,y[8],x4,y[9],paint);
			canvas.drawLine(x4,y[9],x5,y[10],paint);
			canvas.drawLine(x5,y[10],x6,y[11],paint);
			//画点
			paint.setColor(Color.argb(255, 10, 10, 255));
			canvas.drawCircle(x1, y[6], 6, paint);
			canvas.drawCircle(x2, y[7], 6, paint);
			canvas.drawCircle(x3, y[8], 6, paint);
			canvas.drawCircle(x4, y[9], 6, paint);
			canvas.drawCircle(x5, y[10], 6, paint);
			canvas.drawCircle(x6, y[11], 6, paint);
			//添加温度值
			paint.setColor(Color.argb(255, 0, 0, 0));
			paint.setTextSize(25);
			canvas.drawText(TempArray[6]+"°c", x1-20, y[6]+40, paint);
			canvas.drawText(TempArray[7]+"°c", x2-20, y[7]+40, paint);
			canvas.drawText(TempArray[8]+"°c", x3-20, y[8]+40, paint);
			canvas.drawText(TempArray[9]+"°c", x4-20, y[9]+40, paint);
			canvas.drawText(TempArray[10]+"°c", x5-20, y[10]+40, paint);
			canvas.drawText(TempArray[11]+"°c", x6-20, y[11]+40, paint);
		}
	}
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension(screenWidth,Screen.getViewHeight(screenHeight));
	}
	public static void setScreenWidth(int width)
	{
		screenWidth=width;
	}
	public static void setScreenHeight(int height)
	{
		screenHeight=height;
	}
	public static void setTemp(String[] dayTemp,String[] nightTemp)
	{
		TempArray[0]=Integer.parseInt(dayTemp[0].replaceAll("°c", ""));
		TempArray[1]=Integer.parseInt(dayTemp[1].replaceAll("°c", ""));
		TempArray[2]=Integer.parseInt(dayTemp[2].replaceAll("°c", ""));
		TempArray[3]=Integer.parseInt(dayTemp[3].replaceAll("°c", ""));
		TempArray[4]=Integer.parseInt(dayTemp[4].replaceAll("°c", ""));
		TempArray[5]=Integer.parseInt(dayTemp[5].replaceAll("°c", ""));

		TempArray[6]=Integer.parseInt(nightTemp[0].replaceAll("°c", ""));
		TempArray[7]=Integer.parseInt(nightTemp[1].replaceAll("°c", ""));
		TempArray[8]=Integer.parseInt(nightTemp[2].replaceAll("°c", ""));
		TempArray[9]=Integer.parseInt(nightTemp[3].replaceAll("°c", ""));
		TempArray[10]=Integer.parseInt(nightTemp[4].replaceAll("°c", ""));
		TempArray[11]=Integer.parseInt(nightTemp[5].replaceAll("°c", ""));
	}
	public static void setCanDraw(boolean b)
	{
		canDraw = b;
	}

}
