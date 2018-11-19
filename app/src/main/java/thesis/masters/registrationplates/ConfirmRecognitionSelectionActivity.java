package thesis.masters.registrationplates;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class ConfirmRecognitionSelectionActivity extends AppCompatActivity {
    private TextView confirmationOfSelectionTextView;
    private Button selectResourceButton;
    private SeekBar distanceFromPlateSeekBar;
    private Switch oldPlatesSwitch;
    private RadioGroup amountOfPlatesRadioGroup;
    private String selection = null;
    private String liveOrGallery;
    private String recognitionMethod;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_recognition_selection);
        this.confirmationOfSelectionTextView = findViewById(R.id.confirmationOfRecognitionSelectionTextView);
        this.selectResourceButton = findViewById(R.id.selectResourceButton);
        this.distanceFromPlateSeekBar = findViewById(R.id.plateDistanceSeekBar);
        this.oldPlatesSwitch = findViewById(R.id.oldPlatesSwitch);

        this.amountOfPlatesRadioGroup = findViewById(R.id.amountOfPlatesRadioGroup);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.liveOrGallery = extras.getString("liveOrGallerySelectionText");
            String videOrPhoto = extras.getString("videoOrPhotoSelectionText");
            this.recognitionMethod = extras.getString("spinnerValue");
            this.confirmationOfSelectionTextView.setText("Increase accuracy for " + this.recognitionMethod + " method");
            switch (this.liveOrGallery) {
                case "LIVE":
                        this.selectResourceButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.photo_live_icon,0,0,0);
                        this.selection = "Image";
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
        this.oldPlatesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!recognitionMethod.equals("Morphological Transformations")) {
                    if (b)
                        Toast.makeText(getApplicationContext(),"Old plates mode is supported by Morphological Processing.\nSwitching method to Morphological Transformations.",Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getApplicationContext(),"Restoring settings.",Toast.LENGTH_SHORT).show();
                }
            }
        });
        this.amountOfPlatesRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (!recognitionMethod.equals("Morphological Transformations")) {
                    if (i == R.id.twoPlatesRadioButton)
                        Toast.makeText(getApplicationContext(),"Higher number of plates is supported by Morphological Processing.\nSwitching method to Morphological Transformations.",Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getApplicationContext(),"Restoring settings.",Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                case "Image":
                    Log.v("Button Selection", "GalleryPhoto");
                    recognizeOnImageActivity(distanceFromPlate,oldPlatesMode,amountOfPlates);
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

    public void recognizeOnImageActivity(int distanceFromPlate ,boolean oldPlatesMode, int amountOfPlates) {
        Intent recognizeOnImageIntent = new Intent(this, RecognizeOnImageActivity.class);
        recognizeOnImageIntent.putExtra("recognitionMethod",this.recognitionMethod);
        recognizeOnImageIntent.putExtra("liveGallerySelection",this.liveOrGallery);
        recognizeOnImageIntent.putExtra("distanceFromPlate",distanceFromPlate);
        recognizeOnImageIntent.putExtra("oldPlatesMode",oldPlatesMode);
        recognizeOnImageIntent.putExtra("amountOfPlates",amountOfPlates);
        startActivity(recognizeOnImageIntent);
    }

}