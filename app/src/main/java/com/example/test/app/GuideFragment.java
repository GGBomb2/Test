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
    //浏览路线节点相关
    Button mBtnPre = null;//上一个节点
    Button mBtnNext = null;//下一个节点
    RelativeLayout.LayoutParams mlayoutParams=null;


    //地图相关，使用继承MapView的MyRouteMapView目的是重写touch事件实现泡泡处理
    //如果不处理touch事件，则无需继承，直接使用MapView即可
    //MapView mMapView = null;    // 地图View
    //BaiduMap mBaidumap = null;
    //定位相关
    //LocationClient mLocClient;  //定位图层
    //public MyLocationListenner myListener =null;
    //boolean isFirstLoc = true;// 是否首次定位


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        view= inflater.inflate(R.layout.guide,container,false);
        /*
        CharSequence titleLable = "路线规划功能";
        //初始化地图
        mMapView = (MapView) view.findViewById(R.id.bmapView);
        mBaidumap = mMapView.getMap();
        //定位相关
        float zoomLevel=15.0f;
        MapStatusUpdate u= MapStatusUpdateFactory.zoomTo(zoomLevel);
        mBaidumap.animateMapStatus(u);
        mBaidumap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, false, null));
        myListener= new MyLocationListenner();
        // 开启定位图层
        mBaidumap.setMyLocationEnabled(true);

        // 定位初始化
        mLocClient = new LocationClient(this.getActivity());
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
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
        //按键消息监听
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
     * 定位SDK监听函数
     */
    /*
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null)
                return;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
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
