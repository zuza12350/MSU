package com.example.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Coordinates"));
        tabLayout.addTab(tabLayout.newTab().setText("Pixels"));
        tabLayout.addTab(tabLayout.newTab().setText("Map preview"));

        loadFragment(new CoordinatesFragment());
        TabLayout.Tab first = tabLayout.getTabAt(0);
        if (first != null) first.select();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment;
                switch (tab.getPosition()) {
                    case 0: fragment = new CoordinatesFragment(); break;
                    case 1: fragment = new PixelsFragment(); break;
                    case 2: fragment = new MapPreviewFragment(); break;
                    default: fragment = new CoordinatesFragment();
                }
                loadFragment(fragment);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.tabsContent, fragment)
                .commit();
    }
}
