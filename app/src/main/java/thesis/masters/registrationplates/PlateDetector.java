package thesis.masters.registrationplates;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class PlateDetector {

    public Bitmap edgeContourRecognitionMethod(Bitmap bitmap) {

        int initialImageBitmapHeight = bitmap.getHeight();
        int initialImageBitmapWidth = bitmap.getWidth();

        Mat imageMat1 = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat imageMat2 = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat imageMat3 = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat imageMat4 = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat tmp = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));

        Bitmap copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(copyBitmap, imageMat1);

        //RGB -> Gray
        Imgproc.cvtColor(imageMat1, imageMat3, Imgproc.COLOR_RGB2GRAY, 8);

        Imgproc.dilate(imageMat3, tmp, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 9)));
        Imgproc.erode(tmp, imageMat4, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 9)));

        Core.absdiff(imageMat4, imageMat3, imageMat2);

        Imgproc.GaussianBlur(imageMat2, imageMat2, new Size(5, 5), 2);

        Imgproc.Sobel(imageMat2, imageMat2, CvType.CV_8U, 1, 0, 3, 1, 0.4, 4);

        //TEST
        double maxPixelValue = Core.minMaxLoc(imageMat2).maxVal;

        //Toast.makeText(context,Double.toString(maxPixelValue), Toast.LENGTH_LONG).show();

        Imgproc.threshold(imageMat2,imageMat2,maxPixelValue-70,255,Imgproc.THRESH_BINARY);
        //END TEST
        Imgproc.dilate(imageMat2, imageMat2, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));

        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(27, 9));

        Imgproc.morphologyEx(imageMat2, imageMat2, Imgproc.MORPH_CLOSE, element);

        Imgproc.findContours(imageMat2, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);


        if(contours.size() > 0) {
            int biggestContourIndex = 0;
            MatOfPoint biggestContour = contours.get(0);
            if (contours.size() > 1) {
                double biggestContourArea = Imgproc.contourArea(biggestContour);
                for (int i = 1; i < contours.size(); i++) {
                    if (biggestContourArea < Imgproc.contourArea(contours.get(i))) {
                        biggestContourArea = Imgproc.contourArea(contours.get(i));
                        biggestContourIndex = i;
                    }
                }
            }
            Imgproc.drawContours(imageMat1, contours, biggestContourIndex, new Scalar(100, 255, 255), 5);
        }

        Bitmap bitmapToReturn = Bitmap.createBitmap(initialImageBitmapWidth, initialImageBitmapHeight, Bitmap.Config.RGB_565);
        Utils.matToBitmap(imageMat1, bitmapToReturn);
        return bitmapToReturn;
    }








    // DODAC PAREMTRY I MENU USTAWIEN -> h = daleko/sredni/blisko,  -> czarna tablica czy biala w if
    public Bitmap morphologicalTransformationsRecognitionMethod(Bitmap bitmap){

        int initialImageBitmapHeight = bitmap.getHeight();
        int initialImageBitmapWidth = bitmap.getWidth();

        Mat initialImageMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat grayScaleMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat higherContrastMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat matToReturn = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));

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

        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, h/2));

        Imgproc.morphologyEx(higherContrastMat,higherContrastMat,Imgproc.MORPH_OPEN,kernel);

        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(h*2, 1));

        Imgproc.morphologyEx(higherContrastMat,higherContrastMat,Imgproc.MORPH_OPEN,kernel);

        double maxPixelValue = Core.minMaxLoc(higherContrastMat).maxVal;

        //Toast.makeText(context,Double.toString(maxPixelValue), Toast.LENGTH_LONG).show();

        Imgproc.threshold(higherContrastMat,higherContrastMat,maxPixelValue-20,255,Imgproc.THRESH_BINARY);

        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(h, h/2));

        Imgproc.dilate(higherContrastMat,higherContrastMat,kernel);

        //kontury

        List<MatOfPoint> contours = new ArrayList<>();

        Imgproc.findContours(higherContrastMat,contours,new Mat(),Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);

        Utils.bitmapToMat(bitmap,matToReturn);

        int contourThickness = bitmap.getHeight()/100;

        for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(matToReturn, contours, i, new Scalar(0, 0, 255), contourThickness);
        }


        Bitmap bitmapToReturn = Bitmap.createBitmap(initialImageBitmapWidth, initialImageBitmapHeight, Bitmap.Config.RGB_565);

        Utils.matToBitmap(matToReturn,bitmapToReturn);

        return bitmapToReturn;



    }

    public Bitmap medianCenterOfMomentRecognitionMethod (Bitmap bitmap){

        int initialImageBitmapHeight = bitmap.getHeight();
        int initialImageBitmapWidth = bitmap.getWidth();
        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint> contours2 = new ArrayList<>();
        ArrayList<Point> pointsOfContours = new ArrayList<Point>();
        int centerX = 0;
        int centerY = 0;
        int numberOfPoints = 0;

        Mat initialImageMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat grayScaleMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat averageImage = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat thresholdedImage = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat matToReturn = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));

        /* Mat imageMat4 = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));

        Mat tmp = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));*/

        Bitmap copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(copyBitmap, initialImageMat);

        Imgproc.cvtColor(initialImageMat, grayScaleMat, Imgproc.COLOR_RGB2GRAY, 8);

        Imgproc.medianBlur(grayScaleMat,grayScaleMat,3);

        Imgproc.blur(grayScaleMat,averageImage,new Size(20,20));

        Core.subtract(grayScaleMat,averageImage,averageImage);



        Imgproc.threshold(averageImage,thresholdedImage,30,255,Imgproc.THRESH_BINARY);

        Imgproc.findContours(thresholdedImage,contours2,new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);

        //Blisko ksize np. 47, srednio ksize np. 31
        Imgproc.medianBlur(thresholdedImage,averageImage, 47);



        Imgproc.findContours(averageImage,contours,new Mat(),Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);

        Moments moments;


        Utils.bitmapToMat(bitmap,matToReturn);

        int contourThickness = bitmap.getHeight()/100;
        Point center;
        for (int i = 0; i < contours.size(); i++) {
            moments = Imgproc.moments(contours.get(i));
            int x = (int) (moments.get_m10() / moments.get_m00());
            int y = (int) (moments.get_m01() / moments.get_m00());
            if(x > initialImageBitmapWidth/10 && x < (initialImageBitmapWidth - (initialImageBitmapWidth/10))) {
                pointsOfContours.add(new Point(x, y));
                centerX += x;
                centerY += y;
                numberOfPoints++;
                //Imgproc.drawContours(matToReturn, contours, i, new Scalar(0, 0, 255), contourThickness);
                //Imgproc.circle(matToReturn, pointsOfContours.get(i), 7, new Scalar(0, 0, 255), -1);
            }

        }

        if (numberOfPoints != 0) {
            centerX = centerX / numberOfPoints;
            centerY = centerY / numberOfPoints;

            Imgproc.circle(matToReturn, new Point(centerX, centerY), 7, new Scalar(255, 0, 0), -1);

            for (int i = 0; i < contours2.size(); i++) {
                double test = Imgproc.pointPolygonTest(new MatOfPoint2f(contours2.get(i).toArray()), new Point(centerX, centerY), false);
                if (test > -1)
                    Imgproc.drawContours(matToReturn, contours2, i, new Scalar(0, 0, 255), -1);

            }

            //To uzaleznic od wielkosci obrazka
            double smallPlateRectangleWidth = initialImageBitmapWidth / 5;
            double smallPlateRectangleHeight = initialImageBitmapHeight / 20;


            Imgproc.rectangle(matToReturn, new Point(centerX - smallPlateRectangleWidth, centerY - smallPlateRectangleHeight), new Point(centerX + smallPlateRectangleWidth, centerY + smallPlateRectangleHeight), new Scalar(0, 255, 0));

        }






        Bitmap bitmapToReturn = Bitmap.createBitmap(initialImageBitmapWidth, initialImageBitmapHeight, Bitmap.Config.RGB_565);

        Utils.matToBitmap(matToReturn,bitmapToReturn);



        return bitmapToReturn;
    }
























 /*   public Bitmap method3 (Bitmap bitmap) {

        int initialImageBitmapHeight = bitmap.getHeight();
        int initialImageBitmapWidth = bitmap.getWidth();

        Mat initialImageMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat grayScaleMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat sobelMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));

        Bitmap copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(copyBitmap, initialImageMat);

        Imgproc.GaussianBlur(initialImageMat,initialImageMat,new Size(3,3),0,0, Core.BORDER_DEFAULT);

        Imgproc.cvtColor(initialImageMat, grayScaleMat, Imgproc.COLOR_RGB2GRAY, 8);


        Imgproc.Canny(grayScaleMat,sobelMat,50,200,3,false);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(sobelMat,contours,new Mat(),Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);


        for (int i = 0; i < contours2.size(); i++) {
                Imgproc.drawContours(matToReturn, contours2, i, new Scalar(0, 0, 255), -1);

        }


        //Imgproc.Sobel(grayScaleMat,sobelMat,CvType.CV_8U,1,1,3);

        //Core.subtract(sobelMat,grayScaleMat,grayScaleMat);


        Bitmap bitmapToReturn = Bitmap.createBitmap(initialImageBitmapWidth, initialImageBitmapHeight, Bitmap.Config.RGB_565);

        Utils.matToBitmap(grayScaleMat,bitmapToReturn);

        return bitmapToReturn;

    }*/

}
