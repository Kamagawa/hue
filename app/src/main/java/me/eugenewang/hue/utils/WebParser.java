package me.eugenewang.hue.utils;

import android.os.AsyncTask;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by yujwang on 9/18/2017.
 */

public class WebParser extends AsyncTask<String, Void, String> {
    String url;



    private void inAction(){




    }

    @Override
    protected String doInBackground(String... params) {
        return params[1];
    }


}
