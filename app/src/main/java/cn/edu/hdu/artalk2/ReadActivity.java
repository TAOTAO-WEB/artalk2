package cn.edu.hdu.artalk2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.edu.hdu.artalk2.adapter.ReadAdapter;
import cn.edu.hdu.artalk2.utils.OkHttpManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ReadActivity extends AppCompatActivity {
    private ViewPager viewPager;    //留言标签
    private Button com_btn;         //评论按钮
    private Button fav_btn;         //点赞按钮
    private TextView usertitle;     //用户名标签
    private ImageView back_btn;     //返回按钮

    private static final String url = "http://47.112.174.246:3389/";

    //处理后台传入的数据
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String jsonData = (String) msg.obj;
            try {
                JSONObject jsonObject = new JSONObject(jsonData);
                String misurl = jsonObject.get("Voice").toString();
                String likenum = jsonObject.get("likeCount").toString();
                int commentCount = Integer.parseInt(jsonObject.get("commentCount").toString());
                String text = jsonObject.get("Text").toString();

                List<Fragment> fragments = new ArrayList<>();
                //留言页面
                fragments.add(ReadMessageFragment.newInstance(Integer.parseInt(likenum),0,
                        url+misurl,text,"2020/4/26"));

                //评论页面
                JSONObject commentList = (JSONObject) jsonObject.get("commentList");
                for (Iterator<String> it = commentList.keys(); it.hasNext(); ) {
                    String id = it.next();
                    String com = commentList.getString(id);
                    fragments.add(ReadCommentFragment.newInstance("2020/4/26",com,
                            0,0,id));
                }
                ReadAdapter adapter = new ReadAdapter(fragments,getSupportFragmentManager());
                viewPager.setAdapter(adapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

//        /*
//       隐藏标题栏
//        */
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.hide();
//        }
//        //隐藏状态栏
//        if(Build.VERSION.SDK_INT >= 21){
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
//        }

        back_btn = findViewById(R.id.readbackbtn);
        com_btn = findViewById(R.id.read_com);
        fav_btn = findViewById(R.id.read_fav);
        usertitle = findViewById(R.id.readuser);

        //返回按钮
//        back_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(ReadActivity.this,ArScanActivity.class);
//                startActivity(intent);
//            }
//        });

        viewPager = findViewById(R.id.readviewpager);

        OkHttpManager okHttpManager = OkHttpManager.getInstance();

        //接受跨页面传输的值
        Intent intent = getIntent();
        String msgid = intent.getStringExtra("msgid");
        String cx = intent.getStringExtra("cx");
        String cy = intent.getStringExtra("cy");
        String username = intent.getStringExtra("username");
        //usertitle.setText(username);

        //post参数
        Map<String,String> map = new HashMap<>();
        map.put("mId","29");
        map.put("Cx","30");
        map.put("Cy","30");

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
                //往主线程传输数据
                handler.sendMessage(msg);
            }
        };

        okHttpManager.sendComplexFrom(url+"/getMessage/",map,callback);
    }
}
