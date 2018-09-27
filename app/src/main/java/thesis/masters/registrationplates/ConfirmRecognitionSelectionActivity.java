package thesis.masters.registrationplates;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
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
    private ImageView resourceForRecognitionImageView;
    private static final int SELECTED_PICTURE = 1;
    private String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_recognition_selection);
        this.confirmationOfSelectionTextView = (TextView) findViewById(R.id.confirmationOfRecognitionSelectionTextView);
        this.selectResourceButton = (Button) findViewById(R.id.selectResourceButton);
        this.resourceForRecognitionImageView = (ImageView) findViewById(R.id.resourceForRecognitionImageView);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String liveOrGallery = extras.getString("liveOrGallerySelectionText");
            String videOrPhoto = extras.getString("videoOrPhotoSelectionText");
            String recognitionMethod = extras.getString("spinnerValue");
            this.confirmationOfSelectionTextView.setText(liveOrGallery + videOrPhoto + recognitionMethod);
            switch (liveOrGallery) {
                case "Live":
                    if (videOrPhoto.equals("Video")) {
                        this.selectResourceButton.setBackgroundResource(R.drawable.video_live_icon);
                        this.selection = "LiveVideo";
                    } else {
                        this.selectResourceButton.setBackgroundResource(R.drawable.photo_live_icon);
                        this.selection = "LivePhoto";
                    }
                    break;
                case "Gallery":
                    if (videOrPhoto.equals("Video")) {
                        this.selectResourceButton.setBackgroundResource(R.drawable.video_gallery_icon);
                        this.selection = "GalleryPhoto";

                    } else {
                        this.selectResourceButton.setBackgroundResource(R.drawable.photo_gallery_icon);
                        this.selection = "GalleryPhoto";

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
                    break;
                case "LivePhoto":
                    Log.v("Button Selection", "LivePhoto");
                    break;
                case "GalleryPhoto":
                    Log.v("Button Selection", "GalleryPhoto");
                    chooseImageFromGallery();
                    break;
                case "GalleryVideo":
                    Log.v("Button Selection", "GalleryVideo");
                    break;
                default:
                    Log.v("Problem with code", "Push button problem");
                    break;
            }
        }
    }

    public void chooseImageFromGallery() {
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
    }
}