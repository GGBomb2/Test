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
    // ��λ���
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    // UI���
    boolean isFirstLoc = true;// �Ƿ��״ζ�λ
    //ָ����
    private static final int EXIT_TIME = 2000;// ���ΰ����ؼ��ļ���ж�
    private long firstExitTime = 0L;// ���������һ�ΰ����ؼ���ʱ��
    public String mData;
    public String address;
    private SensorManager mSensorManager;// �������������
    private Sensor mOrientationSensor;// ����������
    private float mTargetDirection;// Ŀ�긡�㷽��
    TextView mLatitudeTV;// γ��
    TextView mLongitudeTV;// ����
    //Fragments
    GuideFragment mGuideFragment=null;
    SurfaceFragment mSurfaceFragment=null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        //�����ޱ���
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //����ȫ��
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        setDefaultFragment();

        //��ȡ��ͼ�ؼ�����
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
        // ������λͼ��
        mBaiduMap.setMyLocationEnabled(true);

        // ��λ��ʼ��
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// ��gps
        option.setCoorType("bd09ll"); // ������������
        option.setScanSpan(1000);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mLocClient.setLocOption(option);

        //Compass
        //setContentView(R.layout.activity_main);
        initResources();// ��ʼ��view
        initServices();// ��ʼ����������λ�÷���
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
     * ��λSDK��������
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view ���ٺ��ڴ����½��յ�λ��
            if (location == null || mMapView == null)
                return;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // �˴����ÿ����߻�ȡ���ķ�����Ϣ��˳ʱ��0-360
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
            // sb.append("ʱ��: ");
            // sb.append(location.getTime());
            sb.append("Lat : ");
            sb.append(location.getLatitude() );
            sb.append(", Lon : ");
            sb.append(location.getLongitude());
//			sb.append(", ���� : ");
//			sb.append(location.getRadius() + " ��");
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
    protected void onPause() {// ����ͣ������������ע�������������λ�ø��·���

        // activity ��ͣʱͬʱ��ͣ��ͼ�ؼ�
        mMapView.onPause();
        super.onPause();
        if (mOrientationSensor != null) {
            mSensorManager.unregisterListener(mOrientationSensorEventListener);
        }
    }

    @Override
    protected void onResume() {// �ڻָ��������������жϡ�����λ�ø��·���ʹ���������

        mMapView.onResume();
        super.onResume();
        // activity �ָ�ʱͬʱ�ָ���ͼ�ؼ�
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
        // �˳�ʱ���ٶ�λ
        mLocClient.stop();
        // �رն�λͼ��
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed()
    //�����а����ؼ�ʱҪ�ͷ��ڴ�
    {
        // TODO Auto-generated method stub
        long curTime = System.currentTimeMillis();
        if (curTime - firstExitTime < EXIT_TIME) {// ���ΰ����ؼ���ʱ��С��2����˳�Ӧ��
            finish();
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Press again to Exit", Toast.LENGTH_SHORT).show();
            firstExitTime = curTime;
            super.onBackPressed();
        }
        //MainActivity.this.finish();

    }



    // ��ʼ��view
    private void initResources() {
        mTargetDirection = 0.0f;// ��ʼ��Ŀ�귽��

    }
    // ��ʼ����������λ�÷���
    private void initServices() {
        // sensor manager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mOrientationSensor = mSensorManager.getSensorList(
                Sensor.TYPE_ORIENTATION).get(0);//
        // Log.i("way", mOrientationSensor.getName());

        // location manager
        // mLocationManager = (LocationManager)
        // getSystemService(Context.LOCATION_SERVICE);
        // Criteria criteria = new Criteria();// �������󣬼�ָ���������˻��LocationProvider
        // criteria.setAccuracy(Criteria.ACCURACY_FINE);// �ϸ߾���
        // criteria.setAltitudeRequired(false);// �Ƿ���Ҫ�߶���Ϣ
        // criteria.setBearingRequired(false);// �Ƿ���Ҫ������Ϣ
        // criteria.setCostAllowed(true);// �Ƿ��������
        // criteria.setPowerRequirement(Criteria.POWER_LOW);// ���õ͵��
        // mLocationProvider = mLocationManager.getBestProvider(criteria,
        // true);// ��ȡ������õ�Provider

    }


    // ���򴫸����仯����
    private SensorEventListener mOrientationSensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float direction = event.values[mSensorManager.DATA_X] * -1.0f;
            mTargetDirection = normalizeDegree(direction);// ��ֵ��ȫ�ֱ�������ָ������ת
            // Log.i("way", event.values[mSensorManager.DATA_Y] + "");
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    // �������򴫸�����ȡ��ֵ
    private float normalizeDegree(float degree) {
        return (degree + 720) % 360;
    }


}
