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


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        view= inflater.inflate(R.layout.guide,container,false);

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

        return view;
    }



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
