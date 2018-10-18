package thesis.masters.registrationplates;

import android.Manifest;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ConfirmRecognitionSelectionActivity extends AppCompatActivity {
    private TextView confirmationOfSelectionTextView;
    private Button selectResourceButton;
    private String selection = null;
    String liveOrGallery;


    /*private ImageView resourceForRecognitionImageView;
    private static final int SELECTED_PICTURE = 1;
    private String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_recognition_selection);
        this.confirmationOfSelectionTextView = (TextView) findViewById(R.id.confirmationOfRecognitionSelectionTextView);
        this.selectResourceButton = (Button) findViewById(R.id.selectResourceButton);
        //this.resourceForRecognitionImageView = (ImageView) findViewById(R.id.resourceForRecognitionImageView);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            liveOrGallery = extras.getString("liveOrGallerySelectionText");
            String videOrPhoto = extras.getString("videoOrPhotoSelectionText");
            String recognitionMethod = extras.getString("spinnerValue");
            this.confirmationOfSelectionTextView.setText("Increase accuracy for " + recognitionMethod + " method");
            switch (liveOrGallery) {
                case "LIVE":
                    if (videOrPhoto.equals("VIDEO")) {
                        this.selectResourceButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.video_live_icon,0,0,0);
                        this.selection = "LiveVideo";
                    } else {
                        this.selectResourceButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.photo_live_icon,0,0,0);
                        this.selection = "Image";
                    }
                    break;
                case "GALLERY":
                    if (videOrPhoto.equals("VIDEO")) {
                        this.selectResourceButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.video_gallery_icon,0,0,0);
                        this.selection = "GalleryVideo";

                    } else {
                        this.selectResourceButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.photo_gallery_icon,0,0,0);
                        this.selection = "Image";

                    }
                    break;

                default:
                    Log.d("Problem with code", "No live or gallery selection");
                    break;
            }
        }
    }

    public void pickResourceForRecognitionWithButton(View view) {
        if (this.selection != null) {
            switch (this.selection) {
                case "LiveVideo":
                    Log.v("Button Selection", "LiveVideo");
                    recognizeOnLiveVideoActivity();
                    break;
                case "Image":
                    Log.v("Button Selection", "GalleryPhoto");
                    recognizeOnImageActivity();
                    break;
                case "GalleryVideo":
                    Log.v("Button Selection", "GalleryVideo");
                    recognizeOnPreRecordedVideoActivity();
                    break;
                default:
                    Log.v("Problem with code", "Push button problem");
                    break;
            }
        }
    }

    /*public void chooseImageFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, SELECTED_PICTURE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == SELECTED_PICTURE) {
            Uri imageURI = data.getData();
            this.resourceForRecognitionImageView.setImageURI(imageURI);
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            this.resourceForRecognitionImageView.setColorFilter(filter);
        }
    }*/

    public void recognizeOnLiveVideoActivity() {
        Intent recognizeOnLiveVideoIntent = new Intent(this, RecognizeOnLiveVideoActivity.class);
        startActivity(recognizeOnLiveVideoIntent);
    }

    public void recognizeOnImageActivity() {
        Intent recognizeOnImageIntent = new Intent(this, RecognizeOnImageActivity.class);
        recognizeOnImageIntent.putExtra("liveGallerySelection",liveOrGallery);
        startActivity(recognizeOnImageIntent);
    }

    public void recognizeOnPreRecordedVideoActivity() {
        Intent recognizeOnPreRecordedVideoIntent = new Intent(this, RecognizeOnPreRecordedVideoActivity.class);
        startActivity(recognizeOnPreRecordedVideoIntent);
    }


}