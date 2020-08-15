package com.mycompany.newchatapp.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.mycompany.newchatapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class ViewMedia extends AppCompatActivity {

    ImageView imageView;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_media);
        imageView = findViewById(R.id.image);
        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        Glide.with(this).load(url).into(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.saveImage) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    downloadImage(url);
                    Snackbar snackbar = Snackbar.make(getWindow().getDecorView(), "Image Downloaded",
                            Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    public void downloadImage(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            createDirectoryAndSaveFile(myBitmap, System.currentTimeMillis() + ".jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDirectoryAndSaveFile(Bitmap imageToSave, String fileName) {

        File direct = Environment.getExternalStorageDirectory();

        File dir = new File(direct.getAbsoluteFile() + "/Chatz/");
        if (!dir.exists()) {
            File wallpaperDirectory = new File(direct.getAbsoluteFile() + "/Chatz/");
            wallpaperDirectory.mkdirs();
        }
        File file = new File(dir, fileName);
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}