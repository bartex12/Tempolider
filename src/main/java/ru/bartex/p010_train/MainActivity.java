package ru.bartex.p010_train;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG ="33333";
    public static final String FILENAME = "ru.bartex.p010_train.filename";
    public static final String ACCURANCY = "ru.bartex.p010_train.accurancy";
    public static final int MAIN_ACTIVITY = 111;
    public final int PREF_ACTIVITY_ACCURANCY = 1;
    int accurancy;

    ListView mListView;
    //ArrayList<String> tempFiles = new ArrayList<>();
    ArrayList<String> arList = new ArrayList<>();
    ArrayAdapter<String> ara;
    SimpleAdapter sara;
    ArrayList<Map<String, Object>> data;
    Map<String,Object> m;
    String[] stringListMain;
    String[] stringListSubMain;
    int array_size; //размер массива строк для списка в MainActivity
    int[] idPicture = {R.drawable.sec1,R.drawable.metronome,R.drawable.my_logo1};

    final String ATTR_PICTURE = "time";
    final String ATTR_BASE_TEXT = "fraction_time";
    final String ATTR_SUB_TEXT = "number";

    private SharedPreferences prefSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"MainActivity onCreate");


        mListView = (ListView) findViewById(R.id.listView);
        //адаптер  - в onResume
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (i == 0) {
                    Intent intent = new Intent(MainActivity.this, TimeMeterActivity.class);
                    //intent.putExtra(PersonCategoryActivity.PERSON_RESOURS_ID, position);
                    startActivity(intent);
                }else if(i == 1){
                    Intent intent = new Intent(MainActivity.this, SetListActivity.class);
                    intent.putExtra(SingleFragmentActivity.FROM_ACTIVITY,MAIN_ACTIVITY);
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
        getSupportActionBar().setTitle("Главное меню");

        //установить портретную ориентацию экрана
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //прячем плавающую кнопку
        fab.hide();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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

        /*
        //Формируем правильный список
        ArrayList<String> temp = new ArrayList<>();
        temp.add("Проба:123:type_timemeter");
        //пишем правильный файл
        writeArrayList (temp, SingleFragmentActivity.FILENAME_NAMES_OF_FILES);
        */

        //читаем файл с именами файлов с раскладками
        ArrayList<String> allNamesOfFiles = readArrayList(FileSaver.FILENAME_NAMES_OF_FILES);
        //заполняем синглет-держатель имён файлов новыми данными из списка имён файлов
       //пишем имя, дату, тип и номер строки списка для каждого имени файла
        Stat.addAllFileSaverToFileSaverLabFromList(allNamesOfFiles);
        Log.d(TAG, "TabBarActivity размер   списка файлов с именами файлов = " + allNamesOfFiles.size());
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
        //получаем ссылку на экземпляр FileSaverLab
        FileSaverLab fileSaverLab = FileSaverLab.get();
        //получаем весь список имён файлов из FileSaverLab
        ArrayList<String> listNamesOfFiles = fileSaverLab.getListFullNamesOfFiles();
        //пишем список имён в файл имён в формате
        // String.format("%s:%s:%s",name,date,type);
        writeArrayList(listNamesOfFiles,FileSaver.FILENAME_NAMES_OF_FILES);
        Log.d(TAG,"MainActivity onDestroy записаны имена в FILENAME_NAMES_OF_FILES");
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
        data = new ArrayList<Map<String, Object>>(array_size);
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

    private void openQuitDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(this);
        quitDialog.setTitle("Выход: Вы уверены?");

        quitDialog.setPositiveButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        quitDialog.setNegativeButton("Да", new DialogInterface.OnClickListener() {
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
            intent.putExtra(SingleFragmentActivity.FROM_ACTIVITY,MAIN_ACTIVITY);
            startActivity(intent);

        } else if (id == R.id.nav_raskladki) {
            Intent intent = new Intent(this,TabBarActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }


        // Выделяем выбранный пункт меню в шторке
        item.setChecked(true);
        // Выводим выбранный пункт в заголовке
        setTitle(item.getTitle());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Записать список меток времени для каждого найденного шага   в файл
    public void writeArrayList(ArrayList<String> arrayList) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    openFileOutput(FILENAME, MODE_PRIVATE)));
            for (String line : arrayList) {
                //функция write не работает для CharSequence, поэтому String
                bw.write(line);
                // тут мог бы быть пробел если надо в одну строку
                //сли не включать эту строку, то в файле будет всего одна строчка, а нужен массив
                bw.write(System.getProperty("line.separator"));
            }
            Log.d(TAG, "Файл ArrayList записан ");
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Прочитать список имён с данными из файла
    private ArrayList<String> readArrayList(String fileName) {

        ArrayList<String> newArrayList = new ArrayList<String>();

        try {
            // открываем поток для чтения
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    openFileInput(fileName)));
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    newArrayList.add(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return newArrayList;
    }


    //Записать список имён с данными  в файл
    private void writeArrayList(ArrayList<String> arrayList, String fileName) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    openFileOutput(fileName, MODE_PRIVATE)));
            for (String line : arrayList) {
                //функция write не работает для CharSequence, поэтому String
                bw.write(line);
                // тут мог бы быть пробел если надо в одну строку
                //сли не включать эту строку, то в файле будет всего одна строчка, а нужен массив
                bw.write(System.getProperty("line.separator"));
            }
            Log.d(TAG, "Файл ArrayList записан ");
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
