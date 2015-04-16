package com.example.archon.imageuploadhike;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by archon on 16-04-2015.
 */
public class ImageUpload extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * to name the worker thread, important only for debugging.
     */
    public ImageUpload() {
        super("ImageUpload");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(MyConstants.TAG,"inside intentservice");
        try{
        String imagestring=intent.getStringExtra("ImageArray");
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("ImageString",imagestring);

        String imgjson="";


        HttpClient httpClient=new DefaultHttpClient();
        HttpPost httpPost=new HttpPost("the url to hit");
        imgjson=jsonObject.toString();
            StringEntity se=new StringEntity(imgjson);
            httpPost.setEntity(se);

        HttpResponse httpResponse=httpClient.execute(httpPost);

        int responsecode=httpResponse.getStatusLine().getStatusCode();
            if(responsecode==200){//this is assuming that the hike server would be sending 200 when imgupload is successful
                Intent broadcastreceiverintent=new Intent();
                broadcastreceiverintent.setAction(MyConstants.UploadImage);
                broadcastreceiverintent.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastreceiverintent.putExtra("Successful","image upload Successful");
                sendBroadcast(broadcastreceiverintent);//here iam sending the status back to the broadcast reciever which can communicate with the Ui thread to pop the toast

            }
            if(responsecode!=200){//assuming image upload has failed.
                Intent broadcastreceiverintent=new Intent();
                broadcastreceiverintent.setAction(MyConstants.Failedupload);
                broadcastreceiverintent.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastreceiverintent.putExtra("Failed","image upload Failed");
                sendBroadcast(broadcastreceiverintent);//here iam sending the status back to the broadcast reciever which can communicate with the Ui thread to pop the toast
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
