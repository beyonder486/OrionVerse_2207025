package com.orionverse.devplatform;

import android.app.Application;

import com.orionverse.devplatform.utils.CloudinaryUtil;

public class OrionVerseApp extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Cloudinary
        CloudinaryUtil.init(this);
    }
}
