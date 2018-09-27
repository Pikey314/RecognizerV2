package thesis.masters.registrationplates;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

        this.recognitionMethodSpinner = (Spinner) findViewById(R.id.recognitionMethodSpinner);
        ArrayAdapter <CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.recognitionMethodsArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.recognitionMethodSpinner.setPrompt(getResources().getString(R.string.recognitionMethodSpinnerPrompt));
        this.recognitionMethodSpinner.setAdapter(adapter);

        this.liveOrGalleryRadioGroup = (RadioGroup) findViewById(R.id.liveOrPrerecordedRadioGroup);
        this.videoOrPhotoRadioGroup = (RadioGroup) findViewById(R.id.videoOrPhotoRadioGroup);



    }

    public void goToConfirmRecognitionSelectionActivity(View view){
        Intent intent = new Intent(this, ConfirmRecognitionSelectionActivity.class);
        String liveOrGallerySelectionText = getRadioButtonValue(this.liveOrGalleryRadioGroup);
        String videoOrPhotoSelectionText = getRadioButtonValue(this.videoOrPhotoRadioGroup);

        intent.putExtra("spinnerValue", this.recognitionMethodSpinner.getSelectedItem().toString());
        intent.putExtra("liveOrGallerySelectionText", liveOrGallerySelectionText);
        intent.putExtra("videoOrPhotoSelectionText", videoOrPhotoSelectionText);

        startActivity(intent);


    }

    private String getRadioButtonValue(RadioGroup radioGroup){
        String radioButtonTextValue = ((RadioButton) findViewById( radioGroup.getCheckedRadioButtonId())).getText().toString();
        return radioButtonTextValue;
    }
}