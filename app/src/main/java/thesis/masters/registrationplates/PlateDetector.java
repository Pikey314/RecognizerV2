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

    //DONE
    public Bitmap edgeContourRecognitionMethod(Bitmap bitmap, int distanceFromPlate) {

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


        Imgproc.cvtColor(imageMat1, imageMat3, Imgproc.COLOR_RGB2GRAY, 8);

        int dilateErodeKernel;
        int gaussianBlurKernel;
        int sobelDx;
        int secondDilateKernel;
        int closeKernelWidth;
        int closeKernelHeight;
        switch (distanceFromPlate) {
            case 0:
                dilateErodeKernel = 13;
                gaussianBlurKernel = 13;
                sobelDx = 1;
                secondDilateKernel = 13;
                closeKernelHeight = initialImageBitmapHeight / 20;
                closeKernelWidth = closeKernelHeight * 4;
                break;
            case 1:
                dilateErodeKernel = 11;
                gaussianBlurKernel = 11;
                sobelDx = 1;
                secondDilateKernel = 9;
                closeKernelHeight = initialImageBitmapHeight / 30;
                closeKernelWidth = closeKernelHeight * 3;
                break;
            case 2:
                dilateErodeKernel = 9;
                gaussianBlurKernel = 9;
                sobelDx = 1;
                secondDilateKernel = 7;
                closeKernelHeight = initialImageBitmapHeight / 40;
                closeKernelWidth = closeKernelHeight * 3;
                break;
            case 3:
                dilateErodeKernel = 7;
                gaussianBlurKernel = 7;
                sobelDx = 1;
                secondDilateKernel = 7;
                closeKernelHeight = initialImageBitmapHeight / 56;
                closeKernelWidth = closeKernelHeight * 3;
                break;
            default:
                dilateErodeKernel = 3;
                gaussianBlurKernel = 3;
                sobelDx = 1;
                secondDilateKernel = 3;
                closeKernelHeight = initialImageBitmapHeight / 113;
                closeKernelWidth = closeKernelHeight * 3;
                break;

        }

        //TUTAJ distance from plate zmienic (3 dla małego, 9 dla średniego itp)
        Imgproc.dilate(imageMat3, tmp, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(dilateErodeKernel, dilateErodeKernel)));
        Imgproc.erode(tmp, imageMat4, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(dilateErodeKernel, dilateErodeKernel)));

        Core.absdiff(imageMat4, imageMat3, imageMat2);

        //3 dla daleko, 9 dla srednio itd
        Imgproc.GaussianBlur(imageMat2, imageMat2, new Size(gaussianBlurKernel, gaussianBlurKernel), 0);

        //tutaj z dx można pomyśleć tylko tutaj tylko można 1 albo 2
        Imgproc.Sobel(imageMat2, imageMat2, CvType.CV_8U, sobelDx, 0, 3, 1, 0);
        double maxPixelValue = Core.minMaxLoc(imageMat2).maxVal;

        //Zmenilem z -70 na -40
        Imgproc.threshold(imageMat2, imageMat2, maxPixelValue - 40, 255, Imgproc.THRESH_BINARY);

        //dla małych obbrazów np 3 dla srednich np 7
        Imgproc.dilate(imageMat2, imageMat2, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(secondDilateKernel, secondDilateKernel)));

        //dla wiekszych obrazów więcej - dla małego 27,9 jest ok
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(closeKernelWidth, closeKernelHeight));

        Imgproc.morphologyEx(imageMat2, imageMat2, Imgproc.MORPH_CLOSE, element);

        Imgproc.findContours(imageMat2, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        int contourThickness = bitmap.getHeight() / 150;
        if (contours.size() > 0) {
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
            Imgproc.drawContours(imageMat1, contours, biggestContourIndex, new Scalar(255, 0, 0), contourThickness);
        }

        Bitmap bitmapToReturn = Bitmap.createBitmap(initialImageBitmapWidth, initialImageBitmapHeight, Bitmap.Config.RGB_565);
        Utils.matToBitmap(imageMat1, bitmapToReturn);
        return bitmapToReturn;
    }


    // DONE
    public Bitmap morphologicalTransformationsRecognitionMethod(Bitmap bitmap, int distanceFromPlate) {

        int initialImageBitmapHeight = bitmap.getHeight();
        int initialImageBitmapWidth = bitmap.getWidth();

        Mat initialImageMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat grayScaleMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat higherContrastMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat matToReturn = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));


        Bitmap copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(copyBitmap, initialImageMat);

        Imgproc.cvtColor(initialImageMat, grayScaleMat, Imgproc.COLOR_RGB2GRAY, 8);

        Imgproc.equalizeHist(grayScaleMat, higherContrastMat);
        //distanceFromPlate CLOSE 0,1,2,3,4 FAR
        //moj obrazek uznaje ze jest 1 -> h = 13
        //KERNEL START
        //Wielkość zależy od wysokości tablicy rejestracyjnej(h) -> kernel = h/5
        //TUTAJ POWINIENEM DOROBIC TO ABY UZYTKOWNIK MOGL WYBRAC CZY REJSTRACJA DALEKO/SREDNIO/BLISKO
        int h;
        switch (distanceFromPlate) {
            case 0:
                h = initialImageBitmapHeight / 9;
                break;
            case 1:
                h = initialImageBitmapHeight / 13;
                break;
            case 2:
                h = initialImageBitmapHeight / 17;
                break;
            case 3:
                h = initialImageBitmapHeight / 21;
                break;
            default:
                h = initialImageBitmapHeight / 25;
                break;

        }


        int kernelSize = h / 5;
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(kernelSize, kernelSize));
        Imgproc.morphologyEx(higherContrastMat, higherContrastMat, Imgproc.MORPH_TOPHAT, kernel);
        Imgproc.morphologyEx(higherContrastMat, higherContrastMat, Imgproc.MORPH_BLACKHAT, kernel);

        kernelSize = h * 4;
        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(kernelSize, 1));
        Imgproc.morphologyEx(higherContrastMat, higherContrastMat, Imgproc.MORPH_CLOSE, kernel);

        kernelSize = h / 2;
        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, kernelSize));
        Imgproc.morphologyEx(higherContrastMat, higherContrastMat, Imgproc.MORPH_OPEN, kernel);

        kernelSize = h * 2;
        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(kernelSize, 1));
        Imgproc.morphologyEx(higherContrastMat, higherContrastMat, Imgproc.MORPH_OPEN, kernel);

        //TUTAJ OBCZAIC CZY 20 to odpowiednia wartosc
        double maxPixelValue = Core.minMaxLoc(higherContrastMat).maxVal;
        Imgproc.threshold(higherContrastMat, higherContrastMat, maxPixelValue - 20, 255, Imgproc.THRESH_BINARY);

        //TUTAJ RYZYKOWNY KERNEL
        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(h * 1.25, h / 3));
        Imgproc.dilate(higherContrastMat, higherContrastMat, kernel);

        //kontury

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(higherContrastMat, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        Utils.bitmapToMat(bitmap, matToReturn);

        int contourThickness = bitmap.getHeight() / 150;
        for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(matToReturn, contours, i, new Scalar(0, 0, 255), contourThickness);
        }


        Bitmap bitmapToReturn = Bitmap.createBitmap(initialImageBitmapWidth, initialImageBitmapHeight, Bitmap.Config.RGB_565);
        Utils.matToBitmap(matToReturn, bitmapToReturn);

        return bitmapToReturn;

    }


    //DONE
    public Bitmap medianCenterOfMomentRecognitionMethod(Bitmap bitmap, int distanceFromPlate) {

        int initialImageBitmapHeight = bitmap.getHeight();
        int initialImageBitmapWidth = bitmap.getWidth();
        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint> contours2 = new ArrayList<>();
        ArrayList<Point> pointsOfContours = new ArrayList<Point>();
        int centerX = 0;
        int centerY = 0;
        int numberOfPoints = 0;
        int contourThickness = bitmap.getHeight() / 300;

        Mat initialImageMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat grayScaleMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat averageImage = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat thresholdedImage = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat thresholdedImageForContours = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat matToReturn = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));


        Bitmap copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(copyBitmap, initialImageMat);

        Imgproc.cvtColor(initialImageMat, grayScaleMat, Imgproc.COLOR_RGB2GRAY, 8);

        //Tutaj nic nie zmieniam to tylko wygladzenie obrazka
        Imgproc.medianBlur(grayScaleMat, grayScaleMat, 3);

        int averageBlurSize;
        boolean closingFlag;
        int medianBlurSize;
        double smallPlateRectangleWidth;
        double smallPlateRectangleHeight;
        switch (distanceFromPlate) {
            case 0:
                averageBlurSize = 30;
                closingFlag = false;
                medianBlurSize = 57;
                smallPlateRectangleHeight = initialImageBitmapHeight / 27;
                smallPlateRectangleWidth = smallPlateRectangleHeight * 4;
                break;
            case 1:
                averageBlurSize = 20;
                closingFlag = false;
                medianBlurSize = 47;
                smallPlateRectangleHeight = initialImageBitmapHeight / 43;
                smallPlateRectangleWidth = smallPlateRectangleHeight * 4;
                break;
            case 2:
                averageBlurSize = 15;
                closingFlag = false;
                medianBlurSize = 37;
                smallPlateRectangleHeight = initialImageBitmapHeight / 65;
                smallPlateRectangleWidth = smallPlateRectangleHeight * 4;
                break;
            case 3:
                averageBlurSize = 10;
                closingFlag = true;
                medianBlurSize = 27;
                smallPlateRectangleHeight = initialImageBitmapHeight / 83;
                smallPlateRectangleWidth = smallPlateRectangleHeight * 4;
                break;
            default:
                averageBlurSize = 10;
                closingFlag = true;
                medianBlurSize = 17;
                smallPlateRectangleHeight = initialImageBitmapHeight / 100;
                smallPlateRectangleWidth = smallPlateRectangleHeight * 4;
                break;

        }
        //20 DOBRA DLA SREDNICH I CHYBA OGÓLNIE ALE MOŻNA SPRÓBOWAĆ DLA MAŁYCH 10
        //Dla malych warto 10
        Imgproc.blur(grayScaleMat, averageImage, new Size(averageBlurSize, averageBlurSize));

        Core.subtract(grayScaleMat, averageImage, averageImage);

        Imgproc.threshold(averageImage, thresholdedImage, 30, 255, Imgproc.THRESH_BINARY);

        //TO MOZE TYLKO DLA MALYCH OBRAZKOW
        if (closingFlag)
            Imgproc.morphologyEx(thresholdedImage, thresholdedImageForContours, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(8, 1)));
        else
            thresholdedImageForContours = thresholdedImage;

        Imgproc.findContours(thresholdedImageForContours, contours2, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        //Blisko ksize np. 47, srednio ksize np. 31
        //DLA SREDNICH-BLISKICH DOBRE JEST 47
        //Dla malych 17
        Imgproc.medianBlur(thresholdedImage, averageImage, medianBlurSize);

        Imgproc.findContours(averageImage, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        Moments moments;

        Utils.bitmapToMat(bitmap, matToReturn);

        Point center;
        for (int i = 0; i < contours.size(); i++) {
            moments = Imgproc.moments(contours.get(i));
            int x = (int) (moments.get_m10() / moments.get_m00());
            int y = (int) (moments.get_m01() / moments.get_m00());
            if (x > initialImageBitmapWidth / 10 && x < (initialImageBitmapWidth - (initialImageBitmapWidth / 10)) && y > initialImageBitmapHeight / 10 && y < (initialImageBitmapHeight - (initialImageBitmapHeight / 10))) {
                pointsOfContours.add(new Point(x, y));
                centerX += x;
                centerY += y;
                numberOfPoints++;
            }
        }

        if (numberOfPoints != 0) {
            centerX = centerX / numberOfPoints;
            centerY = centerY / numberOfPoints;
            Imgproc.circle(matToReturn, new Point(centerX, centerY), 7, new Scalar(255, 0, 0), -1);
            for (int i = 0; i < contours2.size(); i++) {
                double test = Imgproc.pointPolygonTest(new MatOfPoint2f(contours2.get(i).toArray()), new Point(centerX, centerY), false);
                if (test > -1)
                    Imgproc.drawContours(matToReturn, contours2, i, new Scalar(0, 255, 0), contourThickness);
            }
            Imgproc.rectangle(matToReturn, new Point(centerX - smallPlateRectangleWidth, centerY - smallPlateRectangleHeight), new Point(centerX + smallPlateRectangleWidth, centerY + smallPlateRectangleHeight), new Scalar(0, 255, 0));
        }
        Bitmap bitmapToReturn = Bitmap.createBitmap(initialImageBitmapWidth, initialImageBitmapHeight, Bitmap.Config.RGB_565);
        Utils.matToBitmap(matToReturn, bitmapToReturn);
        return bitmapToReturn;
    }

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







/* OLD VERISON
    // DODAC PAREMTRY I MENU USTAWIEN -> h = daleko/sredni/blisko,  -> czarna tablica czy biala w if

        int initialImageBitmapHeight = bitmap.getHeight();
        int initialImageBitmapWidth = bitmap.getWidth();

        Mat initialImageMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat grayScaleMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat higherContrastMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat matToReturn = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));



        Bitmap copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(copyBitmap, initialImageMat);

        Imgproc.cvtColor(initialImageMat, grayScaleMat, Imgproc.COLOR_RGB2GRAY, 8);

        Imgproc.equalizeHist(grayScaleMat,higherContrastMat);

        //distanceFromPlate CLOSE 0,1,2,3,4 FAR
        //moj obrazek uznaje ze jest 1 -> h = 13
        //KERNEL START
        //Wielkość zależy od wysokości tablicy rejestracyjnej(h) -> kernel = h/5
        //TUTAJ POWINIENEM DOROBIC TO ABY UZYTKOWNIK MOGL WYBRAC CZY REJSTRACJA DALEKO/SREDNIO/BLISKO
        int h;
        switch (distanceFromPlate) {
            case 0:
                h = initialImageBitmapHeight/9;
                break;
            case 1:
                h = initialImageBitmapHeight/13;
                break;
            case 2:
                h = initialImageBitmapHeight/17;
                break;
            case 3:
                h = initialImageBitmapHeight/21;
                break;
            default:
                h = initialImageBitmapHeight/25;
                break;

        }


        //int h = initialImageBitmapHeight/20;
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



    }*/


/*Imgproc.Sobel(imageMat2, imageMat2, CvType.CV_8U, 1, 0, 3, 1, 0.4, 4);

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
        return bitmapToReturn;*/
