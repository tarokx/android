




package com.example.gpstest;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import android.location.Location;
import android.widget.TextView;
import android.content.Intent;
import android.provider.Settings;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnMapLoadedCallback, LocationListener {

    private GoogleMap mMap = null;
    LocationManager locationManager;

    private DatabaseHelper helper;

    //緯度経度の桁数制限
    DecimalFormat df = new DecimalFormat();

    //Widget宣言
    TextView txt01;
    Button btn01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        df.setMaximumFractionDigits(3);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //GPS起動
        locationStart();

        //
        //Widget初期化
        txt01 = findViewById(R.id.txt01);
        btn01 = findViewById(R.id.btn01);

        //ボタンクリック
        btn01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //httpリクエスト
                try{
                    //okhttpを利用するカスタム関数（下記）
                    httpRequest("http://10.0.2.2:8080/map?data1=35&data2=140");
                }catch(Exception e){
                    Log.e("Hoge",e.getMessage());
                }

            }
        });
    }

    void httpRequest(String url) throws IOException {

        //OkHttpClinet生成
        OkHttpClient client = new OkHttpClient();

        //request生成
        Request request = new Request.Builder()
                .url(url)
                .build();

        //非同期リクエスト
        client.newCall(request)
                .enqueue(new Callback() {

                    //エラーのとき
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.e("Hoge",e.getMessage());
                    }

                    //正常のとき
                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                        //response取り出し
                        final String jsonStr = response.body().string();
                        Log.d("Hoge","jsonStr=" + jsonStr);

                        //JSON処理
                        try{
                            //jsonパース
                            JSONArray jsonArray = new JSONArray(jsonStr);
                            final ArrayList<String> list = new ArrayList<String>();

                            final ArrayList<Double> latitude = new ArrayList<Double>();

                            final ArrayList<Double> longitude = new ArrayList<Double>();

                            final ArrayList<String> name = new ArrayList<String>();
                            if (jsonArray != null) {
                                int len = jsonArray.length();
                                for (int i=0;i<len;i++){
                                    //list.add(jsonArray.get(i).toString());
                                    list.add(((JSONObject)jsonArray.get(i)).getString("name"));
                                    latitude.add(((JSONObject)jsonArray.get(i)).getDouble("longi"));
                                    longitude.add(((JSONObject)jsonArray.get(i)).getDouble("latut"));
                                    name.add(((JSONObject)jsonArray.get(i)).getString("name"));
                                }
                            }

                            final String[] items = list.toArray(new String[0]);
                            //親スレッドUI更新
                            Handler mainHandler = new Handler(Looper.getMainLooper());
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    new AlertDialog.Builder(MapsActivity.this)
                                            .setTitle("選択")
                                            .setItems(items, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    txt01.setText(items[which]);
                                                    setLocation(latitude.get(which), longitude.get(which), name.get(which));
                                                }
                                            })
                                            .show();
                                }
                            });



                        }catch(Exception e){
                            Log.e("Hoge",e.getMessage());
                        }

                    }
                });
    }

    //表示位置設定
    public void setLocation(double latitude,double longitude,String name) {
        Log.v("Map", "latitude = " + latitude + ", longitude = " + longitude);
        CameraPosition cameraPos = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude)).zoom(10.0f)
                .bearing(0).build();

        LatLng sydney = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(sydney).title(name));
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
    }

    private void DBset() {
        helper = new DatabaseHelper(this);
        try {
            helper.createDatabase();
        }
        catch (IOException e) {
            throw new Error("Unable to create database");
        }
        StringBuilder builder = new StringBuilder();

        SQLiteDatabase db = helper.getWritableDatabase();


        String sql = "select * from addressbook";

        try {
            Cursor cursor = db.rawQuery(sql, null);
            while(cursor.moveToNext()) {
                builder.append(cursor.getInt(0) + " ");
                builder.append(cursor.getString(1) + " ");
                builder.append(cursor.getString(2) + "\n");

                double lat = Double.parseDouble(cursor.getString(2));
                double lon = Double.parseDouble(cursor.getString(1));

                // Add a marker in Sydney and move the camera
                LatLng sydney = new LatLng(lat, lon);
                mMap.addMarker(new MarkerOptions().position(sydney).title(sydney.toString()));
            }
        } finally {
            db.close();
        }
        Toast.makeText(this, builder.toString(), Toast.LENGTH_LONG).show();
    }

    //住所から緯度経度取得
    public void onGetLocation(View view) {
        Geocoder gcoder = new Geocoder(this, Locale.getDefault());
        List<Address> lstAddr;
        EditText name = findViewById(R.id.editAddr);

        SQLiteDatabase db = helper.getWritableDatabase();

        helper = new DatabaseHelper(this);
        try {
            helper.createDatabase();
        }
        catch (IOException e) {
            throw new Error("Unable to create database");
        }
        ContentValues cv = new ContentValues();


        try {
            // 位置情報の取得
            lstAddr = gcoder.getFromLocationName(name.getText().toString(), 1);
            if (lstAddr != null && lstAddr.size() > 0) {
                // 緯度/経度取得
                Address addr = lstAddr.get(0);
                double latitude = Double.parseDouble(df.format(addr.getLatitude()));
                double longitude = Double.parseDouble(df.format(addr.getLongitude()));
                Toast.makeText(this, "位置\n緯度:" + latitude + "\n経度:" + longitude, Toast.LENGTH_LONG).show();
                /* 地図表示 */
                LatLng sydney = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(sydney).title(sydney.toString()));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

                cv = new ContentValues();
                cv.put("seq", getToday());
                cv.put("long", longitude);
                cv.put("lati", latitude);

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



        try {
            db.insert("addressbook", null,cv);

            Toast.makeText(this, cv.toString(), Toast.LENGTH_LONG).show();

        } finally {
            db.close();
        }

    }
    private int getToday() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmm", Locale.getDefault());
        return Integer.parseInt(sdf.format(date));
    }

    //緯度経度から住所取得
    public void onGetAddress(View view) {
        // 中心位置の緯度/経度取得
        CameraPosition cameraPos = mMap.getCameraPosition();
        //Toast.makeText(this, "中心位置\n緯度:" + cameraPos.target.latitude +
        //        "\n経度:" + cameraPos.target.longitude, Toast.LENGTH_LONG).show();

        double latitude = Double.parseDouble(df.format(cameraPos.target.latitude));
        double longitude = Double.parseDouble(df.format(cameraPos.target.longitude));
        // 住所の取得
        StringBuffer strAddr = new StringBuffer();
        Geocoder gcoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> lstAddrs = gcoder.getFromLocation(latitude, longitude, 1);
            for (Address addr : lstAddrs) {
                int idx = addr.getMaxAddressLineIndex();
                for (int i = 0; i <= idx; i++) {
                    strAddr.append(addr.getAddressLine(i));
                    Log.v("addr", addr.getAddressLine(i));
                }
            }
            Toast.makeText(this, strAddr.toString(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //表示範囲取得
    public void onGetArea(View view) {
        Projection proj = mMap.getProjection();
        VisibleRegion vRegion = proj.getVisibleRegion();
        // 北東 = top/right, 南西 = bottom/left
        double topLatitude = vRegion.latLngBounds.northeast.latitude;
        double bottomLatitude = vRegion.latLngBounds.southwest.latitude;
        double leftLongitude = vRegion.latLngBounds.southwest.longitude;
        double rightLongitude = vRegion.latLngBounds.northeast.longitude;
        Toast.makeText(this, "地図表示範囲\n緯度:" + bottomLatitude + "～" + topLatitude +
                "\n経度:" + leftLongitude + "～" + rightLongitude, Toast.LENGTH_LONG).show();
    }

    //表示位置設定
    public void onSetFuji(View view) {
        // 富士山の位置：北緯35度21分39秒, 東経138度43分39秒
        double latitude = 35.0d + 21.0d / 60 + 39.0d / (60 * 60);
        double longitude = 138.0d + 43.0d / 60 + 39.0d / (60 * 60);
        Log.v("Map", "latitude = " + latitude + ", longitude = " + longitude);
        CameraPosition cameraPos = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude)).zoom(10.0f)
                .bearing(0).build();

        LatLng sydney = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(sydney).title("富士山"));
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
    }

    //緯度軽度取得
    public void onGetCenter(View view) {
        CameraPosition cameraPos = mMap.getCameraPosition();
        Toast.makeText(this, "中心位置\n緯度:" + cameraPos.target.latitude + "\n経度:" + cameraPos.target.longitude, Toast.LENGTH_LONG).show();
    }

    //マップ開始時設定
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLoadedCallback(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "許可なし", Toast.LENGTH_LONG).show();

            return;
        } else {
/*
            //スマホ
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000, 50, this);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            */
            //エミュレータ
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000, 50, this);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            double lat = location.getLatitude();
            double lon = location.getLongitude();

            // Add a marker in Sydney and move the camera
            LatLng sydney = new LatLng(lat, lon);
            mMap.addMarker(new MarkerOptions().position(sydney).title(sydney.toString()));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        }
    }

    private void locationStart() {
        Log.d("debug", "locationStart()");

        // LocationManager インスタンス生成
        locationManager =
                (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Log.d("debug", "location manager Enabled");
        } else {
            // GPSを設定するように促す
            Intent settingsIntent =
                    new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
            Log.d("debug", "not gpsEnable, startActivity");
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);

            Log.d("debug", "checkSelfPermission false");
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                1000, 50, this);

    }

    // 結果の受け取り
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("debug", "checkSelfPermission true");


                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                        (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                }else {
                    locationStart();
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    double lat = location.getLatitude();
                    double lon = location.getLongitude();

                    // Add a marker in Sydney and move the camera
                    LatLng sydney = new LatLng(lat, lon);
                    mMap.addMarker(new MarkerOptions().position(sydney).title(sydney.toString()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                }
            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(this,
                        "これ以上なにもできません", Toast.LENGTH_SHORT);
                toast.show();
            }

        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // 緯度の表示
        TextView textView1 = (TextView) findViewById(R.id.text_view1);
        String str1 = "Latitude:" + location.getLatitude();
        textView1.setText(str1);

        // 経度の表示
        TextView textView2 = (TextView) findViewById(R.id.text_view2);
        String str2 = "Longtude:" + location.getLongitude();
        textView2.setText(str2);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //マップロード完了時動作
    @Override
    public void onMapLoaded() {
        if (mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
            Log.v("Map", "Zoom Level = " + mMap.getCameraPosition().zoom);
        }
        // 地図の描画完了時の処理を実装
        Toast.makeText(this, "地図の描画完", Toast.LENGTH_LONG).show();

        //データベース
        DBset();
    }
}