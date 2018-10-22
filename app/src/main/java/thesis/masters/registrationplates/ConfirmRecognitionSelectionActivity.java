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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class ConfirmRecognitionSelectionActivity extends AppCompatActivity {
    private TextView confirmationOfSelectionTextView;
    private Button selectResourceButton;
    private SeekBar distanceFromPlateSeekBar;
    private Switch oldPlatesSwitch;
    private RadioGroup amountOfPlatesRadioGroup;
    private String selection = null;
    private String liveOrGallery;
    private String recognitionMethod;



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
        this.distanceFromPlateSeekBar = findViewById(R.id.plateDistanceSeekBar);
        this.oldPlatesSwitch = findViewById(R.id.oldPlatesSwitch);
        this.amountOfPlatesRadioGroup = findViewById(R.id.amountOfPlatesRadioGroup);
        //this.resourceForRecognitionImageView = (ImageView) findViewById(R.id.resourceForRecognitionImageView);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            liveOrGallery = extras.getString("liveOrGallerySelectionText");
            String videOrPhoto = extras.getString("videoOrPhotoSelectionText");
            this.recognitionMethod = extras.getString("spinnerValue");
            this.confirmationOfSelectionTextView.setText("Increase accuracy for " + this.recognitionMethod + " method");
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
            //distanceFromPlate = 0,1,2,3,4
            int distanceFromPlate = this.distanceFromPlateSeekBar.getProgress();
            //oldPlatesMode = false/true
            boolean oldPlatesMode = this.oldPlatesSwitch.isChecked();
            //amountOfPlates = 1,2
            int amountOfPlates = getRadioButtonValue(this.amountOfPlatesRadioGroup);
            switch (this.selection) {
                case "LiveVideo":
                    Log.v("Button Selection", "LiveVideo");
                    recognizeOnLiveVideoActivity(distanceFromPlate,oldPlatesMode,amountOfPlates);
                    break;
                case "Image":
                    Log.v("Button Selection", "GalleryPhoto");
                    recognizeOnImageActivity(distanceFromPlate,oldPlatesMode,amountOfPlates);
                    break;
                case "GalleryVideo":
                    Log.v("Button Selection", "GalleryVideo");
                    recognizeOnPreRecordedVideoActivity(distanceFromPlate,oldPlatesMode,amountOfPlates);
                    break;
                default:
                    Log.v("Problem with code", "Push button problem");
                    break;
            }
        }
    }

    private int getRadioButtonValue(RadioGroup radioGroup){
        String radioButtonTextValue = ((RadioButton) findViewById( radioGroup.getCheckedRadioButtonId())).getText().toString();
        if (radioButtonTextValue.equals(getString(R.string.one)))
            return 1;
        else
            return 2;
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

    public void recognizeOnLiveVideoActivity(int distanceFromPlate ,boolean oldPlatesMode, int amountOfPlates) {
        Intent recognizeOnLiveVideoIntent = new Intent(this, RecognizeOnLiveVideoActivity.class);
        recognizeOnLiveVideoIntent.putExtra("recognitionMethod",this.recognitionMethod);
        recognizeOnLiveVideoIntent.putExtra("distanceFromPlate",distanceFromPlate);
        recognizeOnLiveVideoIntent.putExtra("oldPlatesMode",oldPlatesMode);
        recognizeOnLiveVideoIntent.putExtra("amountOfPlates",amountOfPlates);
        startActivity(recognizeOnLiveVideoIntent);
    }

    public void recognizeOnImageActivity(int distanceFromPlate ,boolean oldPlatesMode, int amountOfPlates) {
        Intent recognizeOnImageIntent = new Intent(this, RecognizeOnImageActivity.class);
        recognizeOnImageIntent.putExtra("recognitionMethod",this.recognitionMethod);
        recognizeOnImageIntent.putExtra("liveGallerySelection",liveOrGallery);
        recognizeOnImageIntent.putExtra("distanceFromPlate",distanceFromPlate);
        recognizeOnImageIntent.putExtra("oldPlatesMode",oldPlatesMode);
        recognizeOnImageIntent.putExtra("amountOfPlates",amountOfPlates);
        startActivity(recognizeOnImageIntent);
    }

    public void recognizeOnPreRecordedVideoActivity(int distanceFromPlate ,boolean oldPlatesMode, int amountOfPlates) {
        Intent recognizeOnPreRecordedVideoIntent = new Intent(this, RecognizeOnPreRecordedVideoActivity.class);
        recognizeOnPreRecordedVideoIntent.putExtra("recognitionMethod",this.recognitionMethod);
        recognizeOnPreRecordedVideoIntent.putExtra("distanceFromPlate",distanceFromPlate);
        recognizeOnPreRecordedVideoIntent.putExtra("oldPlatesMode",oldPlatesMode);
        recognizeOnPreRecordedVideoIntent.putExtra("amountOfPlates",amountOfPlates);
        startActivity(recognizeOnPreRecordedVideoIntent);
    }


}