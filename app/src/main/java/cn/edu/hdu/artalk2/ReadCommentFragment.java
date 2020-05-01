package cn.edu.hdu.artalk2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.edu.hdu.artalk2.utils.OkHttpManager;
import cn.edu.hdu.artalk2.utils.ToastUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class ReadCommentFragment extends Fragment {

    private static final String url = "http://47.112.174.246:3389/";
    //控件
    private TextView cmtime;              //评论时间
    private ImageView cmzan_btn;          //赞按钮
    private ImageView cmzancopy_btn;      //踩按钮
    private TextView cmzannum;            //赞数量
    private TextView cmzancopynum;        //踩数量
    private TextView cmusername;          //评论的用户名
    private TextView cmcontent;           //评论内容


    //传入参数
    private String datetime;              //时间
    private String strcmcontent;          //评论内容
    private Integer strzannum;            //赞数量
    private Integer strcainum;            //踩数量
    private String struser;               //用户名
    private String cmid;                  //评论id

    //处理后台点赞点踩数据
    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1: //点赞数据
                    String zanData = (String) msg.obj;
                    Log.d("data",zanData);
                    try {
                        JSONObject jsonObject = new JSONObject(zanData);
                        String rs = jsonObject.getString("code");
                        if(Integer.parseInt(rs)>0){
                            if(rs.equals("1")){  //点赞
                                strzannum += 1;
                                cmzannum.setText(String.valueOf(strzannum));
                            }
                            else if(rs.equals("3")){  //取消赞
                                strzannum -= 1;
                                cmzannum.setText(String.valueOf(strzannum));
                            }
                        }
                        else {
                            if(rs.equals("-1")){
                                ToastUtils.show(getContext(),"请不要同时点赞和点踩哦");
                            }
                            else {
                                ToastUtils.show(getContext(),"请求错误");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:  //点踩数据
                    String caiData = (String) msg.obj;
                    Log.d("data",caiData);
                    try {
                        JSONObject jsonObject = new JSONObject(caiData);
                        String rs = jsonObject.getString("code");
                        Log.d("rs",rs);
                        if(Integer.parseInt(rs)>0){
                            if(rs.equals("2")){  //点踩
                                strcainum += 1;
                                cmzancopynum.setText(String.valueOf(strcainum));
                            }
                            else if(rs.equals("3")){  //取消踩
                                strcainum -= 1;
                                cmzancopynum.setText(String.valueOf(strcainum));
                            }
                        }
                        else {
                            if(rs.equals("-1")){
                                ToastUtils.show(getContext(),"请不要同时点赞和点踩哦");
                            }
                            else {
                                ToastUtils.show(getContext(),"请求错误");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }

        }
    };

    public ReadCommentFragment() {
        // Required empty public constructor
    }


    public static ReadCommentFragment newInstance(String datetime, String strcmcontent, Integer strzannum,
                                                  Integer strcainum,String struser, String cmid) {
        ReadCommentFragment fragment = new ReadCommentFragment();
        Bundle args = new Bundle();
        args.putString("datetime", datetime);
        args.putString("strcmcontent", strcmcontent);
        args.putInt("strzannum",strzannum);
        args.putInt("strcainum",strcainum);
        args.putString("struser",struser);
        args.putString("cmid",cmid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            datetime = getArguments().getString("datetime");
            strcmcontent = getArguments().getString("strcmcontent");
            strcainum = getArguments().getInt("strcainum");
            strzannum = getArguments().getInt("strzannum");
            struser = getArguments().getString("struser");
            cmid = getArguments().getString("cmid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_read_comment, container, false);
        cmtime = view.findViewById(R.id.readcmtime);
        cmzan_btn = view.findViewById(R.id.readcmzan);
        cmzancopy_btn = view.findViewById(R.id.readcmzancopy);
        cmzannum = view.findViewById(R.id.readcmzannum);
        cmzancopynum = view.findViewById(R.id.readcmcainum);
        cmcontent = view.findViewById(R.id.readcmcontent);
        cmusername = view.findViewById(R.id.readcmuser);

        //设置参数
        cmtime.setText(datetime);
        cmzannum.setText(String.valueOf(strzannum));
        cmzancopynum.setText(String.valueOf(strcainum));
        cmcontent.setText(strcmcontent);
        cmusername.setText(struser);

        //点赞按钮
        cmzan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OkHttpManager okHttpManager = OkHttpManager.getInstance();
                Map<String,String> map = new HashMap<>();
                //传入评论id和用户id
                map.put("cmId",cmid);
                map.put("userId","1");

                Callback callback = new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("onFailure: ","请求失败");
                        Log.e("onFailure",e.toString());
                        Log.e("onFailure",call.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.d("success","请求成功");
                        Message msg = new Message();
                        msg.obj = response.body().string();
                        msg.what = 1;
                        //往主线程传输数据
                        handler.sendMessage(msg);
                    }
                };
                okHttpManager.sendComplexFrom(url+"toggleCmLike/",map,callback);
            }
        });

        //点踩按钮
        cmzancopy_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OkHttpManager okHttpManager = OkHttpManager.getInstance();
                Map<String,String> map = new HashMap<>();
                //传入评论id和用户id
                map.put("cmId",cmid);
                map.put("userId","1");

                Callback callback = new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("onFailure: ","请求失败");
                        Log.e("onFailure",e.toString());
                        Log.e("onFailure",call.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.d("success","请求成功");
                        Message msg = new Message();
                        msg.obj = response.body().string();
                        msg.what = 2;
                        //往主线程传输数据
                        handler.sendMessage(msg);
                    }
                };
                okHttpManager.sendComplexFrom(url+"toggleCmDisLike/",map,callback);
            }
        });
        // Inflate the layout for this fragment
        return view;
    }
}
