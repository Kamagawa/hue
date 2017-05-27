package me.eugenewang.hue.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

import me.eugenewang.hue.R;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class SearchView extends AppCompatActivity {
    TextView mTextView;
    WebView mWebView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_view);

        mWebView = (WebView) findViewById(R.id.webview);


        Intent intent = getIntent();
        String url = intent.getStringExtra("URL");

        mWebView.loadUrl("http://www.colorhexa.com/"+url);



    }
}
