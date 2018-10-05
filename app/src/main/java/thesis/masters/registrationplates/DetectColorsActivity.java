package thesis.masters.registrationplates;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class DetectColorsActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    JavaCameraView javaCameraView;
    Mat mat1, mat2, mat3;
    Scalar scalarLow, scalarHigh;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_colors);
        OpenCVLoader.initDebug();
        javaCameraView = (JavaCameraView) findViewById(R.id.colorDetectionCameraView);
        javaCameraView.setCameraIndex(0);

        scalarLow = new Scalar(0,0,0);
        scalarHigh = new Scalar(150,10,30);
        javaCameraView.setCvCameraViewListener(this);
        javaCameraView.enableView();


    }

    @Override
    protected void onPause() {
        super.onPause();
        javaCameraView.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        javaCameraView.enableView();
    }

    @Override
    protected void onDestroy() {
        javaCameraView.disableView();
        super.onDestroy();
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mat1 = new Mat(width,height,CvType.CV_16UC4);
        mat2 = new Mat(width,height,CvType.CV_16UC4);
        mat3 = new Mat(width,height,CvType.CV_8UC4);

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Imgproc.cvtColor(inputFrame.rgba(),mat1, Imgproc.COLOR_BGR2HSV);
        //Core.inRange(mat1,scalarLow,scalarHigh,mat2);

        Core.transpose(mat1,mat2);
        Imgproc.resize(mat2,mat3,mat3.size(),0,0,0);
        Core.flip(mat3,mat1,1);
        Core.inRange(mat1,scalarLow,scalarHigh,mat2);

        return mat2;
    }
}
