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

    public int getTextFromImage(Bitmap bitmap, Context context, TextView textView, TextView textView2, TextView textView3, TextView textView4){
        int flag = 0;
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

        if (textRecognizer.isOperational())
        {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = textRecognizer.detect(frame);
            String sb, s1="", s2="", s3="";

            for (int i=0; i<items.size(); i++) {
                TextBlock myItems = items.valueAt(i);
                sb = myItems.getValue();
                if (sb.length() >= 4 && sb.length() < 10 && !sb.matches(".*[a-z].*") && sb.matches(".*\\d+.*")) {
                    if (flag == 0) {
                        textView.setText(sb);
                        s1 = sb;
                        flag = 1;
                    } else if (flag == 1 && !sb.equals(s1)) {
                        textView2.setText(sb);
                        s2 = sb;
                        flag = 2;
                    } else if (flag == 2 && !sb.equals(s1) && !sb.equals(s2)) {
                        textView3.setText(sb);
                        s3 = sb;
                        flag = 3;
                    } else if (flag == 3 && !sb.equals(s1) && !sb.equals(s2) && !sb.equals(s3)) {
                        textView3.setText(sb);
                        flag = 4;
                        break;
                    }

                }
            }

        }
        else
        {
            Toast.makeText(context,"Can't get text from Image",Toast.LENGTH_SHORT).show();
        }
        return flag;
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
                        String sb;

                        for (int i = 0; i < items.size(); i++) {
                            TextBlock myItems = items.valueAt(i);
                            sb = myItems.getValue();
                            if (sb.length() >= 4 && sb.length() < 10 && !sb.matches(".*[a-z].*") && sb.matches(".*\\d+.*")) {
                                textView.setText(sb);
                                break;
                            }
                        }

                    }
                });
            }
        }

    }

}
