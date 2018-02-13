package jp.co.altplus.dxshieldsample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class EventReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("DxShield", "Receive a broadcast event.");
        Intent newIntent = new Intent(context, TestService.class);
        context.startService(newIntent);
    }
}
