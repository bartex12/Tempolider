package ru.bartex.p010_train;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class TabBarActivity extends AppCompatActivity {

    ArrayList<String> tempFiles = new ArrayList<>();
    static String TAG = "33333";
    final int PAGE_COUNT = 3; //количество вкладок
    private String tabTitles[] = new String[] { "Сек", "Темп", "Избр" };

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    public ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_bar);
        Log.d(TAG, "TabBarActivity onCreate");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar acBar = getSupportActionBar();
        acBar.setTitle("Раскладки");
        //показать стрелку Назад
        acBar.setDisplayHomeAsUpEnabled(true );
        acBar.setHomeButtonEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        Log.d(TAG, "TabBarActivity currentItem =  " +  mViewPager.getCurrentItem());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        /*
        //создаём массив иконок и перебирая вкладки, устанавливаем их вместо текста,
        //который был отменён в  getPageTitle(int position) строкой return null;
        int[] imageResId = {
                R.drawable.ic_tab_sec, R.drawable.ic_tab_temp, R.drawable.ic_tab_like
        };
        for (int i = 0; i < imageResId.length; i++) {
            tabLayout.getTabAt(i).setIcon(imageResId[i]);
        }

        //цвет Background
        //tabLayout.setBackgroundColor(Color.YELLOW);
        //Цвет и толщина линии-маркера выделенной вкладки
        tabLayout.setSelectedTabIndicatorColor(Color.RED);
        tabLayout.setSelectedTabIndicatorHeight(10);
        */

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab.hide();  //прячем пока
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "TabBarActivity onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //отслеживание нажатия кнопки HOME
    @Override
    protected void onUserLeaveHint() {

        //Toast toast = Toast.makeText(getApplicationContext(), "onUserLeaveHint", Toast.LENGTH_SHORT);
        //toast.show();
        //включаем звук
        AudioManager audioManager =(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);

        super.onUserLeaveHint();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tab_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //чтобы работала стрелка Назад, а не происходил крах приложения
        if (id == android.R.id.home) {
            Log.d(TAG, "Домой");
            onBackPressed();
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

        //это адаптер, который возвращает фрагменты в зависимости от выбранной вкладки
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position){
                case 0:
                    TabBarSecFragment tabBarSecFragment= TabBarSecFragment.newInstance(0);
                    return tabBarSecFragment;
                case 1:
                    TabBarTempFragment tabBarTempFragment= TabBarTempFragment.newInstance(1);
                    return tabBarTempFragment;
                case 2:
                    TabBarLikeFragment tabBarLikeFragment= TabBarLikeFragment.newInstance(2);
                    return tabBarLikeFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            //возвращаем имя вкладки
           return tabTitles[position];

            //для установки иконок надо return null
            //return null;
        }

            //метод из PageAdapter, при return POSITION_NONE позволяет обновить адаптер с помощью
            //метода notifyDataSetChanged() из любого фрагмента
            @Override
            public int getItemPosition(Object object) {
               // POSITION_NONE makes it possible to reload the PagerAdapter
                return POSITION_NONE;
            }


    }

}
