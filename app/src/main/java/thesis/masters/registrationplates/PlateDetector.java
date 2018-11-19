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
import org.opencv.core.Rect;
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

        Mat initialImageMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat processingMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat grayScaleMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat dilateErodeMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat tmp = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));

        Bitmap copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(copyBitmap, initialImageMat);


        Imgproc.cvtColor(initialImageMat, grayScaleMat, Imgproc.COLOR_RGB2GRAY, 8);

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
        Imgproc.dilate(grayScaleMat, tmp, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(dilateErodeKernel, dilateErodeKernel)));
        Imgproc.erode(tmp, dilateErodeMat, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(dilateErodeKernel, dilateErodeKernel)));

        Core.absdiff(dilateErodeMat, grayScaleMat, processingMat);

        //3 dla daleko, 9 dla srednio itd
        Imgproc.GaussianBlur(processingMat, processingMat, new Size(gaussianBlurKernel, gaussianBlurKernel), 0);

        //tutaj z dx można pomyśleć tylko tutaj tylko można 1 albo 2
        Imgproc.Sobel(processingMat, processingMat, CvType.CV_8U, sobelDx, 0, 3, 1, 0);
        double maxPixelValue = Core.minMaxLoc(processingMat).maxVal;

        //Zmenilem z -70 na -40
        Imgproc.threshold(processingMat, processingMat, maxPixelValue - 40, 255, Imgproc.THRESH_BINARY);

        //dla małych obbrazów np 3 dla srednich np 7
        Imgproc.dilate(processingMat, processingMat, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(secondDilateKernel, secondDilateKernel)));

        //dla wiekszych obrazów więcej - dla małego 27,9 jest ok
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(closeKernelWidth, closeKernelHeight));

        Imgproc.morphologyEx(processingMat, processingMat, Imgproc.MORPH_CLOSE, element);

        Imgproc.findContours(processingMat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

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
            //Imgproc.drawContours(initialImageMat, contours, biggestContourIndex, new Scalar(255, 0, 0), contourThickness);
            //TEST RECT START
            Rect rect = Imgproc.boundingRect(contours.get(biggestContourIndex));
            Point tl = rect.tl();
            Point br = rect.br();
            Imgproc.rectangle(initialImageMat,tl,br,new Scalar(255, 0, 0), contourThickness);
            //TEST RECT END

        }

        Bitmap bitmapToReturn = Bitmap.createBitmap(initialImageBitmapWidth, initialImageBitmapHeight, Bitmap.Config.RGB_565);
        Utils.matToBitmap(initialImageMat, bitmapToReturn);
        return bitmapToReturn;
    }


    // DONE
    public Bitmap morphologicalTransformationsRecognitionMethod(Bitmap bitmap, int distanceFromPlate) {

        int initialImageBitmapHeight = bitmap.getHeight();
        int initialImageBitmapWidth = bitmap.getWidth();

        Mat initialImageMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat grayScaleMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat processingMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat matToReturn = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));


        Bitmap copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(copyBitmap, initialImageMat);

        Imgproc.cvtColor(initialImageMat, grayScaleMat, Imgproc.COLOR_RGB2GRAY, 8);

        Imgproc.equalizeHist(grayScaleMat, processingMat);
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
        Imgproc.morphologyEx(processingMat, processingMat, Imgproc.MORPH_TOPHAT, kernel);
        Imgproc.morphologyEx(processingMat, processingMat, Imgproc.MORPH_BLACKHAT, kernel);

        kernelSize = h * 4;
        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(kernelSize, 1));
        Imgproc.morphologyEx(processingMat, processingMat, Imgproc.MORPH_CLOSE, kernel);

        kernelSize = h / 2;
        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, kernelSize));
        Imgproc.morphologyEx(processingMat, processingMat, Imgproc.MORPH_OPEN, kernel);

        kernelSize = h * 2;
        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(kernelSize, 1));
        Imgproc.morphologyEx(processingMat, processingMat, Imgproc.MORPH_OPEN, kernel);

        //TUTAJ OBCZAIC CZY 20 to odpowiednia wartosc
        double maxPixelValue = Core.minMaxLoc(processingMat).maxVal;
        Imgproc.threshold(processingMat, processingMat, maxPixelValue - 20, 255, Imgproc.THRESH_BINARY);

        //TUTAJ RYZYKOWNY KERNEL
        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(h * 1.25, h / 3));
        Imgproc.dilate(processingMat, processingMat, kernel);

        //kontury

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(processingMat, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

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
        Mat grayScaleBlurredMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat processingMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat thresholdedMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat forContoursMat = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));
        Mat matToReturn = new Mat(initialImageBitmapHeight, initialImageBitmapWidth, CvType.CV_8U, new Scalar(4));


        Bitmap copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(copyBitmap, initialImageMat);

        Imgproc.cvtColor(initialImageMat, grayScaleBlurredMat, Imgproc.COLOR_RGB2GRAY, 8);

        //Tutaj nic nie zmieniam to tylko wygladzenie obrazka
        Imgproc.medianBlur(grayScaleBlurredMat, grayScaleBlurredMat, 3);

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
        Imgproc.blur(grayScaleBlurredMat, processingMat, new Size(averageBlurSize, averageBlurSize));

        Core.subtract(grayScaleBlurredMat, processingMat, processingMat);

        Imgproc.threshold(processingMat, thresholdedMat, 30, 255, Imgproc.THRESH_BINARY);

        //TO MOZE TYLKO DLA MALYCH OBRAZKOW
        if (closingFlag)
            Imgproc.morphologyEx(thresholdedMat, forContoursMat, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(8, 1)));
        else
            forContoursMat = thresholdedMat;

        Imgproc.findContours(forContoursMat, contours2, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        //Blisko ksize np. 47, srednio ksize np. 31
        //DLA SREDNICH-BLISKICH DOBRE JEST 47
        //Dla malych 17
        Imgproc.medianBlur(thresholdedMat, processingMat, medianBlurSize);

        Imgproc.findContours(processingMat, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

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
            //Imgproc.circle(matToReturn, new Point(centerX, centerY), 7, new Scalar(255, 0, 0), -1);
            for (int i = 0; i < contours2.size(); i++) {
                double test = Imgproc.pointPolygonTest(new MatOfPoint2f(contours2.get(i).toArray()), new Point(centerX, centerY), false);
                if (test > -1) {
                    //TEST RECT START
                    //Imgproc.drawContours(matToReturn, contours2, i, new Scalar(0, 255, 0), contourThickness);
                    Rect rect = Imgproc.boundingRect(contours2.get(i));
                    Point tl = rect.tl();
                    Point br = rect.br();
                    Imgproc.rectangle(matToReturn,tl,br,new Scalar(0, 255, 0), contourThickness);
                    //TEST RECT END


                }
            }
            //Imgproc.rectangle(matToReturn, new Point(centerX - smallPlateRectangleWidth, centerY - smallPlateRectangleHeight), new Point(centerX + smallPlateRectangleWidth, centerY + smallPlateRectangleHeight), new Scalar(0, 255, 0));
        }
        Bitmap bitmapToReturn = Bitmap.createBitmap(initialImageBitmapWidth, initialImageBitmapHeight, Bitmap.Config.RGB_565);
        Utils.matToBitmap(matToReturn, bitmapToReturn);
        return bitmapToReturn;
    }







    //METHODS FOR VIDEO RECOGNITION START


    public Mat medianCenterOfMomentRecognitionMethodForVideo(Mat originalImageMat, Mat grayScaleBluredMat, Mat processingMat, Mat thresholdedMat, Mat contoursMat, int distanceFromPlate) {

        int originalMatHeight = originalImageMat.rows();
        int originalMatWidth = originalImageMat.cols();
        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint> contours2 = new ArrayList<>();
        ArrayList<Point> pointsOfContours = new ArrayList<Point>();
        int centerX = 0;
        int centerY = 0;
        int numberOfPoints = 0;
        int contourThickness = 10;


        Imgproc.cvtColor(originalImageMat, grayScaleBluredMat, Imgproc.COLOR_RGB2GRAY, 8);

        //Tutaj nic nie zmieniam to tylko wygladzenie obrazka
        Imgproc.medianBlur(grayScaleBluredMat, grayScaleBluredMat, 3);

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
                smallPlateRectangleWidth = originalMatHeight / 27;
                smallPlateRectangleHeight = smallPlateRectangleWidth * 4;
                break;
            case 1:
                averageBlurSize = 20;
                closingFlag = false;
                medianBlurSize = 47;
                smallPlateRectangleHeight = originalMatHeight / 43;
                smallPlateRectangleWidth = smallPlateRectangleHeight * 4;
                break;
            case 2:
                averageBlurSize = 15;
                closingFlag = false;
                medianBlurSize = 37;
                smallPlateRectangleHeight = originalMatHeight / 65;
                smallPlateRectangleWidth = smallPlateRectangleHeight * 4;
                break;
            case 3:
                averageBlurSize = 10;
                closingFlag = true;
                medianBlurSize = 27;
                smallPlateRectangleHeight = originalMatHeight / 83;
                smallPlateRectangleWidth = smallPlateRectangleHeight * 4;
                break;
            default:
                averageBlurSize = 10;
                closingFlag = true;
                medianBlurSize = 17;
                smallPlateRectangleHeight = originalMatHeight / 100;
                smallPlateRectangleWidth = smallPlateRectangleHeight * 4;
                break;

        }
        //20 DOBRA DLA SREDNICH I CHYBA OGÓLNIE ALE MOŻNA SPRÓBOWAĆ DLA MAŁYCH 10
        //Dla malych warto 10
        Imgproc.blur(grayScaleBluredMat, processingMat, new Size(averageBlurSize, averageBlurSize));

        Core.subtract(grayScaleBluredMat, processingMat, processingMat);

        Imgproc.threshold(processingMat, thresholdedMat, 30, 255, Imgproc.THRESH_BINARY);

        //TO MOZE TYLKO DLA MALYCH OBRAZKOW
        if (closingFlag)
            Imgproc.morphologyEx(thresholdedMat, contoursMat, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(8, 1)));
        else
            contoursMat = thresholdedMat;

        Imgproc.findContours(contoursMat, contours2, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        //Blisko ksize np. 47, srednio ksize np. 31
        //DLA SREDNICH-BLISKICH DOBRE JEST 47
        //Dla malych 17
        Imgproc.medianBlur(thresholdedMat, processingMat, medianBlurSize);

        Imgproc.findContours(processingMat, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        Moments moments;
        Point center;
        for (int i = 0; i < contours.size(); i++) {
            moments = Imgproc.moments(contours.get(i));
            int x = (int) (moments.get_m10() / moments.get_m00());
            int y = (int) (moments.get_m01() / moments.get_m00());
            if (x > originalMatWidth / 10 && x < (originalMatWidth - (originalMatWidth / 10)) && y > originalMatHeight / 10 && y < (originalMatHeight - (originalMatHeight / 10))) {
                pointsOfContours.add(new Point(x, y));
                centerX += x;
                centerY += y;
                numberOfPoints++;
            }
        }

        if (numberOfPoints != 0) {
            centerX = centerX / numberOfPoints;
            centerY = centerY / numberOfPoints;
            Imgproc.circle(originalImageMat, new Point(centerX, centerY), 7, new Scalar(255, 0, 0), -1);
            for (int i = 0; i < contours2.size(); i++) {
                double test = Imgproc.pointPolygonTest(new MatOfPoint2f(contours2.get(i).toArray()), new Point(centerX, centerY), false);
                if (test > -1) {
                    //RECT TEST START
                    // Imgproc.drawContours(originalImageMat, contours2, i, new Scalar(0, 255, 0), contourThickness);
                    Rect rect = Imgproc.boundingRect(contours2.get(i));
                    Point tl = rect.tl();
                    Point br = rect.br();
                    Imgproc.rectangle(originalImageMat, tl, br, new Scalar(0, 255, 0), contourThickness);
                    //TEST RECT END
                }
            }
            //Imgproc.rectangle(originalImageMat, new Point(centerX - smallPlateRectangleWidth, centerY - smallPlateRectangleHeight), new Point(centerX + smallPlateRectangleWidth, centerY + smallPlateRectangleHeight), new Scalar(0, 255, 0));
        }

        return originalImageMat;
    }


    
    public Mat morphologicalTransformationsRecognitionMethodForVideo(Mat originalImageMat, Mat grayScaleMat, Mat processingMat, int distanceFromPlate) {

        int originalMatHeight = originalImageMat.rows();


        Imgproc.cvtColor(originalImageMat, grayScaleMat, Imgproc.COLOR_RGB2GRAY, 8);

        Imgproc.equalizeHist(grayScaleMat, processingMat);
        //distanceFromPlate CLOSE 0,1,2,3,4 FAR
        //moj obrazek uznaje ze jest 1 -> h = 13
        //KERNEL START
        //Wielkość zależy od wysokości tablicy rejestracyjnej(h) -> kernel = h/5
        //TUTAJ POWINIENEM DOROBIC TO ABY UZYTKOWNIK MOGL WYBRAC CZY REJSTRACJA DALEKO/SREDNIO/BLISKO
        int h;
        switch (distanceFromPlate) {
            case 0:
                h = originalMatHeight / 9;
                break;
            case 1:
                h = originalMatHeight / 13;
                break;
            case 2:
                h = originalMatHeight / 17;
                break;
            case 3:
                h = originalMatHeight / 21;
                break;
            default:
                h = originalMatHeight / 25;
                break;

        }


        int kernelSize = h / 5;
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(kernelSize, kernelSize));
        Imgproc.morphologyEx(processingMat, processingMat, Imgproc.MORPH_TOPHAT, kernel);
        Imgproc.morphologyEx(processingMat, processingMat, Imgproc.MORPH_BLACKHAT, kernel);

        kernelSize = h * 4;
        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(kernelSize, 1));
        Imgproc.morphologyEx(processingMat, processingMat, Imgproc.MORPH_CLOSE, kernel);

        kernelSize = h / 2;
        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, kernelSize));
        Imgproc.morphologyEx(processingMat, processingMat, Imgproc.MORPH_OPEN, kernel);

        kernelSize = h * 2;
        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(kernelSize, 1));
        Imgproc.morphologyEx(processingMat, processingMat, Imgproc.MORPH_OPEN, kernel);

        //TUTAJ OBCZAIC CZY 20 to odpowiednia wartosc
        double maxPixelValue = Core.minMaxLoc(processingMat).maxVal;
        Imgproc.threshold(processingMat, processingMat, maxPixelValue - 20, 255, Imgproc.THRESH_BINARY);

        //TUTAJ RYZYKOWNY KERNEL
        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(h * 1.25, h / 3));
        Imgproc.dilate(processingMat, processingMat, kernel);

        //kontury

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(processingMat, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);


        int contourThickness = originalMatHeight / 150;
        for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(originalImageMat, contours, i, new Scalar(0, 0, 255), contourThickness);
        }

        return originalImageMat;
    }


    public Mat edgeContourRecognitionMethodForVideo(Mat originalImageMat, Mat grayScaleMat, Mat tempMat, Mat dilateErodeMat, Mat processingMat, int distanceFromPlate) {

        int originalMatHeight = originalImageMat.rows();


        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();



        Imgproc.cvtColor(originalImageMat, grayScaleMat, Imgproc.COLOR_RGB2GRAY, 8);

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
                closeKernelHeight = originalMatHeight / 20;
                closeKernelWidth = closeKernelHeight * 4;
                break;
            case 1:
                dilateErodeKernel = 11;
                gaussianBlurKernel = 11;
                sobelDx = 1;
                secondDilateKernel = 9;
                closeKernelHeight = originalMatHeight / 30;
                closeKernelWidth = closeKernelHeight * 3;
                break;
            case 2:
                dilateErodeKernel = 9;
                gaussianBlurKernel = 9;
                sobelDx = 1;
                secondDilateKernel = 7;
                closeKernelHeight = originalMatHeight / 40;
                closeKernelWidth = closeKernelHeight * 3;
                break;
            case 3:
                dilateErodeKernel = 7;
                gaussianBlurKernel = 7;
                sobelDx = 1;
                secondDilateKernel = 7;
                closeKernelHeight = originalMatHeight / 56;
                closeKernelWidth = closeKernelHeight * 3;
                break;
            default:
                dilateErodeKernel = 3;
                gaussianBlurKernel = 3;
                sobelDx = 1;
                secondDilateKernel = 3;
                closeKernelHeight = originalMatHeight / 113;
                closeKernelWidth = closeKernelHeight * 3;
                break;

        }

        //TUTAJ distance from plate zmienic (3 dla małego, 9 dla średniego itp)
        Imgproc.dilate(grayScaleMat, tempMat, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(dilateErodeKernel, dilateErodeKernel)));
        Imgproc.erode(tempMat, dilateErodeMat, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(dilateErodeKernel, dilateErodeKernel)));

        Core.absdiff(dilateErodeMat, grayScaleMat, processingMat);

        //3 dla daleko, 9 dla srednio itd
        Imgproc.GaussianBlur(processingMat, processingMat, new Size(gaussianBlurKernel, gaussianBlurKernel), 0);

        //tutaj z dx można pomyśleć tylko tutaj tylko można 1 albo 2
        Imgproc.Sobel(processingMat, processingMat, CvType.CV_8U, sobelDx, 0, 3, 1, 0);
        double maxPixelValue = Core.minMaxLoc(processingMat).maxVal;

        //Zmenilem z -70 na -40
        Imgproc.threshold(processingMat, processingMat, maxPixelValue - 40, 255, Imgproc.THRESH_BINARY);

        //dla małych obbrazów np 3 dla srednich np 7
        Imgproc.dilate(processingMat, processingMat, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(secondDilateKernel, secondDilateKernel)));

        //dla wiekszych obrazów więcej - dla małego 27,9 jest ok
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(closeKernelWidth, closeKernelHeight));

        Imgproc.morphologyEx(processingMat, processingMat, Imgproc.MORPH_CLOSE, element);

        Imgproc.findContours(processingMat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        int contourThickness = originalMatHeight / 150;
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
            //TEST RECT START
            //Imgproc.drawContours(originalImageMat, contours, biggestContourIndex, new Scalar(255, 0, 0), contourThickness);
            Rect rect = Imgproc.boundingRect(contours.get(biggestContourIndex));
            Point tl = rect.tl();
            Point br = rect.br();
            Imgproc.rectangle(originalImageMat,tl,br,new Scalar(255, 0, 0), contourThickness);
            //TEST RECT END
        }
        return originalImageMat;
    }
}
