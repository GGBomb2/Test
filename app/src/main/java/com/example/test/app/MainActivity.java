package com.example.test.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.hardware.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {
    MapView mMapView=null;
    BaiduMap mBaiduMap;
    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    // UI相关
    boolean isFirstLoc = true;// 是否首次定位
    //指南针
    private static final int EXIT_TIME = 2000;// 两次按返回键的间隔判断
    private long firstExitTime = 0L;// 用来保存第一次按返回键的时间
    public String mData;
    public String address;
    private SensorManager mSensorManager;// 传感器管理对象
    private Sensor mOrientationSensor;// 传感器对象
    private float mTargetDirection;// 目标浮点方向
    TextView mLatitudeTV;// 纬度
    TextView mLongitudeTV;// 经度
    //Fragments
    GuideFragment mGuideFragment=null;
    SurfaceFragment mSurfaceFragment=null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        //设置无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        setDefaultFragment();

        //获取地图控件引用
        //mMapView = (MapView) findViewById(R.id.bmapView);
        RelativeLayout raly=(RelativeLayout)findViewById(R.id.relativeLayout);
        mMapView=new MapView(this,new BaiduMapOptions().compassEnabled(false));
        raly.addView(mMapView);
        mMapView.showZoomControls(false);
        mMapView.bringToFront();
        mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;
        mBaiduMap = mMapView.getMap();
        float zoomLevel=18.0f;
        MapStatusUpdate u=MapStatusUpdateFactory.zoomTo(zoomLevel);
        mBaiduMap.animateMapStatus(u);
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mLocClient.setLocOption(option);

        //Compass
        //setContentView(R.layout.activity_main);
        initResources();// 初始化view
        initServices();// 初始化传感器和位置服务
        mLocClient.start();
        ////mCompassView.bringToFront();

    }

    private void setDefaultFragment()
    {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        mSurfaceFragment = new SurfaceFragment();
        transaction.replace(R.id.id_content, mSurfaceFragment);
        transaction.commit();
    }


    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null)
                return;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mTargetDirection)
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
            }
            //Compass
            StringBuffer sb = new StringBuffer(256);
            // sb.append("时间: ");
            // sb.append(location.getTime());
            sb.append("Lat : ");
            sb.append(location.getLatitude() );
            sb.append(", Lon : ");
            sb.append(location.getLongitude());
//			sb.append(", 精度 : ");
//			sb.append(location.getRadius() + " 米");
            mData = sb.toString();
            if(mLatitudeTV != null) mLatitudeTV.setText(sb);

            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                address = "Speed : " + location.getSpeed();
                // sb.append("\n??? : ");
                // sb.append(location.getSpeed());
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                // sb.append("\n??? : ");
                // sb.append(location.getAddrStr());
                address = "Address : " + location.getAddrStr();
            }
            if(mLongitudeTV !=null) mLongitudeTV.setText(address);


        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override
    protected void onPause() {// 在暂停的生命周期里注销传感器服务和位置更新服务

        // activity 暂停时同时暂停地图控件
        mMapView.onPause();
        super.onPause();
        if (mOrientationSensor != null) {
            mSensorManager.unregisterListener(mOrientationSensorEventListener);
        }
    }

    @Override
    protected void onResume() {// 在恢复的生命周期里判断、启动位置更新服务和传感器服务

        mMapView.onResume();
        super.onResume();
        // activity 恢复时同时恢复地图控件
        if (mOrientationSensor != null) {
            mSensorManager.registerListener(mOrientationSensorEventListener,
                    mOrientationSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            // Toast.makeText(this, R.string.cannot_get_sensor,
            // Toast.LENGTH_SHORT)
            // .show();
        }
        //mStopDrawing=false;
        //mHandler.postDelayed(mCompassViewUpdater,20);
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed()
    //无意中按返回键时要释放内存
    {
        // TODO Auto-generated method stub
        long curTime = System.currentTimeMillis();
        if (curTime - firstExitTime < EXIT_TIME) {// 两次按返回键的时间小于2秒就退出应用
            finish();
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Press again to Exit", Toast.LENGTH_SHORT).show();
            firstExitTime = curTime;
            super.onBackPressed();
        }
        //MainActivity.this.finish();

    }



    // 初始化view
    private void initResources() {
        mTargetDirection = 0.0f;// 初始化目标方向

    }
    // 初始化传感器和位置服务
    private void initServices() {
        // sensor manager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mOrientationSensor = mSensorManager.getSensorList(
                Sensor.TYPE_ORIENTATION).get(0);//
        // Log.i("way", mOrientationSensor.getName());

        // location manager
        // mLocationManager = (LocationManager)
        // getSystemService(Context.LOCATION_SERVICE);
        // Criteria criteria = new Criteria();// 条件对象，即指定条件过滤获得LocationProvider
        // criteria.setAccuracy(Criteria.ACCURACY_FINE);// 较高精度
        // criteria.setAltitudeRequired(false);// 是否需要高度信息
        // criteria.setBearingRequired(false);// 是否需要方向信息
        // criteria.setCostAllowed(true);// 是否产生费用
        // criteria.setPowerRequirement(Criteria.POWER_LOW);// 设置低电耗
        // mLocationProvider = mLocationManager.getBestProvider(criteria,
        // true);// 获取条件最好的Provider

    }


    // 方向传感器变化监听
    private SensorEventListener mOrientationSensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float direction = event.values[mSensorManager.DATA_X] * -1.0f;
            mTargetDirection = normalizeDegree(direction);// 赋值给全局变量，让指南针旋转
            // Log.i("way", event.values[mSensorManager.DATA_Y] + "");
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    // 调整方向传感器获取的值
    private float normalizeDegree(float degree) {
        return (degree + 720) % 360;
    }


}
