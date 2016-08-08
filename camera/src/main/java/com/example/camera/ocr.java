package com.example.camera;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.abbyy.ocrsdk.*;

/**
 * @author abentov
 */
public class ocr {

        /**
         * where to upload to...
         */
        final static String UPLOAD_URL = "http://kwee.herokuapp.com/eupload";
        final static int SLEEP_OCR_TASK_CHECK_MS = 1000;

        static MainActivity act=null;

        static Client get_rest_client(){

                Client rest_client = new Client();
                rest_client.serverUrl = "http://cloud.ocrsdk.com";
                rest_client.applicationId = "test54321";
                rest_client.password = "vob3MppjOfIQrs5aBpmvfbGP";

                return rest_client;
        }

        /**
         * posts img direct to abbyy to save time and resulting text to kwee
         * then the img will also be posted in addition to kwee, you know, for saving, relating to entry, correction, all that rock...
         */
        public static String post_img_to_ocr_n_result_2kwee(byte[] bytes, HashMap<String,String> mp) throws Exception{

                try {

                        ProcessingSettings settings = new ProcessingSettings();
                        settings.setLanguage("English");
                        //settings.setOutputFormat( ProcessingSettings.OutputFormat.txtUnstructured );
                        settings.setOutputFormat( ProcessingSettings.OutputFormat.txt );

                        //System.out.println("Uploading file..");

                        Client rest_client = get_rest_client();

                        //send image bytes to abbyy -screw them
                        Task task = rest_client.process_image_bytes(bytes, settings);

                        waitAndDownloadResult(task, rest_client, mp);

                } catch (Exception ex) {

                        act.post_error( "post_img_to_ocr_n_result_2kwee: " + act.err_str(ex) );
                }

                return null;
        }

        /**
         * post text, used code from the NFC...
         */
        static void post_text(String text, HashMap<String,String> mp){

                try{

                        final ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("text",text));
                nameValuePairs.add(new BasicNameValuePair("account",mp.get("account")));
                nameValuePairs.add(new BasicNameValuePair("id_inv",mp.get("id_inv")));

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(UPLOAD_URL);
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            httpclient.execute(httppost);

        }catch(Exception ex){
                        act.post_error( "post_text err: " + act.err_str(ex) );
        }
        }

        /**
         * Wait until task processing finishes and download result.
         */
        static void waitAndDownloadResult(Task task, Client restClient, HashMap<String,String> mp) throws Exception {

                try{

                        task = waitForCompletion(task, restClient);

                        if (task.Status == Task.TaskStatus.Completed) {

                                //System.out.println("Downloading..");
                                //restClient.downloadResult(task, outputPath);
                                String txt = Client.url_read(task.DownloadUrl);
                                //System.out.println("Ready");

                                //mar 2015, yep!
                                //DONE - here is where you should get the text straight, not write to file, faster...
                                //and post it to kwee...
                                //may the force be with you
                                //DONE - use the NFC code...
                                post_text(txt, mp);

                        } else if (task.Status == Task.TaskStatus.NotEnoughCredits) {

                                /*System.out.println("Not enough credits to process document. "
                                                + "Please add more pages to your application's account.");*/
                        } else {

                                //System.out.println("Task failed");
                        }
                }
                catch (Exception ex) {

                        act.post_error( "waitAndDownloadResult: " + act.err_str(ex) );
                }
        }

        /**
         * Wait until task processing finishes
         */
        static Task waitForCompletion(Task task, Client restClient) throws Exception {

                try{
                        while (task.isTaskActive()) {

                                Thread.sleep(SLEEP_OCR_TASK_CHECK_MS);
                                //System.out.println("Waiting..");
                                task = restClient.getTaskStatus(task.Id);
                        }
                }
                catch (Exception ex) {
                        act.post_error( "waitForCompletion: " + act.err_str(ex) );
                }

                return task;
        }
}
