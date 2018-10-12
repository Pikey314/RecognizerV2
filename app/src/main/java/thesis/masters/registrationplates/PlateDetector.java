package thesis.masters.registrationplates;

import android.graphics.Bitmap;
import android.net.Uri;

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

import java.util.ArrayList;
import java.util.ListIterator;

public class PlateDetector {

    public Bitmap testMethod(Bitmap bitmap){

        int initialImageBitmapHeight = bitmap.getHeight();
        int initialImageBitmapWidth = bitmap.getWidth();

        Mat imageMat1 = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat imageMat2 = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat imageMat3 = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat imageMat4 = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));

        Mat tmp = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));

        Bitmap copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(copyBitmap, imageMat1);

        //RGB -> Gray
        Imgproc.cvtColor(imageMat1, imageMat3, Imgproc.COLOR_RGB2GRAY, 8);

        Imgproc.dilate(imageMat3, tmp, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 9)));
        Imgproc.erode(tmp, imageMat4, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 9)));

        Core.absdiff(imageMat4, imageMat3, imageMat2);

        Imgproc.Sobel(imageMat2, imageMat2, CvType.CV_8U, 1, 0, 3, 1, 0.4, 4);

        Imgproc.GaussianBlur(imageMat2, imageMat2, new Size(5, 5), 2);

        Imgproc.dilate(imageMat2, imageMat2, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));

        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(17, 3));

        Imgproc.morphologyEx(imageMat2, imageMat2, Imgproc.MORPH_CLOSE, element);

        Imgproc.threshold(imageMat2, imageMat2, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);

        Bitmap bitmapToReturn = Bitmap.createBitmap(initialImageBitmapWidth, initialImageBitmapHeight, Bitmap.Config.RGB_565);


        ArrayList<RotatedRect> rects = new ArrayList<RotatedRect>();
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(imageMat2, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        ListIterator<MatOfPoint> itc = contours.listIterator();
        while (itc.hasNext()) {
            MatOfPoint2f mp2f = new MatOfPoint2f(itc.next().toArray());
            RotatedRect mr = Imgproc.minAreaRect(mp2f);
            double area = Math.abs(Imgproc.contourArea(mp2f));

            double bbArea = mr.size.area();
            double ratio = area / bbArea;
            if ((ratio < 0.45) || (bbArea < 400)) {
                itc.remove();  // other than deliberately making the program slow,
                // does erasing the contour have any purpose?
            } else {
                rects.add(mr);
            }


        }

        for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(imageMat1, contours, i, new Scalar(100, 255, 255), 5);
        }


        Utils.matToBitmap(imageMat2, bitmapToReturn);

        return bitmapToReturn;
    }

    public Bitmap morphologicalRecognitionMethod(Bitmap bitmap){

        int initialImageBitmapHeight = bitmap.getHeight();
        int initialImageBitmapWidth = bitmap.getWidth();

        Mat initialImageMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat grayScaleMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat higherContrastMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));

        /* Mat imageMat4 = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));

        Mat tmp = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));*/

        Bitmap copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(copyBitmap, initialImageMat);

        Imgproc.cvtColor(initialImageMat, grayScaleMat, Imgproc.COLOR_RGB2GRAY, 8);

        Imgproc.equalizeHist(grayScaleMat,higherContrastMat);

        //KERNEL START
        //Wielkość zależy od wysokości tablicy rejestracyjnej(h) -> kernel = h/5
        //TUTAJ POWINIENEM DOROBIC TO ABY UZYTKOWNIK MOGL WYBRAC CZY REJSTRACJA DALEKO/SREDNIO/BLISKO
        int h = initialImageBitmapHeight/20;
        int kernelWidth = h*2;
        int kernelHeight = h/5;
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(kernelWidth, kernelHeight));
        //KERLEN END

        Imgproc.morphologyEx(higherContrastMat,higherContrastMat,Imgproc.MORPH_TOPHAT,kernel);
        Imgproc.morphologyEx(higherContrastMat,higherContrastMat,Imgproc.MORPH_BLACKHAT,kernel);

        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(h*4, 1));

        Imgproc.morphologyEx(higherContrastMat,higherContrastMat,Imgproc.MORPH_CLOSE,kernel);

        Bitmap bitmapToReturn = Bitmap.createBitmap(initialImageBitmapWidth, initialImageBitmapHeight, Bitmap.Config.RGB_565);

        Utils.matToBitmap(higherContrastMat,bitmapToReturn);

        return bitmapToReturn;



    }
}
