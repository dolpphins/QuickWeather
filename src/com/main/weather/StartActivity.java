package com.main.weather;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.data.weather.MySQLite;
import com.draw.weather.ChartView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class StartActivity extends Activity{
	private String tag = "StartActivity";
	
	private int rate = 30;//��ǰ���ؽ���
	private static Handler handler;
	
	private static int screenWidth;//������Ļ�Ŀ��
	private static int screenHeight;//������Ļ�ĸ߶�
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.start_frame);
        
        //�õ���Ļ�Ĵ�С
        getScreenInfo();
        //��Ӧ���̷߳�������Ϣ
        handler = new Handler(){
        	public void handleMessage(Message msg)
        	{
        		ProgressBar start_progress = (ProgressBar) findViewById(R.id.start_progress);
        		switch(msg.what)
        		{
	        		case 1:
	        				start_progress.setProgress(rate);
	        				break;
	        		case 2:
	        			final TextView start_tip = (TextView)findViewById(R.id.start_tip);
	        			start_progress.setProgress(rate);
	        			start_tip.setText("�������");
	        			//�л�����
	        			switchToMainActivity();
	        			break;
	        		case 3:
	        			 try {
	        					Thread.sleep(500);
	        				} catch (InterruptedException e) {
	        					// TODO �Զ����ɵ� catch ��
	        					e.printStackTrace();
	        				}
	        			loadCityData();
	        			break;
        		}
        		super.handleMessage(msg);
        	}
        };
        
        //������������
        try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
        final RelativeLayout start_name_weather = (RelativeLayout) findViewById(R.id.start_name_weather);
        final TextView start_name = (TextView)findViewById(R.id.start_name);
        final TextView start_weather = (TextView)findViewById(R.id.start_weather);
        final TextView start_tip = (TextView)findViewById(R.id.start_tip);
        
        final AlphaAnimation alpa = new AlphaAnimation(0.1f,1.0f);
        alpa.setDuration(1500);
        TranslateAnimation tran = new TranslateAnimation(0,screenWidth*5/6,0,0);
        tran.setDuration(1000);
        
        start_name_weather.startAnimation(tran);
        tran.setAnimationListener(new AnimationListener(){
        	@Override
			public void onAnimationEnd(Animation anim) {
				start_name_weather.setPadding(screenWidth*5/6, 0, 0, 0);
				 try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO �Զ����ɵ� catch ��
						e.printStackTrace();
					}
				start_weather.setText("����");
				start_weather.startAnimation(alpa);
			}

			@Override
			public void onAnimationRepeat(Animation anim) {
				
			}
			@Override
			public void onAnimationStart(Animation anim) {
				
			}
        });
        alpa.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation anim) {
				//����������ʼ���н�����ʾ
				start_tip.setText("���ڼ�������...");
				ProgressBar start_progress = (ProgressBar) findViewById(R.id.start_progress);
				start_progress.setAlpha(1);//��ʾ������
				handler.sendEmptyMessage(3);
			}

			@Override
			public void onAnimationRepeat(Animation anim) {
			}
			@Override
			public void onAnimationStart(Animation anim) {
			}
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    //��ȡ��Ļ��С��Ϣ
    public void getScreenInfo()
    {
    	DisplayMetrics metric = new DisplayMetrics();
    	this.getWindowManager().getDefaultDisplay().getMetrics(metric);
    	screenWidth=metric.widthPixels;
    	screenHeight=metric.heightPixels;
    }
    //������ɺ���øú����л���������
    public void switchToMainActivity()
    {
    	Intent intent = new Intent(StartActivity.this,MainActivity.class);
    	startActivity(intent);
    	StartActivity.this.finish();
    }
    public void loadCityData()
    {
    	File dir = new File("/data/data/com.main.weather/databases/");
        if(!dir.exists()) dir.mkdir();
        File file = new File("/data/data/com.main.weatherdatabases/city.db");
        if(!file.exists())
        {
        	handler.sendEmptyMessage(1);
        	InputStream is = getResources().openRawResource(R.raw.city);
        	try {
				FileOutputStream fos = new FileOutputStream("/data/data/com.main.weather/databases/city.db");
				byte[] buffer = new byte[1024];
				int length = 0;
				while((length=is.read(buffer))>0)
				{
					fos.write(buffer, 0, length);
				}
				is.close();
				fos.close();
				rate  = 100;
				handler.sendEmptyMessage(2);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
        }
        else
        {
        	handler.sendEmptyMessage(2);
        }
    }
}
