package com.example.rain;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity2 extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    public Bitmap rainResult=null;
    ProgressBar progressBar;
    TextView text;
    ImageView imageView1;
    Button buttonFile;
    public Bitmap bitmap;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main2);
            Intent intent = getIntent();
            Uri uri = intent.getParcelableExtra("imageUri");
            progressBar = findViewById(R.id.progressbar);
            progressBar.setVisibility(View.INVISIBLE);
            text = findViewById(R.id.textView5);
            imageView1 = findViewById(R.id.iv_newPicture);
            buttonFile = findViewById(R.id.button2);
            text.setVisibility(View.INVISIBLE);
            imageView1.setVisibility(View.INVISIBLE);
            buttonFile.setVisibility(View.INVISIBLE);
            try {
                bitmap = getBitmapFromUri(uri);
                ImageView imageView = findViewById(R.id.iv_picture);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //添加去雨算法
        public Bitmap rainRemove(Bitmap bmp) {
            SystemClock.sleep(2000);
            return bmp;
        }
        //去雨算法结束

        public void buttonRun(View view){
            RainRemoveTask rrt=new RainRemoveTask();
            rrt.execute();
        }

        public void buttonFile(View view) {
            verifyStoragePermissions(this);
            saveImageToGallery(this, rainResult);
        }

        private Bitmap getBitmapFromUri(Uri uri) throws IOException {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        }

        public void saveImageToGallery(Context context, Bitmap bmp) {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Toast.makeText(this, "请至权限中心打开应用权限", Toast.LENGTH_SHORT).show();
            } else {
                File appDir = new File(context.getExternalFilesDir(null).getPath() + "BarcodeBitmap");
                if (!appDir.exists()) {
                    appDir.mkdir();
                }
                String fileName = System.currentTimeMillis() + ".jpg";
                File file = new File(appDir, fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                    Toast.makeText(MainActivity2.this, "已保存结果至相册",
                            Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    MediaStore.Images.Media.insertImage(context.getContentResolver(),
                            file.getAbsolutePath(), fileName, null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                Toast.makeText(this, fileName, Toast.LENGTH_LONG);
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            }
        }

        public static void verifyStoragePermissions(Activity activity) {

            try {
                int permission = ActivityCompat.checkSelfPermission(activity,
                        "android.permission.WRITE_EXTERNAL_STORAGE");
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @SuppressLint("StaticFieldLeak")
        class RainRemoveTask extends AsyncTask<Bitmap,Integer,Boolean> {
            @Override
            protected void onPreExecute() {
                progressBar.setVisibility(View.VISIBLE);
                super.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(Bitmap... bitmaps) {
                rainResult=rainRemove(bitmap);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean bl) {
                progressBar.setVisibility(View.GONE);
                imageView1.setImageBitmap(rainResult);
                text.setVisibility(View.VISIBLE);
                imageView1.setVisibility(View.VISIBLE);
                buttonFile.setVisibility(View.VISIBLE);
                super.onPostExecute(bl);
            }

        }
    }

