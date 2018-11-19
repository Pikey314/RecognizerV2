package thesis.masters.registrationplates;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

public class RecognizeOnPreRecordedVideoActivity extends AppCompatActivity{

    private static final int SELECTED_VIDEO = 102;
    String videoPath;
    Bitmap videoBitmap;
    Bitmap recognizedBitmap;
    MediaMetadataRetriever videoFromGallery;
    long durationMs;
    VideoView vView;
    ImageView iView;
    private int distanceFromPlate;
    PlateDetector plateDetector;
    private String recognitionMethod;
    SeekBar seekBarDistanceFromPlatePreRecorded;
    TextView textViewVideoRecognitionOutputPreRecorded;
    CharacterRecognition characterRecognition;
    PlateDetailsInformator plateDetailsInformator;
    private final int SHARING_PERMISSIONS_CODE = 643;
    private String[] SHARING_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize_on_pre_recorded_video);
        vView = findViewById(R.id.vview);
        iView = findViewById(R.id.iview);
        textViewVideoRecognitionOutputPreRecorded = findViewById(R.id.recognitionPreRecordedVideoOutputTextView);
        plateDetector = new PlateDetector();
        plateDetailsInformator = new PlateDetailsInformator(this);
        characterRecognition = new CharacterRecognition();
        this.distanceFromPlate = 0;
        Bundle extras = getIntent().getExtras();
        if (extras != null)
            this.recognitionMethod = extras.getString("spinnerValue");
        else
            throw new RuntimeException("Problem with settings");
        this.seekBarDistanceFromPlatePreRecorded = findViewById(R.id.plateDistanceSeekBarPreRecordedVideo);
        this.seekBarDistanceFromPlatePreRecorded.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        Toast.makeText(this,"This screen supports only portrait orientation of the device", Toast.LENGTH_SHORT).show();

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
            try{
                vView.setVideoURI(videoURI);
                vView.start();
                videoPath = getRealPathFromURI(this,videoURI);
                videoFromGallery = new MediaMetadataRetriever();
                videoFromGallery.setDataSource(videoPath);
                findViewById(R.id.pickRecordedVideoImageView).setVisibility(View.INVISIBLE);
                findViewById(R.id.recognizePreRecordedVideoImageView).setVisibility(View.VISIBLE);
                findViewById(R.id.sharePreRecordedVideoImageView).setVisibility(View.INVISIBLE);
                findViewById(R.id.plateInfoPreRecordedVideoImageView).setVisibility(View.INVISIBLE);
                this.textViewVideoRecognitionOutputPreRecorded.setText("");
            } catch(Exception e){
                e.printStackTrace();
            }
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

    public void recognizeFrame(View v){
        if (vView != null && videoFromGallery != null) {
            int framePosition = vView.getCurrentPosition();
            this.videoBitmap = videoFromGallery.getFrameAtTime(framePosition);


            switch (this.recognitionMethod) {
                case "Morphological Transformations":
                    this.recognizedBitmap = plateDetector.morphologicalTransformationsRecognitionMethod(this.videoBitmap, this.distanceFromPlate);
                    break;
                case "Median Center of Moments":
                    this.recognizedBitmap = plateDetector.medianCenterOfMomentRecognitionMethod(this.videoBitmap, this.distanceFromPlate);
                    break;
                case "Vertical Edge Contouring":
                    this.recognizedBitmap = plateDetector.edgeContourRecognitionMethod(this.videoBitmap, this.distanceFromPlate);
                    break;
            }
            iView.setImageBitmap(this.recognizedBitmap);
            this.characterRecognition.getTextFromImage(this.recognizedBitmap,this,textViewVideoRecognitionOutputPreRecorded,textViewVideoRecognitionOutputPreRecorded,textViewVideoRecognitionOutputPreRecorded,textViewVideoRecognitionOutputPreRecorded,1);

            findViewById(R.id.sharePreRecordedVideoImageView).setVisibility(View.VISIBLE);
            findViewById(R.id.plateInfoPreRecordedVideoImageView).setVisibility(View.VISIBLE);
            findViewById(R.id.pickRecordedVideoImageView).setVisibility(View.VISIBLE);
            findViewById(R.id.recognizePreRecordedVideoImageView).setVisibility(View.INVISIBLE);


        }

    }

    public void shareRecognizedImagePreRecorded(View v){
        if (ContextCompat.checkSelfPermission(RecognizeOnPreRecordedVideoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(RecognizeOnPreRecordedVideoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestSharingPermissionsImage();
        } else {
            if (this.recognizedBitmap != null) {
                if ((this.textViewVideoRecognitionOutputPreRecorded.getText().toString()).equals(""))
                    prepareFileToShare(this.recognizedBitmap, "Recognizer_license_plate");
                else
                    prepareFileToShare(this.recognizedBitmap, "Plate nr: " + (this.textViewVideoRecognitionOutputPreRecorded.getText().toString()));
            }
        }


    }


    private void prepareFileToShare (Bitmap bitmap,String filename) {

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

    public void showPolishPlateDetailedInfoPreRecorded(View v){
        if(this.textViewVideoRecognitionOutputPreRecorded.getText().toString()!=null && !this.textViewVideoRecognitionOutputPreRecorded.getText().toString().equals("")){
            String polishPlateDetails = plateDetailsInformator.getPolishPlateDetailedInfo(this.textViewVideoRecognitionOutputPreRecorded.getText().toString());
            if (!polishPlateDetails.equals(""))
                Toast.makeText(this,polishPlateDetails,Toast.LENGTH_LONG).show();
        }
    }


    private void requestSharingPermissionsImage(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE))   {
            new AlertDialog.Builder(this)
                    .setTitle("Storage Access permission needed")
                    .setMessage("Do You allow Recognizer application to use the external storage? This permission is necessary for sharing.")
                    .setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(RecognizeOnPreRecordedVideoActivity.this, SHARING_PERMISSIONS,SHARING_PERMISSIONS_CODE);
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
        if (requestCode == SHARING_PERMISSIONS_CODE) {
            if (grantResults.length > 0) {
                boolean permissions_flag = true;
                for (int i = 0; i <  grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                        permissions_flag = false;
                }
                if (permissions_flag) {
                    if (this.recognizedBitmap != null) {
                        if ((this.textViewVideoRecognitionOutputPreRecorded.getText().toString()).equals(""))
                            prepareFileToShare(this.recognizedBitmap, "Recognizer_license_plate");
                        else
                            prepareFileToShare(this.recognizedBitmap, "Plate nr: " + (this.textViewVideoRecognitionOutputPreRecorded.getText().toString()));
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

}
