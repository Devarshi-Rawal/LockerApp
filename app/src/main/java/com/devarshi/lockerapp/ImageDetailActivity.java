package com.devarshi.lockerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;

import com.devarshi.adapter.ViewPagerAdapter;

import java.util.ArrayList;

public class ImageDetailActivity extends AppCompatActivity {

    //creating object of ViewPager
    ViewPager mViewPager;

    //Creating Object of ViewPagerAdapter
    ViewPagerAdapter mViewPagerAdapter;

    //Variables and Intents
    Intent intent;
    ArrayList<String> movedItemsList;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        mViewPager = findViewById(R.id.viewpager);

        intent = getIntent();
        movedItemsList = (ArrayList<String>) intent.getSerializableExtra("imagePathArrayList");
        position = intent.getIntExtra("position",0);
        mViewPagerAdapter = new ViewPagerAdapter(this,movedItemsList);

        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setCurrentItem(position,false);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int currentPosition) {
                position = currentPosition;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}