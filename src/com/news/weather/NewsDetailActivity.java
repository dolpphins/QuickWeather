package com.news.weather;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.main.weather.MainActivity;
import com.main.weather.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NewsDetailActivity extends Activity{
	private String tag = "NewsDetailActivity";
	private Handler handler;
	private Animation anim;//加载图标旋转
	private String url = null;
	private String content = null;
	private String title = null;
	private static Map<String,ContentAttribute> contentSaved = new HashMap<String,ContentAttribute>();
	
	private boolean notExit = true;//标记是否已经按返回键了
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.news_load);
        
        handler = new Handler(){
        	public void handleMessage(Message msg)
        	{
        		switch(msg.what)
        		{
        		case 1:
        			if(notExit)
        			{
        				displayContent();
        			}
        			break;
        		case 2:
        			if(notExit)
        			{
        				TextView load_icon = (TextView)findViewById(R.id.load_icon);
        				load_icon.clearAnimation();
        				Toast.makeText(NewsDetailActivity.this, "获取信息失败", Toast.LENGTH_SHORT).show();
        			}
        			break;
        		}
        	}
        };
        //获取信息
        getNewsDetailInfor();
        //旋转加载图标
        anim = AnimationUtils.loadAnimation(this, R.anim.load_anim);
        anim.setInterpolator(new LinearInterpolator());
        TextView load_icon = (TextView)findViewById(R.id.load_icon);
        load_icon.startAnimation(anim);//开始旋转
        
	}
	public void getNewsDetailInfor()
	{
		new Thread(new Runnable(){
			public void run()
			{
				Intent intent = getIntent();
		        url = intent.getStringExtra("url");
		        if(url==null) {handler.sendEmptyMessage(2);return;}
		        if(contentSaved.get(url)!=null) 
		        {
		        	content = contentSaved.get(url).content;
		        	title = contentSaved.get(url).title;
		        	handler.sendEmptyMessage(1);
		        	return;
		        }
		        String contentHtml = NewsRequest.getContentHtml(url);
		        
		        String contentTemp = NewsRequest.parseContentHtml(contentHtml);
		        if(contentTemp!=null) 
		        {
		        	title = NewsRequest.getTitle(contentTemp);
		        	content = NewsRequest.getContent(contentTemp);
		        	handler.sendEmptyMessage(1);
		        }
		        else
		        {
		        	contentTemp = NewsRequest.parsePictureHtml(contentHtml);
		        	if(contentTemp==null)
		        	{
		        		handler.sendEmptyMessage(2);
		        	}
		        	else
		        	{
		        		title = NewsRequest.getPictureTitle(contentTemp);
			        	content = NewsRequest.getPictureContent(contentTemp);
			        	handler.sendEmptyMessage(1);
		        	}
		        }
			}
		}).start();
	}
	public void displayContent()
	{
		if(content==null||title==null) handler.sendEmptyMessage(2);
		else
		{
			if(contentSaved.get(url)==null) contentSaved.put(url, new ContentAttribute(title,content));
			NewsDetailActivity.this.setContentView(R.layout.news_detail);
			LinearLayout show_news = (LinearLayout) findViewById(R.id.show_news);
			LinearLayout ll_news_detail_title = (LinearLayout) show_news.findViewById(R.id.ll_news_detail_title);
			WebView news_detail_title = (WebView) show_news.findViewById(R.id.news_detail_title);
			WebView news_detail_content = (WebView) show_news.findViewById(R.id.news_detail_content);
			news_detail_title.getSettings().setDefaultTextEncodingName("utf-8");
			news_detail_content.getSettings().setDefaultTextEncodingName("utf-8");
			news_detail_title.setBackgroundColor(Color.rgb(99, 168, 250));
			ll_news_detail_title.setBackgroundColor(Color.rgb(99, 168, 250));
			ll_news_detail_title.setPadding(10, 20, 10, 20);
			news_detail_title.loadDataWithBaseURL(null, title, "text/html", "utf-8", null);
			news_detail_content.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
		}
	
	}
	//捕获返回键
	public boolean onKeyDown(int keyCode,KeyEvent event)
    {
		notExit = false;
		return super.onKeyDown(keyCode, event);
    }
}
