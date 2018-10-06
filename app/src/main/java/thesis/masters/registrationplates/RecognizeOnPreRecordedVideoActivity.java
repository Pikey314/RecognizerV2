package thesis.masters.registrationplates;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

public class RecognizeOnPreRecordedVideoActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final int SELECTED_VIDEO = 102;
    CameraBridgeViewBase cameraBridgeViewBase;
    Mat mat1, mat2, mat3, mat4, tmp, mat5, mat6;
    BaseLoaderCallback baseLoaderCallback;
    String videoPath;
    Bitmap videoBitmap;
    MediaMetadataRetriever videoFromGallery;
    int index;
    long durationMs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        index = 0;
        setContentView(R.layout.activity_recognize_on_pre_recorded_video);
        this.cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.preRecordedVideoCameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        this.baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {

                switch (status)
                {
                    case BaseLoaderCallback.SUCCESS:
                        //cameraBridgeViewBase.enableView();
                        break;
                    default :
                        super.onManagerConnected(status);
                        break;



                }
            }
        };
    }

    public void pickVideoFromGallery(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, SELECTED_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && resultCode == RESULT_OK && requestCode == SELECTED_VIDEO) {
            Uri videoURI = data.getData();
            videoPath = getRealPathFromURI(this,videoURI);

            Toast.makeText(getApplicationContext(),"aaa", Toast.LENGTH_LONG).show();

            videoFromGallery = new MediaMetadataRetriever();
            videoFromGallery.setDataSource(videoPath);
            cameraBridgeViewBase.enableView();
            videoBitmap = videoFromGallery.getFrameAtTime(200000);
            durationMs = Long.parseLong(videoFromGallery.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            durationMs *= 1000;

        }
    }


    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {

            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }





    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if (videoBitmap != null) {
            videoBitmap = videoFromGallery.getFrameAtTime(index);
            index+=500000;
            if (index > durationMs)
                    index = 0;

            Utils.bitmapToMat(videoBitmap, mat1);


        }
        else
            mat1 = inputFrame.rgba();
        // recognize code


        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Core.transpose(mat1,mat5);
            Imgproc.resize(mat5,mat6,mat6.size(),0,0,0);
            Core.flip(mat6,mat1,1);
        }
        return mat1;
    }

    @Override
    public void onCameraViewStopped() {
        mat1.release();
        mat2.release();
        mat3.release();

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mat1 = new Mat(width,height,CvType.CV_8U,new Scalar(4));
        mat2 = new Mat(width,height,CvType.CV_8U,new Scalar(4));
        mat3 = new Mat(width,height,CvType.CV_8U,new Scalar(4));
        mat4 = new Mat(width,height,CvType.CV_8U,new Scalar(4));
        tmp = new Mat(width,height,CvType.CV_8U,new Scalar(4));
        mat5 = new Mat(width,height,CvType.CV_8U,new Scalar(4));
        mat6 = new Mat(width,height,CvType.CV_8U,new Scalar(4));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraBridgeViewBase!=null)
        {
            //cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug())
        {
            Toast.makeText(getApplicationContext(),"OpenCV problem",Toast.LENGTH_SHORT).show();
        }
        else
        {
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraBridgeViewBase!=null)
        {
            cameraBridgeViewBase.disableView();
        }
    }
}
