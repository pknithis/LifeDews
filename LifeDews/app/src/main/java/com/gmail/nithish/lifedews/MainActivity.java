package com.gmail.nithish.lifedews;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class Storage
{
    static String imagePath;
}
class ImageDownloader extends AsyncTask<String,Void,Bitmap> {

    private ImageView imageIcon;

    ImageDownloader(ImageView view)
    {
        this.imageIcon = view;
    }
    @Override
    protected Bitmap doInBackground(String... params) {
        StringBuffer imgUrlData=null;
        try{
            URL urlOb = new URL("http://cs-server.usc.edu:25247/creeper.php");
            HttpURLConnection httpconnection = (HttpURLConnection)urlOb.openConnection();
            httpconnection.connect();
            int response = httpconnection.getResponseCode();
            Log.d("HttpIMG Response", response + "");
            InputStream ip = httpconnection.getInputStream();
            BufferedInputStream bin = new BufferedInputStream(ip);
            int  data = bin.read();
            imgUrlData = new StringBuffer();
            while (data!=-1)
            {
                imgUrlData.append((char)data);
                data = bin.read();
            }
            Storage.imagePath = imgUrlData.toString();
            Log.d("HttpIMG Data",imgUrlData+"");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try{
            Log.d("JSON image background ", params[0]);
            //URL imageUrl=new URL(params[0]);
            URL imageUrl=new URL(imgUrlData.toString());
            return  BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    protected void onPostExecute(Bitmap icon) {
        Log.d("JSON image postExecute ", "Entered");
        imageIcon.setImageBitmap(icon);
    }


}

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageHolder = (ImageView)findViewById(R.id.imageId);
        new ImageDownloader(imageHolder).execute("");
    }

    public void fetchNextImage(View view) {
        setContentView(R.layout.activity_main);

        ImageView imageHolder = (ImageView)findViewById(R.id.imageId);
        new ImageDownloader(imageHolder).execute("");
    }
    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file =  new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bmpUri;
    }
    public void shareImage(View view) {

        ImageView imageHolder = (ImageView)findViewById(R.id.imageId);
        Uri imageUri = getLocalBitmapUri(imageHolder);
        if (imageUri != null) {
            // Construct a ShareIntent with link to image
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setType("image/*");
            // Launch sharing dialog for image
            startActivity(Intent.createChooser(shareIntent, "Share Image"));
        } else {
            Toast.makeText(getApplicationContext(), "Image sharing error", Toast.LENGTH_LONG).show();
        }

    }

    private String saveToInternalSorage(Bitmap bitmapImage,String imagename){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,imagename);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            }catch(Exception ex)
            {
                ex.printStackTrace();
            }

        }
        return directory.getAbsolutePath();
    }

    public void saveImage( Bitmap bmp,String name){
        try {
            File file =  new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), name);
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void saveImage(View view) {

        String imageName=Storage.imagePath;
        imageName=imageName.substring(imageName.lastIndexOf("/"));
        ImageView imageHolder = (ImageView)findViewById(R.id.imageId);
        Bitmap bitmap = ((BitmapDrawable)imageHolder.getDrawable()).getBitmap();
        saveImage(bitmap, imageName);
        Toast.makeText(getApplicationContext(), "Image Saved in Local Storage", Toast.LENGTH_LONG).show();
    }
}
