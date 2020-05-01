package cn.edu.hdu.artalk2;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class ReadCommentFragment extends Fragment {

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


    public ReadCommentFragment() {
        // Required empty public constructor
    }


    public static ReadCommentFragment newInstance(String datetime, String strcmcontent,
                                                  Integer strzannum,Integer strcainum,String struser) {
        ReadCommentFragment fragment = new ReadCommentFragment();
        Bundle args = new Bundle();
        args.putString("datetime", datetime);
        args.putString("strcmcontent", strcmcontent);
        args.putInt("strzannum",strzannum);
        args.putInt("strcainum",strcainum);
        args.putString("struser",struser);
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
        // Inflate the layout for this fragment
        return view;
    }
}
