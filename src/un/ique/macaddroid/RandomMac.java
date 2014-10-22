package un.ique.macaddroid;

import android.app.Activity;
import android.os.Bundle;
import un.ique.macaddroid.Layer2Address;
import un.ique.macaddroid.NativeIOCtller;
import android.widget.TextView;
import android.view.View;
import android.content.Intent;

public class RandomMac extends Activity {
    // Let's hardcode wlan0, for now
    private String dev = "wlan0";
    private Layer2Address mNewNet;

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
}
