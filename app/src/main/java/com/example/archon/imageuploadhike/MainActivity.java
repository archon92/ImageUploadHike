package com.example.archon.imageuploadhike;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends ActionBarActivity {
    private static final int CAMERA_REQUEST = 2;
    private static final int Gallerypick=3;
    private ImageView imageView;
    public UploadReceiver uploadReceiver;
    public faileduploadreceiver faileduploadreceiver;
    Button photoButton,gallerypick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.imageView = (ImageView)this.findViewById(R.id.ivcamera);
         photoButton = (Button) this.findViewById(R.id.btntakepic);
        gallerypick=(Button)this.findViewById(R.id.btngallerypick);


        //Here I am creating Intent filters to register the broadcast receiver to hanbdle responses from my Intnt service
        IntentFilter uploadfilter=new IntentFilter(MyConstants.UploadImage);
        uploadfilter.addCategory(Intent.CATEGORY_DEFAULT);
        uploadReceiver=new UploadReceiver();
        registerReceiver(uploadReceiver,uploadfilter);

        //Here I am creating Intent filters to register the broadcast receiver to hanbdle responses from my Intnt service

        IntentFilter failedfilter=new IntentFilter(MyConstants.Failedupload);
        failedfilter.addCategory(Intent.CATEGORY_DEFAULT);
        faileduploadreceiver=new faileduploadreceiver();
        registerReceiver(faileduploadreceiver,failedfilter);



        photoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        gallerypick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery,Gallerypick);
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        String filepath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/Image";
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);

            ByteArrayOutputStream stream =new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            byte[] byteArray = stream.toByteArray();
            final String imagestring=byteArray.toString();



            //UploadFileToServer method as per the problem statement requires a file object to be passed and returns a value based on the response.
            //The Commented pieces of code does this functionality.
            //I have implemented image upload also in another way with an intent service handling the threading and queueing and returning the status to the broadcastreceivers which then pop up a toast.



           /* File file=new File(filepath);
            try {
                FileOutputStream fos=new FileOutputStream((file));
                photo.compress(Bitmap.CompressFormat.JPEG,90,fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
//Multiple calls can be made to the same intent service and the service queues its uploads
            Intent uploadintent=new Intent(MainActivity.this,ImageUpload.class);
            uploadintent.putExtra("ImageArray",imagestring);
            startService(uploadintent);
            Toast.makeText(getApplicationContext(),"uploading",Toast.LENGTH_SHORT).show();






            //The actual call as requested is being made here.
          /*  if(uploadFileToServer(file)){
                Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_SHORT).show();

            }
            else
                Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();

*/


        }
        //Code to pick an image from gallery and upload to server via services

        // The same code writte above to use the method UploadFileToServer alsoholds good here
        else if(requestCode==Gallerypick){
            Uri SelectedImage=data.getData();
            String[] FilePathToCoulumn = {MediaStore.Images.Media.DATA};
            Cursor SelectedCursor=getApplicationContext().getContentResolver().query(SelectedImage,FilePathToCoulumn,null,null,null);
            SelectedCursor.moveToFirst();

            int Columnindex= SelectedCursor.getColumnIndex(FilePathToCoulumn[0]);

            String picturepath=SelectedCursor.getString(Columnindex);//Im getting the actual path from the gallery here using the cursor
            SelectedCursor.close();

            imageView.setImageBitmap(BitmapFactory.decodeFile(picturepath));//Settibng the imageview by creating the bitmap from the path obtained above

            Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream stream2 =new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,90,stream2);
            byte[] byteArray = stream2.toByteArray();
            final String imagestring2=byteArray.toString();

            Intent uploadintentfromgal=new Intent(MainActivity.this,ImageUpload.class);
            uploadintentfromgal.putExtra("ImageArray",imagestring2);
            startService(uploadintentfromgal);
            Toast.makeText(getApplicationContext(),"uploading",Toast.LENGTH_SHORT).show();


            //The below code is done as per the problem statement.....writing the char steam to a file and then calling the method UploadFileToServer By passing this object.

            /* File file=new File(filepath);
            try {
                FileOutputStream fos=new FileOutputStream((file));
                bitmap.compress(Bitmap.CompressFormat.JPEG,90,fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            //uploadFileToServer(file);

            //The actual call as requested is being made here.
          /*  if(uploadFileToServer(file)){
                Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_SHORT).show();

            }
            else
                Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();

*/




        }
    }

    //This recveive catches the statuses if the image upload is successful from the server side
    public class UploadReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String uploadsuccess=intent.getStringExtra("Successful");
            Toast.makeText(getApplicationContext(),uploadsuccess,Toast.LENGTH_SHORT).show();

        }
    }

    //This recveive catches the statuses if the image upload fails from the server side

    public class faileduploadreceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String failed=intent.getStringExtra("Failed");
            Toast.makeText(getApplicationContext(),failed,Toast.LENGTH_SHORT).show();


        }
    }

    public boolean uploadFileToServer(File file){
         Thread t =new Thread(new Runnable() {
             @Override
             public void run() {
            //The actual hhtp call must be made here and
                //the reponse can be true or false.
             }
         });
        t.start();
        return false;
    }


}
