package thesis.masters.registrationplates;




import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
    //String mCurrentPhotoPath;
    TextView textViewRecognitionOutput, textViewRecognitionOutput2, textViewRecognitionOutput3, textViewRecognitionOutput4;
    ImageView plateImageView,plateImageView2,plateImageView3,plateImageView4;
    CharacterRecognition characterRecognition;
    ViewAdjuster viewAdjuster;
    PlateDetector plateDetector;


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
        if (extras != null)
            this.liveGallerySelection = extras.getString("liveGallerySelection");
        else
            this.liveGallerySelection = "Gallery";

        Button pickImageButton = findViewById(R.id.pickImageButton);
        if (this.liveGallerySelection.equals("Live"))
            pickImageButton.setText(R.string.buttonTakeAPhoto);
        else
            pickImageButton.setText(R.string.buttonPickImageFromGallery);

    }




    public void pickImageFromGallery(View view) {
        switch (this.liveGallerySelection) {
            case "Gallery":
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_PICTURE);
                break;
            case "Live":
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                photoURI = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);


                startActivityForResult(cameraIntent, CAMERA_PHOTO);


                break;
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

            //this.recognizedBitmap = plateDetector.testMethod(this.imageBitmap);
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


