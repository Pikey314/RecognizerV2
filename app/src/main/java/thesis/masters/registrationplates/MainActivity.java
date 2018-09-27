package thesis.masters.registrationplates;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView rimImageView1 = (ImageView) findViewById(R.id.rim1ImageView);
        ImageView rimImageView2 = (ImageView) findViewById(R.id.rim2ImageView);
        Animation rimAnimation =  AnimationUtils.loadAnimation(this, R.anim.spin);
        rimImageView1.startAnimation(rimAnimation);
        rimImageView2.startAnimation(rimAnimation);
        if(OpenCVLoader.initDebug())
        {
            Toast.makeText(getApplicationContext(),"OpenCV is here", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(),"Not loaded",Toast.LENGTH_SHORT).show();
        }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_side_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help:
                InputStream iFile = getResources().openRawResource(R.raw.help);
                return true;
            case R.id.settings:
                System.out.println("Settings");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void goToSelectRecognitionMethodActivity(View view){
        Intent intent = new Intent(this, SelectSourceForRecognitionActivity.class);
        startActivity(intent);
    }
}