package carnero.netmap.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import carnero.netmap.R;
import carnero.netmap.fragment.NetMapFragment;
import carnero.netmap.service.MainService;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        final Intent intent = new Intent(this, MainService.class);
        startService(intent);

        setContentView(R.layout.activity_main);

        if (state == null) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new NetMapFragment())
                    .commit();
        }
    }
}