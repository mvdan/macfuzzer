package un.ique.macaddroid;

import android.app.Activity;
import android.os.Bundle;
import un.ique.macaddroid.Layer2Address;
import un.ique.macaddroid.NativeIOCtller;
import android.widget.TextView;
import android.view.View;
import android.content.Intent;
import java.lang.Process;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.File;
import android.content.res.Resources;
import android.content.Context;

public class RandomMac extends Activity {
    // Let's hardcode wlan0, for now
    private String dev = "wlan0";
    private Layer2Address mNewNet;
    private final String binaryName = "change_mac";
    private String pathToBinary = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.randommac);

        Layer2Address newNet = new Layer2Address();
        newNet.setInterfaceName(dev);
        NativeIOCtller ctller = new NativeIOCtller(newNet);
        newNet.setAddress(ctller.getCurrentMacAddr());
        String addr = newNet.formatAddress();
        TextView macField = (TextView)
            findViewById(R.id.randommac_macaddress);
        if (macField != null) {
            macField.setText(addr);
        }
        byte[] nextAddr = newNet.generateNewAddress();
        mNewNet = new Layer2Address(nextAddr, dev);
        TextView nextMacField = (TextView)
            findViewById(R.id.randommac_nextmacaddress);
        if (nextMacField != null) {
            nextMacField.setText(mNewNet.formatAddress());
        }

        copyBinary();
        getPathToFile();
        File cm = new File(pathToBinary, binaryName);
        if (cm.exists() && cm.isFile()) {
            cm.setExecutable(true);
        } else {
            finish();
        }
    }

    public void showNewAddress(View view) {
        byte[] nextAddr = mNewNet.generateNewAddress();
        mNewNet = new Layer2Address(nextAddr, dev);
        TextView nextMacField = (TextView)
            findViewById(R.id.randommac_nextmacaddress);
        if (nextMacField != null) {
            nextMacField.setText(mNewNet.formatAddress());
        }
    }

    public void applyNewAddress(View view) {
        NativeIOCtller ctller = new NativeIOCtller(mNewNet);
        int err = 11;
        String uid = Integer.toString(ctller.getCurrentUID());
        try {
            String[] args = {"su", "0", "/data/data/un.ique.macaddroid/files/change_mac", dev, mNewNet.formatAddress(), uid};
            Process root_shell = Runtime.getRuntime().exec(args);
            try {
                 root_shell.waitFor();
                 err = root_shell.exitValue();
            } catch (InterruptedException e) {
            }
        } catch (IOException e) {
            return;
        }
        //int err = ctller.setMacAddr(mNewNet.getAddress());
        String errorcode = ctller.getErrorString(err);
        mNewNet.setAddress(ctller.getCurrentMacAddr());
        String addr = mNewNet.formatAddress();
        TextView macField = (TextView)
            findViewById(R.id.randommac_macaddress);
        if (macField != null) {
            macField.setText(addr);
        }
    }

    public void copyBinary() {
        try {
        InputStream is = getResources().openRawResource(R.raw.change_mac);
        FileOutputStream fos = openFileOutput(binaryName, Context.MODE_PRIVATE);
        int bytesRead = -1, round = 0;
        byte[] byteBuffer = new byte[100];
        while (bytesRead != 0) {
            bytesRead = is.read(byteBuffer, round, round + byteBuffer.length);
            if (bytesRead != byteBuffer.length) {
                fos.write(byteBuffer, round, round + bytesRead);
                break;
            }
            fos.write(byteBuffer, byteBuffer.length*round, round + byteBuffer.length);
        }
        fos.close();
        is.close();
        } catch ( FileNotFoundException e) {
        } catch ( IOException e) {
        }
    }

    public void getPathToFile() {
        File file = getFilesDir();
        if (!file.isDirectory())
            return;
        pathToBinary = file.getAbsolutePath();
     }
}
