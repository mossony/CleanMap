package tk.boyangwan.cleanmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActionBar;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText inputText;
    RadioButton standardRadio;
    RadioButton satelliteRadio;
    CheckBox heatBox;
    String currCity;
    LatLng ll;

    LocationClient mLocationClient;

    MapView mMapView;
    BaiduMap mBaiduMap = null;
    boolean isFirstLocate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

//        View decorView = getWindow().getDecorView();
//        // Hide the status bar.
//        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);
//        // Remember that you should never show the action bar if the
//        // status bar is hidden, so hide that too if necessary.
//        ActionBar actionBar = getActionBar();
//        actionBar.hide();

        inputText = findViewById(R.id.inputText);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new mLocationListener());
        mMapView = findViewById(R.id.map_view);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setTrafficEnabled(true);
        mBaiduMap.setMyLocationEnabled(true);
        standardRadio = findViewById(R.id.standardRadio);
        satelliteRadio = findViewById(R.id.satelliteRadio);
        heatBox = findViewById(R.id.heatBox);

        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchLocation(v.getText().toString());
                }
                return false;
            }
        });


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


    public void searchLocation(String location){
        PoiSearch mPoiSearch = PoiSearch.newInstance();
        OnGetPoiSearchResultListener listener = new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                if (poiResult == null
                        || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {// 没有找到检索结果
                    Toast.makeText(MainActivity.this, "No result found", Toast.LENGTH_LONG).show();
                    return;
                }

                if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {// 检索结果正常返回
                    mBaiduMap.clear();
                    PoiOverlay poiOverlay = new PoiOverlay(mBaiduMap);
                    poiOverlay.setData(poiResult);// 设置POI数据
                    poiOverlay.addToMap();// 将所有的overlay添加到地图上
                    poiOverlay.zoomToSpan();
                    //
                    Toast.makeText(MainActivity.this, poiResult.getTotalPoiNum()+"", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }

            @Override
            public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }

        };

        mPoiSearch.setOnGetPoiSearchResultListener(listener);
        mPoiSearch.searchNearby(new PoiNearbySearchOption()
                .location(ll)
                .keyword(location)
                .pageNum(0));

        mPoiSearch.destroy();
    }

    public void setMapType(View v){
        if (v == standardRadio){
            if (mBaiduMap.getMapType() != BaiduMap.MAP_TYPE_NORMAL){
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                Toast.makeText(this, "Standard map", Toast.LENGTH_SHORT).show();
            }
        }

        if (v == satelliteRadio){
            if (mBaiduMap.getMapType() != BaiduMap.MAP_TYPE_SATELLITE){
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                Toast.makeText(this, "Satellite map", Toast.LENGTH_SHORT).show();
            }
        }

        if (heatBox.isChecked()){
            mBaiduMap.setBaiduHeatMapEnabled(true);
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.zoom(12.0f);
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            Toast.makeText(this, "Loading heat map", Toast.LENGTH_SHORT).show();
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
            currCity = bdLocation.getCity();
        }
    }

    private void setMapLocationTo(BDLocation location){
        if (isFirstLocate) {
            ll = new LatLng(location.getLatitude(), location.getLongitude());
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