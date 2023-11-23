package com.example.pdfscanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGES = 105;
    private Button camera, doc;
    private TextView multiple;
    private ImageView img;
    private TextView tv;
    private String[] cameraPermission = {"Manifest.permission.CAMERA"};
    private String[] documentPermission = {"Manifest.permission.WRITE_EXTERNAL_STORAGE",
            "Manifest.permission.READ_EXTERNAL_STORAGE"};


    private Uri imageUri;
    private String imageurl;
    private final int FILE_INTENT_CODE = 101;
    private final int FILE_PERMISSION_CODE = 102;
    private final int CAMERA_PERMISSION_CODE = 103;
    private final int CAMERA_INTENT_CODE = 104;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = findViewById(R.id.camera);
        multiple = findViewById(R.id.mul_img);
        doc = findViewById(R.id.document);
        img = findViewById(R.id.img);
        tv = findViewById(R.id.tv);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((ContextCompat.checkSelfPermission(
                        MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) ||
                        (ContextCompat.checkSelfPermission(
                                MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                            CAMERA_PERMISSION_CODE);
                }
                else
                    pickFromCamera();
            }
        });

        doc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((ContextCompat.checkSelfPermission(
                        MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) ||
                        (ContextCompat.checkSelfPermission(
                                MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                            FILE_PERMISSION_CODE);
                }
                else
                    pickFromDocument();
            }
        });


        multiple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickMultipleImages();
            }
        });
    }

    private void pickMultipleImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGES);
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_INTENT_CODE);
    }


    private void pickFromDocument(){
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("image/jpeg");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(chooseFile, FILE_INTENT_CODE);
    }


    private void setImage(File file){
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        float height = displaymetrics.heightPixels ;
        float width = displaymetrics.widthPixels ;
//        int width =210;
//        int height = 297;


        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
        Bitmap b= ExifUtils.rotateBitmap(file.getPath(), bitmap);

        Bitmap bm = Bitmap.createScaledBitmap(b, (int)width, (int)height, false);


        img.setImageBitmap(bm);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_INTENT_CODE){
            Uri uri = data.getData();
            File destination = new File(RealPathUtil.getRealPath(this, uri));
            Log.i("FILE_URI", uri.toString());
            Log.i("FILE_PATH", destination.getPath());
            setImage(destination);
            tv.setText("File is selected.");
        }else if (requestCode == CAMERA_INTENT_CODE){
            try {
                imageurl = RealPathUtil.getRealPath(MainActivity.this, imageUri);
                File destination = new File(imageurl);
                setImage(destination);
                Log.i("IMG_URL", imageurl);
                Log.i("IMG_URI", imageUri.getPath());
                tv.setText("File is selected.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(requestCode == PICK_IMAGES){
            if (data.getClipData() != null) {
                ClipData mClipData = data.getClipData();
                Log.i("COUNT:", mClipData.getItemCount()+"");
                for (int i = 0; i < mClipData.getItemCount(); i++) {
                    ClipData.Item item = mClipData.getItemAt(i);
                    Uri uri = item.getUri();
                    // display your images
                    Log.i("URI_ADDRESS"+i+":", uri.toString());
                }
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mClipData.getItemAt(0).getUri());
                    img.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                img.setImageURI(mClipData.getItemAt(0).getUri());
            } else if (data.getData() != null) {
                Uri uri = data.getData();
                // display your image
                img.setImageURI(uri);
//                imageView.setImageURI(uri);
            }else {

            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                pickFromCamera();
            }
        }else if (requestCode == FILE_PERMISSION_CODE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                pickFromDocument();
            }
        }
    }
}