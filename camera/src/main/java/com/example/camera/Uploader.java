package com.example.camera;

import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

//import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Base64;

public class Uploader {

        //final String UPLOAD_URL = "http://getfilesdev.adcore.com/upload_image.php";
        final String UPLOAD_URL = "http://kwee.herokuapp.com/eupload";
//	InputStream inputStream;
        final static int SLEEP_UPLOAD_IMG_MS = 3000;

        //credit: http://stackoverflow.com/questions/15759195/reduce-size-of-bitmap-to-some-specified-pixel-in-android
        Bitmap getResizedBitmap ( Bitmap image, int maxSize ) {
        int width = image.getWidth();
        int height = image.getHeight();

        if(width<maxSize && height<maxSize) return image;

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
        }

        /**
         * upload to abbyy first and then store the img for the ref model
         */
        public void upload_via_ocr_4perf ( final Bitmap _bitmap, final MainActivity act, final String account ) {

                final Uploader tthis = this;

                //=================
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {

              try {

                //Bitmap bitmap = getResizedBitmap(_bitmap, 1256);
                Bitmap bitmap = _bitmap;

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        //20 june 2015 - changed compression from 100 to 50 to save upload time and maybe improve quality
                        //note that the image is NOT resized, which is important - high DPI per char, that's what ABBYY say
                        //http://forum.ocrsdk.com/questions/644/recommended-compression-quality-for-jpegs
                //bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream); //compress to which format you want.
                bitmap.compress(Bitmap.CompressFormat.JPEG, 25, stream); //compress to which format you want.

                //set this for login purposes...
                if( ocr.act==null ){
                        //ocr.act = new MainActivity();
                        ocr.act = act;
                }

                HashMap<String,String> mp = new HashMap<String, String>();
                mp.put("account", account);
                mp.put("id_inv", acc.get_id_n_timestemp());

                        //DONE - add here timestamp - send it to ocr so it posts it with text to kwee
                //then there save the text file with the accoun_timestamp thing
                //then do the same for the img, so finally there they can be married together...
                byte[] bytes = stream.toByteArray();

                bitmap.recycle(); _bitmap.recycle(); /*4 july 2015*/

                //TEMP - unmark here to post to abbyy ocr and then to kwee roller...
                        ocr.post_img_to_ocr_n_result_2kwee(bytes, mp);

                        //post img itself with delay - not so important
                        Thread.sleep(SLEEP_UPLOAD_IMG_MS);
                        //DONE - post here the image as well for the ref model/manual correction...
                        tthis.Upload(bytes, act, mp);
              }
              catch(Exception ex){
                  act.post_error( "upload_via_ocr_4perf: " + act.err_str(ex) );
              }
            }
        });

        t.start();
        //=================
        }

        /**
         * uploads to kwee server the scanned image + account from smart-phone
         */
        //public void Upload( Bitmap bitmap, final MainActivity act, String account ){
        public void Upload( byte [] byte_arr, final MainActivity act, HashMap<String,String> mp ){

                //mar 2015 - remarked for new version first via abby ocr...
                /*bitmap = getResizedBitmap(bitmap, 1256);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream); //compress to which format you want.
        byte [] byte_arr = stream.toByteArray();*/

        String image_str = Base64.encodeToString(byte_arr, 0);
        //String image_str = new String(byte_arr);
        final ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();

        nameValuePairs.add( new BasicNameValuePair("image",image_str) );
        nameValuePairs.add( new BasicNameValuePair("account", mp.get("account") ));
        nameValuePairs.add( new BasicNameValuePair("id_inv", mp.get("id_inv") )); /*mar 2015 - send also the id_inv to marry on server*/

        //=================
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {

              try {

                 HttpClient httpclient = new DefaultHttpClient();
                 HttpPost httppost = new HttpPost(UPLOAD_URL);
                 httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                 httpclient.execute(httppost);
              }
              catch(Exception ex){
                  act.post_error( "uploader.upload: " + act.err_str(ex) );
              }
            }
        });

        t.start();
        //=================
        }
}
