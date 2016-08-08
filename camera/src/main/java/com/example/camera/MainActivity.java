package com.example.camera;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

//https://code.google.com/p/copycutcurrentline/ - line ctr+c, line ctrl+v plugin
//answered Jan 31 '11 at 8:54,larsch,39027
//http://stackoverflow.com/questions/2321938/eclipse-copy-paste-entire-line-keyboard-shortcut - ctr
public class MainActivity extends ActionBarActivity {

    final String UPLOAD_URL = "https://app.adcore.com/errmail/";

    static final int REQUEST_CODE_MAX = 65534;
    ImageView imgFavorite;
    WebView wv;
    Uri mImageUri;
    MainActivity tthis = this;

    String accc = null;
    String get_acc(){

        if(accc==null){

            //			tthis.post_error( "get_acc hit" ); - tested - just once, yep!
            accc = acc.get_acc(tthis);
        }
        return accc;
    }

    //clears the old .temp dir
    void cleanse(){

        File f = Environment.getExternalStorageDirectory();
        f = new File(f.getAbsolutePath()+"/.temp/");

        if(!f.exists()) return;

        File[] list = f.listFiles();

        if( list.length<1 ) return;

        for (File temp : list) {
            temp.delete();
        }
    }

    //create temp file
    File get_file_temp(String part, String ext, int id ) throws Exception {
        //DONE - cleanse/delete the previous /.temo dir with the temp.txt script

        File dir = null, ffile;
        try{
            dir=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/kwee/");
            if(!dir.exists())
                dir.mkdir();

            ffile = new File(dir.getAbsolutePath()+"/"+part+id+ext);
            //ffile = File.createTempFile(part, ext, dir);
            //if(ffile.exists()) ffile.delete();

            return ffile;
        }
        catch(Exception ex){
            tthis.post_error( "Camera, MainActivity.java, get_file_temp: " + err_str(ex) );
        }

        //just an error case return, same as return null
        return dir;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override /*not implemented in evdroid*/
    public void onStart(){

        super.onStart();

        if(this.wv==null){

            this.wv = (WebView)findViewById(R.id.wv1);
            this.wv.setWebViewClient(new WebViewClient());

            this.wv.getSettings().setJavaScriptEnabled(true);

            wv.loadUrl("http://kwee.herokuapp.com/roller/");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // a dummy call to get initial acc
        tthis.get_acc();

        //2 july 2015 - change to 1==1 to test imediate err...
        //http://www.acra.ch/
        if(1==0) throw new NullPointerException("ffds");

        //============

        this.imgFavorite = (ImageView)findViewById(R.id.imageView1);
        this.imgFavorite.setBackgroundColor(Color.WHITE);

        //tthis.put_svg();
        this.imgFavorite.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    try{
                        open();
                        tthis.cleanse();
                    }
                    catch(Exception ex){

                        tthis.post_error( "Camera, MainActivity.java, imgFavorite.setOnClickListener: " + err_str(ex) );
                    }
                }
            });
    }

    //http://stackoverflow.com/questions/21306720/uploading-image-from-android-to-php-server - for the first icon upload
    //http://stackoverflow.com/questions/6448856/android-camera-intent-how-to-get-full-sized-photo
    //for the full size
    public void open ( ) {

        //this.post_error( "Camera, that's fine, starting activity in MainActivity.java, open()" );
        Intent intent = new Intent ( android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        int img_id = 0;
        File photo;
        try
            {
                Random randomGenerator = new Random();
                // place where to store camera taken picture
                img_id = randomGenerator.nextInt(REQUEST_CODE_MAX);
                photo = this.get_file_temp( "kwee", ".jpg", img_id );
            }
        catch ( Exception ex )
            {
                show_msg("Can't create file to take picture! Please check SD card! Image shot is impossible!");
                tthis.post_error( "Camera, MainActivity.java, Can't create file to take picture! Please check SD card!, open(): " + err_str(ex) );
                return;
            }

        try{
            mImageUri = Uri.fromFile(photo);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
            startActivityForResult(intent,  img_id );
        }
        catch ( Exception ex ) {
            tthis.post_error( "Camera, MainActivity.java, open() 2: "  + err_str(ex) );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        try{
            //if(requestCode==MenuShootImage && resultCode==RESULT_OK){
            if( resultCode==RESULT_OK){

                Bitmap bitmap=null;
                try
                    {
                        BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
                        //btmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888; /*check*/
                        //btmapOptions.inPreferredConfig = Bitmap.Config.ALPHA_8; /*check*/
                        btmapOptions.inPreferredConfig = Bitmap.Config.RGB_565; /*check*/
                        //btmapOptions.inSampleSize = 2;

                        File ff = tthis.get_file_temp("kwee", ".jpg", requestCode);
                        bitmap = BitmapFactory.decodeFile(ff.getAbsolutePath(), btmapOptions);

                        tthis.post_error(
                                         "Camera, MainActivity.java, onActivityResult in bytes count: " +
                                         bitmap.getByteCount()
                                         );

                        //                              don't change image
                        //                              if(bitmap!=null)
                        //                                      imgFavorite.setImageBitmap(bitmap);

                        show_msg("sending...");

                        //try to upload the image
                        //new Uploader().Upload( bitmap, this, tthis.get_acc() );

                        //mar 2015 - upload first to abbyy for better performance/faster
                        new Uploader().upload_via_ocr_4perf(bitmap, this, tthis.get_acc());

                        ff.delete();
                    }
                catch (Exception ex)
                    {
                        tthis.post_error( "Camera, MainActivity.java, onActivityResult in: " + err_str(ex) );
                    }
            }
            else{
                //nothing, take no action, no image came here...
            }

            super.onActivityResult(requestCode, resultCode, data);
        }
        catch(Exception ex){
            tthis.post_error( "Camera, MainActivity.java, onActivityResult out: " + err_str(ex) );
        }
    }

    void show_msg ( String msg ) {

        try {
            Context context = getApplicationContext();
            CharSequence text = msg;
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        catch(Exception ex){
            tthis.post_error( "Camera, MainActivity.java, show_msg: " + err_str(ex) );
        }
    }

    @Override
    protected void onSaveInstanceState ( Bundle state ) {
        super.onSaveInstanceState ( state );
        state.putAll(state);
    }

    @Override
    public boolean onCreateOptionsMenu ( Menu menu ) {

        try {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.main, menu);
        }
        catch(Exception ex){
            tthis.post_error( "Camera, MainActivity.java, imgFavorite.setOnClickListener: " + err_str(ex) );
        }
        return true;
    }

    //============
    //main fn to send errors
    //============
    void post_error ( final String text ) {

        try {
            //=================
            Thread t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try{
                            ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();
                            nameValuePairs.add(new BasicNameValuePair("text",text));

                            HttpClient httpclient = new DefaultHttpClient();
                            HttpPost httppost = new HttpPost(UPLOAD_URL);
                            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                            //HttpResponse response =
                            httpclient.execute(httppost);

                        } catch( Exception e ){

                            //check what to do with error
                            //show_msg(e.toString());
                        }
                    }
                });

            t.start();
            //=================
        }catch(Exception ex){
            //e.printStackTrace();
        }
    }

    /**
     * print stack
     */
    public String err_str ( Exception ex ) {

        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter( writer );
        ex.printStackTrace( printWriter );
        printWriter.flush();

        return writer.toString();
    }
}
