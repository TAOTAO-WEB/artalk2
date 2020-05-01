package cn.edu.hdu.artalk2.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.hdu.artalk2.dto.Message;
import cn.edu.hdu.artalk2.utils.OkHttpManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 后台服务发起语音列表请求
 */
public class GetMessageListService extends Service {

    /** 绑定的客户端接口 */
    private final IBinder binder = new Binder();

    private LocationClient mLocationClient;

    // 请求网址
    private static final String POST_COORDINATE_URL = "http://47.112.174.246:3389/getMessage/";
    // 返回的message列表
    private List<Message> messageList;


    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class GetMessagesBinder extends Binder {
        public GetMessageListService getService() {
            // Return this instance of LocalService so clients can call public methods
            return GetMessageListService.this;
        }
    }

    /**
     * 监听位置变化。
     * 一旦有变化被 onReceiveLocation捕捉，并发送网络请求获取最新messageList
     */
    class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {

            if (location == null){
                return;
            }

            //获取纬度信息
            double latitude = location.getLatitude();
            //获取经度信息
            double longitude = location.getLongitude();
            //获取定位精度，默认值为0.0f
            float radius = location.getRadius();
            // 获取手机方向
            float direction = location.getDirection();
            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
            String coorType = location.getCoorType();
            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
            int errorCode = location.getLocType();

            String toast = "纬度："+latitude+"，经度："+longitude+"，精度:"+
                    radius+", 方向:"+direction+",errorCode:"+errorCode;

            Log.d("coordinate",toast);

            Map<String,String> form = new HashMap<>();
            form.put("Cx",String.valueOf(latitude));
            form.put("Cy",String.valueOf(longitude));

            OkHttpManager.getInstance().sendComplexFrom(POST_COORDINATE_URL, form,new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("posterror",e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String res = response.body().string();

                    Log.d("body", response.body().string());

                    ObjectMapper mapper = new ObjectMapper();
                    messageList = mapper.readValue(res, new TypeReference<List<Message>>(){});
                }
            });

        }
    }


    /** method for clients */
    public List<Message> getMessageList() {
        return messageList;
    }

    /** 通过bindService()绑定到服务的客户端 */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        initLocation();

        return binder;
    }


    /**
     * 初始化定位信息
     */
    private void initLocation() {

        mLocationClient = new LocationClient(getApplicationContext());

        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(5000);// 设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        option.setNeedDeviceDirect(true); // 设置是否需要设备方向结果
        option.setIgnoreKillProcess(true);

        //设置打开自动回调位置模式，该开关打开后，
        // 期间只要定位SDK检测到位置变化就会主动回调给开发者，
        // 该模式下开发者无需再关心定位间隔是多少，
        // 定位SDK本身发现位置变化就会及时回调给开发者
        option.setOpenAutoNotifyMode();

        //　参数含义：
        // minTimeInterval:3000
        // minDistance: 1
        // locSensitivity:
        option.setOpenAutoNotifyMode(3000,2,LocationClientOption.LOC_SENSITIVITY_HIGHT);


        //设置locationClientOption
        mLocationClient.setLocOption(option);

        //注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        //开启地图定位图层
        mLocationClient.start();
    }

}
