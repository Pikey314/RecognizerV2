package thesis.masters.registrationplates;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
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

public class RecognizeOnLiveVideoActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private CameraBridgeViewBase cameraBridgeViewBase;
    private Mat originalImageMat, processingMat1, processingMat2, processingMat3, transposeMat, flipMat, ocrMat, processingMat4;
    private Bitmap recognitionBitmap, recognitionBitmapPortrait, recognitionBitmapLandscape, shareBitmapPortrait, shareBitmapLandscape;
    private BaseLoaderCallback baseLoaderCallback;
    private TextView textViewVideoRecognitionOutput;
    private ImageView plateImageView;
    private Switch switchOCRActive;
    private Switch switchOldPlates;
    private TextRecognizer textRecognizer;
    private CharacterRecognition characterRecognition;
    private ShareExecuter shareExecuter;
    private int orientation;
    private PlateDetector plateDetector;
    private PlateDetailsInformator plateDetailsInformator;
    private SeekBar seekBarDistanceFromPlate;
    private boolean oldPlatesMode;
    private boolean ocrActive;
    private int distanceFromPlate;
    private String recognitionMethod;
    private final int CAMERA_PERMISSION_CODE = 333;
    private final int SHARING_PERMISSIONS_CODE = 432;
    private String[] SHARING_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
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
                    findViewById(R.id.plateInfoVideoImageView).setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.ocrOff),Toast.LENGTH_SHORT).show();
                    ocrActive = false;
                    plateImageView.setVisibility(View.INVISIBLE);
                    textViewVideoRecognitionOutput.setText("");
                    findViewById(R.id.plateInfoVideoImageView).setVisibility(View.INVISIBLE);
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
        this.characterRecognition = new CharacterRecognition();
        this.shareExecuter = new ShareExecuter();
        this.textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        this.plateDetector = new PlateDetector();
        this.plateDetailsInformator = new PlateDetailsInformator(this);
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
        this.cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        this.cameraBridgeViewBase.setCvCameraViewListener(this);
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

    private void requestSharingPermissions(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE))   {
            new AlertDialog.Builder(this)
                    .setTitle("Storage Access permission needed")
                    .setMessage("Do You allow Recognizer application to use the external storage? This permission is necessary for sharing.")
                    .setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(RecognizeOnLiveVideoActivity.this, SHARING_PERMISSIONS,SHARING_PERMISSIONS_CODE);
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
            ActivityCompat.requestPermissions(this, SHARING_PERMISSIONS,SHARING_PERMISSIONS_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.cameraBridgeViewBase.enableView();
                Toast.makeText(this, "Camera Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Camera Permission not granted", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == SHARING_PERMISSIONS_CODE) {
            if (grantResults.length > 0) {
                boolean permissions_flag = true;
                for (int i = 0; i <  grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                        permissions_flag = false;
                }
                if (permissions_flag) {
                    if (this.originalImageMat != null) {
                        if (this.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            Utils.matToBitmap(this.originalImageMat, this.shareBitmapPortrait);
                            if ((this.textViewVideoRecognitionOutput.getText().toString()).equals(""))
                                prepareFileToShareVideoActivity(this.shareBitmapPortrait, "Recognizer_license_plate");
                            else
                                prepareFileToShareVideoActivity(this.shareBitmapPortrait, "Plate nr: " + (this.textViewVideoRecognitionOutput.getText().toString()));
                        } else {
                            Utils.matToBitmap(this.originalImageMat, this.shareBitmapLandscape);
                            if ((this.textViewVideoRecognitionOutput.getText().toString()).equals(""))
                                prepareFileToShareVideoActivity(this.shareBitmapLandscape, "Recognizer_license_plate");
                            else
                                prepareFileToShareVideoActivity(this.shareBitmapLandscape, "Plate nr: " + (this.textViewVideoRecognitionOutput.getText().toString()));
                        }
                    }
                    Toast.makeText(this,"Sharing permission granted",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,"Permission not granted",Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(this,"Permission not granted",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        this.originalImageMat = inputFrame.rgba();
        this.ocrMat = this.originalImageMat;
        this.orientation = getResources().getConfiguration().orientation;
        if (this.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Core.transpose(this.originalImageMat, this.transposeMat);
            Imgproc.resize(this.transposeMat, this.flipMat, this.flipMat.size(), 0, 0, 0);
            Core.flip(this.flipMat, this.originalImageMat, 1);
        }
        if (this.oldPlatesMode)
            this.originalImageMat = this.plateDetector.morphologicalTransformationsRecognitionMethodForVideo(this.originalImageMat, this.processingMat1, this.processingMat2, this.distanceFromPlate);
        else {
            switch (this.recognitionMethod) {
                case "Morphological Transformations":
                    this.originalImageMat = this.plateDetector.morphologicalTransformationsRecognitionMethodForVideo(this.originalImageMat, this.processingMat1, this.processingMat2, this.distanceFromPlate);
                    break;
                case "Median Center of Moments":
                    this.originalImageMat = this.plateDetector.medianCenterOfMomentRecognitionMethodForVideo(this.originalImageMat, this.processingMat1, this.processingMat2, this.processingMat3, this.processingMat4, this.distanceFromPlate);
                    break;
                case "Vertical Edge Contouring":
                    this.originalImageMat = this.plateDetector.edgeContourRecognitionMethodForVideo(this.originalImageMat, this.processingMat1, this.processingMat2, this.processingMat3, this.processingMat4, this.distanceFromPlate);
                    break;
            }
        }
        //Rozpoznowanie na co 5 klatce
        if (this.ocrActive) {
            if (this.ocrDelay == 3)
                this.characterRecognition.getTextFromVideo(this.ocrMat, this.orientation, this.recognitionBitmapPortrait, this.recognitionBitmapLandscape, this.recognitionBitmap, this.textRecognizer, this.textViewVideoRecognitionOutput);
            else if (this.ocrDelay == 4)
                this.ocrDelay = 0;
            else
                this.ocrDelay++;
        }
        return originalImageMat;
    }

    @Override
    public void onCameraViewStopped() {
        this.originalImageMat.release();
        this.processingMat1.release();
        this.processingMat2.release();
        this.processingMat3.release();
        this.processingMat4.release();
        this.transposeMat.release();
        this.flipMat.release();
        this.ocrMat.release();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        this.originalImageMat = new Mat(width, height, CvType.CV_8U, new Scalar(4));
        this.processingMat1 = new Mat(width, height, CvType.CV_8U, new Scalar(4));
        this.processingMat2 = new Mat(width, height, CvType.CV_8U, new Scalar(4));
        this.processingMat3 = new Mat(width, height, CvType.CV_8U, new Scalar(4));
        this.processingMat4 = new Mat(width, height, CvType.CV_8U, new Scalar(4));
        this.transposeMat = new Mat(width, height, CvType.CV_8U, new Scalar(4));
        this.flipMat = new Mat(width, height, CvType.CV_8U, new Scalar(4));
        this.ocrMat = new Mat(width, height, CvType.CV_8U, new Scalar(4));
        this.recognitionBitmapPortrait = Bitmap.createBitmap(this.originalImageMat.cols(), this.originalImageMat.rows(), Bitmap.Config.ARGB_8888);
        this.recognitionBitmapLandscape = Bitmap.createBitmap(this.originalImageMat.rows(), this.originalImageMat.cols(), Bitmap.Config.ARGB_8888);
        this.shareBitmapPortrait = Bitmap.createBitmap(this.originalImageMat.cols(), this.originalImageMat.rows(), Bitmap.Config.ARGB_8888);
        this.shareBitmapLandscape = Bitmap.createBitmap(this.originalImageMat.rows(), this.originalImageMat.cols(), Bitmap.Config.ARGB_8888);
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
            this.baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.cameraBridgeViewBase != null) {
            this.cameraBridgeViewBase.disableView();
        }
    }

    public void shareRecognizedFrame(View v){
        if (ContextCompat.checkSelfPermission(RecognizeOnLiveVideoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(RecognizeOnLiveVideoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestSharingPermissions();
        } else {
            if (this.originalImageMat != null) {
                if (this.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    Utils.matToBitmap(this.originalImageMat, this.shareBitmapPortrait);
                    if ((this.textViewVideoRecognitionOutput.getText().toString()).equals(""))
                        prepareFileToShareVideoActivity(this.shareBitmapPortrait, "Recognizer_license_plate");
                    else
                        prepareFileToShareVideoActivity(this.shareBitmapPortrait, "Plate nr: " + (this.textViewVideoRecognitionOutput.getText().toString()));
                } else {
                    Utils.matToBitmap(this.originalImageMat, this.shareBitmapLandscape);
                    if ((this.textViewVideoRecognitionOutput.getText().toString()).equals(""))
                        prepareFileToShareVideoActivity(this.shareBitmapLandscape, "Recognizer_license_plate");
                    else
                        prepareFileToShareVideoActivity(this.shareBitmapLandscape, "Plate nr: " + (this.textViewVideoRecognitionOutput.getText().toString()));
                }
            }
        }
    }

    private void prepareFileToShareVideoActivity (Bitmap bitmap,String filename) {
        Intent share = this.shareExecuter.prepareFileToShare(bitmap,filename);
        startActivity(Intent.createChooser(share, "Recognizer share license plate"));

    }

    public void showPolishPlateDetailedInfoVideo(View v){
        if(this.textViewVideoRecognitionOutput.getText().toString()!=null && !this.textViewVideoRecognitionOutput.getText().toString().equals("")){
            String polishPlateDetails = this.plateDetailsInformator.getPolishPlateDetailedInfo(this.textViewVideoRecognitionOutput.getText().toString());
            if (!polishPlateDetails.equals(""))
                Toast.makeText(this,polishPlateDetails,Toast.LENGTH_LONG).show();
        }
    }

}