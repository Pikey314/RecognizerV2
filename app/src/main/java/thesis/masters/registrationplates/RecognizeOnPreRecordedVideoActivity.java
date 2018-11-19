package thesis.masters.registrationplates;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class RecognizeOnPreRecordedVideoActivity extends AppCompatActivity{

    private static final int SELECTED_VIDEO = 102;
    private String videoPath;
    private Bitmap videoBitmap;
    private Bitmap recognizedBitmap;
    private MediaMetadataRetriever videoFromGallery;
    private VideoView vView;
    private ImageView iView;
    private int distanceFromPlate;
    private PlateDetector plateDetector;
    private String recognitionMethod;
    private SeekBar seekBarDistanceFromPlatePreRecorded;
    private TextView textViewVideoRecognitionOutputPreRecorded;
    private ShareExecuter shareExecuter;
    private CharacterRecognition characterRecognition;
    private PlateDetailsInformator plateDetailsInformator;
    private final int MEDIA_ACCESS_CODE = 643;
    private String[] MEDIA_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize_on_pre_recorded_video);
        this.vView = findViewById(R.id.vview);
        this.iView = findViewById(R.id.iview);
        this.textViewVideoRecognitionOutputPreRecorded = findViewById(R.id.recognitionPreRecordedVideoOutputTextView);
        this.plateDetector = new PlateDetector();
        this.shareExecuter = new ShareExecuter();
        this.plateDetailsInformator = new PlateDetailsInformator(this);
        this.characterRecognition = new CharacterRecognition();
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
        if (ContextCompat.checkSelfPermission(RecognizeOnPreRecordedVideoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(RecognizeOnPreRecordedVideoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestMediaPermissions();
        }
        else {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, SELECTED_VIDEO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == RESULT_OK && requestCode == SELECTED_VIDEO) {
            Uri videoURI = data.getData();
                try {
                    this.vView.setVideoURI(videoURI);
                    this.vView.start();
                    this.videoPath = getRealPathFromURI(this, videoURI);
                    this.videoFromGallery = new MediaMetadataRetriever();
                    this.videoFromGallery.setDataSource(this.videoPath);
                    findViewById(R.id.pickRecordedVideoImageView).setVisibility(View.INVISIBLE);
                    findViewById(R.id.recognizePreRecordedVideoImageView).setVisibility(View.VISIBLE);
                    findViewById(R.id.sharePreRecordedVideoImageView).setVisibility(View.INVISIBLE);
                    findViewById(R.id.plateInfoPreRecordedVideoImageView).setVisibility(View.INVISIBLE);
                    this.textViewVideoRecognitionOutputPreRecorded.setText("");
                } catch (Exception e) {
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
        if (this.vView != null && this.videoFromGallery != null) {
            int framePosition = this.vView.getCurrentPosition();
            this.videoBitmap = videoFromGallery.getFrameAtTime(framePosition);
            switch (this.recognitionMethod) {
                case "Morphological Transformations":
                    this.recognizedBitmap = this.plateDetector.morphologicalTransformationsRecognitionMethod(this.videoBitmap, this.distanceFromPlate);
                    break;
                case "Median Center of Moments":
                    this.recognizedBitmap = this.plateDetector.medianCenterOfMomentRecognitionMethod(this.videoBitmap, this.distanceFromPlate);
                    break;
                case "Vertical Edge Contouring":
                    this.recognizedBitmap = this.plateDetector.edgeContourRecognitionMethod(this.videoBitmap, this.distanceFromPlate);
                    break;
            }
            this.iView.setImageBitmap(this.recognizedBitmap);
            this.characterRecognition.getTextFromImage(this.videoBitmap,this,this.textViewVideoRecognitionOutputPreRecorded,this.textViewVideoRecognitionOutputPreRecorded,this.textViewVideoRecognitionOutputPreRecorded,this.textViewVideoRecognitionOutputPreRecorded,1);
            findViewById(R.id.sharePreRecordedVideoImageView).setVisibility(View.VISIBLE);
            findViewById(R.id.plateInfoPreRecordedVideoImageView).setVisibility(View.VISIBLE);
            findViewById(R.id.pickRecordedVideoImageView).setVisibility(View.VISIBLE);
            findViewById(R.id.recognizePreRecordedVideoImageView).setVisibility(View.INVISIBLE);


        }

    }

    public void shareRecognizedImagePreRecorded(View v){
        if (ContextCompat.checkSelfPermission(RecognizeOnPreRecordedVideoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(RecognizeOnPreRecordedVideoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestMediaPermissions();
        } else {
            if (this.recognizedBitmap != null) {
                if ((this.textViewVideoRecognitionOutputPreRecorded.getText().toString()).equals(""))
                    prepareFileToSharePreRecordedVideoActivity(this.recognizedBitmap, "Recognizer_license_plate");
                else
                    prepareFileToSharePreRecordedVideoActivity(this.recognizedBitmap, "Plate nr: " + (this.textViewVideoRecognitionOutputPreRecorded.getText().toString()));
            }
        }
    }

    private void prepareFileToSharePreRecordedVideoActivity (Bitmap bitmap,String filename) {
        Intent share = this.shareExecuter.prepareFileToShare(bitmap,filename);
        startActivity(Intent.createChooser(share, "Recognizer share license plate"));
    }

    public void showPolishPlateDetailedInfoPreRecorded(View v){
        if(this.textViewVideoRecognitionOutputPreRecorded.getText().toString()!=null && !this.textViewVideoRecognitionOutputPreRecorded.getText().toString().equals("")){
            String polishPlateDetails = plateDetailsInformator.getPolishPlateDetailedInfo(this.textViewVideoRecognitionOutputPreRecorded.getText().toString());
            if (!polishPlateDetails.equals(""))
                Toast.makeText(this,polishPlateDetails,Toast.LENGTH_LONG).show();
        }
    }


    private void requestMediaPermissions(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE))   {
            new AlertDialog.Builder(this)
                    .setTitle("Media access permission needed")
                    .setMessage("Do You allow Recognizer application to use the external storage? This permission is necessary for gallery-video recognition.")
                    .setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(RecognizeOnPreRecordedVideoActivity.this, MEDIA_PERMISSIONS,MEDIA_ACCESS_CODE);
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
            ActivityCompat.requestPermissions(this, MEDIA_PERMISSIONS,MEDIA_ACCESS_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MEDIA_ACCESS_CODE) {
            if (grantResults.length > 0) {
                boolean permissions_flag = true;
                for (int i = 0; i <  grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                        permissions_flag = false;
                }
                if (permissions_flag) {
                    Toast.makeText(this,"Media permissions granted",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,"Permissions not granted",Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(this,"Permissions not granted",Toast.LENGTH_SHORT).show();
            }
        }
    }

}
