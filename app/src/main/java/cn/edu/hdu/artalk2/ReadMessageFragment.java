package cn.edu.hdu.artalk2;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ReadMessageFragment extends Fragment {

    private boolean isStop;              //线程标志位

    //控件
    private MediaPlayer mediaPlayer;     //声音
    private TextView time;               //时间
    private ImageView zan_btn;           //赞按钮
    private ImageView zancopy_btn;       //踩按钮
    private TextView zannum;             //赞数量
    private TextView zancopynum;         //踩数量
    private ImageView start_btn;         //播放按钮
    private ImageView end_btn;           //结束按钮
    private TextView content;            //留言
    private SeekBar seekBar;             //进度条
    private TextView curtime;            //声音现在播放的时间
    private TextView endtime;            //声音总时长

    //传入的参数
    private Integer strzannum;     //赞数量
    private Integer strcainum;     //踩数量
    private String mscurl;         //音频文件url
    private String strcontent;     //留言内容
    private String datetime;       //时间

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 将SeekBar位置设置到当前播放位置
            seekBar.setProgress(msg.what);
            //获得音乐的当前播放时间
            curtime.setText(formatime(msg.what));
        }
    };

    public ReadMessageFragment() {
        // Required empty public constructor
    }


    public static ReadMessageFragment newInstance(Integer strzannum, Integer cainum,
                                                  String mscurl,String strcontent,String datetime) {
        ReadMessageFragment fragment = new ReadMessageFragment();
        Bundle args = new Bundle();
        args.putInt("strzannum", strzannum);
        args.putInt("strcainum", cainum);
        args.putString("mscurl",mscurl);
        args.putString("strcontent",strcontent);
        args.putString("datetime",datetime);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            strzannum = getArguments().getInt("strzannum");
            strcainum = getArguments().getInt("strcainum");
            mscurl = getArguments().getString("mscurl");
            strcontent = getArguments().getString("strcontent");
            datetime = getArguments().getString("datetime");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_read_message, container, false);
        time = view.findViewById(R.id.readtime);
        zan_btn = view.findViewById(R.id.readzan);
        zancopy_btn = view.findViewById(R.id.readzancopy);
        zannum = view.findViewById(R.id.readzannum);
        zancopynum = view.findViewById(R.id.readcainum);
        start_btn = view.findViewById(R.id.readstartbtn);
        end_btn = view.findViewById(R.id.readpausebtn);
        content = view.findViewById(R.id.readcontent);
        seekBar = view.findViewById(R.id.readmis);
        curtime = view.findViewById(R.id.curtime);
        endtime = view.findViewById(R.id.endtime);

        //设置参数
        zannum.setText(String.valueOf(strzannum));
        zancopynum.setText(String.valueOf(strcainum));
        content.setText(strcontent);
        time.setText(datetime);

        mediaPlayer = new MediaPlayer();
        //调用子线程
        play();

        //进度条监听
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //参数b表示是否为用户手动操作
                if(b){
                    mediaPlayer.seekTo(i);   //用户手动调进度
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //开始播放
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }
            }
        });

        //暂停按钮
        end_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                }
            }
        });
        // Inflate the layout for this fragment
        return view;
    }

    //销毁时所做的工作
    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.reset();
    }


    class MuiscThread implements Runnable {
        @Override
        //实现run方法
        public void run() {
            //判断音乐的状态，在不停止与不暂停的情况下向总线程发出信息
            while (mediaPlayer != null && !isStop) {
                try {
                    // 每100毫秒更新一次位置
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //发出的信息
                handler.sendEmptyMessage(mediaPlayer.getCurrentPosition());
            }
        }
    }

    private void play(){
        isStop = false;
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(mscurl);
            // 准备
            mediaPlayer.prepare();
            //获取音乐时长
            endtime.setText(formatime(mediaPlayer.getDuration()));
            // 启动
            mediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
        // 设置seekbar的最大值
        seekBar.setMax(mediaPlayer.getDuration());
        // 创建一个线程
        Thread thread = new Thread(new MuiscThread());
        // 启动线程
        thread.start();
    }

    //时间转换类，将得到的音乐时间毫秒转换为时分秒格式
    private String formatime(int lengrh) {
        Date date = new Date(lengrh);
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        String totalTime = sdf.format(date);
        return totalTime;
    }
}
