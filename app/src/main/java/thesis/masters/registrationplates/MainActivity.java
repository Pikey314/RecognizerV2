package thesis.masters.registrationplates;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(OpenCVLoader.initDebug())
        {
            Toast.makeText(getApplicationContext(),"OpenCV is here", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(),"Not loaded",Toast.LENGTH_SHORT).show();
    }
}