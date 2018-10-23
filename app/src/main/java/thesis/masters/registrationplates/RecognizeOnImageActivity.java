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

import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

public class RecognizeOnImageActivity extends AppCompatActivity {
    private static final int GALLERY_PICTURE = 101;
    private static final int CAMERA_PHOTO = 202;

    ImageView imageView;
    Uri imageURI;
    Uri photoURI;
    Bitmap imageBitmap ,recognizedBitmap;
    String liveGallerySelection;
    private boolean oldPlatesMode;
    private int numberOfPlates;
    private int distanceFromPlate;
    //String mCurrentPhotoPath;
    TextView textViewRecognitionOutput, textViewRecognitionOutput2, textViewRecognitionOutput3, textViewRecognitionOutput4;
    ImageView plateImageView,plateImageView2,plateImageView3,plateImageView4;
    CharacterRecognition characterRecognition;
    ViewAdjuster viewAdjuster;
    PlateDetector plateDetector;
    private String recognitionMethod;
    private final int ALL_PERMISSIONS_CODE = 444;
    private String[] ALL_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize_on_image);
        this.characterRecognition = new CharacterRecognition();
        this.plateDetector = new PlateDetector();
        this.viewAdjuster = new ViewAdjuster();
        this.imageView = findViewById(R.id.imageToBeRecognizedImageView);
        this.textViewRecognitionOutput = findViewById(R.id.recognitionImageOutputTextView);
        this.textViewRecognitionOutput2 = findViewById(R.id.recognitionImageOutputTextView2);
        this.textViewRecognitionOutput3 = findViewById(R.id.recognitionImageOutputTextView3);
        this.textViewRecognitionOutput4 = findViewById(R.id.recognitionImageOutputTextView4);
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
            pickImageButton.setText(R.string.buttonTakeAPhoto);
        else
            pickImageButton.setText(R.string.buttonPickImageFromGallery);

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
                    photoURI = getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
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
                    photoURI = getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
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

        if (data != null && resultCode == RESULT_OK && requestCode == GALLERY_PICTURE) {
            this.imageURI = data.getData();

            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageURI);
                if (imageBitmap.getHeight()>ViewAdjuster.MAX_ALLOWED_BITMAP_HEIGHT || imageBitmap.getWidth()>ViewAdjuster.MAX_ALLOWED_BITMAP_WIDTH)
                    imageBitmap = viewAdjuster.adjustBitmap(imageBitmap);
                if (imageBitmap.getByteCount() > viewAdjuster.MAX_ALLOWED_BYTE_SIZE)
                    this.imageBitmap = viewAdjuster.scaleBitmap(this.imageBitmap);

                this.imageView.setImageBitmap(imageBitmap);
                Toast.makeText(getApplicationContext(),Integer.toString(this.imageBitmap.getByteCount()), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (resultCode == RESULT_OK && requestCode == CAMERA_PHOTO) {
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
                if (imageBitmap.getHeight()>ViewAdjuster.MAX_ALLOWED_BITMAP_HEIGHT || imageBitmap.getWidth()>ViewAdjuster.MAX_ALLOWED_BITMAP_WIDTH)
                    imageBitmap = viewAdjuster.adjustBitmap(imageBitmap);
                if (imageBitmap.getByteCount() > viewAdjuster.MAX_ALLOWED_BYTE_SIZE)
                    this.imageBitmap = viewAdjuster.scaleBitmap(this.imageBitmap);

                this.imageView.setImageBitmap(imageBitmap);
                Toast.makeText(getApplicationContext(),Integer.toString(this.imageBitmap.getByteCount()), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




/*    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  *//* prefix *//*
                ".jpg",         *//* suffix *//*
                storageDir      *//* directory *//*
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }*/


    public void recognizeTestMethod1(View view) {

        if (this.imageBitmap != null)
        {
            switch (this.recognitionMethod) {
                case "Morphological Transformations":
                    this.recognizedBitmap = plateDetector.morphologicalTransformationsRecognitionMethod(this.imageBitmap,this.distanceFromPlate);
                    break;
                case "Median Center of Moments":
                    this.recognizedBitmap = plateDetector.medianCenterOfMomentRecognitionMethod(this.imageBitmap,this.distanceFromPlate);
                    break;
                case "Vertical Edge Contouring":
                    this.recognizedBitmap = plateDetector.edgeContourRecognitionMethod(this.imageBitmap,this.distanceFromPlate);
                    break;
            }
            imageView.setImageBitmap(this.recognizedBitmap);
            int recognizedPlates = this.characterRecognition.getTextFromImage(imageBitmap, getApplicationContext(), this.textViewRecognitionOutput, this.textViewRecognitionOutput2, this.textViewRecognitionOutput3, this.textViewRecognitionOutput4);
            this.viewAdjuster.showPlateImageViewsAfterRecognition(recognizedPlates, this.plateImageView, this.plateImageView2, this.plateImageView3, this.plateImageView4);

        }
    }
}

  /*  public void convertToGray(View view){

        Mat Rgba = new Mat();
        Mat grayMat = new Mat();
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inDither = false;
        o.inSampleSize = 4;

        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();

        grayBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565);

        //bitmap to MAT

        Utils.bitmapToMat(imageBitmap,Rgba);

        //get it gray

        Imgproc.cvtColor(Rgba,grayMat,Imgproc.COLOR_RGB2GRAY);

        Utils.matToBitmap(grayMat,grayBitmap);

        imageView.setImageBitmap(grayBitmap);


    }
}*/


