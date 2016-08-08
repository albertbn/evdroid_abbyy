package com.example.camera;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Build;

import java.util.Random;

//get acc from phone
public class acc {

        //	get first acc from phone
        public static String get_acc( Context context ) {

//		get first one
                Account[] accounts = AccountManager.get(context).getAccounts();

                return ( accounts!=null && accounts.length>0 ) ? accounts[0].name : null ;
        }


        /**
         * get unique id per device
         */
        public static String get_id_device(){

                //credits: http://www.pocketmagic.net/android-unique-device-id/
                //http://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id/9186943#9186943 stansult, Jared Burrows
                //the above is one hell of a post!!! wow
                @SuppressWarnings("deprecation")
                String id = "35" + //we make this look like a valid IMEI
                        Build.BOARD.length()%10+ Build.BRAND.length()%10 +
                        Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 +
                        Build.DISPLAY.length()%10 + Build.HOST.length()%10 +
                        Build.ID.length()%10 + Build.MANUFACTURER.length()%10 +
                        Build.MODEL.length()%10 + Build.PRODUCT.length()%10 +
                        Build.TAGS.length()%10 + Build.TYPE.length()%10 +
                        Build.USER.length()%10 ; //13 digits

                return id;
        }

        /**
         * get device id + timestemp + random
         */
        public static String get_id_n_timestemp(){

                Random randomGenerator = new Random();

                return get_id_device() + '_' + System.currentTimeMillis() + '_' + randomGenerator.nextInt(65535);
        }
}
