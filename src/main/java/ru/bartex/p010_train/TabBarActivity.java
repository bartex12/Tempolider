package ru.bartex.p010_train;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import ru.bartex.p010_train.ru.bartex.p010_train.data.P;

public class TabBarActivity extends AppCompatActivity {

    static String TAG = "33333";
    final int PAGE_COUNT = 3; //количество вкладок
    private String tabTitles[] = new String[] { "Сек", "Темп", "Избр" };

    private SectionsPagerAdapter mSectionsPagerAdapter;
    //ViewPager that will host the section contents.
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

        // Создайте адаптер, который вернет фрагмент для каждого из трех
        //        первичных разделов активности.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Установите ViewPager с адаптером вкладок
        mViewPager = (ViewPager) findViewById(R.id.container);
        Log.d(TAG, "TabBarActivity currentItem =  " +  mViewPager.getCurrentItem());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        int currentItem;
        Intent intent = getIntent();
        if (intent.getExtras()!=null){

            String type = intent.getStringExtra(P.TYPE_OF_FILE);
            switch (type){
                case P.TYPE_TIMEMETER:
                    currentItem = 0;
                    break;
                case P.TYPE_TEMPOLEADER:
                    currentItem = 1;
                    break;
                case P.TYPE_LIKE:
                    currentItem = 2;
                    break;
                    default:
                        currentItem = 0;
                        break;
            }
            mViewPager.setCurrentItem(currentItem);
        }

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

        if (id == R.id.action_settings) {
            Log.d(TAG, "OptionsItem = action_settings");
            Intent intentSettings = new Intent(this, PrefActivity.class);
            startActivity(intentSettings);
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
                    return TabBarSecFragment.newInstance(0);
                case 1:
                    return TabBarTempFragment.newInstance(1);
                case 2:
                    return TabBarLikeFragment.newInstance(2);
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
