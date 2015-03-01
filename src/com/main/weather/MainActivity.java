package com.main.weather;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import com.data.weather.CityAttribute;
import com.data.weather.MySQLite;
import com.draw.weather.ChartView;
import com.draw.weather.Screen;
import com.location.weather.NetworkLocation;
import com.news.weather.ContentAttribute;
import com.news.weather.NewsAttribute;
import com.news.weather.NewsDetailActivity;
import com.news.weather.NewsRequest;
import com.settings.weather.VersionActivity;
import com.weatherinfo.weather.FiveDayWeatherAttribute;
import com.weatherinfo.weather.ForecastFiveDayRequest;
import com.weatherinfo.weather.WeatherAttribute;
import com.weatherinfo.weather.WeatherRequest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
	String tag="MainActivity";
	//选项卡主界面属性
	private FragmentTabHost tabHost;
	private LayoutInflater layoutInflater;
	private Class fragmentArray[]={HomePage.class,TendencyPage.class,NewsPage.class,MorePage.class};
	private int imageViewArray[]={R.drawable.home_btn,R.drawable.tendency_btn,R.drawable.news_btn,R.drawable.more_btn};
	private String textViewArray[]={"天气","趋势","资讯","设置"};
	//数据库实例
	private MySQLite sqlite;
	//保存当前的城市信息属性
	private String currentCityID="101010100";
	private String province="北京";
	private String currentCityName="北京";//默认城市为北京
	private static WeatherAttribute currentWeatherAttibute=null;//常规天气属性
	private static FiveDayWeatherAttribute currentFiveDayWeatherAttibute=null;//未来五天天气属性
	//对子线程发出的消息进行响应
	public static Handler handler;
	//用于切换Activity
	private Intent intent;
	private static int currentUI = 0;//0表示当前为主界面，1表示其他界面
	
	private static String isFirstRun="YES";//一定要声明为静态变量
	//保存对话框标识
	private static ProgressDialog LoadWeatherPd=null;
	private static ProgressDialog GetFiveDayPd = null;
	private ProgressDialog pd=null;//用于显示加载进度
	
	private boolean loadFivDay;//标记调用drawChart是否因为通过网络获取数据
	
	private static boolean isTip=true;//标记是否提示网络不可用
	
	private long reallyexit=0;//用于判断是否在2秒内连续按下返回键退出
	
	private int screenWidth;//保存屏幕的宽度
	private int screenHeight;//保存屏幕的高度
	
	private static String currentTab="天气";//保存当前的Tab选项
	
	private static ArrayList<NewsAttribute> newsInfo=new ArrayList<NewsAttribute>();//存放新闻列表信息
	private static ArrayList<ContentAttribute> contentInfor=new ArrayList<ContentAttribute>();//保存每条新闻的内容信息
	
	private boolean canFlush=false;//标志资讯界面是否能刷新
	private float downY=-100;//记录资讯界面按下的y坐标
	private int flushMargin=-160;//刷新TextView当前的margin值
	
	private int lastNewsIndex=9;//listview中最后一条新闻的索引
	
	private int CurrentListViewSelect=0;//记录当前listview在屏幕上显示的第一条的索引
	
	private String flushType;//标记updateView的调用是因为刷新或者加载更多产生的
	
	private boolean isUpdate = false;//标记是是否因为刷新调用initView
	
	private boolean flushLock = true;//标记下拉刷新最多只能有一个子线程执行
	private boolean loadMoreLock = true;//标记下加载更多最多只能有一个子线程执行
	
	private Animation anim;//更新图标旋转
	private boolean isRotate = false;//标记更新图标是否在旋转
	private Animation flush_downtoup_anim;//下拉刷新箭头翻转动画
	private Animation flush_uptodown_anim;//下拉刷新箭头翻转动画
	private int flushCount = 0;
	private Animation flush_anim;//正在刷新动画
	
	private static boolean[] isClicked = new boolean[2000];//记录被点过的资讯
	
	private static NetworkLocation location;//获取地理位置信息实例
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉系统自带标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置布局
        setContentView(R.layout.activity_main);
       
        //初始化主界面  
        initView();

        //获取屏幕大小信息
        getScreenInfo();
        
        //初始化更新图标动画
        anim = AnimationUtils.loadAnimation(this, R.anim.update_anim);
        anim.setInterpolator(new LinearInterpolator());
        //初始化下拉刷新箭头翻转及刷新动画
        flush_downtoup_anim = AnimationUtils.loadAnimation(this, R.anim.flush_downtoup_anim);
        flush_downtoup_anim.setInterpolator(new AccelerateDecelerateInterpolator());
        flush_downtoup_anim.setFillAfter(true);
        flush_uptodown_anim = AnimationUtils.loadAnimation(this, R.anim.flush_uptodown_anim);
        flush_uptodown_anim.setInterpolator(new AccelerateDecelerateInterpolator());
        flush_uptodown_anim.setFillAfter(true);
        flush_anim = AnimationUtils.loadAnimation(this, R.anim.flush_anim);
        flush_anim.setInterpolator(new LinearInterpolator());
        flush_anim.setFillAfter(true);
        //读取上次所设置的城市并初始化天气信息
        if("YES".equals(isFirstRun))
        {
        	isFirstRun="NO";
        	LoadWeatherPd = ProgressDialog.show(MainActivity.this, "提示", "正在加载数据");
        	String cityId=readLasttimeCity();
        	currentCityID=cityId;
        	initWeather(currentCityID);
        	//创建获取位置信息的实例并开始监听位置变化
        	
        }//程序的初始化结束，进入响应用户操作状态
        //响应子线程发来的消息
        handler=new Handler(){
        	public void handleMessage(Message msg)
        	{
        		switch(msg.what)
        		{
        		case 0:setInfor(currentWeatherAttibute);
		        	   if(location==null)
		        		{
		        		    location = new NetworkLocation(MainActivity.this);
			        	    location.startListenLocationChange();
		        		}
        			   break;
        		case 1:Toast.makeText(MainActivity.this, "网络不可用", Toast.LENGTH_SHORT).show();break;
        		case 2:Toast.makeText(MainActivity.this, "更新中...",Toast.LENGTH_LONG).show();break;
        		case 3:
        			if(GetFiveDayPd!=null) {GetFiveDayPd.dismiss();GetFiveDayPd=null;}
        			Toast.makeText(MainActivity.this, "获取数据失败", Toast.LENGTH_SHORT).show();
        			break;
        		case 4:
        			if(GetFiveDayPd!=null) {GetFiveDayPd.dismiss();GetFiveDayPd=null;}
        			drawChart();//更新城市名并且绘制未来五天天气趋势图
        			break;
        		case 5:
        			updateNewsList();
        			flushAndLoad();//设置事件
        			break;//为listview增加监听事件
        		case 6:break;
        		case 7:if(pd!=null) {pd.dismiss();pd=null;}
        				break;
        		
        		case 8: if("资讯".equals(currentTab))
        				{
        					LinearLayout newslist1=(LinearLayout)findViewById(R.id.news);
        					RelativeLayout flush_header1 = (RelativeLayout) findViewById(R.id.flush_header);
        					TextView flush_text1 = (TextView) newslist1.findViewById(R.id.flush_text);
        					TextView flush_picture1 = (TextView) newslist1.findViewById(R.id.flush_picture);
        					flush_picture1.setBackgroundResource(R.drawable.pushdown);
        					flush_text1.setText("刷新失败");
        					Toast.makeText(MainActivity.this, "刷新失败", Toast.LENGTH_SHORT).show();
        					setMargins(flush_header1, 0, -300, 0, 0);
        				}
        					flushMargin=-160;
        					break;
						
        		case 9:if("资讯".equals(currentTab))
        				{
        					LinearLayout newslist2=(LinearLayout)findViewById(R.id.news);
        					RelativeLayout flush_header2 = (RelativeLayout) findViewById(R.id.flush_header);
        					TextView flush_text2 = (TextView) newslist2.findViewById(R.id.flush_text);
        					TextView flush_picture2 = (TextView) newslist2.findViewById(R.id.flush_picture);
        					flush_picture2.setBackgroundResource(R.drawable.pushdown);
        					flush_text2.setText("刷新成功");
        					Toast.makeText(MainActivity.this, "刷新成功", Toast.LENGTH_SHORT).show();
        					updateNewsList();//刷新成功后更新界面
        					setMargins(flush_header2, 0, -300, 0, 0);
        				}
        					flushMargin=-160;
        					break;
						
        		case 10:
        			//加载更多没有新数据
        			if("资讯".equals(currentTab))
        			{
        				LinearLayout newslist3=(LinearLayout)findViewById(R.id.news);
        				TextView load_more = (TextView) newslist3.findViewById(R.id.load_more);
        				load_more.setText("加载更多");
        				setMargins(newslist3, 0, 0, 0, 0);
        			}
        				Toast.makeText(MainActivity.this, "没有新数据", Toast.LENGTH_SHORT).show();
        				break;
        		case 11:
        			//加载更多成功
        			if("资讯".equals(currentTab))
        			{
	    				LinearLayout news = (LinearLayout) MainActivity.this.findViewById(R.id.news);
	    		    	ListView lv_newslist = (ListView) news.findViewById(R.id.lv_newslist);
	    		    	flushType="加载更多";
	    		    	updateNewsList();//重新加载listview
	    		    	setMargins(lv_newslist,0,0,0,0);
	    		    	Toast.makeText(MainActivity.this, "加载成功", Toast.LENGTH_SHORT).show();
        			}
        			break;
        		case 12:
        			//加载更多发生异常
        			if("资讯".equals(currentTab))
        			{
        				LinearLayout newslist=(LinearLayout)findViewById(R.id.news);
        				TextView load_more = (TextView)newslist.findViewById(R.id.load_more);
        				load_more.setText("加载更多");
        				Toast.makeText(MainActivity.this, "加载失败,请重试", Toast.LENGTH_SHORT).show();
        			}
        			break;
        		case 13:
        			//当选择资讯选项卡并且获取数据失败时
        				if(pd!=null) {pd.dismiss();pd=null;}
        				Toast.makeText(MainActivity.this, "获取数据失败", Toast.LENGTH_SHORT).show();
        				break;
        		case 14:
        			initSet();
        			break;
        		case 15:
        			if(newsInfo!=null&&newsInfo.size()>0&&contentInfor!=null&&contentInfor.size()>0)
        			{
        				updateNewsList();
        			}
        			else
        			{
        				if(newsInfo!=null) newsInfo.clear();
        				if(contentInfor!=null) contentInfor.clear();
        				getNewsInfor();
        			}
        			break;
        		case 16:
        			if(pd!=null) {pd.dismiss();pd=null;}
    				Toast.makeText(MainActivity.this, "获取数据成功", Toast.LENGTH_SHORT).show();
    				updateNewsList();
    				flushAndLoad();//设置下拉刷新和加载更多
    				break;
        		case 17:
        			setInfor(currentWeatherAttibute);
        			break;
        		case 18:
        			if(LoadWeatherPd!=null) {LoadWeatherPd.dismiss();LoadWeatherPd=null;}
        			break;
        		case 19:
        			Toast.makeText(MainActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
        		case 20:
        			updateNewsList();
        			flushAndLoad();//设置下拉刷新和加载更多
        			break;
        			//开始和停止更新图标旋转
        		case 21:
        			waitForStopAnim();
        			break;
        		case 22:
        			LinearLayout home1 =(LinearLayout) findViewById(R.id.home);
        			ImageView update1 = (ImageView) home1.findViewById(R.id.update);
        			update1.clearAnimation();
        			break;
        		case 23:
        			LinearLayout home2 =(LinearLayout) findViewById(R.id.home);
        			ImageView update2 = (ImageView) home2.findViewById(R.id.update);
        			update2.startAnimation(anim);
        			break;
        		case 24:
        			Toast.makeText(MainActivity.this, "正在更新信息，请稍后再试", Toast.LENGTH_SHORT).show();
        			break;
        		case 25:
        			if(currentUI==0&&pd==null&&LoadWeatherPd==null&&GetFiveDayPd==null)
        			{
        			currentWeatherAttibute = location.getCurrentWeatherAttibute();
        			currentCityID = currentWeatherAttibute.cityId; 
        			province = currentWeatherAttibute.province;
        			currentCityName = currentWeatherAttibute.cityName;
        			currentFiveDayWeatherAttibute = location.getCurrentFiveDayWeatherAttibute();
        			if("天气".equals(currentTab)) setInfor(currentWeatherAttibute);
        			if("趋势".equals(currentTab)) drawChart();
        			Toast.makeText(MainActivity.this, "当前位置为:"+currentWeatherAttibute.cityName, Toast.LENGTH_SHORT).show();
        			}
        			break;
        		}
        		super.handleMessage(msg);
        	}
        };
    }
    //为更换城市按钮绑定点击事件
    public void changeCity(View v)
    {
    	//当没有在更新时切换
    	if(!isRotate)
    	{
    		currentUI = 1;
    		Intent intent=new Intent();
    		intent.setClass(MainActivity.this, ChangeCityActivity.class);
    		MainActivity.this.startActivity(intent);
    		MainActivity.this.finish();
    	}
    	//否则提示等更新再试
    	else
    	{
    		handler.sendEmptyMessage(24);
    	}
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    protected void onResume()
    {
    	super.onResume();
    	currentUI = 0;
    	//当进入程序原先Tab选项为天气时
    	if("天气".equals(currentTab))
    	{
	    	intent=getIntent();
	    	String str1=intent.getStringExtra("fromSelect");
	    	String str2=intent.getStringExtra("fromBack");
	    	
	    	//当onResume方法是因为按搜索按钮返回到主界面调用时
	    	if("yes".equals(str1))
	    	{
	    		WeatherAttribute tempWeatherAttribute=(WeatherAttribute) intent.getSerializableExtra("weatherInformation");
	    		currentWeatherAttibute=tempWeatherAttribute;
	    		currentFiveDayWeatherAttibute = (FiveDayWeatherAttribute) intent.getSerializableExtra("fiveday");
	    		new Thread(new Runnable(){
	    			public void run()
	    			{
						LinearLayout home = (LinearLayout) MainActivity.this.findViewById(R.id.home);
						while(home==null)
						{
							home = (LinearLayout) findViewById(R.id.home);
							//不然操作次数过多会产生异常
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
	    				handler.sendEmptyMessage(17);//通知主线程可以更新界面
	    			}
	    		}).start();
	    	}
	    	//当按返回键返回时
	    	if("yes".equals(str2))
	    	{
	    		if(currentWeatherAttibute!=null)
	    		{
	    			new Thread(new Runnable(){
		    			public void run()
		    			{
							LinearLayout home = (LinearLayout) MainActivity.this.findViewById(R.id.home);
							while(home==null)
							{
								home = (LinearLayout) MainActivity.this.findViewById(R.id.home);
								//不然操作次数过多会产生异常
								try {
									Thread.sleep(50);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
		    				handler.sendEmptyMessage(17);
		    			}
		    		}).start();
	    		}
	    	}
    	}
    }
    public void onDestroy()
    {
    	super.onDestroy();
    	if(LoadWeatherPd!=null)
    	{
    		LoadWeatherPd=null;
    		LoadWeatherPd.dismiss(); 
    	}
    }
    //初始化程序选项卡主界面
    private void initView()
    {
    	layoutInflater=LayoutInflater.from(this);
    	tabHost=(FragmentTabHost)findViewById(R.id.tabhost);
    	tabHost.setup(this,getSupportFragmentManager(),R.id.tabcontent);
    	int count=fragmentArray.length;
    	for(int i=0;i<count;i++)
    	{
    		TabSpec tabSpec=tabHost.newTabSpec(textViewArray[i]).setIndicator(getTabItemView(i));
    		
    		tabHost.addTab(tabSpec,fragmentArray[i],null);
    		tabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.background_selected);
    	}
    	//对选项卡选项进行事件捕获
    	tabHost.setOnTabChangedListener(new OnTabChangeListener(){
			@Override
			public void onTabChanged(String str) {
				if("天气".equals(str))
				{
					currentTab="天气";
					
					if(isRotate)
					{
						new Thread(new Runnable(){
							public void run()
							{
								LinearLayout home = (LinearLayout) findViewById(R.id.home);
								while(home==null)
								{
									home = (LinearLayout) findViewById(R.id.home);
									try {
										Thread.sleep(30);
									} catch (InterruptedException e) {
										// TODO 自动生成的 catch 块
										e.printStackTrace();
									}
								}
								handler.sendEmptyMessage(23);
							}
						}).start();
					}
					
					if(currentWeatherAttibute!=null)
		    		{
						handler.sendEmptyMessage(0);
		    		}
					else
					{
						LoadWeatherPd = ProgressDialog.show(MainActivity.this, "提示", "正在加载数据");
						initWeather(currentCityID);
					}
				}
				else if("趋势".equals(str))
				{
					currentTab="趋势";
					loadFivDay=false;
					if(currentFiveDayWeatherAttibute!=null)
					{
						handler.sendEmptyMessage(4);
					}
					else
					{
						loadFivDay=true;
						GetFiveDayPd = ProgressDialog.show(MainActivity.this,"提示", "正在加载数据");
						getFiveDay();//获取未来五天信息,完成后通知主线程更新UI
					}
				}
				else if("资讯".equals(str))
				{
					currentTab="资讯";
					if(newsInfo.size()>0)
					{
						new Thread(new Runnable(){
							public void run()
							{
								LinearLayout newslist=(LinearLayout)MainActivity.this.findViewById(R.id.news);
								while(newslist==null)
								{
									newslist=(LinearLayout)MainActivity.this.findViewById(R.id.news);
									try {
										Thread.sleep(50);
									} catch (InterruptedException e) {
										// TODO 自动生成的 catch 块
										e.printStackTrace();
									}
								}
								handler.sendEmptyMessage(20);
							}
						}).start();
					}
					else
					{
						pd=ProgressDialog.show(MainActivity.this, "提示", "正在加载数据");//提示用户正在加载数据
						getNewsInfor();
					}
				}
				else if("设置".equals(str))
				{
					currentTab="设置";
					handler.sendEmptyMessage(14);//为设置绑定点击事件
				}
			}
    	});
    }
    private View getTabItemView(int index)
    {
    	View view=layoutInflater.inflate(R.layout.tab_view, null);
    	
    	ImageView imageView=(ImageView)view.findViewById(R.id.imageview);
    	
    	imageView.setImageResource(imageViewArray[index]);
    	TextView textView=(TextView)view.findViewById(R.id.textview);
    	textView.setText(textViewArray[index]);
    	return view;
    }
    //通过网络获取天气信息
    public void initWeather(final String cityId)
    {
    	//在子线程进行网络IO操作
    	new Thread(new Runnable(){
    		public void run()
    		{
	    		if(cityId!=null)
	        	{
	        		String rawResult=WeatherRequest.getWeatherInfor(cityId,MainActivity.this);
	        		WeatherAttribute weatherAttributeTemp = WeatherRequest.parseJson(rawResult);
	        		FiveDayWeatherAttribute currentFiveDayWeatherAttibuteTemp = ForecastFiveDayRequest.getFiveDayWeatherInfor(currentCityID, MainActivity.this);
	        		if(weatherAttributeTemp==null)
	        		{
	        			handler.sendEmptyMessage(18);//取消对话框
	        			if(isTip)
	        			{
	        				isTip=false;
	        				handler.sendEmptyMessage(1);//提示网络不可用
	        			}
	        			else
	        			{
	        				handler.sendEmptyMessage(3);//提示获取数据失败
	        			}
	        		}
	        		else
	        		{
	        			if(currentFiveDayWeatherAttibuteTemp!=null) currentFiveDayWeatherAttibute = currentFiveDayWeatherAttibuteTemp;
	        			isTip=false;
		        		currentWeatherAttibute=weatherAttributeTemp;
		        		handler.sendEmptyMessage(18);//取消对话框
		        		try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO 自动生成的 catch 块
							e.printStackTrace();
						}
		        		handler.sendEmptyMessage(0);//更新UI
		        		if(isUpdate==true)
		        		{
		        			handler.sendEmptyMessage(19);
		        			isUpdate = false;
		        		}
	        		}
	        	}
	    		else handler.sendEmptyMessage(18);//取消对话框
	    		if(isRotate)
	    		{
	    			isRotate = false;
	    			handler.sendEmptyMessage(21);//停止更新图标旋转
	    		}
	    		
    		}
    	}).start();
    }
    //得到未来五天天气信息
    public void getFiveDay()
    {
    	new Thread(new Runnable(){
			@Override
			public void run() {
				currentFiveDayWeatherAttibute=ForecastFiveDayRequest.getFiveDayWeatherInfor(currentCityID, MainActivity.this);
				if(currentFiveDayWeatherAttibute==null) handler.sendEmptyMessage(3);
				//更新城市名并绘制折线图
				else handler.sendEmptyMessage(4);
			}
    	}).start();
    }

    //点击更新图标时调用该函数
    public void updateInfor(View v)
    {
    	if(!isRotate)
    	{
	    	isTip=true;
	    	isUpdate = true;
	    	//开始旋转更新图标
	    	LinearLayout home =(LinearLayout) MainActivity.this.findViewById(R.id.home);
	    	ImageView update = (ImageView) home.findViewById(R.id.update);
	    	update.startAnimation(anim);
	    	isRotate = true;
	    	handler.sendEmptyMessage(2);
	    	initWeather(currentCityID);
    	}
    }
    public void waitForStopAnim()
    {
    	new Thread(new Runnable(){
    		public void run()
    		{
    			LinearLayout home = (LinearLayout) findViewById(R.id.home);
    			while(home==null)
    			{
    				home = (LinearLayout) findViewById(R.id.home);
    				try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
    			}
    			handler.sendEmptyMessage(22);
    		}
    	}).start();
    }
    //设置天气信息
    public void setInfor(WeatherAttribute tempWeatherAttribute)
    {
    	try
		{
    		if(tempWeatherAttribute!=null)
    		{
    		
	    		//通过findViewById获取当前id为R.id.home的布局,用inflate获取的是新的布局不是当前的
	
	    		LinearLayout subLayout=(LinearLayout) MainActivity.this.findViewById(R.id.home);
	    		
	    		TextView cityName = (TextView)subLayout.findViewById(R.id.city);
	    		cityName.setText(tempWeatherAttribute.cityName);//设置城市名
	    		currentCityName=tempWeatherAttribute.cityName;
	    		province=tempWeatherAttribute.province;
	    		currentCityID=tempWeatherAttribute.cityId;
	    		currentWeatherAttibute=tempWeatherAttribute;
	    		
	    		TextView realtimeTemp=(TextView) subLayout.findViewById(R.id.realtimetemp);
	    		realtimeTemp.setText(tempWeatherAttribute.realtimeTemperature);//设置实时温度
	    		ImageView statusImg=(ImageView)subLayout.findViewById(R.id.statusimage);
	    		setStatusImage(statusImg,tempWeatherAttribute.status);//设置天气图标
	    		TextView status=(TextView)subLayout.findViewById(R.id.statustext);
	    		status.setText(tempWeatherAttribute.status);//设置状况
	    		TextView date=(TextView)subLayout.findViewById(R.id.date);
	    		date.setText(tempWeatherAttribute.date);//设置当天日期
	    		TextView temperatureRange=(TextView)subLayout.findViewById(R.id.mintomaxtemp);
	    		temperatureRange.setText(tempWeatherAttribute.temperatureRange);//设置温度范围
	    		TextView windyPower=(TextView)subLayout.findViewById(R.id.windypower);
	    		windyPower.setText(tempWeatherAttribute.windPower);//设置风力
	    		TextView windyDirection=(TextView)subLayout.findViewById(R.id.windydirection);
	    		windyDirection.setText(tempWeatherAttribute.windDirection);//设置风向
	    		TextView humidity=(TextView)subLayout.findViewById(R.id.humidity);
	    		humidity.setText(tempWeatherAttribute.humidity);//设置湿度
	    		TextView airquality=(TextView)subLayout.findViewById(R.id.airqualitynumber);
	    		airquality.setText(tempWeatherAttribute.airQuality);//设置空气质量数值
	    		TextView airqualitygrade=(TextView)subLayout.findViewById(R.id.airqulitygrade);
	    		airqualitygrade.setText(setAirQuality(tempWeatherAttribute.airQuality));//设置空气质量等级
	    		TextView pm=(TextView)subLayout.findViewById(R.id.pm);
	    		pm.setText(tempWeatherAttribute.PM);//设置PM2.5
	    		TextView update=(TextView)subLayout.findViewById(R.id.publishtime);
	    		update.setText(tempWeatherAttribute.updateTime);//设置更新时间
    		
    		}
	    	else
	    	{
	    		Log.i(tag,"1223");
	    		LinearLayout subLayout=(LinearLayout) MainActivity.this.findViewById(R.id.home);
	    		TextView cityName = (TextView)subLayout.findViewById(R.id.city);
	    		cityName.setText("");//设置城市名
	    	}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }
    //从文件中读取上次保存的城市信息，没有的话默认为北京
    public String readLasttimeCity()
    {
    	try {
    		File file=new File("/data/data/com.main.weather/temp.txt");
    		if(!file.exists()) 
    		{
    			FileOutputStream os = new FileOutputStream("/data/data/com.main.weather/temp.txt");
    			String str="北京 北京 101010100";
    			os.write(str.getBytes());
    			os.close();
    			return currentCityID;
    		}
			FileInputStream is = new FileInputStream("/data/data/com.main.weather/temp.txt");
			byte[] b=new byte[1024];
			try {
				int n=is.read(b);
				String str=new String(b,0,n);
				String[] strArray=str.split(" ");
				is.close();
				currentCityName=strArray[0];
				currentCityID=strArray[2];
				return currentCityID;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
    }
    //保存当前城市信息到文件中
    public void saveFile()
    {
    	try {
    		//注意路径名
        	File file=new File("/data/data/com.main.weather/temp.txt");
			FileOutputStream os = new FileOutputStream(file);
			String temp=currentCityName+" "+province+" "+currentCityID;
			os.write(temp.getBytes());
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    //将空气质量转换为等级
    public String setAirQuality(String number)
    {
    	if("无数据".equals(number)) return "";
    	int num=Integer.parseInt(number);
    	if(num>=0&&num<=50) return "优";
    	else if(num>=51&&num<=100) return "良";
    	else if(num>=101&&num<=150) return "轻度污染";
    	else if(num>=151&&num<=200) return "中度污染";
    	else if(num>=201&&num<=300) return "重度污染";
    	else if(num>300) return "严重污染";
    	else return null;
    }
    public void setStatusImage(ImageView tv,String status)
    {
    	if("晴".equals(status)) tv.setImageResource(R.drawable.sunny);
    	else if("少云".equals(status)) tv.setImageResource(R.drawable.shaoyun);
    	else if("多云".equals(status)) tv.setImageResource(R.drawable.shaoyun);
    	else if("阴".equals(status)) tv.setImageResource(R.drawable.yin);
    	else if("阵雨".equals(status)) tv.setImageResource(R.drawable.zhenyu);
    	else if("雷阵雨".equals(status)) tv.setImageResource(R.drawable.leizhenyu);
    	else if("小雨".equals(status)) tv.setImageResource(R.drawable.xiaoyu);
        else if("中雨".equals(status)) tv.setImageResource(R.drawable.zhongyu);
   		else if("大雨".equals(status)) tv.setImageResource(R.drawable.dayu);
    	else if("大到暴雨".equals(status)) tv.setImageResource(R.drawable.dadaobaoyu);
    	else if("暴雨".equals(status)) tv.setImageResource(R.drawable.baoyu);
    	else if("小雪".equals(status)) tv.setImageResource(R.drawable.xiaoxue);
    	else if("中雪".equals(status)) tv.setImageResource(R.drawable.zhongxue);
   		else if("大雪".equals(status)) tv.setImageResource(R.drawable.daxue);
    	else if("暴雪".equals(status)) tv.setImageResource(R.drawable.baoxue);
    	else if("沙尘暴".equals(status)) tv.setImageResource(R.drawable.shachenbao);
    	else if("龙卷风".equals(status)) tv.setImageResource(R.drawable.longjuanfeng);
    	else if("雾".equals(status)) tv.setImageResource(R.drawable.wu);
        else if("霾".equals(status)) tv.setImageResource(R.drawable.mai);
        else if("雨夹雪".equals(status)) tv.setImageResource(R.drawable.yujiaxue);
    	else tv.setImageResource(R.drawable.undefined);
    }
    //捕获返回键
    public boolean onKeyDown(int keyCode,KeyEvent event)
    {
    	if(keyCode==KeyEvent.KEYCODE_BACK)
    	{
    		//实现连续按返回键两次退出程序
    		if(System.currentTimeMillis()-reallyexit>2000)
    		{
    			reallyexit=System.currentTimeMillis();
    			Toast.makeText(this, "再按一次退出程序", 0).show();
    		}
    		else
    		{
    			isFirstRun="YES";
    			isTip=true;
    			//saveNewsToFile();
    			saveFile();//保存当前城市信息到文件中
    			return super.onKeyDown(keyCode, event);
    		}
    	}
    	return true;
    }
    //获取屏幕大小信息
    public void getScreenInfo()
    {
    	DisplayMetrics metric = new DisplayMetrics();
    	this.getWindowManager().getDefaultDisplay().getMetrics(metric);
    	screenWidth=metric.widthPixels;
    	screenHeight=metric.heightPixels;
    	ChartView.setScreenWidth(screenWidth);
    	ChartView.setScreenHeight(screenHeight);
    }
    //绘制折线图
    public void drawChart()
    {
    	try
    	{
	    	if(currentFiveDayWeatherAttibute==null) 
	    	{
	    		LinearLayout tendency=(LinearLayout)MainActivity.this.findViewById(R.id.tendency);
				TextView feature=(TextView)tendency.findViewById(R.id.feature);
				feature.setText("无数据");
	    		//设置不能显示折线图
	    		ChartView.setCanDraw(false);
	    		Toast.makeText(this, "没有相关天气信息", 0).show();
	    	}
	    	else
	    	{
	    		LinearLayout tendency=(LinearLayout)MainActivity.this.findViewById(R.id.tendency);
				TextView feature=(TextView)tendency.findViewById(R.id.feature);
				ChartView chart = (ChartView)tendency.findViewById(R.id.chart);
				
				feature.setText(currentCityName+"未来5天天气");
	    		//设置能显示折线图
	    		ChartView.setCanDraw(true);
	    		//设置星期
	    		TextView week1=(TextView)findViewById(R.id.week1);
	    		week1.setTextSize(Screen.getTextSize(screenWidth));
	    		week1.setText(currentFiveDayWeatherAttibute.weekArray[0]);
	    		TextView week2=(TextView)findViewById(R.id.week2);
	    		week2.setTextSize(Screen.getTextSize(screenWidth));
	    		week2.setText(currentFiveDayWeatherAttibute.weekArray[1]);
	    		TextView week3=(TextView)findViewById(R.id.week3);
	    		week3.setTextSize(Screen.getTextSize(screenWidth));
	    		week3.setText(currentFiveDayWeatherAttibute.weekArray[2]);
	    		TextView week4=(TextView)findViewById(R.id.week4);
	    		week4.setTextSize(Screen.getTextSize(screenWidth));
	    		week4.setText(currentFiveDayWeatherAttibute.weekArray[3]);
	    		TextView week5=(TextView)findViewById(R.id.week5);
	    		week5.setTextSize(Screen.getTextSize(screenWidth));
	    		week5.setText(currentFiveDayWeatherAttibute.weekArray[4]);
	    		TextView week6=(TextView)findViewById(R.id.week6);
	    		week6.setTextSize(Screen.getTextSize(screenWidth));
	    		week6.setText(currentFiveDayWeatherAttibute.weekArray[5]);
	    		//设置日期
	    		TextView date1=(TextView)findViewById(R.id.date1);
	    		date1.setTextSize(Screen.getTextSize(screenWidth));
	    		date1.setText(currentFiveDayWeatherAttibute.dayArray[0]);
	    		TextView date2=(TextView)findViewById(R.id.date2);
	    		date2.setTextSize(Screen.getTextSize(screenWidth));
	    		date2.setText(currentFiveDayWeatherAttibute.dayArray[1]);
	    		TextView date3=(TextView)findViewById(R.id.date3);
	    		date3.setTextSize(Screen.getTextSize(screenWidth));
	    		date3.setText(currentFiveDayWeatherAttibute.dayArray[2]);
	    		TextView date4=(TextView)findViewById(R.id.date4);
	    		date4.setTextSize(Screen.getTextSize(screenWidth));
	    		date4.setText(currentFiveDayWeatherAttibute.dayArray[3]);
	    		TextView date5=(TextView)findViewById(R.id.date5);
	    		date5.setTextSize(Screen.getTextSize(screenWidth));
	    		date5.setText(currentFiveDayWeatherAttibute.dayArray[4]);
	    		TextView date6=(TextView)findViewById(R.id.date6);
	    		date6.setTextSize(Screen.getTextSize(screenWidth));
	    		date6.setText(currentFiveDayWeatherAttibute.dayArray[5]);
	    		//设置白天天气状况
	    		TextView daystatus1=(TextView)findViewById(R.id.daystatus1);
	    		daystatus1.setTextSize(Screen.getTextSize(screenWidth));
	    		daystatus1.setText(currentFiveDayWeatherAttibute.dayStatusArray[0]);
	    		TextView daystatus2=(TextView)findViewById(R.id.daystatus2);
	    		daystatus2.setTextSize(Screen.getTextSize(screenWidth));
	    		daystatus2.setText(currentFiveDayWeatherAttibute.dayStatusArray[1]);
	    		TextView daystatus3=(TextView)findViewById(R.id.daystatus3);
	    		daystatus3.setTextSize(Screen.getTextSize(screenWidth));
	    		daystatus3.setText(currentFiveDayWeatherAttibute.dayStatusArray[2]);
	    		TextView daystatus4=(TextView)findViewById(R.id.daystatus4);
	    		daystatus4.setTextSize(Screen.getTextSize(screenWidth));
	    		daystatus4.setText(currentFiveDayWeatherAttibute.dayStatusArray[3]);
	    		TextView daystatus5=(TextView)findViewById(R.id.daystatus5);
	    		daystatus5.setTextSize(Screen.getTextSize(screenWidth));
	    		daystatus5.setText(currentFiveDayWeatherAttibute.dayStatusArray[4]);
	    		TextView daystatus6=(TextView)findViewById(R.id.daystatus6);
	    		daystatus6.setTextSize(Screen.getTextSize(screenWidth));
	    		daystatus6.setText(currentFiveDayWeatherAttibute.dayStatusArray[5]);
	    		//画温度折线图
	    		ChartView.setTemp(currentFiveDayWeatherAttibute.maxTempArray, currentFiveDayWeatherAttibute.minTempArray);
	    		if(loadFivDay) chart.postInvalidate();
	    		//设置夜间天气状况
	    		TextView nightstatus1=(TextView)findViewById(R.id.nightstatus1);
	    		nightstatus1.setTextSize(Screen.getTextSize(screenWidth));
	    		nightstatus1.setText(currentFiveDayWeatherAttibute.nightStatusArray[0]);
	    		TextView nightstatus2=(TextView)findViewById(R.id.nightstatus2);
	    		nightstatus2.setTextSize(Screen.getTextSize(screenWidth));
	    		nightstatus2.setText(currentFiveDayWeatherAttibute.nightStatusArray[1]);
	    		TextView nightstatus3=(TextView)findViewById(R.id.nightstatus3);
	    		nightstatus3.setTextSize(Screen.getTextSize(screenWidth));
	    		nightstatus3.setText(currentFiveDayWeatherAttibute.nightStatusArray[2]);
	    		TextView nightstatus4=(TextView)findViewById(R.id.nightstatus4);
	    		nightstatus4.setTextSize(Screen.getTextSize(screenWidth));
	    		nightstatus4.setText(currentFiveDayWeatherAttibute.nightStatusArray[3]);
	    		TextView nightstatus5=(TextView)findViewById(R.id.nightstatus5);
	    		nightstatus5.setTextSize(Screen.getTextSize(screenWidth));
	    		nightstatus5.setText(currentFiveDayWeatherAttibute.nightStatusArray[4]);
	    		TextView nightstatus6=(TextView)findViewById(R.id.nightstatus6);
	    		nightstatus6.setTextSize(Screen.getTextSize(screenWidth));
	    		nightstatus6.setText(currentFiveDayWeatherAttibute.nightStatusArray[5]);
	    	}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }
    //更新新闻列表
    public void updateNewsList()
    {
		class MyAdapter extends BaseAdapter
		{
			@Override
			public int getCount() {
				return lastNewsIndex+1;
			}
			@Override
			public Object getItem(int arg0) {
				return null;
			}
			@Override
			public long getItemId(int arg0) {
				return 0;
			}
			@Override
			public View getView(int position, View arg1, ViewGroup arg2) {
				View v = View.inflate(MainActivity.this, R.layout.news_item, null);
				TextView news_title = (TextView)v.findViewById(R.id.news_title);
				TextView publishtime=(TextView)v.findViewById(R.id.publishtime);
				if(position<2000&&isClicked[position]==true)
				{
					news_title.setTextColor(Color.GRAY);
					publishtime.setTextColor(Color.GRAY);
				}
				if(newsInfo.get(position).title!=null)
				{
					news_title.setPadding(15, 15, 10, 10);
					news_title.setText(newsInfo.get(position).title);
				}
				if(newsInfo.get(position).updateTime!=null)
				{
					publishtime.setPadding(15, 0, 10, 10);
					publishtime.setText(newsInfo.get(position).updateTime);
				}
				return v;
			}
		}
		if("资讯".equals(currentTab))
		{
			LinearLayout newslist=(LinearLayout)MainActivity.this.findViewById(R.id.news);
			ListView lv_newslist=(ListView)newslist.findViewById(R.id.lv_newslist);
			lv_newslist.setAdapter(new MyAdapter());
			//如果是加载更多刷新界面那么就恢复到原先位置
			if(flushType!=null&&"加载更多".equals(flushType))  lv_newslist.setSelection(CurrentListViewSelect);
			
			//为现有的所有item绑定点击事件
			lv_newslist.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> adapter, View v,
						int position, long arg3) {
					TextView news_title = (TextView) v.findViewById(R.id.news_title);
					TextView publishTime = (TextView) v.findViewById(R.id.publishtime);
					if(position<2000) isClicked[position]=true;
					news_title.setTextColor(Color.GRAY);
					publishTime.setTextColor(Color.GRAY);
					Intent intent = new Intent(MainActivity.this,NewsDetailActivity.class);
					intent.putExtra("url",newsInfo.get(position).url);
					startActivity(intent);
				}
			});
		}
    }
    //首次加载资讯数据时调用该函数
    public void getNewsInfor()
    {
    	new Thread(new Runnable(){
    		public void run()
    		{
    			ArrayList<NewsAttribute> newslistArrayList = new ArrayList<NewsAttribute>();
    			
    			String newslistHtml1 = NewsRequest.getNewsListHtml("http://news.weather.com.cn/list.shtml");
    			ArrayList<NewsAttribute> newslistArrayList1 = NewsRequest.parseListHtml(newslistHtml1);
    			if(newslistArrayList1!=null) newslistArrayList.addAll(newslistArrayList1);
    			if(newslistArrayList.size()>=20) 
    			{newsInfo=newslistArrayList;lastNewsIndex=19;handler.sendEmptyMessage(16);return;}
    			
    			String newslistHtml2 = NewsRequest.getNewsListHtml("http://news.weather.com.cn/list_2.shtml");
    			ArrayList<NewsAttribute> newslistArrayList2 = NewsRequest.parseListHtml(newslistHtml2);
    			if(newslistArrayList2!=null) newslistArrayList.addAll(newslistArrayList2);
    			if(newslistArrayList.size()>=20) 
    			{newsInfo=newslistArrayList;lastNewsIndex=19;handler.sendEmptyMessage(16);return;}
    			
    			String newslistHtml3 = NewsRequest.getNewsListHtml("http://news.weather.com.cn/list_3.shtml");
    			ArrayList<NewsAttribute> newslistArrayList3 = NewsRequest.parseListHtml(newslistHtml3);
    			if(newslistArrayList3!=null) newslistArrayList.addAll(newslistArrayList3);
    			if(newslistArrayList.size()>=20) 
    			{newsInfo=newslistArrayList;lastNewsIndex=19;handler.sendEmptyMessage(16);return;}
    			
    			String newslistHtml4 = NewsRequest.getNewsListHtml("http://news.weather.com.cn/list_4.shtml");
    			ArrayList<NewsAttribute> newslistArrayList4 = NewsRequest.parseListHtml(newslistHtml4);
    			if(newslistArrayList4!=null) newslistArrayList.addAll(newslistArrayList4);
    			if(newslistArrayList.size()>=20) 
    			{newsInfo=newslistArrayList;lastNewsIndex=19;handler.sendEmptyMessage(16);return;}
    			
    			String newslistHtml5 = NewsRequest.getNewsListHtml("http://news.weather.com.cn/list_5.shtml");
    			ArrayList<NewsAttribute> newslistArrayList5 = NewsRequest.parseListHtml(newslistHtml5);
    			if(newslistArrayList5!=null) newslistArrayList.addAll(newslistArrayList5);
    			if(newslistArrayList.size()>=20)
    			{newsInfo=newslistArrayList;lastNewsIndex=19;handler.sendEmptyMessage(16);return;}
    			
    			//获取数据失败
    			handler.sendEmptyMessage(13);//获取数据失败
    		}
    	}).start();
    }
    //下拉刷新调用该函数
    public void flushAndLoad()
    {
    	if("资讯".equals(currentTab))
    	{
	        LinearLayout newslist=(LinearLayout)findViewById(R.id.news);
	        final RelativeLayout flush_header = (RelativeLayout) newslist.findViewById(R.id.flush_header);
			final TextView flush_text = (TextView) newslist.findViewById(R.id.flush_text);
	        final ListView lv_newslist=(ListView)newslist.findViewById(R.id.lv_newslist);
	        final TextView load_more = (TextView)newslist.findViewById(R.id.load_more);
	        lv_newslist.setOnScrollListener(new OnScrollListener() {
				@Override
				public void onScrollStateChanged(AbsListView v, int scrollState) {
					//当滚动到最底部时
					if(v.getLastVisiblePosition()==(v.getCount()-1))
					{
						//load_more.setText("加载更多");
						setMargins(lv_newslist, 0, 0, 0, 130);
						load_more.setText("正在加载...");
				    	loadMoreNews();
						//为加载更多绑定点击事件
				    	load_more.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								load_more.setText("正在加载...");
						    	loadMoreNews();
							}
						});
					}
				}
				@Override
				public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
					
				}
			});
	        //下拉刷新
			lv_newslist.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					LinearLayout newslist=(LinearLayout)findViewById(R.id.news);
					RelativeLayout flush_header = (RelativeLayout) newslist.findViewById(R.id.flush_header);
					TextView flush_text = (TextView) newslist.findViewById(R.id.flush_text);
					TextView flush_picture = (TextView) newslist.findViewById(R.id.flush_picture);
					switch(event.getAction())
					{
					case MotionEvent.ACTION_DOWN:
						setMargins(flush_header, 0, -350, 0, 0);
						if(lv_newslist.getFirstVisiblePosition()==0)
						{
							canFlush=true;
							downY=event.getRawY();
						}
						break;
					case MotionEvent.ACTION_MOVE:
						//每次屏幕滚动记录最后一条记录的索引
						CurrentListViewSelect=lv_newslist.getLastVisiblePosition();
						//每次屏幕滚动而且不在最底部时隐藏加载更多TextView
						if(lv_newslist.getLastVisiblePosition()<lv_newslist.getCount()-1) setMargins(lv_newslist,0,0,0,0);
						//判断是否符合刷新的条件
						if(canFlush)
						{	
							int distance =(int)(event.getRawY()-downY);
							if(flushMargin>=-160&&flushMargin<0)
							{
								flushMargin=flushMargin+distance/2;
								setMargins(flush_header, 0, flushMargin, 0, 0);
								if(flushCount%2==1)
								{
									flush_picture.startAnimation(flush_uptodown_anim);
									flushCount=0;
								}
								flush_text.setText("下拉刷新");
							}
							else if(flushMargin>=0&&flushMargin<80)
							{
								flushMargin=flushMargin+distance/4;
								setMargins(flush_header, 0, flushMargin, 0, 0);
								if(flushCount%2==0) 
								{
									flush_picture.startAnimation(flush_downtoup_anim);
									flushCount=1;
								}
								flush_text.setText("释放立即刷新");
							}
							else if(flushMargin>=80&&flushMargin<260)
							{
								flushMargin=flushMargin+distance/5;
								setMargins(flush_header, 0, flushMargin, 0, 0);
								flush_text.setText("释放立即刷新");
							}
							else if(flushMargin>=260)
							{
								flush_text.setText("释放立即刷新");
							}
							downY=event.getRawY();
						}
						break;
					case MotionEvent.ACTION_UP:
						canFlush=false;
						if(flushMargin>=0)
						{
							setMargins(flush_header, 0, 0, 0, 0);
							flushMargin=0;
							flush_picture.setBackgroundResource(R.drawable.flushnews);
							flush_picture.startAnimation(flush_anim);
							flush_text.setText("正在刷新...");
							//获取新数据执行完成后通主线程
							flushGetNewsInfor();
						}
						else
						{
							setMargins(flush_header, 0, -300, 0, 0);
							flushMargin=-160;
							flush_picture.setBackgroundResource(R.drawable.pushdown);
							flush_text.setText("下拉刷新");
						}
						break;
					}
					//调用默认的onTouchEvent函数，不然listview将无法滚动
					return lv_newslist.onTouchEvent(event);
				}
			});
    	}
    }
    //封装设置margin函数
    public static void setMargins(View v,int left,int top,int right,int bottom)
    {
    	if(v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)
    	{
    		ViewGroup.MarginLayoutParams p = (MarginLayoutParams) v.getLayoutParams();
    		p.setMargins(left, top, right, bottom);
    		v.requestLayout();
    	}
    }
    public void flushGetNewsInfor()
    {
    	if(flushLock)
    	{
    		flushLock = false;
	    	new Thread(new Runnable(){
	    		public void run()
	    		{
	    			ArrayList<NewsAttribute> newslistArrayList = new ArrayList<NewsAttribute>();
	    			
	    			String newslistHtml1 = NewsRequest.getNewsListHtml("http://news.weather.com.cn/list.shtml");
	    			ArrayList<NewsAttribute> newslistArrayList1 = NewsRequest.parseListHtml(newslistHtml1);
	    			if(newslistArrayList1!=null) newslistArrayList.addAll(newslistArrayList1);
	    			if(newslistArrayList.size()>=20) 
	    			{
	    				newsInfo=newslistArrayList;
	    				lastNewsIndex=19;
	    				flushLock = true;
	    				flushType="下拉刷新";
	    				handler.sendEmptyMessage(9);
	    				return;
	    			}
	    			
	    			String newslistHtml2 = NewsRequest.getNewsListHtml("http://news.weather.com.cn/list_2.shtml");
	    			ArrayList<NewsAttribute> newslistArrayList2 = NewsRequest.parseListHtml(newslistHtml2);
	    			if(newslistArrayList2!=null) newslistArrayList.addAll(newslistArrayList2);
	    			if(newslistArrayList.size()>=20) 
    				if(newslistArrayList.size()>=20) 
	    			{
	    				newsInfo=newslistArrayList;
	    				lastNewsIndex=19;
	    				flushLock = true;
	    				flushType="下拉刷新";
	    				handler.sendEmptyMessage(9);
	    				return;
	    			}
	    			
	    			String newslistHtml3 = NewsRequest.getNewsListHtml("http://news.weather.com.cn/list_3.shtml");
	    			ArrayList<NewsAttribute> newslistArrayList3 = NewsRequest.parseListHtml(newslistHtml3);
	    			if(newslistArrayList3!=null) newslistArrayList.addAll(newslistArrayList3);
	    			if(newslistArrayList.size()>=20) 
    				if(newslistArrayList.size()>=20) 
	    			{
	    				newsInfo=newslistArrayList;
	    				lastNewsIndex=19;
	    				flushLock = true;
	    				flushType="下拉刷新";
	    				handler.sendEmptyMessage(9);
	    				return;
	    			}
	    			
	    			String newslistHtml4 = NewsRequest.getNewsListHtml("http://news.weather.com.cn/list_4.shtml");
	    			ArrayList<NewsAttribute> newslistArrayList4 = NewsRequest.parseListHtml(newslistHtml4);
	    			if(newslistArrayList4!=null) newslistArrayList.addAll(newslistArrayList4);
	    			if(newslistArrayList.size()>=20) 
    				if(newslistArrayList.size()>=20) 
	    			{
	    				newsInfo=newslistArrayList;
	    				lastNewsIndex=19;
	    				flushLock = true;
	    				flushType="下拉刷新";
	    				handler.sendEmptyMessage(9);
	    				return;
	    			}
	    			
	    			String newslistHtml5 = NewsRequest.getNewsListHtml("http://news.weather.com.cn/list_5.shtml");
	    			ArrayList<NewsAttribute> newslistArrayList5 = NewsRequest.parseListHtml(newslistHtml5);
	    			if(newslistArrayList5!=null) newslistArrayList.addAll(newslistArrayList5);
	    			if(newslistArrayList.size()>=20)
    				if(newslistArrayList.size()>=20) 
	    			{
	    				newsInfo=newslistArrayList;
	    				lastNewsIndex=19;
	    				flushLock = true;
	    				flushType="下拉刷新";
	    				handler.sendEmptyMessage(9);
	    				return;
	    			}
	    			
	    			//获取数据失败
	    			flushLock = true;
	    			handler.sendEmptyMessage(8);//获取数据失败
	    		}
	    	}).start();
    	}
    }
    //点击加载更多时调用该函数
    public void loadMoreNews()
    {
    	if(loadMoreLock)
    	{
    		loadMoreLock = false;
	    	new Thread(new Runnable(){
	    		public void run()
	    		{
	    			
	    			ArrayList<NewsAttribute> newslistArrayList = new ArrayList<NewsAttribute>();
	    			
	    			String newslistHtml1 = NewsRequest.getNewsListHtml("http://news.weather.com.cn/list.shtml");
	    			ArrayList<NewsAttribute> newslistArrayList1 = NewsRequest.parseListHtml(newslistHtml1);
	    			if(newslistArrayList1!=null) newslistArrayList.addAll(newslistArrayList1);
	    			if(newslistArrayList.size()>=20+lastNewsIndex+1) 
	    			{
	    				newsInfo=newslistArrayList;
	    				lastNewsIndex=lastNewsIndex+20;
	    				loadMoreLock = true;
	    				flushType="加载更多";
	    				handler.sendEmptyMessage(11);
	    				return;
	    			}
	    			
	    			String newslistHtml2 = NewsRequest.getNewsListHtml("http://news.weather.com.cn/list_2.shtml");
	    			ArrayList<NewsAttribute> newslistArrayList2 = NewsRequest.parseListHtml(newslistHtml2);
	    			if(newslistArrayList2!=null) newslistArrayList.addAll(newslistArrayList2);
	    			if(newslistArrayList.size()>=20) 
    				if(newslistArrayList.size()>=20+lastNewsIndex+1) 
	    			{
	    				newsInfo=newslistArrayList;
	    				lastNewsIndex=lastNewsIndex+20;
	    				loadMoreLock = true;
	    				flushType="加载更多";
	    				handler.sendEmptyMessage(11);
	    				return;
	    			}
	    			
	    			String newslistHtml3 = NewsRequest.getNewsListHtml("http://news.weather.com.cn/list_3.shtml");
	    			ArrayList<NewsAttribute> newslistArrayList3 = NewsRequest.parseListHtml(newslistHtml3);
	    			if(newslistArrayList3!=null) newslistArrayList.addAll(newslistArrayList3);
	    			if(newslistArrayList.size()>=20) 
    				if(newslistArrayList.size()>=20+lastNewsIndex+1) 
	    			{
	    				newsInfo=newslistArrayList;
	    				lastNewsIndex=lastNewsIndex+20;
	    				loadMoreLock = true;
	    				flushType="加载更多";
	    				handler.sendEmptyMessage(11);
	    				return;
	    			}
	    			
	    			String newslistHtml4 = NewsRequest.getNewsListHtml("http://news.weather.com.cn/list_4.shtml");
	    			ArrayList<NewsAttribute> newslistArrayList4 = NewsRequest.parseListHtml(newslistHtml4);
	    			if(newslistArrayList4!=null) newslistArrayList.addAll(newslistArrayList4);
	    			if(newslistArrayList.size()>=20) 
    				if(newslistArrayList.size()>=20+lastNewsIndex+1) 
	    			{
	    				newsInfo=newslistArrayList;
	    				lastNewsIndex=lastNewsIndex+20;
	    				loadMoreLock = true;
	    				flushType="加载更多";
	    				handler.sendEmptyMessage(11);
	    				return;
	    			}
	    			
	    			String newslistHtml5 = NewsRequest.getNewsListHtml("http://news.weather.com.cn/list_5.shtml");
	    			ArrayList<NewsAttribute> newslistArrayList5 = NewsRequest.parseListHtml(newslistHtml5);
	    			if(newslistArrayList5!=null) newslistArrayList.addAll(newslistArrayList5);
	    			if(newslistArrayList.size()>=20)
    				if(newslistArrayList.size()>=20+lastNewsIndex+1) 
	    			{
	    				newsInfo=newslistArrayList;
	    				lastNewsIndex=lastNewsIndex+20;
	    				loadMoreLock = true;
	    				flushType="加载更多";
	    				handler.sendEmptyMessage(11);
	    				return;
	    			}
	    			//加载更多失败
	    			loadMoreLock = true;
	    			handler.sendEmptyMessage(12);//获取数据失败
	    		}
	    	}).start();
    	}
    }
    //初始化设置选项卡
    public void initSet()
    {
    	LinearLayout more = (LinearLayout) MainActivity.this.findViewById(R.id.more);
    	final TextView version = (TextView) more.findViewById(R.id.version);
    	final TextView exit = (TextView) more.findViewById(R.id.exit);
    	version.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction())
				{
				case MotionEvent.ACTION_DOWN:
					version.setBackgroundColor(Color.parseColor("#c59898"));
					break;
				case MotionEvent.ACTION_UP:
					version.setBackgroundColor(Color.WHITE);
					//切换到版本信息页面
					currentUI = 1;
					Intent intent = new Intent(MainActivity.this,VersionActivity.class);
					startActivity(intent);
					break;
				case MotionEvent.ACTION_MOVE:
					version.setBackgroundColor(Color.WHITE);;
					break;
				}
				return true;
			}
    	});
    	exit.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction())
				{
				case MotionEvent.ACTION_DOWN:
					exit.setBackgroundColor(Color.parseColor("#c59898"));
					break;
				case MotionEvent.ACTION_UP:
					exit.setBackgroundColor(Color.WHITE);
					System.exit(0);
					break;
				}
				return true;
			}
    	});
    }
}



























