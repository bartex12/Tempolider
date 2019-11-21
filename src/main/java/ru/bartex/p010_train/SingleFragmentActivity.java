package ru.bartex.p010_train;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import ru.bartex.p010_train.ru.bartex.p010_train.data.P;
import ru.bartex.p010_train.ru.bartex.p010_train.data.TempDBHelper;

/**
 * Created by Андрей on 02.05.2018.
 */
public abstract class SingleFragmentActivity extends AppCompatActivity implements
        SetListFragment.OnShowTotalValuesListener, DialogSetDelay.DelayListener {

    // Лайаут этого класса R.layout.activity_set_list

    protected abstract Fragment createFragment(String nameOfFile);
    //реализация в SetListFragment через SetListActivity и её метод createFragment(String nameOfFile)

    static String TAG = "33333";

    LinearLayout mNameLayout; // Layout для имени файла - на нём щелчок для вызова списка имён

    private Button mStartButton;
    private Button mStopButton;
    private Button mResetButton;
    private Button mDelayButton;

    private ProgressBar mProgressBarTime;
    private TextView mCurrentTime;
    private TextView mCurrentReps;
    private TextView mTextViewDelay;
    private TextView mTextViewRest;
    private TextView mTimeLabel;
    private TextView mRepsLabel;
    private TextView mtextViewCountDown;
    private TextView mNameOfFile;

    private Timer mTimer;
    private Timer mTimerRest;
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

    private long mTimeRestStart = 0; //начальное время отдыха
    private long mTimeRestCurrent = 0; //текущее время отдыха

    private int mCurrentRep = 0; //счётчик повторений
    private int mTotalReps = 0;  //общее количество повторений в подходе
    private int mCurrentTotalReps = 0; //суммарное текущее количество повторений
    private int mCountFragment = 0;  //номер фрагмента подхода
    private int mTotalCountFragment = 0;  //количество фрагментов в подходе

    private boolean workOn = false; //признак начала работы
    private boolean restOn = false; //признак начала отдыха
    private boolean end = false; //признак окончания подхода
    public static boolean start = false;//признак нажатия на старт: public static для доступа из фрагмента

    int mCountFragmentLast = 0;
    //код -откуда пришли данные 111 --Main, 222-TimeMeterActivity, 333-ListOfFilesActivity
    //444 -DetailActivity  555 - NewExerciseActivity
    int fromActivity;

    int accurancy; //точность отсечек - количество знаков после запятой - от MainActivity
    boolean sound = true; // включение / выключение звука
    private SharedPreferences prefSetting;// предпочтения из PrefActivity
    private SharedPreferences shp; //предпочтения для записи задержки общей для всех раскладок
    private int  timeOfDelay = 0; //задержка в секундах

    TempDBHelper mTempDBHelper = new TempDBHelper(this);
    //имя файла с раскладкой
    private String finishFileName;
    //id файла, загруженного в темполидер
    long fileId;
    //количество фрагментов подхода
    int countOfSet ;


    // Метод интерфейса из класса SetListFragment
    @Override
    public void  onShowTotalValues( String time, String reps){
        Log.d(TAG, "SingleFragmentActivity - onShowTotalValues");
        mTimeLabel.setText(time);
        mRepsLabel.setText(reps);
    }

    //метод интерфейса для передачи величины задержки из диалога
    @Override
    public void onDelayTransmit(int delay) {

        Log.d(TAG, "SingleFragmentActivity - onDelayTransmit");
        timeOfDelay = delay;
        String DelayStr = String.valueOf(timeOfDelay);

        shp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor edit = shp.edit();
        edit.putInt(P.KEY_DELAY, timeOfDelay);
        edit.apply();

        mDelayButton.setText(DelayStr);
        if (!restOn){
            mtextViewCountDown.setText(String.valueOf(timeOfDelay));
        }
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


        mNameOfFile = (TextView) findViewById(R.id.textViewName);

        //текстовая метка Задержка, сек
        mTextViewDelay = (TextView) findViewById(R.id.textViewDelay);

        //Текстовая метка До старта, сек и Время отдыха, сек в зависимости от состояния
        mTextViewRest = (TextView) findViewById(R.id.textViewRest);
        mTextViewRest.setText(R.string.textViewTimeRemain);

        mDelayButton = (Button) findViewById(R.id.buttonDelay);
        shp = getPreferences(MODE_PRIVATE);
        timeOfDelay = shp.getInt(P.KEY_DELAY, 6);
        mDelayButton.setText(String.valueOf(timeOfDelay));
        mDelayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String delay = mDelayButton.getText().toString();
                DialogSetDelay dialogFragment = DialogSetDelay.newInstance(delay);
                dialogFragment.show(getSupportFragmentManager(),"delayDialog");
            }
        });

        mProgressBarTime = (ProgressBar)findViewById(R.id.progressBarTime);
        //mProgressBarTime.setBackgroundColor();

        mCurrentTime = (TextView)findViewById(R.id.current_time);
        //для mCurrentReps в макете стоит запрет на выключение экрана android:keepScreenOn="true"
        mCurrentReps = (TextView)findViewById(R.id.current_reps);
        mTimeLabel = (TextView)findViewById(R.id.time_item1_label);
        mRepsLabel = (TextView)findViewById(R.id.reps_item1_label);

        //счётчик времени задержки и времени отдыха
        mtextViewCountDown = (TextView)findViewById(R.id.textViewCountDown);
        mtextViewCountDown.setText(String.valueOf(timeOfDelay));

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

                restOn = false; //признак начала отдыха
                mTimeRestCurrent = 0; //текущее время отдыха

                //играем мелодию начала подхода  с задержкой
                mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 200);

                //запускаем TimerTask на выполнение с периодом mKvant
                mTimer.scheduleAtFixedRate(mTimerTask,mKvant,mKvant);

                //выставляем доступность кнопок
                buttonsEnable (false,true,false);
                Log.d(TAG, "countMilliSecond = " + countMilliSecond +"  countReps = " + countReps);
                //выставляем флаг нажатия на Старт = да
                start = true;

                //делаем изменение задержки недоступным
                mDelayButton.setEnabled(false);

                mTextViewDelay.setText(R.string.textViewDelay); //Задержка, сек
                mTextViewRest.setText(R.string.textViewTimeRemain); //До старта, сек

                //вызываем onPrepareOptionsMenu чтобы скрыть элементы тулбара пока старт
                invalidateOptionsMenu();
            }
        });

        mStopButton = (Button) findViewById(R.id.stop_button);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Pressed StopButton");
                //if (mTimer!=null)mTimer.cancel();
                //выставляем доступность кнопок
                buttonsEnable (true,false,true);
                mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 500);
                //выставляем флаг нажатия на Старт = нет
                start = false;
                restOn = true; //признак начала отдыха
                mTimeRestCurrent = 0; //обнуляем текущее время отдыха
                //фиксируем момент начала отдыха
                mTimeRestStart = System.currentTimeMillis();
                //делаем имя файла доступным для щелчка
               // mNameLayout.setEnabled(true);
                //делаем изменение задержки доступным
                mDelayButton.setEnabled(true);
                mTextViewRest.setText(R.string.textViewRestTime);  //Время отдыха, сек

                //вызываем onPrepareOptionsMenu чтобы открыть элементы тулбара если стоп
                //invalidateOptionsMenu();
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
                restOn = false; //признак начала отдыха
                mTimeRestCurrent = 0; //текущее время отдыха
                mTextViewDelay.setText(R.string.textViewDelay); //задержка,сек
                mTextViewRest.setText(R.string.textViewTimeRemain); //До старта, сек
                mtextViewCountDown.setTextColor(Color.RED);
                mtextViewCountDown.setText(String.valueOf(timeOfDelay));// величина задержки из поля timeOfDelay

                //устанавливаем цвет маркера фрагмента подхода в исходный цвет, обновляя адаптер
                changeMarkColor(R.id.fragment_container, mCountFragment, end);
                start = false;
                //вызываем onPrepareOptionsMenu чтобы открыть элементы тулбара
                invalidateOptionsMenu();
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
            //если файл был удалён, fileId = -1 и тогда вместо finishNameFile
            // передаём Автосохранение секундомера
            if ((mTempDBHelper.getIdFromFileName(finishFileName)) == -1){
                finishFileName = P.FILENAME_OTSECHKI_TEMP;
            }
            Log.d(TAG, " finishFileName = " + finishFileName);

            //если интент пришел от TimeGrafActivity, он принёс с собой  отсечеки в файле
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

        //если интент пришёл от NewExerciseActivity после + в Главном меню
        } else if(fromActivity == 555) {
            //получаем имя файла из интента
            finishFileName = intent.getStringExtra(P.FINISH_FILE_NAME);
            timeOfDelay = mTempDBHelper.getFileDelayFromTabFile(finishFileName);
            mDelayButton.setText(String.valueOf(timeOfDelay));

        }else Log.d(TAG, " intentTransfer = null ");

        Log.d(TAG, " fromActivity =  " + fromActivity +" mNameOfFile = " + finishFileName);

            //получаем id файла
            fileId = mTempDBHelper.getIdFromFileName(finishFileName);
            //количество фрагментов подхода
            countOfSet =mTempDBHelper.getSetFragmentsCount(fileId);
            Log.d(TAG, " getSetFragmentsCount =  " + countOfSet);

        //Вставляем фрагмент, реализованный в классе, наследующем SingleFragmentActivity
        //и реализующем абстрактный метод createFragment()
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            //Вызываем класс, реализующий абстрактный метод createFragment(),т.е. SetListFragment(),
            //и  передаём имя файла в абстрактном методе createFragment()
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

        Log.d(TAG, "fileId  = " + fileId);
        //получаем количество фрагментов в выполняемом подходе если было удаление или добавление
        //фрагмента подхода, нужно пересчитывать каждый раз - это по кнопке Старт
        mTotalCountFragment = mTempDBHelper.getSetFragmentsCount(fileId);

        //посчитаем общее врямя выполнения подхода в секундах
        mTimeOfSet = mTempDBHelper.getSumOfTimeSet(fileId);
        Log.d(TAG, "Суммарное время подхода  = " + mTimeOfSet);

        //посчитаем общее количество повторений в подходе
        mTotalReps = mTempDBHelper.getSumOfRepsSet(fileId);
        Log.d(TAG, "Суммарное количество повторений  = " + mTotalReps + " fileId = " + fileId);

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
        Log.d(TAG, "SingleFragmentActivity - onUserLeaveHint");
        //включаем звук
        AudioManager audioManager =(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);

        super.onUserLeaveHint();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "SingleFragmentActivity onActivityResult");
        if (resultCode == RESULT_OK){
            Log.d(TAG, "SingleFragmentActivity onActivityResult resultCode == RESULT_OK = "+
                    + resultCode + "  requestCode = " + requestCode);
            //если пришли из редактора, получаем finishFileName и отдаём его фрагменту в
            //методе фрагмента setFinishFileName(), в котором оно присваивается переменной фрагмента
            if (requestCode == P.REDACT_REQUEST){
                finishFileName = data.getExtras().getString(P.FINISH_FILE_NAME);
                 SetListFragment fragment = (SetListFragment)getSupportFragmentManager().
                findFragmentById(R.id.fragment_container);
                fragment.setFinishFileName(finishFileName);
            }
        }else {
            Log.d(TAG, "SingleFragmentActivity onActivityResult resultCode != RESULT_OK = " +
                    resultCode + "  requestCode = " + requestCode);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.singl_fragment_activity_menu,menu);
        Log.d(TAG, "onCreateOptionsMenu");
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ActionBar acBar = getSupportActionBar();
        Log.d(TAG, "onPrepareOptionsMenu");
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
                //так нельзя делать, иначе будет возврат к  MainActivity при выходе из программы
                //Intent intentHome = new Intent(this,MainActivity.class);
                //startActivity(intentHome);
                //а так нужно, иначе не работает стрелка -
                // (можно еще onBackPressed(), как в ChangeTempActivity но там на 1 позицию возврат)
                finish();
                return true;

            case R.id.change_data:
                Log.d(TAG, "change_data");
                Intent intent = new Intent(this, ChangeTempActivity.class);
                intent.putExtra(P.FINISH_FILE_NAME, finishFileName);
                startActivityForResult(intent,P.REDACT_REQUEST);
                return true;

            case R.id.show_list_of_files:
                //определяем тип файла
                String type =  mTempDBHelper.getFileTypeFromTabFile(fileId);
                //вызываем TabBarActivity
                Intent intentList = new Intent(getBaseContext(), TabBarActivity.class);
                intentList.putExtra(P.TYPE_OF_FILE, type);
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
                String time = showFormatString(mTotalTime, mKvant);
                //показ текущ времени
                mCurrentTime.setText(time);

                //показ текущ количества повторений
                mCurrentReps.setText(Integer.toString(mCurrentTotalReps));

                //показываем текущую задержку
                float f =(countMillisDelay - mCurrentDelay)/1000;
                if (f>=0) {
                    if (!restOn){
                        mTextViewRest.setText(R.string.textViewTimeRemain); //До старта, сек
                        mtextViewCountDown.setTextColor(Color.RED);
                        mtextViewCountDown.setText(Float.toString((f)));
                    }else {
                        mTextViewRest.setText(R.string.textViewRestTime);  //Время отдыха, сек
                        String timeRest = showFormatString(mTimeRestCurrent, mKvant);
                        mtextViewCountDown.setTextColor(Color.BLUE);
                        mtextViewCountDown.setText(timeRest);
                    }
                }else{
                    //если работа а не отдых
                    if (workOn&&!restOn){
                        mTextViewRest.setText(""); //До старта, сек
                        mtextViewCountDown.setTextColor(Color.RED);
                        mtextViewCountDown.setText("РАБОТА");

                    }
                    //если отдых
                    if (restOn) {
                        mTextViewRest.setText(R.string.textViewRestTime);  //Время отдыха, сек
                        String timeRest = showFormatString(mTimeRestCurrent, mKvant);
                        mtextViewCountDown.setTextColor(Color.BLUE);
                        mtextViewCountDown.setText(timeRest);
                    }
                }

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

    private String showFormatString (long total, long kvant) {
        //формируем формат строки показа времени
        int minut = ((int)total/60000)%60;
        int second = ((int)total/1000)%60;
        int decim = (int)((total%1000)/kvant);
        int hour = (int)((total/3600000)%24);

        // общее время подхода
        String time = "";
        if (hour<1){
            if(minut<10) {
                time = String.format(Locale.ENGLISH,"%d:%02d.%d",minut, second, decim);
            }else if (minut<60){
                time = String.format(Locale.ENGLISH,"%02d:%02d.%d",minut,second,decim);
            }
        }else {
            time = String.format(Locale.ENGLISH,"%d:%02d:%02d.%d",hour,minut,second,decim);
        }
        return time;
    }

    //======================class MyTimerTask=================================//

    public class MyTimerTask extends TimerTask{
        @Override
        public void run() {  //запускаем MyTimerTask в методе run()

            //фиксируем изменения на экране (в пользовательском потоке)
            doChangeOnViThread();

            mCurrentDelay += mKvant;

            //если не отдых
            if (!restOn){

                if ((mCurrentDelay<=countMillisDelay-500)&&(mCurrentDelay>countMillisDelay-600)){
                    //играем мелодию начала подхода
                    mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 150);
                    SystemClock.sleep(250);
                    mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 300);
                }
                if (mCurrentDelay>=countMillisDelay){
                    workOn = true;
                }
                // если отдых
            }else {
                mTimeRestCurrent = System.currentTimeMillis() - mTimeRestStart;
                Log.d(TAG, "mTimeRestCurrent = " + mTimeRestCurrent);

                //фиксируем изменения на экране (в пользовательском потоке)
                doChangeOnViThread();
            }

            if (workOn&&!restOn) {

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
                    //if (mTimer != null) mTimer.cancel();
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
                    workOn = false;  //признак начала работы
                    restOn = true; //признак начала отдыха
                    //фиксируем момент начала отдыха
                    mTimeRestStart = System.currentTimeMillis();

                    //изменяем свойство кнопок и сбрасываем цвет, восстанавливаем меню
                    // в пользовательском потоке
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //чтобы не оставался последний фрагмент подхода со старым цветом
                            changeMarkColor(R.id.fragment_container, mCountFragment, end);
                            buttonsEnable(true, false, true);
                            mDelayButton.setEnabled(true);
                        }
                    });
                }
            }
        }
    }

}
