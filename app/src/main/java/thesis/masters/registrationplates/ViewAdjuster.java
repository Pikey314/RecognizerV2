package thesis.masters.registrationplates;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ViewAdjuster {

    public static final int MAX_ALLOWED_BITMAP_HEIGHT = 4096;
    public static final int MAX_ALLOWED_BITMAP_WIDTH = 4096;


    // Method to prevent bitmap from oversizing to fit it into imageView
    public Bitmap adjustBitmap(Bitmap bitmap){

        Bitmap fixedBitmap;
        int oversizeWidth = bitmap.getWidth() - MAX_ALLOWED_BITMAP_WIDTH;
        int oversizeHeight = bitmap.getHeight() - MAX_ALLOWED_BITMAP_HEIGHT;
        int areaToCutWidth = oversizeWidth / 2;
        int areaToCutHeight = oversizeHeight / 2;

        if (bitmap.getHeight() > MAX_ALLOWED_BITMAP_HEIGHT && bitmap.getWidth() <= MAX_ALLOWED_BITMAP_WIDTH) {
            areaToCutHeight += 1;
            areaToCutWidth = 0;
            oversizeWidth = 0;
        }
        else if (bitmap.getHeight() <= MAX_ALLOWED_BITMAP_HEIGHT && bitmap.getWidth() > MAX_ALLOWED_BITMAP_WIDTH) {
            areaToCutWidth += 1;
            areaToCutHeight = 0;
            oversizeHeight = 0;
        }
        else {
            areaToCutWidth += 1;
            areaToCutHeight += 1;
        }
        fixedBitmap = Bitmap.createBitmap(bitmap,areaToCutWidth,areaToCutHeight,bitmap.getWidth()-oversizeWidth,bitmap.getHeight()-oversizeHeight);
        return fixedBitmap;

    }

    //Make all Image View with plates invisible
    public void hideAllPlateImageViews (ImageView iv1, ImageView iv2, ImageView iv3, ImageView iv4) {
        iv1.setVisibility(View.INVISIBLE);
        iv2.setVisibility(View.INVISIBLE);
        iv3.setVisibility(View.INVISIBLE);
        iv4.setVisibility(View.INVISIBLE);
    }

    //Make all Text Views inside plates Image Views invisible
    public void hideAllPlateOutputTextViews (TextView tv1, TextView tv2, TextView tv3, TextView tv4) {
        tv1.setText("");
        tv2.setText("");
        tv3.setText("");
        tv4.setText("");
    }

    public void showPlateImageViewsAfterRecognition(int recognizedPlates, ImageView iv1, ImageView iv2, ImageView iv3, ImageView iv4){
        switch (recognizedPlates) {
            case 1:
                iv1.setVisibility(View.VISIBLE);
                break;
            case 2:
                iv1.setVisibility(View.VISIBLE);
                iv2.setVisibility(View.VISIBLE);
                break;
            case 3:
                iv1.setVisibility(View.VISIBLE);
                iv2.setVisibility(View.VISIBLE);
                iv3.setVisibility(View.VISIBLE);
                break;
            case 4:
                iv1.setVisibility(View.VISIBLE);
                iv2.setVisibility(View.VISIBLE);
                iv3.setVisibility(View.VISIBLE);
                iv4.setVisibility(View.VISIBLE);
                break;

        }
    }

}
