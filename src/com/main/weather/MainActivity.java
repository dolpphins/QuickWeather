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
	//ѡ�����������
	private FragmentTabHost tabHost;
	private LayoutInflater layoutInflater;
	private Class fragmentArray[]={HomePage.class,TendencyPage.class,NewsPage.class,MorePage.class};
	private int imageViewArray[]={R.drawable.home_btn,R.drawable.tendency_btn,R.drawable.news_btn,R.drawable.more_btn};
	private String textViewArray[]={"����","����","��Ѷ","����"};
	//���ݿ�ʵ��
	private MySQLite sqlite;
	//���浱ǰ�ĳ�����Ϣ����
	private String currentCityID="101010100";
	private String province="����";
	private String currentCityName="����";//Ĭ�ϳ���Ϊ����
	private static WeatherAttribute currentWeatherAttibute=null;//������������
	private static FiveDayWeatherAttribute currentFiveDayWeatherAttibute=null;//δ��������������
	//�����̷߳�������Ϣ������Ӧ
	public static Handler handler;
	//�����л�Activity
	private Intent intent;
	private static int currentUI = 0;//0��ʾ��ǰΪ�����棬1��ʾ��������
	
	private static String isFirstRun="YES";//һ��Ҫ����Ϊ��̬����
	//����Ի����ʶ
	private static ProgressDialog LoadWeatherPd=null;
	private static ProgressDialog GetFiveDayPd = null;
	private ProgressDialog pd=null;//������ʾ���ؽ���
	
	private boolean loadFivDay;//��ǵ���drawChart�Ƿ���Ϊͨ�������ȡ����
	
	private static boolean isTip=true;//����Ƿ���ʾ���粻����
	
	private long reallyexit=0;//�����ж��Ƿ���2�����������·��ؼ��˳�
	
	private int screenWidth;//������Ļ�Ŀ��
	private int screenHeight;//������Ļ�ĸ߶�
	
	private static String currentTab="����";//���浱ǰ��Tabѡ��
	
	private static ArrayList<NewsAttribute> newsInfo=new ArrayList<NewsAttribute>();//��������б���Ϣ
	private static ArrayList<ContentAttribute> contentInfor=new ArrayList<ContentAttribute>();//����ÿ�����ŵ�������Ϣ
	
	private boolean canFlush=false;//��־��Ѷ�����Ƿ���ˢ��
	private float downY=-100;//��¼��Ѷ���水�µ�y����
	private int flushMargin=-160;//ˢ��TextView��ǰ��marginֵ
	
	private int lastNewsIndex=9;//listview�����һ�����ŵ�����
	
	private int CurrentListViewSelect=0;//��¼��ǰlistview����Ļ����ʾ�ĵ�һ��������
	
	private String flushType;//���updateView�ĵ�������Ϊˢ�»��߼��ظ��������
	
	private boolean isUpdate = false;//������Ƿ���Ϊˢ�µ���initView
	
	private boolean flushLock = true;//�������ˢ�����ֻ����һ�����߳�ִ��
	private boolean loadMoreLock = true;//����¼��ظ������ֻ����һ�����߳�ִ��
	
	private Animation anim;//����ͼ����ת
	private boolean isRotate = false;//��Ǹ���ͼ���Ƿ�����ת
	private Animation flush_downtoup_anim;//����ˢ�¼�ͷ��ת����
	private Animation flush_uptodown_anim;//����ˢ�¼�ͷ��ת����
	private int flushCount = 0;
	private Animation flush_anim;//����ˢ�¶���
	
	private static boolean[] isClicked = new boolean[2000];//��¼���������Ѷ
	
	private static NetworkLocation location;//��ȡ����λ����Ϣʵ��
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ȥ��ϵͳ�Դ�������
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //���ò���
        setContentView(R.layout.activity_main);
       
        //��ʼ��������  
        initView();

        //��ȡ��Ļ��С��Ϣ
        getScreenInfo();
        
        //��ʼ������ͼ�궯��
        anim = AnimationUtils.loadAnimation(this, R.anim.update_anim);
        anim.setInterpolator(new LinearInterpolator());
        //��ʼ������ˢ�¼�ͷ��ת��ˢ�¶���
        flush_downtoup_anim = AnimationUtils.loadAnimation(this, R.anim.flush_downtoup_anim);
        flush_downtoup_anim.setInterpolator(new AccelerateDecelerateInterpolator());
        flush_downtoup_anim.setFillAfter(true);
        flush_uptodown_anim = AnimationUtils.loadAnimation(this, R.anim.flush_uptodown_anim);
        flush_uptodown_anim.setInterpolator(new AccelerateDecelerateInterpolator());
        flush_uptodown_anim.setFillAfter(true);
        flush_anim = AnimationUtils.loadAnimation(this, R.anim.flush_anim);
        flush_anim.setInterpolator(new LinearInterpolator());
        flush_anim.setFillAfter(true);
        //��ȡ�ϴ������õĳ��в���ʼ��������Ϣ
        if("YES".equals(isFirstRun))
        {
        	isFirstRun="NO";
        	LoadWeatherPd = ProgressDialog.show(MainActivity.this, "��ʾ", "���ڼ�������");
        	String cityId=readLasttimeCity();
        	currentCityID=cityId;
        	initWeather(currentCityID);
        	//������ȡλ����Ϣ��ʵ������ʼ����λ�ñ仯
        	
        }//����ĳ�ʼ��������������Ӧ�û�����״̬
        //��Ӧ���̷߳�������Ϣ
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
        		case 1:Toast.makeText(MainActivity.this, "���粻����", Toast.LENGTH_SHORT).show();break;
        		case 2:Toast.makeText(MainActivity.this, "������...",Toast.LENGTH_LONG).show();break;
        		case 3:
        			if(GetFiveDayPd!=null) {GetFiveDayPd.dismiss();GetFiveDayPd=null;}
        			Toast.makeText(MainActivity.this, "��ȡ����ʧ��", Toast.LENGTH_SHORT).show();
        			break;
        		case 4:
        			if(GetFiveDayPd!=null) {GetFiveDayPd.dismiss();GetFiveDayPd=null;}
        			drawChart();//���³��������һ���δ��������������ͼ
        			break;
        		case 5:
        			updateNewsList();
        			flushAndLoad();//�����¼�
        			break;//Ϊlistview���Ӽ����¼�
        		case 6:break;
        		case 7:if(pd!=null) {pd.dismiss();pd=null;}
        				break;
        		
        		case 8: if("��Ѷ".equals(currentTab))
        				{
        					LinearLayout newslist1=(LinearLayout)findViewById(R.id.news);
        					RelativeLayout flush_header1 = (RelativeLayout) findViewById(R.id.flush_header);
        					TextView flush_text1 = (TextView) newslist1.findViewById(R.id.flush_text);
        					TextView flush_picture1 = (TextView) newslist1.findViewById(R.id.flush_picture);
        					flush_picture1.setBackgroundResource(R.drawable.pushdown);
        					flush_text1.setText("ˢ��ʧ��");
        					Toast.makeText(MainActivity.this, "ˢ��ʧ��", Toast.LENGTH_SHORT).show();
        					setMargins(flush_header1, 0, -300, 0, 0);
        				}
        					flushMargin=-160;
        					break;
						
        		case 9:if("��Ѷ".equals(currentTab))
        				{
        					LinearLayout newslist2=(LinearLayout)findViewById(R.id.news);
        					RelativeLayout flush_header2 = (RelativeLayout) findViewById(R.id.flush_header);
        					TextView flush_text2 = (TextView) newslist2.findViewById(R.id.flush_text);
        					TextView flush_picture2 = (TextView) newslist2.findViewById(R.id.flush_picture);
        					flush_picture2.setBackgroundResource(R.drawable.pushdown);
        					flush_text2.setText("ˢ�³ɹ�");
        					Toast.makeText(MainActivity.this, "ˢ�³ɹ�", Toast.LENGTH_SHORT).show();
        					updateNewsList();//ˢ�³ɹ�����½���
        					setMargins(flush_header2, 0, -300, 0, 0);
        				}
        					flushMargin=-160;
        					break;
						
        		case 10:
        			//���ظ���û��������
        			if("��Ѷ".equals(currentTab))
        			{
        				LinearLayout newslist3=(LinearLayout)findViewById(R.id.news);
        				TextView load_more = (TextView) newslist3.findViewById(R.id.load_more);
        				load_more.setText("���ظ���");
        				setMargins(newslist3, 0, 0, 0, 0);
        			}
        				Toast.makeText(MainActivity.this, "û��������", Toast.LENGTH_SHORT).show();
        				break;
        		case 11:
        			//���ظ���ɹ�
        			if("��Ѷ".equals(currentTab))
        			{
	    				LinearLayout news = (LinearLayout) MainActivity.this.findViewById(R.id.news);
	    		    	ListView lv_newslist = (ListView) news.findViewById(R.id.lv_newslist);
	    		    	flushType="���ظ���";
	    		    	updateNewsList();//���¼���listview
	    		    	setMargins(lv_newslist,0,0,0,0);
	    		    	Toast.makeText(MainActivity.this, "���سɹ�", Toast.LENGTH_SHORT).show();
        			}
        			break;
        		case 12:
        			//���ظ��෢���쳣
        			if("��Ѷ".equals(currentTab))
        			{
        				LinearLayout newslist=(LinearLayout)findViewById(R.id.news);
        				TextView load_more = (TextView)newslist.findViewById(R.id.load_more);
        				load_more.setText("���ظ���");
        				Toast.makeText(MainActivity.this, "����ʧ��,������", Toast.LENGTH_SHORT).show();
        			}
        			break;
        		case 13:
        			//��ѡ����Ѷѡ����һ�ȡ����ʧ��ʱ
        				if(pd!=null) {pd.dismiss();pd=null;}
        				Toast.makeText(MainActivity.this, "��ȡ����ʧ��", Toast.LENGTH_SHORT).show();
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
    				Toast.makeText(MainActivity.this, "��ȡ���ݳɹ�", Toast.LENGTH_SHORT).show();
    				updateNewsList();
    				flushAndLoad();//��������ˢ�ºͼ��ظ���
    				break;
        		case 17:
        			setInfor(currentWeatherAttibute);
        			break;
        		case 18:
        			if(LoadWeatherPd!=null) {LoadWeatherPd.dismiss();LoadWeatherPd=null;}
        			break;
        		case 19:
        			Toast.makeText(MainActivity.this, "���³ɹ�", Toast.LENGTH_SHORT).show();
        		case 20:
        			updateNewsList();
        			flushAndLoad();//��������ˢ�ºͼ��ظ���
        			break;
        			//��ʼ��ֹͣ����ͼ����ת
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
        			Toast.makeText(MainActivity.this, "���ڸ�����Ϣ�����Ժ�����", Toast.LENGTH_SHORT).show();
        			break;
        		case 25:
        			if(currentUI==0&&pd==null&&LoadWeatherPd==null&&GetFiveDayPd==null)
        			{
        			currentWeatherAttibute = location.getCurrentWeatherAttibute();
        			currentCityID = currentWeatherAttibute.cityId; 
        			province = currentWeatherAttibute.province;
        			currentCityName = currentWeatherAttibute.cityName;
        			currentFiveDayWeatherAttibute = location.getCurrentFiveDayWeatherAttibute();
        			if("����".equals(currentTab)) setInfor(currentWeatherAttibute);
        			if("����".equals(currentTab)) drawChart();
        			Toast.makeText(MainActivity.this, "��ǰλ��Ϊ:"+currentWeatherAttibute.cityName, Toast.LENGTH_SHORT).show();
        			}
        			break;
        		}
        		super.handleMessage(msg);
        	}
        };
    }
    //Ϊ�������а�ť�󶨵���¼�
    public void changeCity(View v)
    {
    	//��û���ڸ���ʱ�л�
    	if(!isRotate)
    	{
    		currentUI = 1;
    		Intent intent=new Intent();
    		intent.setClass(MainActivity.this, ChangeCityActivity.class);
    		MainActivity.this.startActivity(intent);
    		MainActivity.this.finish();
    	}
    	//������ʾ�ȸ�������
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
    	//���������ԭ��Tabѡ��Ϊ����ʱ
    	if("����".equals(currentTab))
    	{
	    	intent=getIntent();
	    	String str1=intent.getStringExtra("fromSelect");
	    	String str2=intent.getStringExtra("fromBack");
	    	
	    	//��onResume��������Ϊ��������ť���ص����������ʱ
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
							//��Ȼ�����������������쳣
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
	    				handler.sendEmptyMessage(17);//֪ͨ���߳̿��Ը��½���
	    			}
	    		}).start();
	    	}
	    	//�������ؼ�����ʱ
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
								//��Ȼ�����������������쳣
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
    //��ʼ������ѡ�������
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
    	//��ѡ�ѡ������¼�����
    	tabHost.setOnTabChangedListener(new OnTabChangeListener(){
			@Override
			public void onTabChanged(String str) {
				if("����".equals(str))
				{
					currentTab="����";
					
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
										// TODO �Զ����ɵ� catch ��
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
						LoadWeatherPd = ProgressDialog.show(MainActivity.this, "��ʾ", "���ڼ�������");
						initWeather(currentCityID);
					}
				}
				else if("����".equals(str))
				{
					currentTab="����";
					loadFivDay=false;
					if(currentFiveDayWeatherAttibute!=null)
					{
						handler.sendEmptyMessage(4);
					}
					else
					{
						loadFivDay=true;
						GetFiveDayPd = ProgressDialog.show(MainActivity.this,"��ʾ", "���ڼ�������");
						getFiveDay();//��ȡδ��������Ϣ,��ɺ�֪ͨ���̸߳���UI
					}
				}
				else if("��Ѷ".equals(str))
				{
					currentTab="��Ѷ";
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
										// TODO �Զ����ɵ� catch ��
										e.printStackTrace();
									}
								}
								handler.sendEmptyMessage(20);
							}
						}).start();
					}
					else
					{
						pd=ProgressDialog.show(MainActivity.this, "��ʾ", "���ڼ�������");//��ʾ�û����ڼ�������
						getNewsInfor();
					}
				}
				else if("����".equals(str))
				{
					currentTab="����";
					handler.sendEmptyMessage(14);//Ϊ���ð󶨵���¼�
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
    //ͨ�������ȡ������Ϣ
    public void initWeather(final String cityId)
    {
    	//�����߳̽�������IO����
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
	        			handler.sendEmptyMessage(18);//ȡ���Ի���
	        			if(isTip)
	        			{
	        				isTip=false;
	        				handler.sendEmptyMessage(1);//��ʾ���粻����
	        			}
	        			else
	        			{
	        				handler.sendEmptyMessage(3);//��ʾ��ȡ����ʧ��
	        			}
	        		}
	        		else
	        		{
	        			if(currentFiveDayWeatherAttibuteTemp!=null) currentFiveDayWeatherAttibute = currentFiveDayWeatherAttibuteTemp;
	        			isTip=false;
		        		currentWeatherAttibute=weatherAttributeTemp;
		        		handler.sendEmptyMessage(18);//ȡ���Ի���
		        		try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO �Զ����ɵ� catch ��
							e.printStackTrace();
						}
		        		handler.sendEmptyMessage(0);//����UI
		        		if(isUpdate==true)
		        		{
		        			handler.sendEmptyMessage(19);
		        			isUpdate = false;
		        		}
	        		}
	        	}
	    		else handler.sendEmptyMessage(18);//ȡ���Ի���
	    		if(isRotate)
	    		{
	    			isRotate = false;
	    			handler.sendEmptyMessage(21);//ֹͣ����ͼ����ת
	    		}
	    		
    		}
    	}).start();
    }
    //�õ�δ������������Ϣ
    public void getFiveDay()
    {
    	new Thread(new Runnable(){
			@Override
			public void run() {
				currentFiveDayWeatherAttibute=ForecastFiveDayRequest.getFiveDayWeatherInfor(currentCityID, MainActivity.this);
				if(currentFiveDayWeatherAttibute==null) handler.sendEmptyMessage(3);
				//���³���������������ͼ
				else handler.sendEmptyMessage(4);
			}
    	}).start();
    }

    //�������ͼ��ʱ���øú���
    public void updateInfor(View v)
    {
    	if(!isRotate)
    	{
	    	isTip=true;
	    	isUpdate = true;
	    	//��ʼ��ת����ͼ��
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
						// TODO �Զ����ɵ� catch ��
						e.printStackTrace();
					}
    			}
    			handler.sendEmptyMessage(22);
    		}
    	}).start();
    }
    //����������Ϣ
    public void setInfor(WeatherAttribute tempWeatherAttribute)
    {
    	try
		{
    		if(tempWeatherAttribute!=null)
    		{
    		
	    		//ͨ��findViewById��ȡ��ǰidΪR.id.home�Ĳ���,��inflate��ȡ�����µĲ��ֲ��ǵ�ǰ��
	
	    		LinearLayout subLayout=(LinearLayout) MainActivity.this.findViewById(R.id.home);
	    		
	    		TextView cityName = (TextView)subLayout.findViewById(R.id.city);
	    		cityName.setText(tempWeatherAttribute.cityName);//���ó�����
	    		currentCityName=tempWeatherAttribute.cityName;
	    		province=tempWeatherAttribute.province;
	    		currentCityID=tempWeatherAttribute.cityId;
	    		currentWeatherAttibute=tempWeatherAttribute;
	    		
	    		TextView realtimeTemp=(TextView) subLayout.findViewById(R.id.realtimetemp);
	    		realtimeTemp.setText(tempWeatherAttribute.realtimeTemperature);//����ʵʱ�¶�
	    		ImageView statusImg=(ImageView)subLayout.findViewById(R.id.statusimage);
	    		setStatusImage(statusImg,tempWeatherAttribute.status);//��������ͼ��
	    		TextView status=(TextView)subLayout.findViewById(R.id.statustext);
	    		status.setText(tempWeatherAttribute.status);//����״��
	    		TextView date=(TextView)subLayout.findViewById(R.id.date);
	    		date.setText(tempWeatherAttribute.date);//���õ�������
	    		TextView temperatureRange=(TextView)subLayout.findViewById(R.id.mintomaxtemp);
	    		temperatureRange.setText(tempWeatherAttribute.temperatureRange);//�����¶ȷ�Χ
	    		TextView windyPower=(TextView)subLayout.findViewById(R.id.windypower);
	    		windyPower.setText(tempWeatherAttribute.windPower);//���÷���
	    		TextView windyDirection=(TextView)subLayout.findViewById(R.id.windydirection);
	    		windyDirection.setText(tempWeatherAttribute.windDirection);//���÷���
	    		TextView humidity=(TextView)subLayout.findViewById(R.id.humidity);
	    		humidity.setText(tempWeatherAttribute.humidity);//����ʪ��
	    		TextView airquality=(TextView)subLayout.findViewById(R.id.airqualitynumber);
	    		airquality.setText(tempWeatherAttribute.airQuality);//���ÿ���������ֵ
	    		TextView airqualitygrade=(TextView)subLayout.findViewById(R.id.airqulitygrade);
	    		airqualitygrade.setText(setAirQuality(tempWeatherAttribute.airQuality));//���ÿ��������ȼ�
	    		TextView pm=(TextView)subLayout.findViewById(R.id.pm);
	    		pm.setText(tempWeatherAttribute.PM);//����PM2.5
	    		TextView update=(TextView)subLayout.findViewById(R.id.publishtime);
	    		update.setText(tempWeatherAttribute.updateTime);//���ø���ʱ��
    		
    		}
	    	else
	    	{
	    		Log.i(tag,"1223");
	    		LinearLayout subLayout=(LinearLayout) MainActivity.this.findViewById(R.id.home);
	    		TextView cityName = (TextView)subLayout.findViewById(R.id.city);
	    		cityName.setText("");//���ó�����
	    	}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }
    //���ļ��ж�ȡ�ϴα���ĳ�����Ϣ��û�еĻ�Ĭ��Ϊ����
    public String readLasttimeCity()
    {
    	try {
    		File file=new File("/data/data/com.main.weather/temp.txt");
    		if(!file.exists()) 
    		{
    			FileOutputStream os = new FileOutputStream("/data/data/com.main.weather/temp.txt");
    			String str="���� ���� 101010100";
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
    //���浱ǰ������Ϣ���ļ���
    public void saveFile()
    {
    	try {
    		//ע��·����
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
    //����������ת��Ϊ�ȼ�
    public String setAirQuality(String number)
    {
    	if("������".equals(number)) return "";
    	int num=Integer.parseInt(number);
    	if(num>=0&&num<=50) return "��";
    	else if(num>=51&&num<=100) return "��";
    	else if(num>=101&&num<=150) return "�����Ⱦ";
    	else if(num>=151&&num<=200) return "�ж���Ⱦ";
    	else if(num>=201&&num<=300) return "�ض���Ⱦ";
    	else if(num>300) return "������Ⱦ";
    	else return null;
    }
    public void setStatusImage(ImageView tv,String status)
    {
    	if("��".equals(status)) tv.setImageResource(R.drawable.sunny);
    	else if("����".equals(status)) tv.setImageResource(R.drawable.shaoyun);
    	else if("����".equals(status)) tv.setImageResource(R.drawable.shaoyun);
    	else if("��".equals(status)) tv.setImageResource(R.drawable.yin);
    	else if("����".equals(status)) tv.setImageResource(R.drawable.zhenyu);
    	else if("������".equals(status)) tv.setImageResource(R.drawable.leizhenyu);
    	else if("С��".equals(status)) tv.setImageResource(R.drawable.xiaoyu);
        else if("����".equals(status)) tv.setImageResource(R.drawable.zhongyu);
   		else if("����".equals(status)) tv.setImageResource(R.drawable.dayu);
    	else if("�󵽱���".equals(status)) tv.setImageResource(R.drawable.dadaobaoyu);
    	else if("����".equals(status)) tv.setImageResource(R.drawable.baoyu);
    	else if("Сѩ".equals(status)) tv.setImageResource(R.drawable.xiaoxue);
    	else if("��ѩ".equals(status)) tv.setImageResource(R.drawable.zhongxue);
   		else if("��ѩ".equals(status)) tv.setImageResource(R.drawable.daxue);
    	else if("��ѩ".equals(status)) tv.setImageResource(R.drawable.baoxue);
    	else if("ɳ����".equals(status)) tv.setImageResource(R.drawable.shachenbao);
    	else if("�����".equals(status)) tv.setImageResource(R.drawable.longjuanfeng);
    	else if("��".equals(status)) tv.setImageResource(R.drawable.wu);
        else if("��".equals(status)) tv.setImageResource(R.drawable.mai);
        else if("���ѩ".equals(status)) tv.setImageResource(R.drawable.yujiaxue);
    	else tv.setImageResource(R.drawable.undefined);
    }
    //���񷵻ؼ�
    public boolean onKeyDown(int keyCode,KeyEvent event)
    {
    	if(keyCode==KeyEvent.KEYCODE_BACK)
    	{
    		//ʵ�����������ؼ������˳�����
    		if(System.currentTimeMillis()-reallyexit>2000)
    		{
    			reallyexit=System.currentTimeMillis();
    			Toast.makeText(this, "�ٰ�һ���˳�����", 0).show();
    		}
    		else
    		{
    			isFirstRun="YES";
    			isTip=true;
    			//saveNewsToFile();
    			saveFile();//���浱ǰ������Ϣ���ļ���
    			return super.onKeyDown(keyCode, event);
    		}
    	}
    	return true;
    }
    //��ȡ��Ļ��С��Ϣ
    public void getScreenInfo()
    {
    	DisplayMetrics metric = new DisplayMetrics();
    	this.getWindowManager().getDefaultDisplay().getMetrics(metric);
    	screenWidth=metric.widthPixels;
    	screenHeight=metric.heightPixels;
    	ChartView.setScreenWidth(screenWidth);
    	ChartView.setScreenHeight(screenHeight);
    }
    //��������ͼ
    public void drawChart()
    {
    	try
    	{
	    	if(currentFiveDayWeatherAttibute==null) 
	    	{
	    		LinearLayout tendency=(LinearLayout)MainActivity.this.findViewById(R.id.tendency);
				TextView feature=(TextView)tendency.findViewById(R.id.feature);
				feature.setText("������");
	    		//���ò�����ʾ����ͼ
	    		ChartView.setCanDraw(false);
	    		Toast.makeText(this, "û�����������Ϣ", 0).show();
	    	}
	    	else
	    	{
	    		LinearLayout tendency=(LinearLayout)MainActivity.this.findViewById(R.id.tendency);
				TextView feature=(TextView)tendency.findViewById(R.id.feature);
				ChartView chart = (ChartView)tendency.findViewById(R.id.chart);
				
				feature.setText(currentCityName+"δ��5������");
	    		//��������ʾ����ͼ
	    		ChartView.setCanDraw(true);
	    		//��������
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
	    		//��������
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
	    		//���ð�������״��
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
	    		//���¶�����ͼ
	    		ChartView.setTemp(currentFiveDayWeatherAttibute.maxTempArray, currentFiveDayWeatherAttibute.minTempArray);
	    		if(loadFivDay) chart.postInvalidate();
	    		//����ҹ������״��
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
    //���������б�
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
		if("��Ѷ".equals(currentTab))
		{
			LinearLayout newslist=(LinearLayout)MainActivity.this.findViewById(R.id.news);
			ListView lv_newslist=(ListView)newslist.findViewById(R.id.lv_newslist);
			lv_newslist.setAdapter(new MyAdapter());
			//����Ǽ��ظ���ˢ�½�����ô�ͻָ���ԭ��λ��
			if(flushType!=null&&"���ظ���".equals(flushType))  lv_newslist.setSelection(CurrentListViewSelect);
			
			//Ϊ���е�����item�󶨵���¼�
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
    //�״μ�����Ѷ����ʱ���øú���
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
    			
    			//��ȡ����ʧ��
    			handler.sendEmptyMessage(13);//��ȡ����ʧ��
    		}
    	}).start();
    }
    //����ˢ�µ��øú���
    public void flushAndLoad()
    {
    	if("��Ѷ".equals(currentTab))
    	{
	        LinearLayout newslist=(LinearLayout)findViewById(R.id.news);
	        final RelativeLayout flush_header = (RelativeLayout) newslist.findViewById(R.id.flush_header);
			final TextView flush_text = (TextView) newslist.findViewById(R.id.flush_text);
	        final ListView lv_newslist=(ListView)newslist.findViewById(R.id.lv_newslist);
	        final TextView load_more = (TextView)newslist.findViewById(R.id.load_more);
	        lv_newslist.setOnScrollListener(new OnScrollListener() {
				@Override
				public void onScrollStateChanged(AbsListView v, int scrollState) {
					//����������ײ�ʱ
					if(v.getLastVisiblePosition()==(v.getCount()-1))
					{
						//load_more.setText("���ظ���");
						setMargins(lv_newslist, 0, 0, 0, 130);
						load_more.setText("���ڼ���...");
				    	loadMoreNews();
						//Ϊ���ظ���󶨵���¼�
				    	load_more.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								load_more.setText("���ڼ���...");
						    	loadMoreNews();
							}
						});
					}
				}
				@Override
				public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
					
				}
			});
	        //����ˢ��
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
						//ÿ����Ļ������¼���һ����¼������
						CurrentListViewSelect=lv_newslist.getLastVisiblePosition();
						//ÿ����Ļ�������Ҳ�����ײ�ʱ���ؼ��ظ���TextView
						if(lv_newslist.getLastVisiblePosition()<lv_newslist.getCount()-1) setMargins(lv_newslist,0,0,0,0);
						//�ж��Ƿ����ˢ�µ�����
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
								flush_text.setText("����ˢ��");
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
								flush_text.setText("�ͷ�����ˢ��");
							}
							else if(flushMargin>=80&&flushMargin<260)
							{
								flushMargin=flushMargin+distance/5;
								setMargins(flush_header, 0, flushMargin, 0, 0);
								flush_text.setText("�ͷ�����ˢ��");
							}
							else if(flushMargin>=260)
							{
								flush_text.setText("�ͷ�����ˢ��");
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
							flush_text.setText("����ˢ��...");
							//��ȡ������ִ����ɺ�ͨ���߳�
							flushGetNewsInfor();
						}
						else
						{
							setMargins(flush_header, 0, -300, 0, 0);
							flushMargin=-160;
							flush_picture.setBackgroundResource(R.drawable.pushdown);
							flush_text.setText("����ˢ��");
						}
						break;
					}
					//����Ĭ�ϵ�onTouchEvent��������Ȼlistview���޷�����
					return lv_newslist.onTouchEvent(event);
				}
			});
    	}
    }
    //��װ����margin����
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
	    				flushType="����ˢ��";
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
	    				flushType="����ˢ��";
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
	    				flushType="����ˢ��";
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
	    				flushType="����ˢ��";
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
	    				flushType="����ˢ��";
	    				handler.sendEmptyMessage(9);
	    				return;
	    			}
	    			
	    			//��ȡ����ʧ��
	    			flushLock = true;
	    			handler.sendEmptyMessage(8);//��ȡ����ʧ��
	    		}
	    	}).start();
    	}
    }
    //������ظ���ʱ���øú���
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
	    				flushType="���ظ���";
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
	    				flushType="���ظ���";
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
	    				flushType="���ظ���";
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
	    				flushType="���ظ���";
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
	    				flushType="���ظ���";
	    				handler.sendEmptyMessage(11);
	    				return;
	    			}
	    			//���ظ���ʧ��
	    			loadMoreLock = true;
	    			handler.sendEmptyMessage(12);//��ȡ����ʧ��
	    		}
	    	}).start();
    	}
    }
    //��ʼ������ѡ�
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
					//�л����汾��Ϣҳ��
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



























