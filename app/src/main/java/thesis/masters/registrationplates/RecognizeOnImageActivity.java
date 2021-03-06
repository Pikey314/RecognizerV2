package thesis.masters.registrationplates;




import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import java.io.IOException;

public class RecognizeOnImageActivity extends AppCompatActivity {
    private static final int GALLERY_PICTURE = 101;
    private static final int CAMERA_PHOTO = 202;

    private ImageView imageView;
    private Uri imageURI;
    private Uri photoURI;
    private Bitmap imageBitmap ,recognizedBitmap;
    private String liveGallerySelection;
    private boolean oldPlatesMode;
    private int numberOfPlates;
    private int distanceFromPlate;
    private TextView textViewRecognitionOutput, textViewRecognitionOutput2, textViewRecognitionOutput3, textViewRecognitionOutput4;
    private ImageView plateImageView,plateImageView2,plateImageView3,plateImageView4;
    private CharacterRecognition characterRecognition;
    private ViewAdjuster viewAdjuster;
    private PlateDetailsInformator plateDetailsInformator;
    private ShareExecuter shareExecuter;
    private PlateDetector plateDetector;
    private String recognitionMethod;
    private final int ALL_PERMISSIONS_CODE = 444;
    private String[] ALL_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private final int SHARING_PERMISSIONS_CODE = 543;
    private String[] SHARING_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize_on_image);
        this.characterRecognition = new CharacterRecognition();
        this.plateDetector = new PlateDetector();
        this.shareExecuter = new ShareExecuter();
        this.viewAdjuster = new ViewAdjuster();
        this.imageView = findViewById(R.id.imageToBeRecognizedImageView);
        this.textViewRecognitionOutput = findViewById(R.id.recognitionImageOutputTextView);
        this.textViewRecognitionOutput2 = findViewById(R.id.recognitionImageOutputTextView2);
        this.textViewRecognitionOutput3 = findViewById(R.id.recognitionImageOutputTextView3);
        this.textViewRecognitionOutput4 = findViewById(R.id.recognitionImageOutputTextView4);
        this.plateDetailsInformator = new PlateDetailsInformator(this);
        this.plateImageView = findViewById(R.id.plateStaticImageView);
        this.plateImageView2 = findViewById(R.id.plateStaticImageView2);
        this.plateImageView3 = findViewById(R.id.plateStaticImageView3);
        this.plateImageView4 = findViewById(R.id.plateStaticImageView4);
        this.viewAdjuster.hideAllPlateImageViews(this.plateImageView, this.plateImageView2, this.plateImageView3, this.plateImageView4);
        this.viewAdjuster.hideAllPlateOutputTextViews(this.textViewRecognitionOutput,this.textViewRecognitionOutput2 ,this.textViewRecognitionOutput3 ,this.textViewRecognitionOutput4);
        OpenCVLoader.initDebug();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.liveGallerySelection = extras.getString("liveGallerySelection");
            this.recognitionMethod = extras.getString("recognitionMethod");
            this.distanceFromPlate = extras.getInt("distanceFromPlate");
            this.oldPlatesMode = extras.getBoolean("oldPlatesMode");
            this.numberOfPlates = extras.getInt("amountOfPlates");
        }
        else {
            throw new RuntimeException("Problem with settings");
        }
        Button pickImageButton = findViewById(R.id.pickImageButton);
        if (this.liveGallerySelection.equals("LIVE"))
            pickImageButton.setBackgroundResource(R.drawable.takephotobutton);
        else
            pickImageButton.setBackgroundResource(R.drawable.pickimagebutton);

    }



    private void requestAllPermissions(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE))   {
            new AlertDialog.Builder(this)
                    .setTitle("Camera and Storage Access permissions needed")
                    .setMessage("Do You allow Recognizer application to use the camera and the external storage? Those permissions are necessary for live image mode.")
                    .setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(RecognizeOnImageActivity.this, ALL_PERMISSIONS,ALL_PERMISSIONS_CODE);
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
            ActivityCompat.requestPermissions(this, ALL_PERMISSIONS,ALL_PERMISSIONS_CODE);
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
                            ActivityCompat.requestPermissions(RecognizeOnImageActivity.this, SHARING_PERMISSIONS,SHARING_PERMISSIONS_CODE);
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
        if (requestCode == ALL_PERMISSIONS_CODE) {
            if (grantResults.length > 0) {
                boolean permissions_flag = true;
                for (int i = 0; i <  grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                        permissions_flag = false;
                }
                if (permissions_flag) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "Recognizer App Photo");
                    values.put(MediaStore.Images.Media.DESCRIPTION, "Live image recognition resource photo");
                    this.photoURI = getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, this.photoURI);
                    startActivityForResult(cameraIntent, CAMERA_PHOTO);
                    Toast.makeText(this,"All permissions granted",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,"Not all permissions granted",Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(this,"Not all permissions granted",Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == SHARING_PERMISSIONS_CODE) {
            if (grantResults.length > 0) {
                boolean permissions_flag = true;
                for (int i = 0; i <  grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                        permissions_flag = false;
                }
                if (permissions_flag) {
                    if (this.recognizedBitmap != null) {
                        if ((this.textViewRecognitionOutput.getText().toString()).equals(""))
                            prepareFileToShareImageActivity(this.recognizedBitmap, "Recognizer_license_plate");
                        else
                            prepareFileToShareImageActivity(this.recognizedBitmap, "Plate nr: " + (this.textViewRecognitionOutput.getText().toString()));
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


    public void pickImageFromGallery(View view) {
        switch (this.liveGallerySelection) {
            case "GALLERY":
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_PICTURE);
                break;
            case "LIVE":
                if (ContextCompat.checkSelfPermission(RecognizeOnImageActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(RecognizeOnImageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(RecognizeOnImageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    requestAllPermissions();
                } else {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "Recognizer App Photo");
                    values.put(MediaStore.Images.Media.DESCRIPTION, "Live image recognition resource photo");
                    this.photoURI = getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, this.photoURI);
                    startActivityForResult(cameraIntent, CAMERA_PHOTO);
                    break;
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.viewAdjuster.hideAllPlateImageViews(this.plateImageView, this.plateImageView2, this.plateImageView3, this.plateImageView4);
        this.viewAdjuster.hideAllPlateOutputTextViews(this.textViewRecognitionOutput,this.textViewRecognitionOutput2 ,this.textViewRecognitionOutput3 ,this.textViewRecognitionOutput4);
        findViewById(R.id.plateInfoImageView).setVisibility(View.INVISIBLE);
        findViewById(R.id.shareImageView).setVisibility(View.INVISIBLE);
        findViewById(R.id.recognizeImageButton).setVisibility(View.VISIBLE);
        if (data != null && resultCode == RESULT_OK && requestCode == GALLERY_PICTURE) {
            this.imageURI = data.getData();
            try {
                this.imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), this.imageURI);
                if (this.imageBitmap.getHeight()>ViewAdjuster.MAX_ALLOWED_BITMAP_HEIGHT || this.imageBitmap.getWidth()>ViewAdjuster.MAX_ALLOWED_BITMAP_WIDTH)
                    this.imageBitmap = this.viewAdjuster.adjustBitmap(this.imageBitmap);
                if (this.imageBitmap.getByteCount() > this.viewAdjuster.MAX_ALLOWED_BYTE_SIZE)
                    this.imageBitmap = this.viewAdjuster.scaleBitmap(this.imageBitmap);

                this.imageView.setImageBitmap(this.imageBitmap);
                Toast.makeText(getApplicationContext(),Integer.toString(this.imageBitmap.getByteCount()), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (resultCode == RESULT_OK && requestCode == CAMERA_PHOTO) {
            try {
                this.imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), this.photoURI);
                if (this.imageBitmap.getHeight()>ViewAdjuster.MAX_ALLOWED_BITMAP_HEIGHT || this.imageBitmap.getWidth()>ViewAdjuster.MAX_ALLOWED_BITMAP_WIDTH)
                    this.imageBitmap = viewAdjuster.adjustBitmap(imageBitmap);
                if (this.imageBitmap.getByteCount() > viewAdjuster.MAX_ALLOWED_BYTE_SIZE)
                    this.imageBitmap = viewAdjuster.scaleBitmap(this.imageBitmap);

                this.imageView.setImageBitmap(imageBitmap);
                Toast.makeText(getApplicationContext(),Integer.toString(this.imageBitmap.getByteCount()), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void recognizeOnButtonClick(View view) {
        if (this.imageBitmap != null)
        {
            if (this.oldPlatesMode || this.numberOfPlates > 1)
                this.recognizedBitmap = plateDetector.morphologicalTransformationsRecognitionMethod(this.imageBitmap,this.distanceFromPlate);
            else {
                switch (this.recognitionMethod) {
                    case "Morphological Transformations":
                        this.recognizedBitmap = plateDetector.morphologicalTransformationsRecognitionMethod(this.imageBitmap, this.distanceFromPlate);
                        break;
                    case "Median Center of Moments":
                        this.recognizedBitmap = plateDetector.medianCenterOfMomentRecognitionMethod(this.imageBitmap, this.distanceFromPlate);
                        break;
                    case "Vertical Edge Contouring":
                        this.recognizedBitmap = plateDetector.edgeContourRecognitionMethod(this.imageBitmap, this.distanceFromPlate);
                        break;
                }
            }
            this.imageView.setImageBitmap(this.recognizedBitmap);
            int recognizedPlates = this.characterRecognition.getTextFromImage(this.imageBitmap, getApplicationContext(), this.textViewRecognitionOutput, this.textViewRecognitionOutput2, this.textViewRecognitionOutput3, this.textViewRecognitionOutput4,this.numberOfPlates);
            this.viewAdjuster.showPlateImageViewsAfterRecognition(recognizedPlates, this.plateImageView, this.plateImageView2, this.plateImageView3, this.plateImageView4);
            findViewById(R.id.recognizeImageButton).setVisibility(View.INVISIBLE);
            findViewById(R.id.shareImageView).setVisibility(View.VISIBLE);
            if (this.numberOfPlates == 1 && this.textViewRecognitionOutput.getText().toString()!=null && !this.textViewRecognitionOutput.getText().toString().equals(""))
                findViewById(R.id.plateInfoImageView).setVisibility(View.VISIBLE);
        }
    }

    public void shareRecognizedImage(View v){
        if (ContextCompat.checkSelfPermission(RecognizeOnImageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(RecognizeOnImageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestSharingPermissionsImage();
        } else {
            if (this.recognizedBitmap != null) {
                if ((this.textViewRecognitionOutput.getText().toString()).equals(""))
                    prepareFileToShareImageActivity(this.recognizedBitmap, "Recognizer_license_plate");
                else
                    prepareFileToShareImageActivity(this.recognizedBitmap, "Plate nr: " + (this.textViewRecognitionOutput.getText().toString()));
            }
        }
    }


    private void prepareFileToShareImageActivity (Bitmap bitmap,String filename) {
        Intent share = this.shareExecuter.prepareFileToShare(bitmap,filename);
        startActivity(Intent.createChooser(share, "Recognizer share license plate"));
    }

    public void showPolishPlateDetailedInfo(View v){
        if(this.textViewRecognitionOutput.getText().toString()!=null && !this.textViewRecognitionOutput.getText().toString().equals("")){
            String polishPlateDetails = plateDetailsInformator.getPolishPlateDetailedInfo(this.textViewRecognitionOutput.getText().toString());
            if (!polishPlateDetails.equals(""))
                Toast.makeText(this,polishPlateDetails,Toast.LENGTH_LONG).show();
        }
    }

}
