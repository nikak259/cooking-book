package com.example.nikak.cookingbook;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by NIKAK on 13.02.2016.
 */
public class CookingBookApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}
