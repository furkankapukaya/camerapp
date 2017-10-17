package com.example.cameraapp_1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static java.lang.Math.abs;

public class PickAImageFromSDCard extends AppCompatActivity {
    static {
        if(OpenCVLoader.initDebug()){
            Log.e("OPENCV : ", "Loaded");
        }
        else {Log.e("OPENCV","Not");}
    }

    private static final int SELECT_PHOTO = 100;
    ImageView image;
    ImageView video;
   // VideoView video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_aimage_from_sdcard);

        image = (ImageView) findViewById(R.id.display_image);
        video = (ImageView) findViewById(R.id.display_image);

    //    video = (VideoView) findViewById(R.id.display_image);
    }



    public void pickAImage(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("*/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    InputStream imageStream = null;
                    try {
                        imageStream = getContentResolver().openInputStream(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                  //  video.setVideoURI(selectedImage);
                   // video.start();
                  // image.setImageURI(selectedImage);// To display selected image in image view
                }
        }
    }

    public void pickImageWithPath(View view) {
        File videoFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/Hello_Camera/VID_20171014_134124.mp4");

        Uri videoFileUri = Uri.parse(videoFile.toString());
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoFile.getAbsolutePath());
        ArrayList<Bitmap> rev = new ArrayList<Bitmap>();
        ArrayList<Bitmap> orig = new ArrayList<Bitmap>();

        MediaPlayer mp = MediaPlayer.create(getBaseContext(), videoFileUri);

        int millis = mp.getDuration();
        Bitmap bitmap_old = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST);
        int h = bitmap_old.getHeight();
        int w = bitmap_old.getWidth();
        rev.add(bitmap_old);
        // int color1 = bitmap_old.getPixel(25,25);
        int co = 0;
        for (int i = 1000000; i < millis * 1000; i += 1000000) {

            Bitmap bitmap = retriever.getFrameAtTime(i, MediaMetadataRetriever.OPTION_CLOSEST);
            Bitmap newbit = retriever.getFrameAtTime(i, MediaMetadataRetriever.OPTION_CLOSEST);
            Mat imgToProcess1 = new Mat(h, w, CvType.CV_8UC4);
            Mat imgToProcess2 = new Mat(h, w, CvType.CV_8UC4);

            Utils.bitmapToMat(newbit, imgToProcess1 );
            Utils.bitmapToMat(bitmap_old, imgToProcess2);
            Imgproc.cvtColor(imgToProcess1, imgToProcess1,Imgproc.COLOR_RGB2GRAY);
            Imgproc.cvtColor(imgToProcess2, imgToProcess2,Imgproc.COLOR_RGB2GRAY);
            Mat imgTo = new Mat(h,w,CvType.CV_8UC4);
            Core.absdiff(imgToProcess1,imgToProcess2,imgTo);
            String folder = Environment.getExternalStorageDirectory().toString();
            Imgcodecs.imwrite(folder+"/Pictures/focus/"+ String.valueOf(co)+".jpeg",imgTo);
            co++;
            for (int j = 0; j < w; j++) {

                for (int k = 0; k < h; k++) {
                    int color1 = bitmap_old.getPixel(j, k);
                    int color2 = newbit.getPixel(j, k);

                    int red1 = Color.red(color1);
                    int blue1 = Color.blue(color1);
                    int green1 = Color.green(color1);
                    int red2 = Color.red(color2);
                    int blue2 = Color.blue(color2);
                    int green2 = Color.green(color2);
                    int colorx=0;
                    if (abs(red1 - red2) < 15 && abs(blue1 - blue2) < 15 && abs(green1 - green2) < 15) {
                        newbit.setPixel(j, k, Color.BLACK);

                    } else {
                        newbit.setPixel(j, k, Color.WHITE);
                        colorx=color1;
                    }
                  /*  if(newbit.getPixel(j,k) == Color.GREEN){
                        newbit.setPixel(j,k,colorx);
                    }

                    color2 = newbit.getPixel(j,k);
                    red2 = Color.red(color2);
                    blue2 = Color.blue(color2);
                    if(abs(red1 - red2) < 5 && abs(blue1-blue2)<5 && abs(green1-green2)<5){
                        newbit.setPixel(j,k,Color.WHITE);
                    }*/
                }
            }
            rev.add(newbit);
            bitmap_old = bitmap;
            try {
                saveFrames(rev);
            } catch (IOException e) {

                e.printStackTrace();
            }

        }


    }
    public void saveFrames(ArrayList<Bitmap> saveBitmapList) throws IOException{

        String folder = Environment.getExternalStorageDirectory().toString();
        File saveFolder = new File(folder + "/Pictures/fr123/");
        if(!saveFolder.exists()){
            saveFolder.mkdirs();
        }


        int i=1;
        for (Bitmap b : saveBitmapList){
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
            File f = new File(saveFolder,("frame"+i+".jpg"));

            f.createNewFile();

            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());

            fo.flush();
            fo.close();

            i++;
        }

    }
}
