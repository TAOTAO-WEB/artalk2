package cn.edu.hdu.artalk2;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import cn.edu.hdu.artalk2.adapter.ReadAdapter;

public class ReadActivity extends AppCompatActivity {
    private ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        viewPager = findViewById(R.id.readviewpager);
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new ReadMessageFragment());
        fragments.add(new ReadCommentFragment());
        fragments.add(new ReadCommentFragment());
        ReadAdapter adapter = new ReadAdapter(fragments,getSupportFragmentManager());
        viewPager.setAdapter(adapter);
    }
}
