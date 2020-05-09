package com.example.samsung_project;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
/*
public class ChooseActivity extends AppCompatActivity {

    Button create, enter, MapKit;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_layout);
        create = findViewById(R.id.create_conversation);
        enter = findViewById(R.id.enter_conversation);
        MapKit = findViewById(R.id.MapKit);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent i = new Intent(ChooseActivity.this, CreateActivity.class);
                //startActivity(i);
            }
        });
        MapKit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ChooseActivity.this, MapActivity.class);
                startActivity(i);
            }
        });
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent i = new Intent(ChooseActivity.this, EnterActivity.class);
                //startActivity(i);
            }
        });
    }
}
*/

public class ChooseActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager viewPager = findViewById(R.id.viewPager);
        MainPagerAdapter pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new MapFragment());
        //pagerAdapter.addFragment(new ListFragment());
        viewPager.setAdapter(pagerAdapter);
    }

    static class MainPagerAdapter extends FragmentPagerAdapter{
        private ArrayList<Fragment> fragmentList = new ArrayList<>();
        public MainPagerAdapter(FragmentManager fm){
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        void addFragment(Fragment fragment){
            fragmentList.add(fragment);
        }

    }

}