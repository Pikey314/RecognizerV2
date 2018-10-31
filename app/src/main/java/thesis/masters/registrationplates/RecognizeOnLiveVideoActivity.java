package thesis.masters.registrationplates;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.vision.text.TextRecognizer;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class RecognizeOnLiveVideoActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    CameraBridgeViewBase cameraBridgeViewBase;
    Mat originalImageMat, processingMat1, processingMat2, processingMat3, transposeMat, flipMat, ocrMat, processingMat4;
    Bitmap recognitionBitmap, recognitionBitmapPortrait, recognitionBitmapLandscape, shareBitmapPortrait, shareBitmapLandscape;
    BaseLoaderCallback baseLoaderCallback;
    TextView textViewVideoRecognitionOutput;
    ImageView plateImageView;
    Switch switchOCRActive;
    Switch switchOldPlates;
    TextRecognizer textRecognizer;
    CharacterRecognition characterRecognition;
    private int orientation;
    PlateDetector plateDetector;
    SeekBar seekBarDistanceFromPlate;
    private boolean oldPlatesMode;
    private boolean ocrActive;
    private int distanceFromPlate;
    private String recognitionMethod;
    private final int CAMERA_PERMISSION_CODE = 333;
    private int ocrDelay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize_on_live_video);
        this.cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.rearCameraView);
        this.textViewVideoRecognitionOutput = findViewById(R.id.recognitionVideoOutputTextView);
        this.plateImageView = findViewById(R.id.plateStaticVideoImageView);
        this.plateImageView.setVisibility(View.INVISIBLE);
        this.switchOCRActive = findViewById(R.id.enableOCRLiveVideoSwitch);
        this.switchOCRActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    Toast.makeText(getApplicationContext(), getString(R.string.ocrOn),Toast.LENGTH_SHORT).show();
                    ocrActive = true;
                    plateImageView.setVisibility(View.VISIBLE);
                    ocrDelay = 0;
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.ocrOff),Toast.LENGTH_SHORT).show();
                    ocrActive = false;
                    plateImageView.setVisibility(View.INVISIBLE);
                    textViewVideoRecognitionOutput.setText("");
                }

            }
        });
        this.switchOldPlates = findViewById(R.id.oldPlatesSwitchVideo);
        this.switchOldPlates.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    Toast.makeText(getApplicationContext(), getString(R.string.oldPlatesModeOn), Toast.LENGTH_LONG).show();
                    oldPlatesMode = true;
                    plateImageView.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.oldPlatesModeOff) + " " + recognitionMethod + " method.", Toast.LENGTH_LONG).show();
                    oldPlatesMode = false;
                }
            }
        });
        this.seekBarDistanceFromPlate = findViewById(R.id.plateDistanceSeekBarVideo);
        this.seekBarDistanceFromPlate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i > distanceFromPlate)
                    Toast.makeText(getApplicationContext(), getString(R.string.carDistanceIncreased),Toast.LENGTH_SHORT).show();
                else if (i < distanceFromPlate)
                    Toast.makeText(getApplicationContext(), getString(R.string.carDistanceReduced),Toast.LENGTH_SHORT).show();
                distanceFromPlate = i;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        characterRecognition = new CharacterRecognition();
        textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        plateDetector = new PlateDetector();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.recognitionMethod = extras.getString("spinnerValue");
            if (this.recognitionMethod.equals("Morphological Transformations")) {
                this.switchOldPlates.setChecked(true);
                this.switchOldPlates.setClickable(false);

            }
        } else {
            throw new RuntimeException("Problem with settings");
        }
        this.oldPlatesMode = false;
        this.ocrActive = false;
        this.distanceFromPlate = 2;
        this.seekBarDistanceFromPlate.setProgress(2);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        this.baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {

                switch (status) {
                    case BaseLoaderCallback.SUCCESS:
                        if (ContextCompat.checkSelfPermission(RecognizeOnLiveVideoActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            requestCameraPermission();
                        } else {
                            cameraBridgeViewBase.enableView();
                        }
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            new AlertDialog.Builder(this)
                    .setTitle("Camera permission needed")
                    .setMessage("Do You allow Recognizer application to use the camera? This permission is necessary for live recognition mode.")
                    .setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(RecognizeOnLiveVideoActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Disagree", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraBridgeViewBase.enableView();
                Toast.makeText(this, "Camera Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Camera Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        originalImageMat = inputFrame.rgba();

        ocrMat = originalImageMat;

        this.orientation = getResources().getConfiguration().orientation;
        if (this.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Core.transpose(originalImageMat, transposeMat);
            Imgproc.resize(transposeMat, flipMat, flipMat.size(), 0, 0, 0);
            Core.flip(flipMat, originalImageMat, 1);
        }
        if (this.oldPlatesMode)
            this.originalImageMat = plateDetector.morphologicalTransformationsRecognitionMethodForVideo(this.originalImageMat, this.processingMat1, this.processingMat2, this.distanceFromPlate);
        else {
            switch (this.recognitionMethod) {
                case "Morphological Transformations":
                    this.originalImageMat = plateDetector.morphologicalTransformationsRecognitionMethodForVideo(this.originalImageMat, this.processingMat1, this.processingMat2, this.distanceFromPlate);
                    break;
                case "Median Center of Moments":
                    this.originalImageMat = plateDetector.medianCenterOfMomentRecognitionMethodForVideo(this.originalImageMat, this.processingMat1, this.processingMat2, this.processingMat3, this.processingMat4, this.distanceFromPlate);
                    break;
                case "Vertical Edge Contouring":
                    this.originalImageMat = plateDetector.edgeContourRecognitionMethodForVideo(this.originalImageMat, this.processingMat1, this.processingMat2, this.processingMat3, this.processingMat4, this.distanceFromPlate);
                    break;
            }
        }

        //Rozpoznowanie na co 5 klatce
        if (this.ocrActive) {
            if (this.ocrDelay == 3)
                characterRecognition.getTextFromVideo(ocrMat, orientation, recognitionBitmapPortrait, recognitionBitmapLandscape, recognitionBitmap, textRecognizer, textViewVideoRecognitionOutput);
            else if (this.ocrDelay == 4)
                this.ocrDelay = 0;
            else
                this.ocrDelay++;
        }

        return originalImageMat;
    }


    @Override
    public void onCameraViewStopped() {
        originalImageMat.release();
        processingMat1.release();
        processingMat2.release();
        processingMat3.release();
        processingMat4.release();
        transposeMat.release();
        flipMat.release();
        ocrMat.release();


    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        originalImageMat = new Mat(width, height, CvType.CV_8U, new Scalar(4));
        processingMat1 = new Mat(width, height, CvType.CV_8U, new Scalar(4));
        processingMat2 = new Mat(width, height, CvType.CV_8U, new Scalar(4));
        processingMat3 = new Mat(width, height, CvType.CV_8U, new Scalar(4));
        processingMat4 = new Mat(width, height, CvType.CV_8U, new Scalar(4));
        transposeMat = new Mat(width, height, CvType.CV_8U, new Scalar(4));
        flipMat = new Mat(width, height, CvType.CV_8U, new Scalar(4));
        ocrMat = new Mat(width, height, CvType.CV_8U, new Scalar(4));
        recognitionBitmapPortrait = Bitmap.createBitmap(originalImageMat.cols(), originalImageMat.rows(), Bitmap.Config.ARGB_8888);
        recognitionBitmapLandscape = Bitmap.createBitmap(originalImageMat.rows(), originalImageMat.cols(), Bitmap.Config.ARGB_8888);
        shareBitmapPortrait = Bitmap.createBitmap(originalImageMat.cols(), originalImageMat.rows(), Bitmap.Config.ARGB_8888);
        shareBitmapLandscape = Bitmap.createBitmap(originalImageMat.rows(), originalImageMat.cols(), Bitmap.Config.ARGB_8888);


    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "OpenCV problem", Toast.LENGTH_SHORT).show();
        } else {
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    public void shareRecognizedFrame(View v){
        if (this.originalImageMat != null) {
            if (this.orientation == Configuration.ORIENTATION_PORTRAIT) {
                Utils.matToBitmap(originalImageMat, shareBitmapPortrait);
                if ((this.textViewVideoRecognitionOutput.getText().toString()).equals(""))
                    prepareFileToShareVideo(this.shareBitmapPortrait, "Recognizer_license_plate");
                else
                    prepareFileToShareVideo(this.shareBitmapPortrait, "Plate nr: " + (this.textViewVideoRecognitionOutput.getText().toString()));

            }
            else {
                Utils.matToBitmap(originalImageMat, shareBitmapLandscape);
                if ((this.textViewVideoRecognitionOutput.getText().toString()).equals(""))
                    prepareFileToShareVideo(this.shareBitmapLandscape, "Recognizer_license_plate");
                else
                    prepareFileToShareVideo(this.shareBitmapLandscape, "Plate nr: " + (this.textViewVideoRecognitionOutput.getText().toString()));

            }
        }


    }


    private void prepareFileToShareVideo (Bitmap bitmap,String filename) {

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/png");
        ByteArrayOutputStream bytaArray = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytaArray);
        File imageFile = new File(Environment.getExternalStorageDirectory() + File.separator + filename + ".png");
        try {
            imageFile.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
            fileOutputStream.write(bytaArray.toByteArray());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
        startActivity(Intent.createChooser(share, "Recognizer share license plate"));
        //SPRAWDZIC TO
        imageFile.deleteOnExit();



    }
}