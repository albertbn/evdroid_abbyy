package com.example.camera;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

//http://www.acra.ch/ - crash reports

@ReportsCrashes( formUri = "https://app.adcore.com/errmail/" )
public class Camera extends Application {

        @Override
        public void onCreate() {
                // The following line triggers the initialization of ACRA
                super.onCreate();
                ACRA.init(this);
        }
}
