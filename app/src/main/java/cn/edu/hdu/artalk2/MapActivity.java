package cn.edu.hdu.artalk2;

import android.content.Intent;
import android.support.v4.util.Pair;
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
import com.baidu.mapapi.http.HttpClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.hdu.artalk2.NavigationFragment.TestFragment;
import cn.edu.hdu.artalk2.adapter.ViewPagerAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.baidu.location.h.l.u;
import static com.baidu.location.h.l.v;

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
    String location, id;
    private String url = "http://10.0.2.2:8080/Android/LoginServlet";
    public static final  String TAG="MapActivity";

    //底部导航栏
    /*private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        //底部导航栏切换界面
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.community:
                    Toast.makeText(MapActivity.this,"点击跳转到社区community",Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.record:
                    Toast.makeText(MapActivity.this,"点击跳转到记录record界面",Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.location:
                    Toast.makeText(MapActivity.this,"点击在此定位",Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.channel:
                    Toast.makeText(MapActivity.this,"点击跳转到频道channel界面",Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.mine:
                    Toast.makeText(MapActivity.this,"点击跳转到我的mine界面",Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        }
    };*/

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
        MarkerSet();

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
        mBaiduMap.clear();
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
    /*private  void resetOverlay()
    {
        clearOverlay();
        InitMarker();
    }*/
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

/*private class PostUtils {
    public  static String LOGIN_URL = "http://172.16.2.54:8080/HttpTest/ServletForPost";
    public static String LoginByPost(String number,String passwd)
    {
        String msg = "";
        try{
            HttpURLConnection conn = (HttpURLConnection) new URL(LOGIN_URL).openConnection();
            //设置请求方式,请求超时信息
            conn.setRequestMethod("POST");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            //设置运行输入,输出:
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //Post方式不能缓存,需手动设置为false
            conn.setUseCaches(false);
            //我们请求的数据:
            String data = "passwd="+ URLEncoder.encode(passwd, "UTF-8")+
                    "&number="+ URLEncoder.encode(number, "UTF-8");
            //这里可以写一些请求头的东东...
            //获取输出流
            OutputStream out = conn.getOutputStream();
            out.write(data.getBytes());
            out.flush();
             if (conn.getResponseCode() == 200) {
                    // 获取响应的输入流对象
                    InputStream is = conn.getInputStream();
                    // 创建字节输出流对象
                    ByteArrayOutputStream message = new ByteArrayOutputStream();
                    // 定义读取的长度
                    int len = 0;
                    // 定义缓冲区
                    byte buffer[] = new byte[1024];
                    // 按照缓冲区的大小，循环读取
                    while ((len = is.read(buffer)) != -1) {
                        // 根据读取的长度写入到os对象中
                        message.write(buffer, 0, len);
                    }
                    // 释放资源
                    is.close();
                    message.close();
                    // 返回字符串
                    msg = new String(message.toByteArray());
                    return msg;
             }
        }catch(Exception e){e.printStackTrace();}
        return msg;
    }
}*/

/*private void MarkerPut(){
    if (!TextUtils.isEmpty(address)){
        if (!addressData.contains(address)){  //过滤掉相同的地址信息
            addressData.add(address);
            mSearch.geocode(new GeoCodeOption()     //进行编码
                    .city("")
                    .address(address));
            logUtils.d("地址反编码"+":"+address);
            mapInfoData.add(new mapinfoBean(name,next.getVisitStatusText(),next.getCustomerName(),next.getAddress(),next.getCaseCode(),next.getVisitGuid()));
        }
    }
    OnGetGeoCoderResultListener GeoListener = new OnGetGeoCoderResultListener() {
        @Override
        public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
            if (null != geoCodeResult && null != geoCodeResult.getLocation()) {
                if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    //没有检索到结果
                    return;
                } else {
                    service_flag=true;
                    double latitude = geoCodeResult.getLocation().latitude;
                    double longitude = geoCodeResult.getLocation().longitude;
                    LatLng latLng=new LatLng(latitude,longitude );
                    logUtils.d("地理反编码地址"+"latitude"+latitude+"longitude"+longitude);
                    servicePoint = new LatLng(latitude, longitude);
                    //创建OverlayOptions属性
                    //  lv_mainItemPostion
                    mapInfoBean mapInfoBean=null;
                    if (!click_address_item_flag){

                        mapInfoBean = mapInfoData.get(currrentCount);
                        currrentCount++;    //循环
                    }else {
                        mapInfoBean = mapInfoData.get(lv_mainItemPostion);
                    }
                    BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.man);
                    OverlayOptions option1 =  new MarkerOptions()
                            .position(servicePoint)
                            .icon(bitmap);
                    marker = (Marker) map.addOverlay(option1);
                    Bundle bundle=new Bundle();
                    bundle.putSerializable("info", mapInfoBean);   //携带对象数据
                    marker.setExtraInfo(bundle);
                    MapStatus mMapStatus = new MapStatus.Builder()
                            .target(latLng)
                            .zoom(15)
                            .build();
                    //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
                    MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
//改变地图状态
                    mBaiduMap.animateMapStatus(mMapStatusUpdate);

                }
            }
        }

        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {

        }
    };}*/


private void MarkerSet() {
    //mLastLocationData传入数据库
    LatLng mlocationdata = new LatLng(mLongtitude, mLatitude);
    new Thread() {
        @Override
        public void run() {
            MediaType mediaType = MediaType.parse("text/x-markdown; charset=utf-8");
            String requestBodyo = String.valueOf ((int)mLongtitude);
            Request request = new Request.Builder()
                    .url("https://api.github.com/markdown/raw")
                    .post(RequestBody.create(mediaType, requestBodyo))
                    .build();
            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "onFailure: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //Log.d(TAG, response.protocol() + " " + response.code() + " " + response.message());
                    Headers headers = response.headers();
                    for (int i = 0; i < headers.size(); i++) {
                        Log.d(TAG, headers.name(i) + ":" + headers.value(i));
                    }
                    Log.d(TAG, "onResponse: " + response.body().string());
                }
            });
            Map<String,String>map = new HashMap<>();
            map.put("cx","120；");
            map.put("cy","30");

        }
};

    //数据库返回对应的在范围内的点
    ArrayList<Integer> markerarrayListlong = new ArrayList<Integer>();
    ArrayList<Integer> markerarrayListlan = new ArrayList<Integer>();
    String sql = "'SELECT * FROM ... WHERE '+langtitude +'>= 80 AND '+longtitude '>=80;";
    //ArrayList<Pair<Integer,Integer>>markerlist = new  ArrayList<Pair<Integer,Integer>>();
    int k =0;
    while(k<4){
        //markerlist.add((int)mLatitude,(Integer)mLongtitude);
        markerarrayListlan.add((int)mLatitude);
        markerarrayListlong.add((int)mLongtitude);
        for (int i=0;i<=markerarrayListlan.size();i++)
        {
            double longti=0,lati=0;
            LatLng point = new LatLng(markerarrayListlan.get(i),markerarrayListlong.get(i));
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon);
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions options= new MarkerOptions().position(point).icon(bitmap).visible(true).flat(true);
            //在地图上添加Marker，并显示
            marker = (Marker)mBaiduMap.addOverlay(options);
        }
        k++;
    }

    //为所有marker设置点击事件
    mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            Toast.makeText(MapActivity.this,"点击了marker",Toast.LENGTH_SHORT).show();
            return false;
        }
    });
}
}


