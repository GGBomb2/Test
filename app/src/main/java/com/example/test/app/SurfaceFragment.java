package com.example.test.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.location.Location;
import android.hardware.*;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.animation.AccelerateInterpolator;
import android.widget.*;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.utils.DistanceUtil;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.lang.Math;

/**
 * Created by GGBomb2 on 2015/11/6.
 */
public class SurfaceFragment extends Fragment implements SurfaceHolder.Callback{
    // UI���
    boolean isFirstLoc = true;// �Ƿ��״ζ�λ
    private Camera myCamera = null;  //����ͷ
    private boolean isPreview = false;
    private static final String tag="yan";
    private Camera.AutoFocusCallback myAutoFocusCallback = null;//�Զ��۽�
    private SurfaceView mPreviewSV = null; //Ԥ��SurfaceView
    private SurfaceHolder mySurfaceHolder = null;
    private GuideFragment mGuideFragment=null;
    public boolean isGuiding=false;//�Ƿ����ڵ���
    RouteLine mroute=null;//·��
    Object Nextstep=null;//��һ���нڵ�
    PoiInfo[] poiinfos=null;

    //ָ����
    private static final int EXIT_TIME = 2000;// ���ΰ����ؼ��ļ���ж�
    private long firstExitTime = 0L;// ���������һ�ΰ����ؼ���ʱ��
    public String mData;
    public String address;
    private final float MAX_ROATE_DEGREE = 1.0f;// �����תһ�ܣ���360��
    private SensorManager mSensorManager;// �������������
    private Sensor mOrientationSensor;// ����������
    private AccelerateInterpolator mInterpolator;// �����ӿ�ʼ���������仯����һ�����ٵĹ���,����һ����������
    CompassView mPointer;// ָ����view
    private float mDirection;// ��ǰ���㷽��
    private float mTargetDirection;// Ŀ�긡�㷽��
    private float mLRDirection;//���������
    private float mUDDirection;//���������
    private boolean mChinease;// ϵͳ��ǰ�Ƿ�ʹ������
    protected final Handler mHandler = new Handler();
    LinearLayout mDirectionLayout;// ��ʾ���򣨶�����������view
    LinearLayout mAngleLayout;// ��ʾ���������view
    View mViewGuide;
    //TextView mLatitudeTV;// γ��
    //TextView mLongitudeTV;// ����
    View view=null;
    View mCompassView;
    ImageView mGuideAnimation;
    private boolean mStopDrawing;// �Ƿ�ָֹͣ������ת�ı�־λ

    //�ڵ�ָ����
    CompassView rPointer;// �ڵ�ָ����view
    private float rDirection;// ��ǰ���㷽��
    private float rTargetDirection;// Ŀ�긡�㷽��
    protected final Handler rHandler = new Handler();
    protected final Handler mOverlayHandler=new Handler();//����ͼ�㴦��
    private boolean rStopDrawing;// �Ƿ�ֹͣ�ڵ�ָ������ת�ı�־λ


    // ����Ǹ��½ڵ�ָ������ת���̣߳�handler�����ʹ�ã�ÿ20�����ⷽ��仯ֵ����Ӧ���½ڵ�ָ������ת
    protected Runnable rCompassViewUpdater = new Runnable() {
        @Override
        public void run() {
            if (rPointer != null && !rStopDrawing) {
                if(mroute.getAllStep()==null)
                {
                    Toast.makeText(getActivity(),"getAllstep!=null",Toast.LENGTH_SHORT);
                }
                if (mroute != null || mroute.getAllStep() != null) {
                    Nextstep=mroute.getAllStep().get(1);
                }
                if (rDirection != rTargetDirection) {

                    // calculate the short routine
                    float to = rTargetDirection;
                    if (to - rDirection > 180) {
                        to -= 360;
                    } else if (to - rDirection < -180) {
                        to += 360;
                    }
                    // limit the max speed to MAX_ROTATE_DEGREE
                    float distance = to - rDirection;
                    if (Math.abs(distance) > MAX_ROATE_DEGREE) {
                        distance = distance > 0 ? MAX_ROATE_DEGREE
                                : (-1.0f * MAX_ROATE_DEGREE);
                    }

                    // need to slow down if the distance is short
                    rDirection = normalizeDegree(rDirection
                            + ((to - rDirection) * mInterpolator
                            .getInterpolation(Math.abs(distance) > MAX_ROATE_DEGREE ? 0.4f
                                    : 0.3f)));// ����һ�����ٶ���ȥ��תͼƬ����ϸ��
                    rPointer.updateDirection(rDirection);// ����ָ������ת
                }

                //updateDirection();// ���·���ֵ

                rHandler.postDelayed(rCompassViewUpdater, 20);// 20���׺�����ִ���Լ����ȶ�ʱ����
            }
        }
    };


    protected Handler invisiableHandler = new Handler() {
        public void handleMessage(Message msg) {
            mViewGuide.setVisibility(View.GONE);
        }
    };
    // ����Ǹ���ָ������ת���̣߳�handler�����ʹ�ã�ÿ20�����ⷽ��仯ֵ����Ӧ����ָ������ת
    protected Runnable mCompassViewUpdater = new Runnable() {
        @Override
        public void run() {
            if (mPointer != null && !mStopDrawing) {
                if (mDirection != mTargetDirection) {

                    // calculate the short routine
                    float to = mTargetDirection;
                    if (to - mDirection > 180) {
                        to -= 360;
                    } else if (to - mDirection < -180) {
                        to += 360;
                    }

                    // limit the max speed to MAX_ROTATE_DEGREE
                    float distance = to - mDirection;
                    if (Math.abs(distance) > MAX_ROATE_DEGREE) {
                        distance = distance > 0 ? MAX_ROATE_DEGREE
                                : (-1.0f * MAX_ROATE_DEGREE);
                    }

                    // need to slow down if the distance is short
                    mDirection = normalizeDegree(mDirection
                            + ((to - mDirection) * mInterpolator
                            .getInterpolation(Math.abs(distance) > MAX_ROATE_DEGREE ? 0.4f
                                    : 0.3f)));// ����һ�����ٶ���ȥ��תͼƬ����ϸ��
                    mPointer.updateDirection(mDirection);// ����ָ������ת
                }

                updateDirection();// ���·���ֵ

                mHandler.postDelayed(mCompassViewUpdater, 20);// 20���׺�����ִ���Լ����ȶ�ʱ����
            }
        }
    };

    // Overlayˢ�£�handler�����ʹ�ã�ÿ20�����ⷽ��仯ֵ����Ӧ����ָ������ת
    protected Runnable mOverlayUpdater = new Runnable() {
        @Override
        public void run() {
            RelativeLayout raly2=(RelativeLayout)view.findViewById(R.id.relativelayout_overlay);
            raly2.removeAllViews();
            TextView tx=(TextView)view.findViewById(R.id.textView);
            tx.setText("");
            if(poiinfos!=null&&((MainActivity)getActivity()).mBaiduMap!=null) {
                int count=poiinfos.length;
                LatLng mLocation=new LatLng(((MainActivity)getActivity()).mBaiduMap.getLocationData().latitude,((MainActivity)getActivity()).mBaiduMap.getLocationData().longitude);
                for(int n=0;n<count;n++) {                                                                   //����ͼ����ʾ�ر�
                    ImageView imageView=new ImageView(getActivity());
                    imageView.setImageResource(R.mipmap.ic_launcher);
                    double distance= DistanceUtil. getDistance(mLocation,poiinfos[n].location);                //��þ���
                    if(distance>1000) {                                                                       //�������1000�ײ���ʾ
                        continue;
                    }
                    double rdirection=Math.atan(((poiinfos[n].location.latitude-mLocation.latitude))/((poiinfos[n].location.longitude-mLocation.longitude)));//��õر���������н�
                    //normalizeDegree((((float)rdirection)-direction)*-1.0f);
                    rdirection=normalizeDegree((float)(180*rdirection/Math.PI));
                    rdirection=normalizeDegree((float)(mTargetDirection-rdirection-90.0f));
                    tx.append(Integer.toString(n)+"\t rdirection: "+Double.toString(rdirection)+"\nmTargetDirection: "+Float.toString(mTargetDirection)+"\n");
                    if(Math.abs(rdirection)>90&&Math.abs(rdirection)<270) {
                        continue;
                    }
                    int height=(int)(170-((double)3/25)*distance);
                    int width=height/3*5;
                    RelativeLayout.LayoutParams lp1=new RelativeLayout.LayoutParams(width,height);
                    lp1.topMargin=(int)((((float)1/3)-Math.cos(mUDDirection*Math.PI/180))*raly2.getHeight()-0.5f*height);                            //����ͼ��top
                    //if(lp1.topMargin<0) continue;
                    lp1.leftMargin=(int)((((float)1/2)+Math.sin(rdirection * Math.PI / 180))*raly2.getWidth()-0.5f*width);                           //����ͼ��left
                    //if(lp1.leftMargin<0) continue;
                    raly2.addView(imageView, lp1);
                }
            }
            mOverlayHandler.postDelayed(mOverlayUpdater, 20);
        }
    };

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.surface_fragment, container, false);
        //��ʼ��SurfaceView
        mPreviewSV = (SurfaceView)view.findViewById(R.id.previewSV);
        mySurfaceHolder = mPreviewSV.getHolder();
        mySurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);//translucent��͸�� transparent͸��
        mySurfaceHolder.addCallback(this);
        mySurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //aimͼ��
        //RelativeLayout raly=(RelativeLayout)view.findViewById(R.id.aimView);
        //raly.bringToFront();
        //Compass
        RelativeLayout raly2=(RelativeLayout)view.findViewById(R.id.relativelayout_overlay);
        raly2.bringToFront();
        //setContentView(R.layout.activity_main);
        initResources();// ��ʼ��view
        initServices();// ��ʼ����������λ�÷���
        //
        mCompassView.bringToFront();
        //�Զ��佹�ص�
        myAutoFocusCallback=new Camera.AutoFocusCallback() {
            public void onAutoFocus(boolean success,Camera camera){
                if(success)
                {
                    Log.i(tag, "myAutoFocusCallback:success...");
                }
                else
                {
                    Log.i(tag, "myAutoFocusCallback:ʧ����...");
                }
            }
        };
        //����¼�
        ImageButton button_guide=(ImageButton)view.findViewById(R.id.button_guide);
        button_guide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager fm = getFragmentManager();
                // ����Fragment����
                FragmentTransaction transaction = fm.beginTransaction();
                if (mGuideFragment == null)
                {
                    mGuideFragment=new GuideFragment();
                }
                // ʹ�õ�ǰFragment�Ĳ������id_content�Ŀؼ�
                transaction.replace(R.id.id_content,mGuideFragment,"GuideFragment");
                ((MainActivity)getActivity()).mGuideFragment=mGuideFragment;
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        return view;
    }

    /*����������SurfaceHolder.Callback�����Ļص�����*/
    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height)
    // ��SurfaceView/Ԥ������ĸ�ʽ�ʹ�С�����ı�ʱ���÷���������
    {
        // TODO Auto-generated method stub
        Log.i(tag, "SurfaceHolder.Callback:surfaceChanged!");
        initCamera();
        myCamera.cancelAutoFocus();
    }


    public void surfaceCreated(SurfaceHolder holder)
    // SurfaceView����ʱ/����ʵ������Ԥ�����汻����ʱ���÷��������á�
    {
        // TODO Auto-generated method stub
        myCamera = Camera.open();
        try {
            myCamera.setPreviewDisplay(mySurfaceHolder);
            Log.i(tag, "SurfaceHolder.Callback: surfaceCreated!");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            if(null != myCamera){
                myCamera.release();
                myCamera = null;
            }
            e.printStackTrace();
        }



    }


    public void surfaceDestroyed(SurfaceHolder holder)
    //����ʱ������
    {
        // TODO Auto-generated method stub
        Log.i(tag, "SurfaceHolder.Callback��Surface Destroyed");
        if(null != myCamera)
        {
            myCamera.setPreviewCallback(null); /*������PreviewCallbackʱ���������ǰ��Ȼ�˳�����
            ����ʵ����ע�͵�Ҳû��ϵ*/
            myCamera.stopPreview();
            isPreview = false;
            myCamera.release();
            myCamera = null;
        }

    }

    //��ʼ�����
    public void initCamera(){
        if(isPreview){
            myCamera.stopPreview();
        }
        if(null != myCamera){
            Camera.Parameters myParam = myCamera.getParameters();
            //          //��ѯ��Ļ�Ŀ�͸�
            //          WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
            //          Display display = wm.getDefaultDisplay();
            //          Log.i(tag, "��Ļ��ȣ�"+display.getWidth()+" ��Ļ�߶�:"+display.getHeight());

            myParam.setPictureFormat(PixelFormat.JPEG);//�������պ�洢��ͼƬ��ʽ

            //          //��ѯcamera֧�ֵ�picturesize��previewsize
            //          List<Size> pictureSizes = myParam.getSupportedPictureSizes();
            //          List<Size> previewSizes = myParam.getSupportedPreviewSizes();
            //          for(int i=0; i<pictureSizes.size(); i++){
            //              Size size = pictureSizes.get(i);
            //              Log.i(tag, "initCamera:����ͷ֧�ֵ�pictureSizes: width = "+size.width+"height = "+size.height);
            //          }
            //          for(int i=0; i<previewSizes.size(); i++){
            //              Size size = previewSizes.get(i);
            //              Log.i(tag, "initCamera:����ͷ֧�ֵ�previewSizes: width = "+size.width+"height = "+size.height);
            //
            //          }


            //���ô�С�ͷ���Ȳ���
            //WindowManager wm = this.getWindowManager();
            //SurfaceView sv=(SurfaceView)view.findViewById(R.id.previewSV);
            if(mPreviewSV!=null)
            {
                int width = mPreviewSV.getWidth();
                int height = mPreviewSV.getHeight();
                Camera.Size size=getOptimalPreviewSize(myCamera.getParameters().getSupportedPreviewSizes(),height,width);
                myParam.setPreviewSize(size.width,size.height);
                //myParam.set("rotation", 90);
                myCamera.setDisplayOrientation(90);
                myParam.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                myCamera.setParameters(myParam);
                myCamera.startPreview();
                myCamera.autoFocus(myAutoFocusCallback);
                myCamera.cancelAutoFocus();
                isPreview = true;
            }
            else
            {
                Toast.makeText(this.getActivity(),"�����ʼ��ʧ��",Toast.LENGTH_SHORT).show();
            }

        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes,int w,int h){
        final double ASP_TOLERANCE=0.1;
        double targetRatio=(double)w/h;
        if(sizes==null)return null;
        Camera.Size optimalSize=null;
        double minDiff=Double.MAX_VALUE;
        int targetHeight=h;
        //Try to find an size match aspect ratio and size
        for(Camera.Size size:sizes){
            double ratio=(double)size.width/size.height;
            if(Math.abs(ratio-targetRatio)>ASP_TOLERANCE)continue;
            if(Math.abs(ratio-targetRatio)<minDiff){
                optimalSize=size;
                minDiff=Math.abs(size.height-targetHeight);
            }
        }

        //Cannot find the one match the aspect ratio,ignore the requirement
        if(optimalSize==null){
            minDiff=Double.MAX_VALUE;
            for(Camera.Size size:sizes) {
                if(Math.abs(size.height-targetHeight)<minDiff){
                    optimalSize=size;
                    minDiff=Math.abs(size.height-targetHeight);
                }
            }
        }
        return optimalSize;
    }

    // ��ʼ��view
    private void initResources() {
        mViewGuide = view.findViewById(R.id.view_guide);
        mViewGuide.setVisibility(View.VISIBLE);
        invisiableHandler.sendMessageDelayed(new Message(), 3000);
        mGuideAnimation = (ImageView) view.findViewById(R.id.guide_animation);
        mDirection = 0.0f;// ��ʼ����ʼ����
        mTargetDirection = 0.0f;// ��ʼ��Ŀ�귽��
        rDirection = 0.0f;// ��ʼ����ʼ����
        rTargetDirection = 0.0f;// ��ʼ��Ŀ�귽��
        mInterpolator = new AccelerateInterpolator();// ʵ�������ٶ�������
        mStopDrawing = true;
        rStopDrawing=true;
        mChinease = TextUtils.equals(Locale.getDefault().getLanguage(), "zh");// �ж�ϵͳ��ǰʹ�õ������Ƿ�Ϊ����
        mCompassView = view.findViewById(R.id.view_compass);// ʵ������һ��LinearLayout��װָ����ImageView��λ��TextView
        mPointer = (CompassView) view.findViewById(R.id.compass_pointer);// �Զ����ָ����view
        rPointer=(CompassView)view.findViewById(R.id.compassbackdround);
        // mLocationTextView = (TextView)
        // findViewById(R.id.textview_location);// ��ʾλ����Ϣ��TextView
        mDirectionLayout = (LinearLayout) view.findViewById(R.id.layout_direction);// ������ʾ�������ƣ�������������LinearLayout
        mAngleLayout = (LinearLayout) view.findViewById(R.id.layout_angle);// ������ʾ������������LinearLayout

        // mPointer.setImageResource(mChinease ? R.drawable.compass_cn
        // : R.drawable.compass);// ���ϵͳʹ�����ģ��������ĵ�ָ����ͼƬ
    }
    // ��ʼ����������λ�÷���
    private void initServices() {
        // sensor manager
        mSensorManager = (SensorManager) this.getActivity().getSystemService(Context.SENSOR_SERVICE);
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

    // ���¶���������ʾ�ķ���
    private void updateDirection() {
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        // ���Ƴ�layout�����е�view
        mDirectionLayout.removeAllViews();
        mAngleLayout.removeAllViews();

        // �����Ǹ���mTargetDirection������������ͼƬ�Ĵ���
        ImageView east = null;
        ImageView west = null;
        ImageView south = null;
        ImageView north = null;
        float direction = normalizeDegree(mTargetDirection * -1.0f);
        if (direction > 22.5f && direction < 157.5f) {
            // east
            east = new ImageView(view.getContext());
            east.setImageResource(mChinease ? R.drawable.e_cn : R.drawable.e);
            east.setLayoutParams(lp);
        } else if (direction > 202.5f && direction < 337.5f) {
            // west
            west = new ImageView(view.getContext());
            west.setImageResource(mChinease ? R.drawable.w_cn : R.drawable.w);
            west.setLayoutParams(lp);
        }

        if (direction > 112.5f && direction < 247.5f) {
            // south
            south = new ImageView(view.getContext());
            south.setImageResource(mChinease ? R.drawable.s_cn : R.drawable.s);
            south.setLayoutParams(lp);
        } else if (direction < 67.5 || direction > 292.5f) {
            // north
            north = new ImageView(view.getContext());
            north.setImageResource(mChinease ? R.drawable.n_cn : R.drawable.n);
            north.setLayoutParams(lp);
        }
        // �����Ǹ���ϵͳʹ�����ԣ�������Ӧ������ͼƬ��Դ
        if (mChinease) {
            // east/west should be before north/south
            if (east != null) {
                mDirectionLayout.addView(east);
            }
            if (west != null) {
                mDirectionLayout.addView(west);
            }
            if (south != null) {
                mDirectionLayout.addView(south);
            }
            if (north != null) {
                mDirectionLayout.addView(north);
            }
        } else {
            // north/south should be before east/west
            if (south != null) {
                mDirectionLayout.addView(south);
            }
            if (north != null) {
                mDirectionLayout.addView(north);
            }
            if (east != null) {
                mDirectionLayout.addView(east);
            }
            if (west != null) {
                mDirectionLayout.addView(west);
            }
        }
        // �����Ǹ��ݷ��������ʾ����ͼƬ����
        int direction2 = (int) direction;
        boolean show = false;
        if (direction2 >= 100) {
            mAngleLayout.addView(getNumberImage(direction2 / 100));
            direction2 %= 100;
            show = true;
        }
        if (direction2 >= 10 || show) {
            mAngleLayout.addView(getNumberImage(direction2 / 10));
            direction2 %= 10;
        }
        mAngleLayout.addView(getNumberImage(direction2));
        // ����������һ�����ͼƬ
        ImageView degreeImageView = new ImageView(view.getContext());
        degreeImageView.setImageResource(R.drawable.degree);
        degreeImageView.setLayoutParams(lp);
        mAngleLayout.addView(degreeImageView);
    }

    // ��ȡ���������Ӧ��ͼƬ������ImageView
    private ImageView getNumberImage(int number) {
        ImageView image = new ImageView(view.getContext());
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        switch (number) {
            case 0:
                image.setImageResource(R.drawable.number_0);
                break;
            case 1:
                image.setImageResource(R.drawable.number_1);
                break;
            case 2:
                image.setImageResource(R.drawable.number_2);
                break;
            case 3:
                image.setImageResource(R.drawable.number_3);
                break;
            case 4:
                image.setImageResource(R.drawable.number_4);
                break;
            case 5:
                image.setImageResource(R.drawable.number_5);
                break;
            case 6:
                image.setImageResource(R.drawable.number_6);
                break;
            case 7:
                image.setImageResource(R.drawable.number_7);
                break;
            case 8:
                image.setImageResource(R.drawable.number_8);
                break;
            case 9:
                image.setImageResource(R.drawable.number_9);
                break;
        }
        image.setLayoutParams(lp);
        return image;
    }


    /*
    // ����λ����ʾ
    private void updateLocation(Location location) {
        if (location == null) {
            // mLocationTextView.setText(R.string.getting_location);
            return;
        } else {
            // StringBuilder sb = new StringBuilder();
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            String latitudeStr;
            String longitudeStr;
            if (latitude >= 0.0f) {
                latitudeStr = getString(R.string.location_north,
                        getLocationString(latitude));
            } else {
                latitudeStr = getString(R.string.location_south,
                        getLocationString(-1.0 * latitude));
            }

            // sb.append("    ");

            if (longitude >= 0.0f) {
                longitudeStr = getString(R.string.location_east,
                        getLocationString(longitude));
            } else {
                longitudeStr = getString(R.string.location_west,
                        getLocationString(-1.0 * longitude));
            }
            mLatitudeTV.setText(latitudeStr);
            mLongitudeTV.setText(longitudeStr);
            // mLocationTextView.setText(sb.toString());//
            // ��ʾ��γ�ȣ���ʵ��������������룬��ʾ�����ַ
        }
    }

    // �Ѿ�γ��ת���ɶȷ�����ʾ
    private String getLocationString(double input) {
        int du = (int) input;
        int fen = (((int) ((input - du) * 3600))) / 60;
        int miao = (((int) ((input - du) * 3600))) % 60;
        return String.valueOf(du) + "��" + String.valueOf(fen) + "��"
                + String.valueOf(miao) + "��";
    }
*/
    // ���򴫸����仯����
    private SensorEventListener mOrientationSensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float direction = event.values[mSensorManager.DATA_X] * -1.0f;
            if(Nextstep!=null)
            {
                LatLng nodeLocation =null;
                LatLng mLocation=null;
                if (Nextstep instanceof WalkingRouteLine.WalkingStep) {
                    nodeLocation = ((WalkingRouteLine.WalkingStep) Nextstep).getEntrance().getLocation();
                    if(((MainActivity)getActivity()).mBaiduMap!=null)
                    {
                        mLocation=new LatLng(((MainActivity)getActivity()).mBaiduMap.getLocationData().latitude,((MainActivity)getActivity()).mBaiduMap.getLocationData().longitude);
                    }
                    if(nodeLocation!=null&&mLocation!=null)
                    {
                        double rdirection=Math.atan((nodeLocation.latitude-mLocation.latitude)/(nodeLocation.longitude-mLocation.longitude));
                        double exrTarget=(180*rdirection/Math.PI)-270.0f;
                        rTargetDirection=normalizeDegree((((float)exrTarget)-direction)*-1.0f);
                        //double x=((WalkingRouteLine.WalkingStep) Nextstep).getDirection();
                        //rTargetDirection=normalizeDegree((((WalkingRouteLine.WalkingStep) Nextstep).getDirection()-direction)*-1.0f);
                    }
                }
            }
            mUDDirection=normalizeDegree(event.values[1]);//����
            mLRDirection=normalizeDegree(event.values[2]);//���������
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

    @Override
    public void onPause() {// ����ͣ������������ע�������������λ�ø��·���

        super.onPause();
        mOverlayHandler.removeCallbacks(mOverlayUpdater);
        if (mOrientationSensor != null) {
            mSensorManager.unregisterListener(mOrientationSensorEventListener);
        }
    }

    @Override
    public void onResume() {// �ڻָ��������������жϡ�����λ�ø��·���ʹ���������

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
        mStopDrawing=false;
        if(isGuiding)
        {
            rStopDrawing=false;
        }
        rHandler.postDelayed(rCompassViewUpdater,20);
        mHandler.postDelayed(mCompassViewUpdater,20);
        mOverlayHandler.postDelayed(mOverlayUpdater, 20);
    }

}
