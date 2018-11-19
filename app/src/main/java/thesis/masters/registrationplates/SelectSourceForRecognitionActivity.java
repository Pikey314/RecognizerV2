package thesis.masters.registrationplates;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

public class SelectSourceForRecognitionActivity extends AppCompatActivity {
    private Spinner recognitionMethodSpinner;
    private RadioGroup liveOrGalleryRadioGroup;
    private RadioGroup videoOrPhotoRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_source_for_recognition);
        this.recognitionMethodSpinner = findViewById(R.id.recognitionMethodSpinner);
        ArrayAdapter <CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.recognitionMethodsArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.recognitionMethodSpinner.setPrompt(getResources().getString(R.string.recognitionMethodSpinnerPrompt));
        this.recognitionMethodSpinner.setAdapter(adapter);
        this.liveOrGalleryRadioGroup = findViewById(R.id.liveOrPrerecordedRadioGroup);
        this.videoOrPhotoRadioGroup = findViewById(R.id.videoOrPhotoRadioGroup);



    }

    public void goToConfirmRecognitionSelectionActivity(View view){
        Intent intent;
        //enable resources translation START
        String liveOrGallerySelectionText = getRadioButtonValue(this.liveOrGalleryRadioGroup);
        if (liveOrGallerySelectionText.equals(getString(R.string.radioButtonLive)))
            liveOrGallerySelectionText = "LIVE";
        else
            liveOrGallerySelectionText = "GALLERY";
        String videoOrPhotoSelectionText = getRadioButtonValue(this.videoOrPhotoRadioGroup);
        if (videoOrPhotoSelectionText.equals(getString(R.string.radioButtonVideo)))
            videoOrPhotoSelectionText = "VIDEO";
        else
            videoOrPhotoSelectionText = "IMAGE";
        String spinnerValueString = this.recognitionMethodSpinner.getSelectedItem().toString();
        if (spinnerValueString.equals(getResources().getStringArray(R.array.recognitionMethodsArray)[0]))
            spinnerValueString = "Vertical Edge Contouring";
        else if (spinnerValueString.equals(getResources().getStringArray(R.array.recognitionMethodsArray)[1]))
            spinnerValueString = "Morphological Transformations";
        else
            spinnerValueString = "Median Center of Moments";
        //enable resources translation END
        if (liveOrGallerySelectionText.equals("LIVE") && videoOrPhotoSelectionText.equals("VIDEO")) {
            intent = new Intent(this, RecognizeOnLiveVideoActivity.class);
            intent.putExtra("spinnerValue", spinnerValueString);
            startActivity(intent);
        } else if (liveOrGallerySelectionText.equals("GALLERY") && videoOrPhotoSelectionText.equals("VIDEO")) {
            intent = new Intent(this, RecognizeOnPreRecordedVideoActivity.class);
            intent.putExtra("spinnerValue", spinnerValueString);
            startActivity(intent);
        } else {
            intent = new Intent(this, ConfirmRecognitionSelectionActivity.class);
            intent.putExtra("spinnerValue", spinnerValueString);
            intent.putExtra("liveOrGallerySelectionText", liveOrGallerySelectionText);
            intent.putExtra("videoOrPhotoSelectionText", videoOrPhotoSelectionText);
            startActivity(intent);
        }




    }

    private String getRadioButtonValue(RadioGroup radioGroup){
        String radioButtonTextValue = ((RadioButton) findViewById( radioGroup.getCheckedRadioButtonId())).getText().toString();
        return radioButtonTextValue;
    }
}