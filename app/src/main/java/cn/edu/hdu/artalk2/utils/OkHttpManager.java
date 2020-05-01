package cn.edu.hdu.artalk2.utils;

import java.io.IOException;
import java.util.Map;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



/**
 * OkHttp的简单封装
 * 使用方法 OkHttpManager.getInstance().func_name(,,)
 */
public class OkHttpManager {

    private OkHttpClient mClient;
//    private static Handler mHandler;
    private volatile static OkHttpManager sManager;

    //使用构造方法完成初始化     私有化操作
    private OkHttpManager() {
        mClient = new OkHttpClient();
//        mHandler = new Handler();
    }

    //使用单例模式通过获取的方式拿到对象
    public static OkHttpManager getInstance() {
        if (sManager == null) {
            synchronized (OkHttpManager.class) {
                if (sManager == null) {
                    sManager = new OkHttpManager();
                }
            }
        }
        return sManager;
    }

    /**
     * 异步请求
     * enqueue 实现异步操作
     */
    public void asyncSendRequest(String url, Callback callback) {

        Request request = new Request.Builder().url(url).build();

        mClient.newCall(request).enqueue(callback);


    }

    /**
     * 发送同步请求
     * @return 返回结果，String类型
     */
    public String syncSendRequest(String url)
    {
        Request request = new Request.Builder().url(url) .build();
        Response response= null;
        try {
            response = mClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String message= null;
        try {
            message = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return message;
    }

    /**
     * 异步提交表单
     * @param url 地址
     * @param params 参数
     * @param callBack 回调函数
     */
    public void sendComplexFrom(String url, Map<String,String> params, Callback callBack) {
        FormBody.Builder form_builder = new FormBody.Builder();
        //对键值进行非空判断
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                form_builder.add(entry.getKey(),entry.getValue());
            }
        }
        FormBody formBody = form_builder.build();
        Request request = new Request.Builder().url(url).post(formBody).build();
        mClient.newCall(request).enqueue(callBack);

    }
    //使用Post方式向服务器上提交数据并获取返回提示数据
    public static void sendOkHttpResponse(final String address, final RequestBody requestBody, final okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
//        JSONObject object;
        Request request = new Request.Builder()
                .url(address).post(requestBody).build();
        client.newCall(request).enqueue(callback);
    }

}