package thesis.masters.registrationplates;


/*
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class RecognizeOnGalleryImageActivity extends AppCompatActivity {

    ImageView imageToBeRecognized;
    private static final int SELECTED_PICTURE = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize_on_gallery_image);
        //OpenCVLoader.initDebug();
        this.imageToBeRecognized = (ImageView) findViewById(R.id.imageToBeRecognizedImageView);

    }

    public void pickImageFromGallery(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, SELECTED_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECTED_PICTURE && resultCode == RESULT_OK && data != null)
        {
            Uri imageURI = data.getData();

            //get path from uri

            String imagePath = getPath(imageURI);

            loadImage(imagePath);

            displayImage(sampleImg);
        }

    }

    private void displayImage(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(),mat.rows(),Bitmap.Config.RGB_565);

        // mat to bitmap

        Utils.matToBitmap(mat,bitmap);
        imageToBeRecognized.setImageBitmap(bitmap);
    }
    Mat sampleImg;
    private void loadImage(String path) {

        Mat originalImage = Imgcodecs.imread(path); // image in BGR format
        Mat rgbImg = new Mat();

        //convert BGR to RGB

        Imgproc.cvtColor(originalImage,rgbImg,Imgproc.COLOR_BGR2RGB);

        Display display = getWindowManager().getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);

        int mobile_width = size.x;
        int mobile_height = size.y;

        sampleImg = new Mat();

        double downSampleRatio = calculateSubSimpleSize(rgbImg,mobile_width,mobile_height);

        Imgproc.resize(rgbImg,sampleImg,new Size(),downSampleRatio,downSampleRatio,Imgproc.INTER_AREA);


        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,1);
            switch (orientation){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    sampleImg = sampleImg.t();
                    Core.flip(sampleImg,sampleImg,1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    sampleImg = sampleImg.t();
                    Core.flip(sampleImg,sampleImg,0);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }




    }

    private double calculateSubSimpleSize(Mat src, int mobile_width, int mobile_height) {

        final int width = src.width();
        final int height = src.height();
        double inSampleSize = 1;

        if(height > mobile_height || width > mobile_width)
        {
            final double heightRatio = (double)mobile_height/ (double) height;
            final double widthRatio = (double) mobile_width / (double) width;
            inSampleSize = heightRatio < widthRatio ? height : width;
        }

        return inSampleSize;
    }

    private String getPath(Uri uri) {
        if (uri == null)
        {
            return null;
        }
        else
        {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(uri,projection,null,null,null);
            if (cursor!=null)
            {
                int col_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(col_index);
            }
        }
        return uri.getPath();
    }
}
*/




import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

public class RecognizeOnGalleryImageActivity extends AppCompatActivity {
    private static final int SELECTED_PICTURE = 101;
    ImageView imageView;
    Uri imageURI;
    Bitmap imageBitmap, grayBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize_on_gallery_image);
        this.imageView = (ImageView) findViewById(R.id.imageToBeRecognizedImageView);
        OpenCVLoader.initDebug();
    }

    public void pickImageFromGallery(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, SELECTED_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && resultCode == RESULT_OK && requestCode == SELECTED_PICTURE) {
            this.imageURI = data.getData();

            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageURI);
                this.imageView.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    public void recognizeTestMethod1(View view) {
       // OpenCVLoader.initDebug();
        //Bitmap initialImageBitmap = BitmapFactory.decodeResource(getResources(),R.id.imageToBeRecognizedImageView);

        int initialImageBitmapHeight = imageBitmap.getHeight();
        int initialImageBitmapWidth = imageBitmap.getWidth();

        Mat imageMat1 = new Mat(initialImageBitmapHeight,initialImageBitmapWidth,CvType.CV_8U,new Scalar(4));
        Mat imageMat2 = new Mat(initialImageBitmapHeight,initialImageBitmapWidth,CvType.CV_8U,new Scalar(4));
        Mat imageMat3 = new Mat(initialImageBitmapHeight,initialImageBitmapWidth,CvType.CV_8U,new Scalar(4));
        Mat imageMat4 = new Mat(initialImageBitmapHeight,initialImageBitmapWidth,CvType.CV_8U,new Scalar(4));

        Mat tmp = new Mat(initialImageBitmapHeight,initialImageBitmapWidth,CvType.CV_8U,new Scalar(4));

        Bitmap copyBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888,true);
        Utils.bitmapToMat(copyBitmap,imageMat1);

        //RGB -> Gray
        Imgproc.cvtColor(imageMat1,imageMat3,Imgproc.COLOR_RGB2GRAY,8);

        Imgproc.dilate(imageMat3,tmp,Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9,9)));
        Imgproc.erode(tmp,imageMat4,Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9,9)));

        Core.absdiff(imageMat4,imageMat3,imageMat2);

        Imgproc.Sobel(imageMat2,imageMat2,CvType.CV_8U,1,0,3,1,0.4,4);

        Imgproc.GaussianBlur(imageMat2,imageMat2,new Size(5,5),2);

        Imgproc.dilate(imageMat2,imageMat2,Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,3)));

        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(17,3));

        Imgproc.morphologyEx(imageMat2,imageMat2,Imgproc.MORPH_CLOSE,element);

        Imgproc.threshold(imageMat2, imageMat2, 0 ,255, Imgproc.THRESH_OTSU+Imgproc.THRESH_BINARY);

        grayBitmap = Bitmap.createBitmap(initialImageBitmapWidth,initialImageBitmapHeight,Bitmap.Config.RGB_565);



        ArrayList<RotatedRect> rects = new  ArrayList<RotatedRect>();
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(imageMat2, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        ListIterator<MatOfPoint> itc = contours.listIterator();
        while(itc.hasNext())
        {
            MatOfPoint2f mp2f = new MatOfPoint2f(itc.next().toArray());
            RotatedRect mr = Imgproc.minAreaRect(mp2f);
            double area = Math.abs(Imgproc.contourArea(mp2f));

            double bbArea= mr.size.area();
            double ratio = area / bbArea;
            if( (ratio < 0.45) || (bbArea < 400) )
            {
                itc.remove();  // other than deliberately making the program slow,
                // does erasing the contour have any purpose?
            }
            else
            {
                rects.add(mr);
            }



        }

        for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(imageMat1, contours, i, new Scalar(100, 255, 255), 5);
        }



        Utils.matToBitmap(imageMat1,grayBitmap);

        imageView.setImageBitmap(grayBitmap);

    }
}

  /*  public void convertToGray(View view){

        Mat Rgba = new Mat();
        Mat grayMat = new Mat();
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inDither = false;
        o.inSampleSize = 4;

        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();

        grayBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565);

        //bitmap to MAT

        Utils.bitmapToMat(imageBitmap,Rgba);

        //get it gray

        Imgproc.cvtColor(Rgba,grayMat,Imgproc.COLOR_RGB2GRAY);

        Utils.matToBitmap(grayMat,grayBitmap);

        imageView.setImageBitmap(grayBitmap);


    }
}*/

