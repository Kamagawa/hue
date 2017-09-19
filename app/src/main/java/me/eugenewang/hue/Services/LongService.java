package me.eugenewang.hue.Services;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by yujwang on 9/18/2017.
 */

public class LongService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public LongService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();

    }
}