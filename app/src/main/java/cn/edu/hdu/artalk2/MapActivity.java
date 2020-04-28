package cn.edu.hdu.artalk2;

import android.support.v7.app.AppCompatActivity;
import android.Manifest;
import android.app.ActionBar;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.google.android.material.internal.*;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.edu.hdu.artalk2.NavigationFragment.TestFragment;
import cn.edu.hdu.artalk2.adapter.ViewPagerAdapter;

//主界面
public class MapActivity extends AppCompatActivity {

    //地图相关
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private Context context;
    private ImageButton message_button = null;
    private ImageButton scan_button = null;
    //定位相关
    private double mLatitude;
    private double mLongtitude;
    //方向传感器
    private MyOrientationListener mMyOrientationListener;
    private float mCurrentX;
    //自定义图标
    private BitmapDescriptor mIconLocation;
    private LocationClient mLocationClient;
    public BDAbstractLocationListener myListener;
    private LatLng mLastLocationData;
    private boolean isFirstin = true;
    private BottomNavigationView bottomNavigationView;
    private ViewPager viewPagerAdapter;
    private ViewPager viewPager;
    private MenuItem menuItem;
    private Marker marker;

    //底部导航栏
//    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
//        @Override
//        //底部导航栏切换界面
//        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
//            switch (menuItem.getItemId()){
//                case R.id.community:
//                    Toast.makeText(MapActivity.this,"点击跳转到社区community",Toast.LENGTH_SHORT).show();
//                    return true;
//                case R.id.record:
//                    Toast.makeText(MapActivity.this,"点击跳转到记录record界面",Toast.LENGTH_SHORT).show();
//                    return true;
//                case R.id.location:
//                    Toast.makeText(MapActivity.this,"点击在此定位",Toast.LENGTH_SHORT).show();
//                    return true;
//                case R.id.channel:
//                    Toast.makeText(MapActivity.this,"点击跳转到频道channel界面",Toast.LENGTH_SHORT).show();
//                    return true;
//                case R.id.mine:
//                    Toast.makeText(MapActivity.this,"点击跳转到我的mine界面",Toast.LENGTH_SHORT).show();
//                    return true;
//            }
//            return false;
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SDKInitializer.initialize(getApplicationContext());//初始化SDK
        setContentView(R.layout.activity_map);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        SDKInitializer.setCoordType(CoordType.BD09LL);//定位编码设置
        this.context = this;
        mMapView = (MapView) findViewById(R.id.bmapView);
        //获取地图控件引用
        mBaiduMap = mMapView.getMap();//显示地图
        initMyLocation();//初始化位置方法
        InitMarker();
        //两个按钮借口
        message_button();
        scan_buttton();
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        viewPager = (ViewPager) findViewById(R.id.vp);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                menuItem = bottomNavigationView.getMenu().getItem(position);
                menuItem.setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //viewPagerAdapter = new  ViewPagerAdapter(getSupportFragmentManager());
        //viewPager.setAdapter(viewPagerAdapter);
        List<Fragment> list = new ArrayList<>();
        list.add(TestFragment.newInstance("社区"));
        list.add(TestFragment.newInstance("记录"));
        list.add(TestFragment.newInstance("定位"));
        list.add(TestFragment.newInstance("频道"));
        list.add(TestFragment.newInstance("我的"));
        //viewPagerAdapter.setList(list);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

    }
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            menuItem = item;
            switch (item.getItemId()) {
                case R.id.community:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.record:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.location:
                    viewPager.setCurrentItem(2);
                    return true;
                case R.id.channel:
                    viewPager.setCurrentItem(3);
                    return true;
                case R.id.mine:
                    viewPager.setCurrentItem(4);
                    return true;
            }
            return false;
        }
    };

    //实现地图上标记marker
    private  void InitMarker()
    {
        //定义Maker坐标点
        LatLng point = new LatLng(40.963175, 114.400244);
        LatLng point1 = new LatLng(40.45451,114.5686);
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.scan);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions options= new MarkerOptions().position(point).icon(bitmap).visible(true).flat(true);
        //在地图上添加Marker，并显示
        marker = (Marker)mBaiduMap.addOverlay(options);
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(MapActivity.this,"点击了marker",Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }
    private void clearOverlay()
    {
        mBaiduMap.clear();
        marker =null;
    }
    private  void resetOverlay()
    {
        clearOverlay();
        InitMarker();
    }
    //关于地图图层生命周期的五个方法
    protected void onStart() {
        super.onStart();
        //开启定位
        mBaiduMap.setMyLocationEnabled(true);
        if (!mLocationClient.isStarted())
            mLocationClient.start();
        //开启方向传感器
        mMyOrientationListener.start();
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
    @Override
    protected void onStop() {
        super.onStop();
        //停止定位
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        //停止方向传感器
        mMyOrientationListener.stop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
    }

    //按钮响应
    // 点击信息跳转
    private void message_button() {
        //按钮
        ImageButton message_button = (ImageButton)findViewById(R.id.message_button);
//        //按钮处理
        message_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapActivity.this,"点击跳转到消息界面",Toast.LENGTH_SHORT).show();
            }
        });
    }
//点击扫描跳转
    public void scan_buttton(){
        ImageButton scan_button = (ImageButton)findViewById(R.id.scan_button);
        scan_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapActivity.this,"点击跳转到扫描界面",Toast.LENGTH_SHORT).show();
            }
        });
    }


    //定位
    private class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null){
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentX).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            //设置自定义图标
            MyLocationConfiguration config = new
                    MyLocationConfiguration(
                    MyLocationConfiguration.LocationMode.NORMAL, true, mIconLocation);
            mBaiduMap.setMyLocationConfiguration(config);
            //更新经纬度
            mLatitude = location.getLatitude();
            mLongtitude = location.getLongitude();
            //设置起点
            mLastLocationData = new LatLng(mLatitude, mLongtitude);
            if (isFirstin) {
                centerToMyLocation(location.getLatitude(), location.getLongitude());

                if (location.getLocType() == BDLocation.TypeGpsLocation) {
                    // GPS定位结果
                    Toast.makeText(context, "定位:"+location.getAddrStr(), Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                    // 网络定位结果
                    Toast.makeText(context, "定位:"+location.getAddrStr(), Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
                    // 离线定位结果
                    Toast.makeText(context, "定位:"+location.getAddrStr(), Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    Toast.makeText(context, "定位:服务器错误", Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    Toast.makeText(context, "定位:网络错误", Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    Toast.makeText(context, "定位:手机模式错误，请检查是否飞行", Toast.LENGTH_SHORT).show();
                }
                isFirstin = false;
            }
        }
    }
    //初始化定位
    private void initMyLocation() {
        //缩放地图
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(msu);
        //开启定位
        mBaiduMap.setMyLocationEnabled(true);
        //声明LocationClient类
        mLocationClient = new LocationClient(this);
        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setIsNeedAddress(true);//设置是否需要地址信息
        option.setScanSpan(1000);
        //设置locationClientOption
        mLocationClient.setLocOption(option);
        myListener = new MyLocationListener();
        //注册监听函数
        mLocationClient.registerLocationListener(myListener);
        //初始化图标
        mIconLocation = BitmapDescriptorFactory.fromResource(R.mipmap.dir);
        initOrientation();
        //开始定位
        mLocationClient.start();
    }
    //回到定位中心
    private void centerToMyLocation(double latitude, double longtitude) {
        mBaiduMap.clear();
        mLastLocationData = new LatLng(latitude, longtitude);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(mLastLocationData);
        mBaiduMap.animateMapStatus(msu);
    }
    //传感器
    private void initOrientation() {
        //传感器
        mMyOrientationListener = new MyOrientationListener(context);
        mMyOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mCurrentX = x;
            }
        });
    }
}


