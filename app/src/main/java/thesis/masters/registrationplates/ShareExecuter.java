package thesis.masters.registrationplates;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ShareExecuter {

    public Intent prepareFileToShare (Bitmap bitmap, String filename) {

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/png");
        ByteArrayOutputStream bytaArray = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytaArray);
        File imageFile = new File(Environment.getExternalStorageDirectory() + File.separator + filename + ".png");
        try {
            imageFile.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
            fileOutputStream.write(bytaArray.toByteArray());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
        return share;
    }
}
