package thesis.masters.registrationplates;

import android.content.Context;
import java.io.InputStream;
import java.util.Scanner;

public class PlateDetailsInformator {
    private Context context;

    public PlateDetailsInformator(Context context){
        this.context = context;
    }

    public String getPolishPlateDetailedInfo(String plate){
        String stringToReturn = "";
        String wojewodztwoFromPlate = plate.substring(0,1);
        boolean recognizedWojewodztwo = false;
        String powiatFromPlate3 = plate.substring(0,3);
        boolean recognizedpowiatFromPlate3 = false;
        String powiatFromPlate2 = plate.substring(0,2);
        String warsawEndFromPlate1 = plate.substring(plate.length()-1);
        String warsawEndFromPlate2 = plate.substring(plate.length()-2);
        String[] wojewodztwoFromFile;
        String[] powiatFromFile;
        String[] sluzbyFromFile;
        String[] warsawFromFile;
        boolean endOnFirstStep = false;

        if(wojewodztwoFromPlate.equals("H")) {
            InputStream inputStreamSluzby = this.context.getResources().openRawResource(R.raw.sluzby);
            Scanner scSluzby = new Scanner(inputStreamSluzby);
            while (scSluzby.hasNextLine()){
                sluzbyFromFile = scSluzby.nextLine().split(":");
                if (powiatFromPlate2.equals(sluzbyFromFile[0])) {
                    stringToReturn = " Służby wewnętrzne: " + sluzbyFromFile[1];
                    endOnFirstStep = true;
                    break;
                }
            }
            scSluzby.close();
        } else {
            InputStream inputStreamWarsawNormal = this.context.getResources().openRawResource(R.raw.warszawa_normal);
            Scanner scWarsawNormal = new Scanner(inputStreamWarsawNormal);
            while (scWarsawNormal.hasNextLine()) {
                warsawFromFile = scWarsawNormal.nextLine().split(":");
                if (powiatFromPlate2.equals(warsawFromFile[0])) {
                    stringToReturn = "Województwo: mazowieckie\nMiasto: Warszawa\nDzielnica: " + warsawFromFile[1];
                    endOnFirstStep = true;
                    break;
                }
            }
            scWarsawNormal.close();
        }
        if (!endOnFirstStep) {
            if (powiatFromPlate2.equals("WW")) {
                if (warsawEndFromPlate2.equals("EL")) {
                    stringToReturn = "Województwo: mazowieckie\nMiasto: Warszawa\nDzielnica: Rembertów";
                } else {
                    InputStream inputStreamWarsawWW = this.context.getResources().openRawResource(R.raw.warszawa_ww);
                    Scanner scWarsawWW = new Scanner(inputStreamWarsawWW);
                    while (scWarsawWW.hasNextLine()) {
                        warsawFromFile = scWarsawWW.nextLine().split(":");
                        if (warsawEndFromPlate1.equals(warsawFromFile[0])) {
                            stringToReturn = "Województwo: mazowieckie\nMiasto: Warszawa\nDzielnica: " + warsawFromFile[1];
                            break;
                        }
                    }
                    scWarsawWW.close();
                }
            } else if (powiatFromPlate2.equals("WX")) {
                if (warsawEndFromPlate2.equals("YX") || warsawEndFromPlate2.equals("YV") || warsawEndFromPlate2.equals("YZ")) {
                    stringToReturn = "Województwo: mazowieckie\nMiasto: Warszawa\nDzielnica: Wesoła";
                } else {
                    stringToReturn = "Województwo: mazowieckie\nMiasto: Warszawa\nDzielnica: Żoliborz";
                }
            } else if (powiatFromPlate2.equals("WY")) {
                if (warsawEndFromPlate2.equals("YY"))
                    stringToReturn = "Województwo: mazowieckie\nMiasto: Warszawa\nDzielnica: Sulejówek";
                else
                    stringToReturn = "Województwo: mazowieckie\nMiasto: Warszawa\nDzielnica: Wola";
            } else {
                InputStream inputStream = this.context.getResources().openRawResource(R.raw.wojewodztwa);
                Scanner sc = new Scanner(inputStream);
                while (sc.hasNextLine()) {
                    wojewodztwoFromFile = sc.nextLine().split(":");
                    if (wojewodztwoFromPlate.equals(wojewodztwoFromFile[0])) {
                        stringToReturn = "Województwo: " + wojewodztwoFromFile[1];
                        recognizedWojewodztwo = true;
                        break;
                    }
                }
                sc.close();
                if (recognizedWojewodztwo) {
                    InputStream inputStream1 = this.context.getResources().openRawResource(R.raw.powiaty);
                    Scanner sc1 = new Scanner(inputStream1);
                    while (sc1.hasNextLine()) {
                        powiatFromFile = sc1.nextLine().split(":");
                        if (powiatFromPlate3.equals(powiatFromFile[0])) {
                            stringToReturn += "\nPowiat: " + powiatFromFile[1];
                            recognizedpowiatFromPlate3 = true;
                            break;
                        }
                    }
                    sc1.close();
                    if (!recognizedpowiatFromPlate3) {
                        InputStream inputStream2 = this.context.getResources().openRawResource(R.raw.powiaty);
                        Scanner sc2 = new Scanner(inputStream2);
                        sc2 = new Scanner(inputStream2);
                        while (sc2.hasNextLine()) {
                            powiatFromFile = sc2.nextLine().split(":");
                            if (powiatFromPlate2.equals(powiatFromFile[0])) {
                                stringToReturn = "\nPowiat: " + powiatFromFile[1];
                                break;
                            }
                        }
                        sc2.close();
                    }
                }
            }
        }
        return stringToReturn;
    }

}
