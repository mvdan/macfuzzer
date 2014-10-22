package un.ique.macaddroid;

import android.app.Activity;
import android.os.Bundle;
import un.ique.macaddroid.Layer2Address;
import un.ique.macaddroid.NativeIOCtller;
import android.widget.TextView;
import android.view.View;
import android.content.Intent;

public class Main extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Layer2Address newNet = new Layer2Address();
        // Let's hardcode wlan0, for now
        newNet.setInterfaceName("wlan0");
        NativeIOCtller ctller = new NativeIOCtller(newNet);
        newNet.setAddress(ctller.getCurrentMacAddr());
        String addr = newNet.formatAddress();
        TextView macField = (TextView)
            findViewById(R.id.main_macaddress);
        if (macField != null) {
            macField.setText(addr);
        }
    }

    public void callRandomMac(View view)
    {
        Intent intent = new Intent(this, RandomMac.class);
        startActivity(intent);
    }
}
