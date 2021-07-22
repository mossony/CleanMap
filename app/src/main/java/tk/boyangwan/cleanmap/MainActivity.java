package tk.boyangwan.cleanmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView locationInfo;

    LocationClient mLocationClient;

    MapView mMapView;
    BaiduMap mBaiduMap = null;
    boolean isFirstLocate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        locationInfo = findViewById(R.id.locationInfo);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new mLocationListener());
        mMapView = findViewById(R.id.map_view);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMyLocationEnabled(true);


        List<String> permissions = new ArrayList<String>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissions.isEmpty()){
            String [] pms = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, pms,1);
        }
        else {
            requestLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0){
                    for (int result: grantResults){
                        if (result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this, "Allow the permissions to use Clean Map", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }
                else {
                    Toast.makeText(this, "An error occured", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void requestLocation(){
        initLocation();
        mLocationClient.start();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd0911");
        option.setScanSpan(1000);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.SetIgnoreCacheException(false);
        option.setIgnoreKillProcess(false);
        option.setWifiCacheTimeOut(5*60*1000);
        option.setEnableSimulateGps(false);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }



    private class mLocationListener extends BDAbstractLocationListener{

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            setMapLocationTo(bdLocation);
            /*
            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("Latitude: ").append(bdLocation.getLatitude()).append("\n");
            currentPosition.append("Longitude: ").append(bdLocation.getLongitude()).append("\n");
            currentPosition.append("Country: ").append(bdLocation.getCountry()).append("\n");
            currentPosition.append("Province: ").append(bdLocation.getProvince()).append("\n");
            currentPosition.append("City: ").append(bdLocation.getCity()).append("\n");
            currentPosition.append("District: ").append(bdLocation.getDistrict()).append("\n");
            currentPosition.append("Town: ").append(bdLocation.getTown()).append("\n");
            currentPosition.append("Street: ").append(bdLocation.getStreet()).append("\n");
            currentPosition.append("Address: ").append(bdLocation.getAddrStr()).append("\n");
            currentPosition.append("Positioning method: ");
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation){
                currentPosition.append("GPS");
            }
            else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){
                currentPosition.append("Internet");
            }
            locationInfo.setText(currentPosition);

             */
        }
    }

    private void setMapLocationTo(BDLocation location){
        if (isFirstLocate) {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            mBaiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.longitude(location.getLongitude());
        locationBuilder.latitude(location.getLatitude());
        MyLocationData locationData = locationBuilder.build();
        mBaiduMap.setMyLocationData(locationData);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }
}