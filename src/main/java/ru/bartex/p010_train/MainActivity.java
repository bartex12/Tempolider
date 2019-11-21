package ru.bartex.p010_train;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.util.Log;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;

import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;

import androidx.drawerlayout.widget.DrawerLayout;
import ru.bartex.p010_train.ru.bartex.p010_train.data.P;
import ru.bartex.p010_train.ru.bartex.p010_train.data.TempDBHelper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG ="33333";

    int accurancy;

    ListView mListView;

    SimpleAdapter sara;
    ArrayList<Map<String, Object>> data;
    Map<String,Object> m;
    String[] stringListMain;
    String[] stringListSubMain;
    int array_size; //размер массива строк для списка в MainActivity
    int[] idPicture = {R.drawable.sec1,R.drawable.metronome70,R.drawable.list80x100};

    final String ATTR_PICTURE = "time";
    final String ATTR_BASE_TEXT = "fraction_time";
    final String ATTR_SUB_TEXT = "number";

    private SharedPreferences prefSetting;

    //создаём базу данных, если ее не было
    TempDBHelper mDbHelper = new TempDBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"MainActivity onCreate");

        //если в базе нет записей, добавляем подход из 4х фрагментов в таблицу TabSet
        // и пишем с именем "Автосохранение секундомера" в таблицу TabFile
        mDbHelper.createDefaultSetIfNeed();
        //выводим в лог все строки базы
        mDbHelper.displayDatabaseInfo();

        mListView = (ListView) findViewById(R.id.listView);
        //адаптер  - в onResume
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //0 - вызов секундомера
            //1- вызов темполидера
            //2  - показ списка файлов в 3х табах
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    Intent intent = new Intent(MainActivity.this, TimeMeterActivity.class);
                    startActivity(intent);
                }else if(i == 1){
                    Intent intent = new Intent(MainActivity.this, SetListActivity.class);
                    intent.putExtra(P.FROM_ACTIVITY,P.MAIN_ACTIVITY);
                    startActivity(intent);
                }else if(i == 2){
                    //вариант 1, когда просто список, в котором имя, дата и тип а одной строке
                    //Intent intent = new Intent(MainActivity.this, ListOfFilesActivity.class);
                    //Вариант 2 на основе Tab? что устарело, хотя и работает
                    //Intent intent = new Intent(MainActivity.this, TabActivity.class);

                    //Вариант 3 на основе TabBar с ViewPager и фрагментами
                    Intent intent = new Intent(MainActivity.this, TabBarActivity.class);
                    startActivity(intent);
                }
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //запрещаем показ заголовка тулбара
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        //и тогда можно установить заголовок так
        //toolbar.setTitle("Yes");
        //или заголовок в Toolbar устанавливается так
        getSupportActionBar().setTitle(R.string.main_menu);

        //установить портретную ориентацию экрана
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //прячем плавающую кнопку
        //fab.hide();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();

                //создаём новую запись
                Intent intentAdd = new Intent(MainActivity.this, NewExerciseActivity.class);
                intentAdd.putExtra(P.FROM_MAIN, P.TO_ADD);
                startActivity(intentAdd);

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //показываем шторку в открытом состоянии при запуске
        //drawer.openDrawer(GravityCompat.START);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Задание значений по умолчанию в файле SharedPreferences
        PreferenceManager.setDefaultValues(this, R.xml.pref_setting, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"MainActivity onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"MainActivity onResume");
        //получаем настройки из активности настроек
        prefSetting = PreferenceManager.getDefaultSharedPreferences(this);
        //получаем из файла настроек количество знаков после запятой
        accurancy = Integer.parseInt(prefSetting.getString("accurancy", "1"));
        Log.d(TAG,"MainActivity onResume accurancy = " + accurancy);

        //получаем массив строк из строковых ресурсов
        stringListMain =  getResources().getStringArray(R.array.MenuMain);
        //получаем массив строк из строковых ресурсов
        stringListSubMain =  getResources().getStringArray(R.array.MenuSubMain);
        //получаем размер списка
        array_size = stringListMain.length;
        //готовим данные для SimpleAdapter
        data = new ArrayList<>(array_size);
        for (int i = 0; i<array_size; i++){

            m = new HashMap<>();
            m.put(ATTR_BASE_TEXT,stringListMain[i]);
            m.put(ATTR_SUB_TEXT,stringListSubMain[i]);
            m.put(ATTR_PICTURE,idPicture[i]);
            data.add(m);
        }
        String[] from = {ATTR_PICTURE, ATTR_BASE_TEXT, ATTR_SUB_TEXT};
        int[] to = {R.id.picture, R.id.base_text,R.id.sub_text};
        sara = new SimpleAdapter(this, data, R.layout.list_item, from, to);
        mListView.setAdapter(sara);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "MainActivity onBackPressed");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            openQuitDialog();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"MainActivity onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"MainActivity onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"MainActivity onDestroy");
        //включаем звук
        AudioManager audioManager =(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
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

    private void openQuitDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(this);
        quitDialog.setTitle(R.string.ExitYesNo);

        quitDialog.setPositiveButton(R.string.DeleteNo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        quitDialog.setNegativeButton(R.string.DeleteYes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        quitDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                Log.d(TAG, "OptionsItem = action_settings");
                Intent intentSettings = new Intent(this, PrefActivity.class);
                startActivity(intentSettings);
                return true;
            case R.id.action_help_main:
                Log.d(TAG, "OptionsItem = action_help_main");
                Intent intentHelpMain = new Intent(this, HelpMainActivity.class);
                startActivity(intentHelpMain);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_timermeter) {

            Intent intent = new Intent(this,TimeMeterActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_pischalka) {
            Intent intent = new Intent(this,SetListActivity.class);
            intent.putExtra(P.FROM_ACTIVITY,P.MAIN_ACTIVITY);
            startActivity(intent);

        } else if (id == R.id.nav_raskladki) {
            Intent intent = new Intent(this,TabBarActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_setting) {
            Intent intent = new Intent(this,PrefActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_help) {
            Intent intent = new Intent(this,HelpMainActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_share) {

            //поделиться - передаём ссылку на приложение в маркете
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                   R.string.PaceMaker +
                            "https://play.google.com/store/apps/details?id=" +
                            getPackageName());
            //sendIntent.putExtra(Intent.EXTRA_TEXT,
            //       "Теполидер: " +
            //      "https://play.google.com/store/apps/details?id=" +
             //     "ru.bartex.jubelee_dialog_singllist");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);

        } else if (id == R.id.nav_send) {
            //оценить- попадаем на страницу приложения в маркете
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(
                    "http://play.google.com/store/apps/details?id=" +
                            getPackageName()));
            //intent.setData(Uri.parse(
             //       "http://play.google.com/store/apps/details?id=ru.bartex.jubelee_dialog_singllist"));
            startActivity(intent);
        }
        // Выделяем выбранный пункт меню в шторке
        item.setChecked(true);
        // Выводим выбранный пункт в заголовке
        setTitle(item.getTitle());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
