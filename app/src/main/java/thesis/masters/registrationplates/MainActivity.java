package thesis.masters.registrationplates;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView rimImageView1 = findViewById(R.id.rim1ImageView);
        ImageView rimImageView2 = findViewById(R.id.rim2ImageView);
        ImageView rimImageView3 = findViewById(R.id.rim3ImageView);
        ImageView rimImageView4 = findViewById(R.id.rim4ImageView);
        Animation rimAnimation =  AnimationUtils.loadAnimation(this, R.anim.spin);
        rimImageView1.startAnimation(rimAnimation);
        rimImageView2.startAnimation(rimAnimation);
        rimImageView3.startAnimation(rimAnimation);
        rimImageView4.startAnimation(rimAnimation);
        if(OpenCVLoader.initDebug())
        {
            Toast.makeText(getApplicationContext(),"Have fun!", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(),"OpenCV not loaded",Toast.LENGTH_SHORT).show();
        }

    public void goToSelectRecognitionMethodActivity(View view){
        Intent intent = new Intent(this, SelectSourceForRecognitionActivity.class);
        startActivity(intent);
    }
}