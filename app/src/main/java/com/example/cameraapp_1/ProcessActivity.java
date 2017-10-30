package com.example.cameraapp_1;

import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;


import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.videoio.VideoCapture;

import static java.lang.Math.abs;

public class ProcessActivity extends AppCompatActivity implements CvCameraViewListener2{
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e("OPENCV TAG ", "opencv yok");
        }
    }


    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgba;
    private Mat savedGray;
    private Mat savedRgba;
    private Mat mBlack;
    private int H;
    private int W;
    private double frame1[];
    private double frame2[];
    private double whiteframe[];
    private double blackframe[];
    private Mat mGray;

    private boolean isFrameSaved = false;



    public ProcessActivity() {
        Log.i("TAGTAAGTAG", "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial2_activity_surface_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.enableView();

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mBlack = new Mat(height, width, CvType.CV_8UC4);

        mGray = new Mat(height, width, CvType.CV_8UC4);

        savedRgba = new Mat(height, width, CvType.CV_8UC4);
        savedGray = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {


        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        H = mRgba.height();
        W = mRgba.width();
        if(!isFrameSaved){
            mGray = inputFrame.rgba();
            mBlack=inputFrame.rgba();
            Imgproc.cvtColor(inputFrame.gray(), mGray, Imgproc.COLOR_GRAY2RGBA, 4);
            Imgproc.cvtColor(inputFrame.gray(), mBlack, Imgproc.COLOR_GRAY2RGBA, 4);

            blackframe = mRgba.get(5,5);
            whiteframe = mRgba.get(5,5);
            Log.e("BLACKFRAME", "bblacked");
            for(int i = 0; i<mRgba.get(5,5).length;i++){
                blackframe[i] = 0;
                whiteframe[i] = 255;
            }
            Log.e("WHITE", "white");
            for(int i=0;i<H;i++){
                for(int j =0; j<W;j++){
                    mBlack.put(i,j,blackframe);
                }
            }
            mGray = mBlack.clone();
        }
        if(isFrameSaved) {
            Imgproc.cvtColor(inputFrame.gray(), mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
            Imgproc.cvtColor(savedRgba, savedGray, Imgproc.COLOR_GRAY2RGBA, 4);
            mGray = mBlack.clone();
            Core.absdiff(mRgba, savedGray, mRgba);
            /*for (int i = 100; i < H - 100; i++) {
                for (int j = 100; j < W - 100; j++) {
                    boolean farkli = false;
                    frame1 = mRgba.get(i, j);
                    frame2 = savedGray.get(i, j);
                    for (int k = 0; k < frame1.length; k++) {
                        if (abs(frame1[k] - frame2[k]) > 25) {
                            farkli = true;
                            k = frame1.length + 1;
                        }
                    }
                    mRgba.put(i,j, blackframe);
                    if (farkli) {
                        mRgba.put(i, j, whiteframe);
                    }

                }
            }*/

        }
       // mRgba = mGray.clone();
        savedRgba = inputFrame.gray();
        isFrameSaved = true;
        return mRgba;
    }


}
