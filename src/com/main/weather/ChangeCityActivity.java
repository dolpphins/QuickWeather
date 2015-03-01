package com.main.weather;

import com.data.weather.CityAttribute;
import com.data.weather.MySQLite;
import com.location.weather.NetworkLocation;
import com.weatherinfo.weather.FiveDayWeatherAttribute;
import com.weatherinfo.weather.ForecastFiveDayRequest;
import com.weatherinfo.weather.WeatherAttribute;
import com.weatherinfo.weather.WeatherRequest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ChangeCityActivity extends Activity {
	private String tag="ChangeCityActivity";
	private TextView changecity_back;
	private Button changecity_locate;
	private EditText etSearch;
	private ImageView ivDelete;
	private Button btnSearch;
	private Handler handler;
	private TextView tv_currentCity=null;
	private static ProgressDialog pd=null;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ȥ��ϵͳ�Դ�������
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //���ò���
        setContentView(R.layout.select_city);
        
        changecity_back = (TextView)findViewById(R.id.changecity_back);
        changecity_locate = (Button) findViewById(R.id.changecity_locate);
        etSearch = (EditText)findViewById(R.id.etSearch);
        ivDelete = (ImageView)findViewById(R.id.ivDeleteText);
        btnSearch = (Button)findViewById(R.id.btnSearch);
        
        //Ϊ���ؼ�ͷ�󶨵���¼�
        changecity_back.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(ChangeCityActivity.this,MainActivity.class);
	    		intent.putExtra("fromBack", "yes");
	    		startActivity(intent);
	    		ChangeCityActivity.this.finish();
	    		//���ý����л�Ч��
	    		if(Integer.valueOf(android.os.Build.VERSION.SDK)>=5)
	    		{
	    			ChangeCityActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	    		}
			}
		});
        //Ϊ��λ��ť�󶨵���¼�
        changecity_locate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final WeatherAttribute weatherAttribute = NetworkLocation.getCurrentWeatherAttibute();
				if(weatherAttribute.cityName==null)
				{
					handler.sendEmptyMessage(12);
				}
				else
				{
					new Thread(new Runnable(){
						public void run()
						{
							handler.sendEmptyMessage(13);//��ʾ��ȡ����λ��(��ȡ������Ϣ)
							String rawResult=WeatherRequest.getWeatherInfor(weatherAttribute.cityId,ChangeCityActivity.this);
							if(rawResult==null) 
							{
								handler.sendEmptyMessage(12);//��ʾ��ȡλ��ʧ��(��ȡ����ʧ��)
								handler.sendEmptyMessage(10);//ȡ���Ի���
								return;
							}
							WeatherAttribute result=WeatherRequest.parseJson(rawResult);
							if(result==null)
							{
								handler.sendEmptyMessage(12);//��ʾ��ȡλ��ʧ��(��ȡ����ʧ��)
								handler.sendEmptyMessage(10);//ȡ���Ի���
							}
							else
							{
								FiveDayWeatherAttribute fiveday = ForecastFiveDayRequest.getFiveDayWeatherInfor(weatherAttribute.cityId, ChangeCityActivity.this);
								handler.sendEmptyMessage(10);//ȡ���Ի���
								Intent intent=new Intent(ChangeCityActivity.this,MainActivity.class);
								intent.putExtra("fromSelect","yes");
								intent.putExtra("weatherInformation", result);
								intent.putExtra("fiveday", fiveday);
								startActivity(intent);
								ChangeCityActivity.this.finish();
							}
						}
					}).start();
				}
			}
		});
        //�������etSearchת�Ƶ�ivDelete����ֹ��������
        ivDelete.requestFocus();
        //����ɾ��ͼ��ĵ���¼�
        ivDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				etSearch.setText("");
			}
		});
        //����������ť�ĵ���¼�
        btnSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){
					new Thread(new Runnable(){
						@Override
						public void run() {
							String cityName=etSearch.getText().toString().trim();
							//�ж��Ƿ������˳�����
							if("".equals(cityName))
							{
								handler.sendEmptyMessage(1);//�����̲߳���ʹ��Toast,��ʾ���������
								handler.sendEmptyMessage(9);
							}
							else
							{
								Log.i(tag,"start");
								CityAttribute cityAttribute=MySQLite.getCity(ChangeCityActivity.this, cityName);
								Log.i(tag,"end");
								//�ж����ݿ����Ƿ��иó���
								if(cityAttribute==null)
								{
									handler.sendEmptyMessage(2);//��ʾû�иó�������
									handler.sendEmptyMessage(9);
								}
								else
								{
									handler.sendEmptyMessage(11);//��ʾ���ڻ�ȡ������Ϣ
									String rawResult=WeatherRequest.getWeatherInfor(cityAttribute.cityId,ChangeCityActivity.this);
									if(rawResult==null) 
									{
										handler.sendEmptyMessage(4);//��ʾ��ȡ����ʧ��
										handler.sendEmptyMessage(10);//ȡ���Ի���
										return;
									}
									WeatherAttribute result=WeatherRequest.parseJson(rawResult);
									if(result==null)
									{
										handler.sendEmptyMessage(4);//��ʾ��ȡ����ʧ��
										handler.sendEmptyMessage(10);//ȡ���Ի���
									}
									else
									{
										FiveDayWeatherAttribute fiveday = ForecastFiveDayRequest.getFiveDayWeatherInfor(cityAttribute.cityId, ChangeCityActivity.this);
										handler.sendEmptyMessage(10);//ȡ���Ի���
										Intent intent=new Intent(ChangeCityActivity.this,MainActivity.class);
										intent.putExtra("fromSelect","yes");
										intent.putExtra("weatherInformation", result);
										intent.putExtra("fiveday", fiveday);
										startActivity(intent);
										ChangeCityActivity.this.finish();
									}
								}
							}
						}
					}).start();
			}
        });
        handler = new Handler(){
        	public void handleMessage(Message msg)
        	{
        		switch(msg.what)
        		{
        		case 1:Toast.makeText(ChangeCityActivity.this, "�����������", Toast.LENGTH_SHORT).show();break;
        		case 2:Toast.makeText(ChangeCityActivity.this, "����ĳ����������ڻ���û�иó��е���������", Toast.LENGTH_SHORT).show();break;
        		case 6:
        		case 3:Toast.makeText(ChangeCityActivity.this, "���ڻ�ȡ����...", Toast.LENGTH_SHORT).show();break;
        		case 7:
        		case 4:Toast.makeText(ChangeCityActivity.this, "��ȡ������Ϣʧ��,������", Toast.LENGTH_SHORT).show();break;
        		case 8:
        		case 9:setEnabled();break;
        		case 10:if(pd!=null) pd.dismiss();break;
        		case 11:pd=ProgressDialog.show(ChangeCityActivity.this, "��ʾ", "���ڻ�ȡ������Ϣ");break;//��ʾ���ڻ�ȡ������Ϣ
        		case 12:Toast.makeText(ChangeCityActivity.this, "��λʧ��,������" , Toast.LENGTH_SHORT).show();break;
        		case 13:pd = ProgressDialog.show(ChangeCityActivity.this, "��ʾ", "���ڻ�ȡ����λ��");break;
        		}
        	}
        };
        //�������ų��еĵ���¼�
        //����
        final TextView tv_beijing=(TextView)findViewById(R.id.beijing);
        tv_beijing.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					//���øó���Ϊ�����ã��������Ч��
					tv_currentCity=tv_beijing;
					tv_beijing.setEnabled(false);
					getHotCityWeather("����");
				}
				return true;
			}
		});
        //�Ϻ�
        final TextView tv_shanghai=(TextView)findViewById(R.id.shanghai);
        tv_shanghai.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_shanghai;
					tv_shanghai.setEnabled(false);
					getHotCityWeather("�Ϻ�");
				}
				return true;
			}
		});
        //����
        final TextView tv_guangzhou=(TextView)findViewById(R.id.guangzhou);
        tv_guangzhou.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_guangzhou;
					tv_guangzhou.setEnabled(false);
					getHotCityWeather("����");
				}
				return true;
			}
		});
        //����
        final TextView tv_shenzhen=(TextView)findViewById(R.id.shenzhen);
        tv_shenzhen.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_shenzhen;
					tv_shenzhen.setEnabled(false);
					getHotCityWeather("����");
				}
				return true;
			}
		});
        //�Ͼ�
        final TextView tv_nanjing=(TextView)findViewById(R.id.nanjing);
        tv_nanjing.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_nanjing;
					tv_nanjing.setEnabled(false);
					getHotCityWeather("�Ͼ�");
				}
				return true;
			}
		});
        //����
        final TextView tv_hangzhou=(TextView)findViewById(R.id.hangzhou);
        tv_hangzhou.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_hangzhou;
					tv_hangzhou.setEnabled(false);
					getHotCityWeather("����");
				}
				return true;
			}
		});
        //���
        final TextView tv_tianjin=(TextView)findViewById(R.id.tianjin);
        tv_tianjin.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_tianjin;
					tv_tianjin.setEnabled(false);
					getHotCityWeather("���");
				}
				return true;
			}
		});
        //��ɳ
        final TextView tv_changsha=(TextView)findViewById(R.id.changsha);
        tv_changsha.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_changsha;
					tv_changsha.setEnabled(false);
					getHotCityWeather("��ɳ");
				}
				return true;
			}
		});
        //�人
        final TextView tv_wuhan=(TextView)findViewById(R.id.wuhan);
        tv_wuhan.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_wuhan;
					tv_wuhan.setEnabled(false);
					getHotCityWeather("�人");
				}
				return true;
			}
		});
		//֣��
        final TextView tv_zhengzhou=(TextView)findViewById(R.id.zhengzhou);
        tv_zhengzhou.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_zhengzhou;
					tv_zhengzhou.setEnabled(false);
					getHotCityWeather("֣��");
				}
				return true;
			}
		});
        //������
        final TextView tv_haebin=(TextView)findViewById(R.id.haebin);
        tv_haebin.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_haebin;
					tv_haebin.setEnabled(false);
					getHotCityWeather("������");
				}
				return true;
			}
		});
        //����
        final TextView tv_changchun=(TextView)findViewById(R.id.changchun);
        tv_changchun.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_changchun;
					tv_changchun.setEnabled(false);
					getHotCityWeather("����");
				}
				return true;
			}
		});
        //����
        final TextView tv_shenyang=(TextView)findViewById(R.id.shenyang);
        tv_shenyang.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_shenyang;
					tv_shenyang.setEnabled(false);
					getHotCityWeather("����");
				}
				return true;
			}
		});
        //�ϲ�
        final TextView tv_nanchan=(TextView)findViewById(R.id.nanchan);
        tv_nanchan.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_nanchan;
					tv_nanchan.setEnabled(false);
					getHotCityWeather("�ϲ�");
				}
				return true;
			}
		});
        //�Ϸ�
        final TextView tv_hefei=(TextView)findViewById(R.id.hefei);
        tv_hefei.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_hefei;
					tv_hefei.setEnabled(false);
					getHotCityWeather("�Ϸ�");
				}
				return true;
			}
		});
        //ʯ��ׯ
        final TextView tv_shijiazhuang=(TextView)findViewById(R.id.shijiazhuang);
        tv_shijiazhuang.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_shijiazhuang;
					tv_shijiazhuang.setEnabled(false);
					getHotCityWeather("ʯ��ׯ");
				}
				return true;
			}
		});
        //����
        final TextView tv_chongqing=(TextView)findViewById(R.id.chongqing);
        tv_chongqing.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_chongqing;
					tv_chongqing.setEnabled(false);
					getHotCityWeather("����");
				}
				return true;
			}
		});
        //����
        final TextView tv_nanning=(TextView)findViewById(R.id.nanning);
        tv_nanning.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_nanning;
					tv_nanning.setEnabled(false);
					getHotCityWeather("����");
				}
				return true;
			}
		});
        //����
        final TextView tv_haikou=(TextView)findViewById(R.id.haikou);
        tv_haikou.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_haikou;
					tv_haikou.setEnabled(false);
					getHotCityWeather("����");
				}
				return true;
			}
		});
        //����
        final TextView tv_kunming=(TextView)findViewById(R.id.kunming);
        tv_kunming.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_kunming;
					tv_kunming.setEnabled(false);
					getHotCityWeather("����");
				}
				return true;
			}
		});
        //����
        final TextView tv_xian=(TextView)findViewById(R.id.xian);
        tv_xian.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_xian;
					tv_xian.setEnabled(false);
					getHotCityWeather("����");
				}
				return true;
			}
		});
        //����
        final TextView tv_jinan=(TextView)findViewById(R.id.jinan);
        tv_jinan.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_jinan;
					tv_jinan.setEnabled(false);
					getHotCityWeather("����");
				}
				return true;
			}
		});
        //����
        final TextView tv_fuzhou=(TextView)findViewById(R.id.fuzhou);
        tv_fuzhou.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_fuzhou;
					tv_fuzhou.setEnabled(false);
					getHotCityWeather("����");
				}
				return true;
			}
		});
        //̫ԭ
        final TextView tv_taiyuan=(TextView)findViewById(R.id.taiyuan);
        tv_taiyuan.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					tv_currentCity=tv_taiyuan;
					tv_taiyuan.setEnabled(false);
					getHotCityWeather("̫ԭ");
				}
				return true;
			}
		});
    }
    public void getHotCityWeather(final String cityName)
    {
    	new Thread(new Runnable(){
    		public void run(){
    			CityAttribute cityAttribute=MySQLite.getCity(ChangeCityActivity.this, cityName);
    			handler.sendEmptyMessage(11);//��ʾ���ڻ�ȡ������Ϣ
    			String rawResult=WeatherRequest.getWeatherInfor(cityAttribute.cityId,ChangeCityActivity.this);
    			if(rawResult==null) 
    			{
    				handler.sendEmptyMessage(10);//ȡ���Ի���
    				handler.sendEmptyMessage(7);//��ʾ��ȡ����ʧ��
    				handler.sendEmptyMessage(9);//�������ų��п���
    				return;
    			}
    			WeatherAttribute result=WeatherRequest.parseJson(rawResult);
    			if(result==null)
    			{
    				handler.sendEmptyMessage(10);//ȡ���Ի���
    				handler.sendEmptyMessage(7);//��ʾ��ȡ����ʧ��
    				handler.sendEmptyMessage(9);//�������ų��п���
    				return;
    			}
    			else
    			{
    				FiveDayWeatherAttribute fiveday = ForecastFiveDayRequest.getFiveDayWeatherInfor(cityAttribute.cityId, ChangeCityActivity.this);
    				handler.sendEmptyMessage(10);//ȡ���Ի���
    				//�л�����
    				Intent intent=new Intent(ChangeCityActivity.this,MainActivity.class);
    				intent.putExtra("fromSelect","yes");
    				intent.putExtra("weatherInformation", result);
    				intent.putExtra("fiveday", fiveday);
    				startActivity(intent);
    				ChangeCityActivity.this.finish();
    			}
    		}
    	}).start();
    }
    public void onResume()
    {
    	super.onResume();
    	//�����������ų���Ϊ����
    	setEnabled();
    	
    }
    public boolean onKeyDown(int keyCode,KeyEvent event)
    {
    	//���񷵻ؼ�
    	if(keyCode==KeyEvent.KEYCODE_BACK)
    	{
    		Intent intent=new Intent(this,MainActivity.class);
    		intent.putExtra("fromBack", "yes");
    		startActivity(intent);
    		ChangeCityActivity.this.finish();
    		//���ý����л�Ч��
    		if(Integer.valueOf(android.os.Build.VERSION.SDK)>=5)
    		{
    			this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    		}
    		return true;
    	}
    	else
    	{
    		return super.onKeyDown(keyCode, event);
    	}
    }
    //�����������ų���Ϊ����
    public void setEnabled()
    {
    	if(tv_currentCity!=null)
    	{
    		tv_currentCity.setEnabled(true);
    	}
    }
}




















