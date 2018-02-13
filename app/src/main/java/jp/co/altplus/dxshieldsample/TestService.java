package jp.co.altplus.dxshieldsample;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class TestService extends Service {

    private Timer timer = null;
    private int count = 0;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("DxShield", "onStartCommand");

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("DxShield", "count = " + count);
                count++;
            }
        }, 0, 1000);

        startLocationListener();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("DxShield", "onDestroy");
        super.onDestroy();
        // timer cancel
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startLocationListener() {

        Log.d("DxShield", "Starting to get a GPS location.");

        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        final LocationSettingsRequest locationSettingsRequest = builder.build();

        mSettingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.d("DxShield", "All location settings are satisfied.");

                        if (ActivityCompat.checkSelfPermission(TestService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(TestService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Log.d("DxShield", "Permission denied.");
                            return;
                        }
                        mFusedLocationClient.requestLocationUpdates(locationRequest,
                                new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        Location location = locationResult.getLastLocation();
                                        Log.d("DxShield",
                                                String.format(
                                                        "%f, %f",
                                                        location.getLatitude(),
                                                        location.getLongitude()));

                                    }
                                }, Looper.myLooper());

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        Log.e("DxShield", "Failed to add a listener," + statusCode);
                    }
                });

    }


    /**
     * Scanrisk for 4.3.3
     * DxShield Engineが攻撃検出した際に通知する。
     *
     * @param policy 管理画面上でチェックを入れた項目は1、 そうでないものは0になる。
     * @param code 検知コード
     * @param name 脅威名称
     * @param reason 検知理由
     * @param version 検知エンジンバージョン
     */
//    public static void Scanrisk(
//            int policy,        // ポリシー (0: 許可, 1: 違反)
//            String code,       // 検知コード
//            String name,       // 脅威名称
//            String reason,     // 検知理由
//            String version     // 検知エンジンバージョン
//    ){
//    }

    /**
     * Scanrisk for 4.4.1
     * DxShield Engineが攻撃検出した際に通知する。
     *
     * @param policy 管理画面上でチェックを入れた項目は1、 そうでないものは0になる。
     * @param code 検知コード
     * @param name 脅威名称
     * @param reason 検知理由
     * @param version 検知エンジンバージョン
     * @param packageMD5 パッケージのMD5ハッシュ
     * @param android_Id 端末のAndroidID
     */
    public static void Scanrisk(
            int policy,        // ポリシー (0: 許可, 1: 違反)
            String code,       // 検知コード
            String name,       // 脅威名称
            String reason,     // 検知理由
            String version,    // 検知エンジンバージョン
            String packageMD5, // パッケージのMD5ハッシュ
            String android_Id  // 端末のAndroidID
    ){
        try {
            final String message = String.format(Locale.JAPANESE, "攻撃内容: %d, %s, %s, %s, %s, %s, %s", policy, code, name, reason, version, packageMD5, android_Id);
            Log.e("DxShield", message);
            if (policy == 1) {
                // Activityが存在すれば、アラートを表示
                if (MainActivity.activity != null) {
                    MainActivity.activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(MainActivity.activity)
                                    .setTitle("攻撃を検知しました")
                                    .setMessage(message)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ;
                                        }
                                    })
                                    .create()
                                    .show();
                        }
                    });
                } else {
                }
            }
        } catch (Exception e) {
            Log.e("DxShield", e.getMessage());
        }
    }
}
