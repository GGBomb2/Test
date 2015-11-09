package com.example.test.app;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.*;

/**
 * Created by GGBomb2 on 2015/11/8.
 */
public class GuideFragment extends Fragment {
    View view=null;
    //���·�߽ڵ����
    Button mBtnPre = null;//��һ���ڵ�
    Button mBtnNext = null;//��һ���ڵ�
    RelativeLayout.LayoutParams mlayoutParams=null;


    //��ͼ��أ�ʹ�ü̳�MapView��MyRouteMapViewĿ������дtouch�¼�ʵ�����ݴ���
    //���������touch�¼���������̳У�ֱ��ʹ��MapView����
    //MapView mMapView = null;    // ��ͼView
    //BaiduMap mBaidumap = null;
    //��λ���
    //LocationClient mLocClient;  //��λͼ��
    //public MyLocationListenner myListener =null;
    //boolean isFirstLoc = true;// �Ƿ��״ζ�λ


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        view= inflater.inflate(R.layout.guide,container,false);
        /*
        CharSequence titleLable = "·�߹滮����";
        //��ʼ����ͼ
        mMapView = (MapView) view.findViewById(R.id.bmapView);
        mBaidumap = mMapView.getMap();
        //��λ���
        float zoomLevel=15.0f;
        MapStatusUpdate u= MapStatusUpdateFactory.zoomTo(zoomLevel);
        mBaidumap.animateMapStatus(u);
        mBaidumap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, false, null));
        myListener= new MyLocationListenner();
        // ������λͼ��
        mBaidumap.setMyLocationEnabled(true);

        // ��λ��ʼ��
        mLocClient = new LocationClient(this.getActivity());
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// ��gps
        option.setCoorType("bd09ll"); // ������������
        option.setScanSpan(1000);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mLocClient.setLocOption(option);

        mLocClient.start();
        */
        //
        RelativeLayout rly=(RelativeLayout)getActivity().findViewById(R.id.relativeLayout);
        mlayoutParams=(RelativeLayout.LayoutParams)rly.getLayoutParams();
        RelativeLayout rly2=(RelativeLayout)view.findViewById(R.id.relativeLayout_test);
        RelativeLayout.LayoutParams layoutParams=(RelativeLayout.LayoutParams)rly2.getLayoutParams();
        rly.setLayoutParams(layoutParams);
        mBtnPre = (Button) view.findViewById(R.id.pre);
        mBtnNext = (Button) view.findViewById(R.id.next);
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);

        Log.i(getTag(),"GuideFragment onCreated!");
        //������Ϣ����
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).nodeClick(v);
            }
        });
        mBtnPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).nodeClick(v);
            }
        });
        Button btn_search=(Button)view.findViewById(R.id.walk);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).SearchButtonProcess(v);
            }
        });
        btn_search=(Button)view.findViewById(R.id.drive);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnPre.setVisibility(View.INVISIBLE);
                mBtnNext.setVisibility(View.INVISIBLE);
                ((MainActivity)getActivity()).SearchButtonProcess(v);
            }
        });
        btn_search=(Button)view.findViewById(R.id.transit);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnPre.setVisibility(View.INVISIBLE);
                mBtnNext.setVisibility(View.INVISIBLE);
                ((MainActivity)getActivity()).SearchButtonProcess(v);
            }
        });
        btn_search=(Button)view.findViewById(R.id.customicon);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnPre.setVisibility(View.INVISIBLE);
                mBtnNext.setVisibility(View.INVISIBLE);
                ((MainActivity)getActivity()).changeRouteIcon(v);
            }
        });
        return view;
    }


    /**
     * ��λSDK��������
     */
    /*
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view ���ٺ��ڴ����½��յ�λ��
            if (location == null || mMapView == null)
                return;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // �˴����ÿ����߻�ȡ���ķ�����Ϣ��˳ʱ��0-360
                    .direction(100)
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaidumap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaidumap.animateMapStatus(u);
            }

        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }*/



    @Override
    public void onPause() {
        Log.i(getTag(),"GuideFragment onPaused!");
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.i(getTag(),"GuideFragment onResumed!");
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.i(getTag(),"GuideFragment onDestroyed!");
        ((MainActivity)getActivity()).mGuideFragment=null;
        RelativeLayout rly=(RelativeLayout)getActivity().findViewById(R.id.relativeLayout);
        rly.setLayoutParams(mlayoutParams);
        //mSearch.destroy();
        super.onDestroy();
    }

}
