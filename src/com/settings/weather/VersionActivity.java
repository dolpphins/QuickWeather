package com.settings.weather;

import com.main.weather.MainActivity;
import com.main.weather.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

public class VersionActivity extends Activity{
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.version_infor);
        
        //Ϊ���ؼ�ͷ��ӵ���¼�
        ImageView version_back = (ImageView) findViewById(R.id.version_back);
        version_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//�ֶ����÷���
				onBackPressed();
			}
		});
	}
}
