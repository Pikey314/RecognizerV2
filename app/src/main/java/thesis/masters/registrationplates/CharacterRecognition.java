package thesis.masters.registrationplates;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class CharacterRecognition {

    public void getTextFromImage(Bitmap bitmap, Context context, TextView textView){

        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

        if (textRecognizer.isOperational())
        {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = textRecognizer.detect(frame);
            String sb;

            for (int i=0; i<items.size(); i++) {
                TextBlock myItems = items.valueAt(i);
                sb = myItems.getValue();
                if (sb.length() >= 4 && sb.length() < 10 && !sb.matches(".*[a-z].*")) {
                    textView.setText(sb);
                    break;

                }
            }

        }
        else
        {
            Toast.makeText(context,"Can't get text from Image",Toast.LENGTH_SHORT).show();
        }
    }



    public void getTextFromVideo(Mat mat, int orientation, Bitmap recognitionBitmapPortrait, Bitmap recognitionBitmapLandscape, Bitmap recognitionBitmap, TextRecognizer textRecognizer, final TextView textView){

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Utils.matToBitmap(mat, recognitionBitmapPortrait);
            recognitionBitmap = recognitionBitmapPortrait;
        } else
        {
            Utils.matToBitmap(mat, recognitionBitmapLandscape);
            recognitionBitmap = recognitionBitmapLandscape;
        }
        if (textRecognizer.isOperational())
        {
            Frame frame = new Frame.Builder().setBitmap(recognitionBitmap).build();
            final SparseArray<TextBlock> items = textRecognizer.detect(frame);
            if (items.size()!=0) {
                textView.post(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder sb = new StringBuilder();

                        for (int i = 0; i < items.size(); i++) {
                            TextBlock myItems = items.valueAt(i);
                            sb.append(myItems.getValue());
                            sb.append("\n");
                        }
                        textView.setText(sb.toString());
                    }
                });
            }
        }

    }

}
