package ru.bartex.p010_train;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.series.DataPoint;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import ru.bartex.p010_train.ru.bartex.p010_train.data.P;
import ru.bartex.p010_train.ru.bartex.p010_train.data.TabSet;
import ru.bartex.p010_train.ru.bartex.p010_train.data.TempDBHelper;

/**
 * Created by Андрей on 02.05.2018.
 */
public abstract class SingleFragmentActivity extends AppCompatActivity implements
        SetListFragment.OnShowTotalValuesListener, DialogSaveTempFragment.SaverFragmentListener,
        DialogSetDelay.DelayListener {

    // Лайаут этого класса R.layout.activity_set_list

    protected abstract Fragment createFragment(String nameOfFile);
    //реализация в SetListFragment через SetListActivity и её метод createFragment(String nameOfFile)

    LinearLayout mNameLayout; // Layout для имени файла - на нём щелчок для вызова списка имён

    private Button mStartButton;
    private Button mStopButton;
    private Button mResetButton;
    private Button mDelayButton;

    private ProgressBar mProgressBarTime;
    private TextView mCurrentTime;
    private TextView mCurrentReps;
    private TextView mTimeLabel;
    private TextView mRepsLabel;
    private TextView mtextViewCountDown;
    private TextView mNameOfFile;

    static String TAG = "33333";

    public static final int REQUEST_UUID = 1;
    public static final String ARRAY_STRING_TRANSFER = "ru.bartex.p010_train_transfer";
    public static final String ARRAY_STRING_FROM_LIST = "ru.bartex.p010_train_from_main";
    public static final String FROM_ACTIVITY = "ru.bartex.p010_train_from_activity";

    private Timer mTimer;
    private TimerTask mTimerTask;
    private ToneGenerator mToneGenerator;

    private float countMilliSecond =1000; //колич миллисекунд, получаемое из mEditTextTime
    private float countMillisDelay =5000; //колич миллисекунд, получаемое из mEditTextDelay
    private float mTimeOfSet = 0;   //общее время выполнения подхода в секундах
    private int countReps = 2; //количество повторений,получаемое из текста mEditTextReps

    public final static long mKvant = 100;//время в мс между срабатываниями TimerTask
    private long mTotalKvant = 0;//текущее суммарное время для фрагмента подхода
    private long mTotalTime = 0; //текущее суммарное время для подхода
    private long mCurrentDelay = 0; //текущее время для задержки
    private int mCurrentRep = 0; //счётчик повторений
    private int mTotalReps = 0;  //общее количество повторений в подходе
    private int mCurrentTotalReps = 0; //суммарное текущее количество повторений
    private int mCountFragment = 0;  //номер фрагмента подхода
    private int mTotalCountFragment = 0;  //количество фрагментов в подходе

    private boolean workOn = false; //признак начала работы
    private boolean end = false; //признак окончания подхода
    public static boolean start = false;//признак нажатия на старт: public static для доступа из фрагмента

    ArrayList<String> mListTimeTransfer = new ArrayList<>(); //список отсечек от секундомера
    int mCountFragmentLast = 0;

    public static final String FILENAME_OTSECHKI_TEMP ="автосохранение__темполидера";
    public static final String FILENAME_OTSECHKI_SEC ="автосохранение_секундомера";
    public static final String TYPE_TIMEMETER ="type_timemeter";
    public static final String TYPE_TEMPOLEADER ="type_tempoleader";
    public static final String TYPE_LIKE ="type_like";
    public static final String FILENAME_NAMES_OF_FILES ="ru.bartex.p010_train_names_of_files";
    public static final String FILENAME_TYPE ="ru.bartex.p010_train_type";
    public static final String FILENAME_DATE ="ru.bartex.p010_train_date";

    private static final String KEY_DELAY = "DELAY";
    static final int request_code = 111;

    int fromActivity; //код -откуда пришли данные 111 --Main, 222-TimeMeterActivity, 333-ListOfFilesActivity
    private String typeOfFile;  //тип файла из ListOfFileActivity
    private String date;  //дата из ListOfFileActivity


    int accurancy; //точность отсечек - количество знаков после запятой - от MainActivity
    boolean sound = true; // включение / выключение звука
    private SharedPreferences prefSetting;// предпочтения из PrefActivity
    private SharedPreferences shp; //предпочтения для записи задержки общей для всех раскладок
    private int  timeOfDelay = 0; //задержка в секундах

    TempDBHelper mTempDBHelper = new TempDBHelper(this);
    private String finishFileName; //имя файла с раскладкой
    long fileId; //id файла, загруженного в темполидер

    // Метод интерфейса из класса SetListFragment
    @Override
    public void  onShowTotalValues( String time, String reps){
        Log.d(TAG, "SingleFragmentActivity - onShowTotalValues");
        mTimeLabel.setText(time);
        mRepsLabel.setText(reps);
    }


    //oldNameFile - имя файла для записи в список сохранённых файлов
    @Override
    public void onFileNameTransmit(String oldNameFile, String newNameFile) {

        //имя файла, если строка имени пуста
        String fileNameDefoult =P.FINISH_FILE_NAME;

        long oldFileId = mTempDBHelper.getIdFromFileName(oldNameFile);
        String typeFile = mTempDBHelper.getFileTypeFromTabFile(oldFileId);

        if (newNameFile.isEmpty()) {
            switch (typeFile) {
                case P.TYPE_TIMEMETER:
                    fileNameDefoult = P.FILENAME_OTSECHKI_SEC;
                    break;
                case P.TYPE_TEMPOLEADER:
                    fileNameDefoult = P.FILENAME_OTSECHKI_TEMP;
                    break;
                case P.TYPE_LIKE:
                    fileNameDefoult = P.FILENAME_OTSECHKI_LIKE;
                    break;
            }
        }

        //и устанавливаем соответствующие параметры
        finishFileName = saveDataAndFilename(newNameFile, fileNameDefoult, typeFile);
        //выводим имя файла на экран
        mNameOfFile.setText(finishFileName);

        }


    //метод интерфейса для передачи величины задержки из диалога
    @Override
    public void onDelayTransmit(int delay) {

        timeOfDelay = delay;

        shp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor edit = shp.edit();
        edit.putInt(P.KEY_DELAY, timeOfDelay);
        edit.apply();

        mDelayButton.setText(String.valueOf(timeOfDelay));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_list);
        Log.d(TAG, "SingleFragmentActivity - onCreate");

        //чтобы не выскакивала экранная клавиатура, в манифесте добавлена строка
        //android:windowSoftInputMode="stateHidden"

        //разрешить только портретную ориентацию экрана
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ActionBar acBar = getSupportActionBar();
        acBar.setTitle("Темполидер");
        //показать стрелку Назад
        acBar.setDisplayHomeAsUpEnabled(true );
        acBar.setHomeButtonEnabled(true);

        mNameLayout = (LinearLayout) findViewById(R.id.name_layout);
        mNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //если в активности нажата Старт = true , ничего не делать
                if (start){
                    Toast.makeText(SingleFragmentActivity.this,
                            "Сначала нажмите Стоп", Toast.LENGTH_SHORT).show();

                    //если в активности нажата Стоп, то Старт = false
                }else{
                    //вызываем TabBarActivity
                    Intent intentList = new Intent(getBaseContext(), TabBarActivity.class);
                    startActivity(intentList);
                    finish(); //чтобы не включать в стек возврата,
                }
            }
        });

        mNameOfFile = (TextView) findViewById(R.id.textViewName);

        mDelayButton = (Button) findViewById(R.id.buttonDelay);
        shp = getPreferences(MODE_PRIVATE);
        timeOfDelay = shp.getInt(P.KEY_DELAY, 6);
        mDelayButton.setText(String.valueOf(timeOfDelay));
        mDelayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String delay = mDelayButton.getText().toString();
                DialogSetDelay dialogFragment = DialogSetDelay.newInstance(delay);
                dialogFragment.show(getFragmentManager(),"delayDialog");
            }
        });

        mProgressBarTime = (ProgressBar)findViewById(R.id.progressBarTime);
        //mProgressBarTime.setBackgroundColor();

        mCurrentTime = (TextView)findViewById(R.id.current_time);
        //для mCurrentReps в макете стоит запрет на выключение экрана android:keepScreenOn="true"
        mCurrentReps = (TextView)findViewById(R.id.current_reps);
        mTimeLabel = (TextView)findViewById(R.id.time_item1_label);
        mRepsLabel = (TextView)findViewById(R.id.reps_item1_label);
        mtextViewCountDown = (TextView)findViewById(R.id.textViewCountDown);

        mStartButton = (Button) findViewById(R.id.start_button);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, " mStartButton.onClick");
                if (mTimer!=null)mTimer.cancel();
                mTimer =new Timer();
                mTimerTask = new MyTimerTask();
                mToneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC,100);

                //Выполняем начальные установки параметров, которые могли измениться
                mCurrentRep = 0;  //устанавливаем счётчик повторений во фрагменте в 0
                mCurrentTotalReps = 0; //устанавливаем счётчик выполненных в подходе повторений в 0
                mCurrentDelay = 0;  //устанавливаем текущее время задержки в 0
                mTotalKvant = 0;  //устанавливаем текущее время между повторениями в 0
                mTotalTime = 0; //обнуляем счётчик суммарного времени
                mCountFragment = 0; //обнуляем счётчик фрагментов подхода
                mProgressBarTime.setProgress(100);

                //Получаем время задержки в мс
                countMillisDelay = timeOfDelay*1000;

               // рассчитываем время между повторениями и количество повторений
                //   для первого фрагмента подхода до начала работы таймера

                //получаем время между повторениями mCountFragment = 0 фрагмента подхода
                countMilliSecond = mTempDBHelper.
                        getTimeOfRepInPosition(fileId, mCountFragment)*1000;
                Log.d(TAG, "Время между повторениями = " + countMilliSecond);

                //получаем количество повторений для mCountFragment = 0 фрагмента подхода
                countReps = mTempDBHelper.getRepsInPosition(fileId, mCountFragment);
                Log.d(TAG, "Количество повторений во фрагменте подхода = " + countReps);

                //получаем количество фрагментов в выполняемом подходе. Если было удаление или добавление
                //фрагмента подхода, нужно пересчитывать каждый раз
                mTotalCountFragment = mTempDBHelper.getSetFragmentsCount(fileId);

                //Покажем таймер задержки
                mtextViewCountDown.setText(String.valueOf(timeOfDelay));

                //Выставляем флаг "работа"
                workOn = false;

                //Выставляем флаг конец работы - нет
                end = false;

                //запускаем TimerTask на выполнение с периодом mKvant
                mTimer.scheduleAtFixedRate(mTimerTask,mKvant,mKvant);

                //играем мелодию начала подхода  с задержкой
                mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 200);

                //выставляем доступность кнопок
                buttonsEnable (false,true,false);
                Log.d(TAG, "countMilliSecond = " + countMilliSecond +"  countReps = " + countReps);
                //выставляем флаг нажатия на Старт = да
                start = true;
                //делаем имя файла недоступным для щелчка
               // mNameLayout.setEnabled(false); //всё сделано в слушателе TextView

                //делаем изменение задержки недоступным
                mDelayButton.setEnabled(false);
                //вызываем onPrepareOptionsMenu для создания недоступномсти значков ToolBar
                invalidateOptionsMenu();

            }
        });

        mStopButton = (Button) findViewById(R.id.stop_button);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Pressed StopButton");
                if (mTimer!=null)mTimer.cancel();
                //выставляем доступность кнопок
                buttonsEnable (true,false,true);
                mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 500);
                //выставляем флаг нажатия на Старт = нет
                start = false;
                //делаем имя файла доступным для щелчка
               // mNameLayout.setEnabled(true);
                //делаем изменение задержки доступным
                mDelayButton.setEnabled(true);
                //вызываем onPrepareOptionsMenu для создания доступномсти значков ToolBar
                invalidateOptionsMenu();
            }
        });

        mResetButton = (Button) findViewById(R.id.reset_button);
        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Pressed ResetButton");
                if (mTimer!=null)mTimer.cancel();
                mCurrentRep = 0;
                mCurrentTotalReps = 0;
                mTotalKvant = 0;
                mCurrentDelay = 0;  //устанавливаем текущее время задержки в 0
                mTotalTime = 0;
                mTotalReps = 0;
                mCountFragment = 0;
                mCurrentReps.setText("");
                mCurrentTime.setText("");
                mProgressBarTime.setProgress(100);
                //mProgressBarTotal.setProgress(100);
                //выставляем доступность кнопок
                buttonsEnable (true,false,false);
                mtextViewCountDown.setText("");
                //признак начала работы
                workOn = false;
                //признак окончания подхода в Нет
                end = false;
                //устанавливаем цвет маркера фрагмента подхода в исходный цвет, обновляя адаптер
                changeMarkColor(R.id.fragment_container, mCountFragment, end);
            }
        });

        //************** Получение интента с данными *************

        //получаем интент, он есть в любом случае
        Intent intent = getIntent();
        //считываем значение FROM_ACTIVITY из интента
        // MainActivity =111   TIME_GRAF_ACTIVITY = 222    TabBarActivity = 333
        fromActivity = intent.getIntExtra(P.FROM_ACTIVITY,111);

        //если интент пришел от MainActivity
        if (fromActivity == 111){
            Log.d(TAG, " fromActivity =  " + fromActivity);

            //плучаем имя последнего файла темполидера из преференсис (запись в onDestroy )
            shp = getPreferences(MODE_PRIVATE);
            //имя файла  = null, если не было записи в преференсис-это возможно при первом запуске
            // тогда присваиваем имя записи в базе, сделанной в onCreate MainActivity
            finishFileName = shp.getString(P.KEY_FILENAME,P.FILENAME_OTSECHKI_TEMP);

            //если интент пришел от TimeGrafActivity, он принёс с собой список отсечек
        }else if (fromActivity == 222){
            Log.d(TAG, " SingleFragmentActivity fromActivity =  " + fromActivity);
            //получаем имя файла из интента
            finishFileName = intent.getStringExtra(P.FINISH_FILE_NAME);
            //finishFileName=null, если не было записи в преференсис- тогда присваиваем имя
            //единственной записи в базе, сделанной в onCreate MainActivity
            if (finishFileName==null){
                finishFileName = P.FILENAME_OTSECHKI_SEC;
            }

            //если интент пришёл от TabBarActivity
        }else if(fromActivity == 333) {
            Log.d(TAG, " fromActivity =  " + fromActivity);

            //получаем имя файла из интента
            finishFileName = intent.getStringExtra(P.FINISH_FILE_NAME);
            //получаем id  файла с раскладкой по его имени finishFileName из интента
            fileId = mTempDBHelper.getIdFromFileName(finishFileName);

        } else Log.d(TAG, " intentTransfer = null ");

        Log.d(TAG, " fromActivity =  " + fromActivity +" mNameOfFile = " + finishFileName);



        //Вставляем фрагмент, реализованный в классе, наследующем SingleFragmentActivity
        //и реализующем абстрактный метод createFragment()
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            //Вызываем класс, реализующий абстрактный метод createFragment(),т.е. SetListFragment(),
            //и на всякий случай передаём имя файла в абстрактном методе createFragment()
            fragment = createFragment(finishFileName);
            //Получив View из R.layout.fragment_set_list в SetListFragment(),
            // вставляем фрагмент в FrameLayout (с R.id.fragment_container) разметки fragment_set_list
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }

        //выставляем доступность кнопок
        buttonsEnable (true,false,false);
    }
    //**********************   конец onCreate    ************************//

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "SingleFragmentActivity  onResume ");

        //получаем настройки из активности настроек
        prefSetting = PreferenceManager.getDefaultSharedPreferences(this);
        //получаем из файла настроек количество знаков после запятой
        accurancy = Integer.parseInt(prefSetting.getString("accurancy", "1"));
        Log.d(TAG,"SingleFragmentActivity onResume accurancy = " + accurancy);
        //получаем из файла настроек наличие звука
        sound = prefSetting.getBoolean("cbSound",true);
        Log.d(TAG,"SingleFragmentActivity onResume sound = " + sound);
        //включаем/выключаем звук в зависимости от состояния чекбокса в PrefActivity
        AudioManager audioManager;
        if(sound){
            audioManager =(AudioManager)getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        }else{
            audioManager =    (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        }

        //выводим имя файла на экран
        mNameOfFile.setText(finishFileName);

        //получаем id  файла с раскладкой по его имени finishFileName из интента
        fileId = mTempDBHelper.getIdFromFileName(finishFileName);

        //получаем количество фрагментов в выполняемом подходе если было удаление или добавление
        //фрагмента подхода, нужно пересчитывать каждый раз - это по кнопке Старт
        mTotalCountFragment = mTempDBHelper.getSetFragmentsCount(fileId);

        //посчитаем общее врямя выполнения подхода в секундах
        mTimeOfSet = mTempDBHelper.getSumOfTimeSet(fileId);
        Log.d(TAG, "Суммарное время подхода  = " + mTimeOfSet);

        //посчитаем общее количество повторений в подходе
        mTotalReps = mTempDBHelper.getSumOfRepsSet(fileId);
        Log.d(TAG, "Суммарное количество повторений  = " + mTotalReps);

        //покажем общее время подхода и общее число повторений в подходе
        showTotalValues(mTimeOfSet,mTotalReps, mKvant);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "SingleFragmentActivity - onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "SingleFragmentActivity - onStop");
    }

    //отслеживаем нажатие аппаратной кнопки Back и запрещаем, если темполидер работает
    @Override
    public void onBackPressed() {
        if (start){
            Log.d(TAG,"TimeMeterActivity onBackPressed if (start)");
            Toast.makeText(getApplicationContext(),
                    "Сначала нажмите Стоп", Toast.LENGTH_SHORT).show();
        }else{
            super.onBackPressed();
            Log.d(TAG,"TimeMeterActivity onBackPressed if (!start)");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //записываем последнее имя файла на экране в преференсис активности
        shp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor edit = shp.edit();
        edit.putString(P.KEY_FILENAME, finishFileName);
        edit.apply();

        Log.d(TAG, "SingleFragmentActivity - onDestroy");

        //отключаем таймер
        if (mTimer!=null)mTimer.cancel();
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
        //return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.singl_fragment_activity_menu,menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ActionBar acBar = getSupportActionBar();
    /*
        //можно прятать всю панель
        if (start){
            acBar.hide();
        }else acBar.show();
    */
        //отключаем видимость на время от Старт до Стоп
        //acBar.setHomeButtonEnabled(!start);  //не работает
        acBar.setDisplayHomeAsUpEnabled(!start );
        //отключаем видимость на время от Старт до Стоп
        menu.findItem(R.id.menu_item_new_frag).setVisible(!start);
        menu.findItem(R.id.save_data_in_file).setVisible(!start);
        menu.findItem(R.id.show_list_of_files).setVisible(!start);
        menu.findItem(R.id.action_settings_temp).setVisible(!start);
        menu.findItem(R.id.change_data).setVisible(!start);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                Log.d(TAG, "Домой");
                Intent intentHome = new Intent(this,MainActivity.class);
                startActivity(intentHome);
                finish();
                return true;

            case R.id.menu_item_new_frag:

                //вызываем DetailActivity и передаём туда fileId
                Intent intentDetail = new Intent(getBaseContext(), DetailActivity.class);
                intentDetail.putExtra(P.INTENT_TO_DETILE_FILE_ID, fileId);
                intentDetail.putExtra(P.FROM_ACTIVITY,P.TO_ADD_SET);
                startActivity(intentDetail);
                return true;

            case R.id.change_data:
                Intent intent = new Intent(this, ChangeTempActivity.class);
                //передаём id  файла на экране
                intent.putExtra(P.INTENT_TO_CHANGE_TEMP_FILE_NAME, finishFileName);
                //передаём request_code - откуда пришл интент
                intent.putExtra(P.CHANGE_TEMP_CHANGE_REQUEST, P.CHANGE_TEMP_CHANGE_REQUEST_CODE);
                startActivity(intent);
                return true;

            case R.id.save_data_in_file:
                DialogFragment dialogFragment = DialogSaveTempFragment.newInstance(finishFileName);
                dialogFragment.show(getSupportFragmentManager(),"SavePickerTempolider");
                onPause();
                return true;

            case R.id.show_list_of_files:
                //вызываем TabBarActivity
                Intent intentList = new Intent(getBaseContext(), TabBarActivity.class);
                startActivity(intentList);
                finish();
                return true;

            case R.id.action_settings_temp:
                //вызываем ListOfFilesActivity
                Intent intentPref = new Intent(getBaseContext(), PrefActivity.class);
                startActivity(intentPref);
                //finish();  //не нужно
                return true;


        }
        return super.onOptionsItemSelected(item);
    }

    /*
    public static class SaverFragment extends DialogFragment {
    // Не будем делать внутренний класс, сделаем отдельный, так как почему то не работает
    // запись в файл: openFileOutput не воспринимается системой (возможно из-за того
   // что диалог - это тоже фрагмент
    }
*/

    //состояние кнопок управления темполидером
    private void buttonsEnable(boolean start,boolean stop,boolean reset){
        mStartButton.setEnabled(start);
        mStopButton.setEnabled(stop);
        mResetButton.setEnabled(reset);
    }

    //покажем общее время подхода и общее число повторений в подходе
    private void showTotalValues(float timeOfSet,int totalReps, long kvant){

        float millisTime = timeOfSet*1000;
        //покажем суммарное время подхода
        int minut = ((int)millisTime/60000)%60;
        int second = ((int)millisTime/1000)%60;
        int decim = (int)((millisTime%1000)/kvant);

        // общее время подхода
        String timeTotal = String.format("Время  %d:%02d.%d",minut,second,decim);
        // общее количество повторений в подходе
        String repsTotal = String.format("Количество  %02d", totalReps);

        mTimeLabel.setText(timeTotal);
        mRepsLabel.setText(repsTotal);

        Log.d(TAG, "showTotalValues timeTotal = " + timeTotal +
                "   repsTotal = " + repsTotal);
    }

    // показать изменения в пользовательском потоке
    private void doChangeOnViThread(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //формируем формат строки показа времени
                int minut = ((int)mTotalTime/60000)%60;
                int second = ((int)mTotalTime/1000)%60;
                int decim = (int)((mTotalTime%1000)/mKvant);
                int hour = (int)((mTotalTime/3600000)%24);

                // общее время подхода
                String time = "";
                if (hour<1){
                    if(minut<10) {
                        time = String.format("%d:%02d.%d",minut, second, decim);
                    }else if (minut<60){
                        time = String.format("%02d:%02d.%d",minut,second,decim);
                    }
                }else {
                    time = String.format("%d:%02d:%02d.%d",hour,minut,second,decim);
                }

                //показ текущ времени
                mCurrentTime.setText(time);

                //показ текущ количества повторений
                mCurrentReps.setText(Integer.toString(mCurrentTotalReps));

                //показываем текущую задержку
                float f =(countMillisDelay - mCurrentDelay)/1000;
                if (f>=0) {
                    mtextViewCountDown.setText(Float.toString((f)));
                }else mtextViewCountDown.setText("");

                //при переходе к следующему фрагменту подхода меняем цвет маркера,
                // затем текущий фрагмент подхода обозначаем как предыдущий
                if (mCountFragment!=mCountFragmentLast) {
                    //посылаем в SetListFragment  mCountFragment и признак  end и обновляем адаптер списка
                    changeMarkColor(R.id.fragment_container, mCountFragment, end);
                    mCountFragmentLast = mCountFragment;
                }

                //Показываем прогресс текущего времени фрагмента подхода
                if (countMilliSecond == 0){
                    mProgressBarTime.setProgress(100);
                }else {
                    String s = Float.toString(((float) mTotalKvant) * 100 / countMilliSecond);
                    float i = Float.parseFloat(s);
                    //прогресс текущ времени
                    mProgressBarTime.setProgress(100 - (int) i);
                }
            }
        });
    }

    //получаем  значение mCountFragment и признак окончания подхода end и обновляем адаптер
    private void changeMarkColor(int id, int countFrag, boolean endOfWork){
        SetListFragment fr = (SetListFragment) getSupportFragmentManager().
                findFragmentById(id);
        fr.changeBackColor(countFrag, endOfWork );
    }

    //======================class MyTimerTask=================================//

    public class MyTimerTask extends TimerTask{
        @Override
        public void run() {  //запускаем MyTimerTask в методе run()

            //фиксируем изменения на экране (в пользовательском потоке)
            doChangeOnViThread();

            mCurrentDelay += mKvant;

            if ((mCurrentDelay>=countMillisDelay-600)&&(mCurrentDelay<countMillisDelay-500)){
                //играем мелодию начала подхода
                mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 150);
                SystemClock.sleep(250);
                mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 300);
            }
            if (mCurrentDelay>=countMillisDelay){
                workOn = true;
            }

            if (workOn) {

                mTotalKvant += mKvant;  // добавляем 100мс пока не будет больше времени между повторами
                mTotalTime += mKvant;  //добавляем 100мс к текущему времени подхода
                if (mTotalKvant >= countMilliSecond) {
                    mCurrentRep++; // если стало больше, переходим к следующему повтору
                    Log.d(TAG, "mCurrentRep = " + mCurrentRep);
                    mCurrentTotalReps++; //считаем количество выполненных повторений
                    mTotalKvant = 0;  //при этом обнуляя текущее время между повторами
                    mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 200);
                }//переходим к следующему фрагменту
                if ((mCurrentRep >= countReps) && (mCountFragment < mTotalCountFragment - 1)) {
                    mCountFragment++;
                    countMilliSecond = mTempDBHelper.
                            getTimeOfRepInPosition(fileId, mCountFragment)*1000;
                    countReps = mTempDBHelper.getRepsInPosition(fileId, mCountFragment);
                    Log.d(TAG, "countMilliSecond = " + countMilliSecond + "  countReps = " + countReps);
                    mTotalKvant = 0;
                    mCurrentRep = 0;
                }
                //если в последнем фрагменте - прекращаем выполнение
                if ((mCurrentRep >= countReps) && (mCountFragment >= mTotalCountFragment - 1)) {
                    if (mTimer != null) mTimer.cancel();
                    Log.d(TAG, "mTimer.cancel");
                    mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 500);
                    //выставляем начальные значения - для повторного старта
                    mProgressBarTime.setProgress(0);
                    //mProgressBarTotal.setProgress(0);
                    mCurrentRep = 0;
                    mTotalKvant = 0;
                    mTotalReps = 0;
                    mCountFragment = 0;
                    end = true;
                    start = false; //это для разблокировки кнопки BACK

                    //изменяем свойство кнопок и сбрасываем цвет, восстанавливаем меню
                    // в пользовательском потоке
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //чтобы не оставался последний фрагмент подхода со старым цветом
                            changeMarkColor(R.id.fragment_container, mCountFragment, end);
                            buttonsEnable(true, false, true);
                            mDelayButton.setEnabled(true);
                            invalidateOptionsMenu();
                        }
                    });
                }
            }
        }
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
                //если не включать эту строку, то в файле будет всего одна строчка, а нужен массив
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

    //запись данных в файл и запись имени файла в список сохранённых файлов
    //в зависимости от имени, введённого в диалоге сохранения и типа данных
    public  String saveDataAndFilename(String nameFile, String fileNameDefoult, String typeData){

        Log.d(TAG, "saveDataAndFilename nameFile = " + nameFile);

        String finishFileName;
        //получаем дату и время в нужном для базы данных формате
        String dateFormat  = mTempDBHelper.getDateString();
        String timeFormat  = mTempDBHelper.getTimeString();

        //если строка имени пустая
        if (nameFile.isEmpty()) {
            //имя будет fileNameDefoult
            finishFileName = fileNameDefoult;

            //проверяем, есть ли в базе запись с таким именем
            long repeatId = mTempDBHelper.getIdFromFileName (finishFileName);
            Log.d(TAG,"saveDataAndFilename repeatId = " + repeatId);
            //если есть (repeatId не равно -1), стираем её и потом пишем новые данные под таким именем
            if (repeatId != -1){
                mTempDBHelper.deleteFileAndSets(repeatId);
            }
        }else {
            finishFileName = nameFile;
        }
        Log.d(TAG, "saveDataAndFilename finishFileName = " + finishFileName);
        //======Начало добавления записей в таблицы DataFile и DataSet=========//
        //если имя файла не пустое (может быть fileNameDefoult)
        //создаём экземпляр класса DataFile в конструкторе
        DataFile file1 = new DataFile(finishFileName, dateFormat, timeFormat,
                null,null, typeData, 6);
        //добавляем запись в таблицу TabFile, используя данные DataFile
        long file1_id =  mTempDBHelper.addFile(file1);

        //находим фрагмент по id контейнера
        SetListFragment fr = (SetListFragment)getSupportFragmentManager().
                findFragmentById(R.id.fragment_container);
        ListView listviewFrag = fr.mListView;
        //меняем задний фон строк списка
        //listviewFrag.setBackgroundColor(Color.RED);

        //получаем адаптер списка для доступа к значениям фрагментов подхода
        ListAdapter sara = listviewFrag.getAdapter();

        //готовим данные фрагментов подхода
        // если индекс =0, то первое значение
        for (int j = 0; j < sara.getCount(); j++ ) {
            float time_now;
            int reps_now;
            int number_now;
             HashMap<String,Object> map = (HashMap<String,Object>)sara.getItem(j);

            time_now = Float.parseFloat((map.get(P.ATTR_TIME).toString()));
            reps_now = (int)map.get(P.ATTR_REP);
            number_now = (int)map.get(P.ATTR_NUMBER);

            //создаём экземпляр класса DataSet в конструкторе
            DataSet set = new DataSet(time_now,reps_now,number_now);
            //добавляем запись в таблицу TabSet, используя данные DataSet
            mTempDBHelper.addSet(set, file1_id);
            Log.d(TAG, "SingleFragmentActivity saveDataAndFilename записан файл = " +
                    finishFileName + "  Количество фрагментов = " + sara.getCount());
            //======Окончание добавления записей в таблицы DataFile и DataSet=========//
            }

        return finishFileName;
    }

}
