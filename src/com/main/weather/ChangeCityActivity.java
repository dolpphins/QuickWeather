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
        //去掉系统自带标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置布局
        setContentView(R.layout.select_city);
        
        changecity_back = (TextView)findViewById(R.id.changecity_back);
        changecity_locate = (Button) findViewById(R.id.changecity_locate);
        etSearch = (EditText)findViewById(R.id.etSearch);
        ivDelete = (ImageView)findViewById(R.id.ivDeleteText);
        btnSearch = (Button)findViewById(R.id.btnSearch);
        
        //为返回箭头绑定点击事件
        changecity_back.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(ChangeCityActivity.this,MainActivity.class);
	    		intent.putExtra("fromBack", "yes");
	    		startActivity(intent);
	    		ChangeCityActivity.this.finish();
	    		//设置界面切换效果
	    		if(Integer.valueOf(android.os.Build.VERSION.SDK)>=5)
	    		{
	    			ChangeCityActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	    		}
			}
		});
        //为定位按钮绑定点击事件
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
							handler.sendEmptyMessage(13);//提示获取地理位置(获取天气信息)
							String rawResult=WeatherRequest.getWeatherInfor(weatherAttribute.cityId,ChangeCityActivity.this);
							if(rawResult==null) 
							{
								handler.sendEmptyMessage(12);//提示获取位置失败(获取数据失败)
								handler.sendEmptyMessage(10);//取消对话框
								return;
							}
							WeatherAttribute result=WeatherRequest.parseJson(rawResult);
							if(result==null)
							{
								handler.sendEmptyMessage(12);//提示获取位置失败(获取数据失败)
								handler.sendEmptyMessage(10);//取消对话框
							}
							else
							{
								FiveDayWeatherAttribute fiveday = ForecastFiveDayRequest.getFiveDayWeatherInfor(weatherAttribute.cityId, ChangeCityActivity.this);
								handler.sendEmptyMessage(10);//取消对话框
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
        //将焦点从etSearch转移到ivDelete上阻止弹出键盘
        ivDelete.requestFocus();
        //设置删除图标的点击事件
        ivDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				etSearch.setText("");
			}
		});
        //设置搜索按钮的点击事件
        btnSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){
					new Thread(new Runnable(){
						@Override
						public void run() {
							String cityName=etSearch.getText().toString().trim();
							//判断是否输入了城市名
							if("".equals(cityName))
							{
								handler.sendEmptyMessage(1);//在子线程不能使用Toast,提示输入城市名
								handler.sendEmptyMessage(9);
							}
							else
							{
								Log.i(tag,"start");
								CityAttribute cityAttribute=MySQLite.getCity(ChangeCityActivity.this, cityName);
								Log.i(tag,"end");
								//判断数据库里是否有该城市
								if(cityAttribute==null)
								{
									handler.sendEmptyMessage(2);//提示没有该城市数据
									handler.sendEmptyMessage(9);
								}
								else
								{
									handler.sendEmptyMessage(11);//提示正在获取天气信息
									String rawResult=WeatherRequest.getWeatherInfor(cityAttribute.cityId,ChangeCityActivity.this);
									if(rawResult==null) 
									{
										handler.sendEmptyMessage(4);//提示获取数据失败
										handler.sendEmptyMessage(10);//取消对话框
										return;
									}
									WeatherAttribute result=WeatherRequest.parseJson(rawResult);
									if(result==null)
									{
										handler.sendEmptyMessage(4);//提示获取数据失败
										handler.sendEmptyMessage(10);//取消对话框
									}
									else
									{
										FiveDayWeatherAttribute fiveday = ForecastFiveDayRequest.getFiveDayWeatherInfor(cityAttribute.cityId, ChangeCityActivity.this);
										handler.sendEmptyMessage(10);//取消对话框
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
        		case 1:Toast.makeText(ChangeCityActivity.this, "请输入城市名", Toast.LENGTH_SHORT).show();break;
        		case 2:Toast.makeText(ChangeCityActivity.this, "输入的城市名不存在或者没有该城市的天气数据", Toast.LENGTH_SHORT).show();break;
        		case 6:
        		case 3:Toast.makeText(ChangeCityActivity.this, "正在获取数据...", Toast.LENGTH_SHORT).show();break;
        		case 7:
        		case 4:Toast.makeText(ChangeCityActivity.this, "获取天气信息失败,请重试", Toast.LENGTH_SHORT).show();break;
        		case 8:
        		case 9:setEnabled();break;
        		case 10:if(pd!=null) pd.dismiss();break;
        		case 11:pd=ProgressDialog.show(ChangeCityActivity.this, "提示", "正在获取天气信息");break;//提示正在获取天气信息
        		case 12:Toast.makeText(ChangeCityActivity.this, "定位失败,请重试" , Toast.LENGTH_SHORT).show();break;
        		case 13:pd = ProgressDialog.show(ChangeCityActivity.this, "提示", "正在获取地理位置");break;
        		}
        	}
        };
        //设置热门城市的点击事件
        //北京
        final TextView tv_beijing=(TextView)findViewById(R.id.beijing);
        tv_beijing.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					
				}
				else if(event.getAction()==MotionEvent.ACTION_UP)
				{
					//设置该城市为不可用，产生变灰效果
					tv_currentCity=tv_beijing;
					tv_beijing.setEnabled(false);
					getHotCityWeather("北京");
				}
				return true;
			}
		});
        //上海
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
					getHotCityWeather("上海");
				}
				return true;
			}
		});
        //广州
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
					getHotCityWeather("广州");
				}
				return true;
			}
		});
        //深圳
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
					getHotCityWeather("深圳");
				}
				return true;
			}
		});
        //南京
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
					getHotCityWeather("南京");
				}
				return true;
			}
		});
        //杭州
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
					getHotCityWeather("杭州");
				}
				return true;
			}
		});
        //天津
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
					getHotCityWeather("天津");
				}
				return true;
			}
		});
        //长沙
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
					getHotCityWeather("长沙");
				}
				return true;
			}
		});
        //武汉
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
					getHotCityWeather("武汉");
				}
				return true;
			}
		});
		//郑州
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
					getHotCityWeather("郑州");
				}
				return true;
			}
		});
        //哈尔滨
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
					getHotCityWeather("哈尔滨");
				}
				return true;
			}
		});
        //长春
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
					getHotCityWeather("长春");
				}
				return true;
			}
		});
        //沈阳
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
					getHotCityWeather("沈阳");
				}
				return true;
			}
		});
        //南昌
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
					getHotCityWeather("南昌");
				}
				return true;
			}
		});
        //合肥
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
					getHotCityWeather("合肥");
				}
				return true;
			}
		});
        //石家庄
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
					getHotCityWeather("石家庄");
				}
				return true;
			}
		});
        //重庆
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
					getHotCityWeather("重庆");
				}
				return true;
			}
		});
        //南宁
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
					getHotCityWeather("南宁");
				}
				return true;
			}
		});
        //海口
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
					getHotCityWeather("海口");
				}
				return true;
			}
		});
        //昆明
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
					getHotCityWeather("昆明");
				}
				return true;
			}
		});
        //西安
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
					getHotCityWeather("西安");
				}
				return true;
			}
		});
        //济南
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
					getHotCityWeather("济南");
				}
				return true;
			}
		});
        //福州
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
					getHotCityWeather("福州");
				}
				return true;
			}
		});
        //太原
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
					getHotCityWeather("太原");
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
    			handler.sendEmptyMessage(11);//提示正在获取天气信息
    			String rawResult=WeatherRequest.getWeatherInfor(cityAttribute.cityId,ChangeCityActivity.this);
    			if(rawResult==null) 
    			{
    				handler.sendEmptyMessage(10);//取消对话框
    				handler.sendEmptyMessage(7);//提示获取数据失败
    				handler.sendEmptyMessage(9);//设置热门城市可用
    				return;
    			}
    			WeatherAttribute result=WeatherRequest.parseJson(rawResult);
    			if(result==null)
    			{
    				handler.sendEmptyMessage(10);//取消对话框
    				handler.sendEmptyMessage(7);//提示获取数据失败
    				handler.sendEmptyMessage(9);//设置热门城市可用
    				return;
    			}
    			else
    			{
    				FiveDayWeatherAttribute fiveday = ForecastFiveDayRequest.getFiveDayWeatherInfor(cityAttribute.cityId, ChangeCityActivity.this);
    				handler.sendEmptyMessage(10);//取消对话框
    				//切换界面
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
    	//重新设置热门城市为可用
    	setEnabled();
    	
    }
    public boolean onKeyDown(int keyCode,KeyEvent event)
    {
    	//捕获返回键
    	if(keyCode==KeyEvent.KEYCODE_BACK)
    	{
    		Intent intent=new Intent(this,MainActivity.class);
    		intent.putExtra("fromBack", "yes");
    		startActivity(intent);
    		ChangeCityActivity.this.finish();
    		//设置界面切换效果
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
    //用于设置热门城市为可用
    public void setEnabled()
    {
    	if(tv_currentCity!=null)
    	{
    		tv_currentCity.setEnabled(true);
    	}
    }
}




















