package com.appacitive.khelkund.activities;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.appacitive.khelkund.R;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class HowToPlayActivity extends ActionBarActivity {

    @InjectView(R.id.web_howtoplay)
    public WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);
        ButterKnife.inject(this);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setClickable(true);
        String path = "file:///android_asset/how_to_play.html";
        mWebView.loadUrl(path);
        mWebView.setBackgroundColor(Color.TRANSPARENT);
    }
}