package ru.bartex.p010_train;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by Андрей on 02.05.2018.
 */
public abstract class SingleFragmentActivity extends AppCompatActivity implements
        SetListFragment.OnShowTotalValuesListener, DialogSaveTempFragment.SaverFragmentListener,
        DialogSetDelay.DelayListener {

    // Лайаут этого класса R.layout.activity_set_list

    protected abstract Fragment createFragment(String nameOfFile);
    //реализация в SetListFragment

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
    private String finishFileName = FileSaver.FINISH_FILE_NAME; //имя файла с раскладкой

    int accurancy; //точность отсечек - количество знаков после запятой - от MainActivity
    boolean sound = true; // включение / выключение звука
    private SharedPreferences prefSetting;// предпочтения из PrefActivity
    private SharedPreferences shp; //предпочтения для записи задержки общей для всех раскладок
    private int  timeOfDelay = 0; //задержка в секундах

    // Метод интерфейса из класса SetListFragment
    @Override
    public void  onShowTotalValues( String time, String reps){
        Log.d(TAG, "SingleFragmentActivity - onShowTotalValues");
        mTimeLabel.setText(time);
        mRepsLabel.setText(reps);
    }

    // Метод интерфейса из класса SaverFragment(класс сделан для диалога сохранения файла)
    //dataSave список отсечек секундомера или раскладок темполидера
    //nameFile - имя файла для записи в список сохранённых файлов
    //like - флаг : добавить в избранные или нет
    @Override
    public void onArrayListTransmit(ArrayList<String> dataSave, String nameFile) {

        if (fromActivity ==111) {
            //Если данные пришли из MainActivity,
            finishFileName = saveDataAndFilename(dataSave, nameFile,
                    FILENAME_OTSECHKI_TEMP , SingleFragmentActivity.TYPE_TEMPOLEADER);
            Log.d(TAG, "SingleFragmentActivity - onArrayListTransmit ii = " + fromActivity);
            //выводим имя файла на экран
            mNameOfFile.setText(finishFileName);

        } else if (fromActivity == 222) {
            //если данные пришли  от секундомера - отсечки
            finishFileName = saveDataAndFilename(dataSave, nameFile,
                    FILENAME_OTSECHKI_SEC , SingleFragmentActivity.TYPE_TIMEMETER);
            //выводим имя файла на экран
            mNameOfFile.setText(finishFileName);

        }else if (fromActivity == 333){
            //если данные пришли  из списка сохранённых файлов, смотрим на имя файла
            Log.d(TAG, "SingleFragmentActivity - onArrayListTransmit nameFile = " + nameFile);
            String def = "";
            if (typeOfFile.equalsIgnoreCase(TYPE_TEMPOLEADER)){
                def = FILENAME_OTSECHKI_TEMP;
            }else if (typeOfFile.equalsIgnoreCase(TYPE_TIMEMETER)){
                def = FILENAME_OTSECHKI_SEC;
            }else {
                def = FILENAME_OTSECHKI_SEC;
            }
            //и устанавливаем соответствующие параметры
            finishFileName = saveDataAndFilename(dataSave, nameFile, def , typeOfFile);
            //выводим имя файла на экран
            mNameOfFile.setText(finishFileName);
        }
    }

    //метод интерфейса для передачи величины задержки из диалога
    @Override
    public void onDelayTransmit(int delay) {

        timeOfDelay = delay;

        shp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor edit = shp.edit();
        edit.putInt(KEY_DELAY, timeOfDelay);
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
                    Log.d(TAG, "SingleFragmentActivity onClick  start = " + start);
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
        timeOfDelay = shp.getInt(KEY_DELAY, 5);
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

                //получаем время между повторениями mCountFragment = 0 фрагмента подхода
                countMilliSecond = SetLab.getSet(mCountFragment).getTimeOfRep()*1000;

                //получаем количество повторений для mCountFragment = 0 фрагмента подхода
                countReps =  SetLab.getSet(mCountFragment).getReps();

                //посчитаем общее врямя выполнения подхода в секундах
                mTimeOfSet = SetLab.countSetTime();

                //посчитаем общее количество повторений в подходе
                mTotalReps = SetLab.countTotalReps();

                //покажем общее время подхода и общее число повторений в подходе
                showTotalValues(mTimeOfSet,mTotalReps, mKvant);

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
        fromActivity = intent.getIntExtra(FROM_ACTIVITY,0);
        //получаем имя файла из интента
        finishFileName = intent.getStringExtra(FileSaver.FINISH_FILE_NAME);
        Log.d(TAG, " finishFileName =  " + finishFileName);

        //Перед заполнением новыми данными список должен быть очищен от старых данных
        if (mListTimeTransfer!=null) {
            //стираем данные от секундомера
            mListTimeTransfer.clear();
        }

        //если интент пришел от MainActivity, список должен быть очищен от старых данных
        //а SetLab должен быть обнулён, после этого нужно считать данные из файла
        // и если они есть, распарсить и передать в SetLab
        if (fromActivity == 111){
            Log.d(TAG, " fromActivity =  " + fromActivity);

            //читаем сохранённый файл с ОТСЕЧКАМИ
            mListTimeTransfer = readArrayList(FileSaver.FILENAME_OTSECHKI_TEMP);
            //выводим имя файла на экран
            mNameOfFile.setText(FileSaver.FILENAME_OTSECHKI_TEMP);
            //mNameButton.setText(FileSaver.FILENAME_OTSECHKI_TEMP);

            if (mListTimeTransfer!=null) {
                //заполняем синглет-держатель отсечек новыми данными из списка отсечек
                SetLab setLab = SetLab.get();
                setLab.addAllSetInSetLabFromFormatList(mListTimeTransfer);
            }

            //если интент пришел от TimeMeterActivity, он принёс с собой список отсечек
        }else if (fromActivity == 222){
            Log.d(TAG, " fromActivity =  " + fromActivity);

            //получаем из интента список отсечек
            mListTimeTransfer =  intent.getStringArrayListExtra(ARRAY_STRING_TRANSFER);
            //получаем имя файла из интента
            finishFileName = intent.getStringExtra(FileSaver.FINISH_FILE_NAME);
            //выводим имя файла на экран
            mNameOfFile.setText(finishFileName);
            //mNameButton.setText(finishFileName);
            Log.d(TAG, " finishFileName =  " + finishFileName);
            //если интент пришел со списком отсечек, то можно сделать фрагмент на его основе
            if (mListTimeTransfer!=null){
                //заполняем SetLab и Set данными из списка отсечек секундомера из интента
                SetLab setLab = SetLab.get();
                setLab.fillSetFromArrayListSecundomer(mListTimeTransfer);
        }
            //если интент пришёл от TabBarActivity, считывание данных из SetLab идёт
            //в onResume фрагмента темполидера, а здесь только берём из интента тип файла
        }else if(fromActivity == 333) {
            Log.d(TAG, " fromActivity =  " + fromActivity);
            //получаем тип файла из интента
            typeOfFile = intent.getStringExtra(FILENAME_TYPE);
            //получаем имя файла из интента
            finishFileName = intent.getStringExtra(FileSaver.FINISH_FILE_NAME);
            //выводим имя файла на экран
            mNameOfFile.setText(finishFileName);
            //mNameButton.setText(finishFileName);
            //читаем данные раскладки, записанные в файле с именем finishFileName
            mListTimeTransfer = readArrayList(finishFileName);
            //заполняем SetLab на основе ArrayList<String> list
            SetLab setLab = SetLab.get();
            setLab.addAllSetInSetLabFromFormatList(mListTimeTransfer);

        } else Log.d(TAG, " intentTransfer = null ");

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
    };
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
        Log.d(TAG, "SingleFragmentActivity - onDestroy");
        //**************** сохраняем  в файл список файлов с раскладками *********
        //получаем список имён файлов из FileSaverLab
        ArrayList<String> listNamesOfFiles = getListNamesOfFiles();
        //сохраняем в файл список сохранённых файлов
        writeArrayList(listNamesOfFiles,FILENAME_NAMES_OF_FILES);
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
                //при выборе плюсика на панели инструментов передаём uuid нового фрагмента подхода  newSet
                //в фрагмент детализации SetDetailFragment в интенте
                Set newSet = new Set();
                SetLab setLab = SetLab.get();
                newSet.setNumberOfFrag(setLab.getSets().size());
                setLab.addSet(newSet);
                UUID uuid = newSet.getId();

                //вызываем DetailActivity и передаём туда UUID
                Intent intentDetail = new Intent(getBaseContext(), DetailActivity.class);
                intentDetail.putExtra(DetailActivity.INTENT_SET_UUID,uuid);
                startActivity(intentDetail);
                return true;

            case R.id.save_data_in_file:
                DialogFragment dialogFragment = new DialogSaveTempFragment();
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

            case R.id.change_data:

                //читаем имя
                String name = mNameOfFile.getText().toString();
                Intent intent = new Intent(this, ChangeTempActivity.class);
                //передаём позицию списка, на которой сделано нажатие: так как с тулбара, передаём 0
                intent.putExtra(ChangeTempActivity.POSITION, 0);
                //передаём имя файла
                intent.putExtra(ChangeTempActivity.NAME_OF_FILE, name);
                //передаём request_code
                intent.putExtra(ChangeTempActivity.CHANGE_REQUEST, request_code);
                startActivity(intent);
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
        String time = String.format("Время  %d:%02d.%d",minut,second,decim);
        // общее количество повторений в подходе
        String reps = String.format("Количество  %02d", totalReps);
        onShowTotalValues(time,reps);
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
                if ((mCurrentRep >= countReps) && (mCountFragment < SetLab.getSets().size() - 1)) {
                    mCountFragment++;
                    countMilliSecond = SetLab.getSet(mCountFragment).getTimeOfRep()*1000;;
                    countReps =  SetLab.getSet(mCountFragment).getReps();
                    Log.d(TAG, "countMilliSecond = " + countMilliSecond + "  countReps = " + countReps);
                    mTotalKvant = 0;
                    mCurrentRep = 0;
                }
                //если в последнем фрагменте - прекращаем выполнение
                if ((mCurrentRep >= countReps) && (mCountFragment >= SetLab.getSets().size() - 1)) {
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
    public  String saveDataAndFilename(ArrayList<String> dataSave,
                                     String nameFile, String fileNameDefoult, String typeData){
        String finishFileName = "";
        //добавляем в синглет списка файлов новое имя
        //получаем ссылку на экземпляр FileSaverLab
        FileSaverLab fileSaverLab = FileSaverLab.get();
        FileSaver fileSaver = new FileSaver();
        //получаем список данных о сохранённых файлов
        List<FileSaver> listOfFiles = fileSaverLab.getFileSavers();
        //если имя файла- пустая строка, то пишем отсечки в файл
        // с именем по умолчанию fileNameDefoult, а в синглет ничего не добавляем если
        // запись с именем fileNameDefoult уже была
        if (nameFile.isEmpty()) {
            finishFileName = fileNameDefoult;
            //пишем отсечки dataSave в файл с именем fileNameDefoult
            writeArrayList(dataSave, fileNameDefoult);
            //s = fileNameDefoult;
            //проверяем, не повторяется ли запись с именем fileNameDefoult
            boolean notFirst = false;
            for (FileSaver fs: listOfFiles){
                String name = fs.getTitle();
                if (name.equalsIgnoreCase(fileNameDefoult)){
                    notFirst = true;
                }
            }
            //если запись с таким именем уже есть, пропускаем запись имени файла (сам файл записали уже)
            if (notFirst){
                Log.d(TAG, "SingleFragmentActivity saveDataAndFilename  notFirst " +
                        "Пропускаем запись имени файла ");
            }else{
                //если записи с таким именем нет, пишем имя файла в файл
                //даём имя по умолчанию
                fileSaver.setTitle(fileNameDefoult);
                fileSaver.setTipe(typeData);  //даём тип файлу
                fileSaver.setDate(); //пишем текущую дату и время, их можно прочитать где-то
                //добавляем данные файла в список-хранитель данных
                fileSaverLab.addFileSaver(fileSaver,0);

                //получаем весь список имён файлов из FileSaverLab
                ArrayList<String> listNamesOfFiles = getListNamesOfFiles();
                //пишем список имён в файл имён
                writeArrayList(listNamesOfFiles,FILENAME_NAMES_OF_FILES);
                Log.d(TAG, "SingleFragmentActivity saveDataAndFilename " +
                        "В listNamesOfFiles строк =  " + listNamesOfFiles.size());
            }
            //если имя файла-  НЕ пустая строка, то пишем отсечки в файл
            // с именем nameFile, а в синглет  добавляем FileSaver с данными о файле
        } else {
            finishFileName = nameFile;
            //пишем отсечки dataSave в файл с именем nameFile
            writeArrayList(dataSave, nameFile);
            //s = nameFile;
            //даём имя, считанное из строки
            fileSaver.setTitle(nameFile);
            fileSaver.setTipe(typeData);  //даём тип файлу
            fileSaver.setDate(); //пишем текущую дату и время, их можно прочитать где-то
            //добавляем данные файла в список-хранитель данных
            fileSaverLab.addFileSaver(fileSaver);

            //получаем весь список имён файлов из FileSaverLab
            ArrayList<String> listNamesOfFiles = getListNamesOfFiles();
            //пишем список имён в файл имён
            writeArrayList(listNamesOfFiles,FILENAME_NAMES_OF_FILES);
            Log.d(TAG, "SingleFragmentActivity saveDataAndFilename " +
                  "В listNamesOfFiles строк =  " + listNamesOfFiles.size());
        }
        Log.d(TAG, "SingleFragmentActivity saveDataAndFilename В списке FileSaverLab файлов" +
                "  Количество файлов = " + fileSaverLab.getFileSavers().size());

        return finishFileName;
    }

    //получаем список имён файлов из FileSaverLab
    private ArrayList<String> getListNamesOfFiles(){
        ArrayList<String> listNamesOfFiles = new ArrayList<>();
        FileSaverLab fileSaverLab = FileSaverLab.get();
        //получаем список данных о сохранённых файлов
        List<FileSaver> fi = fileSaverLab.getFileSavers();
        //заводим список сохранённых файлов ArrayList и пишем в лог
        for (FileSaver set: fi){
            String name =set.getTitle();
            String date = set.getDate();
            String type = set.getTipe();
            String nameOfFile = String.format("%s:%s:%s",name,date,type);
            //Log.d(TAG, " number = "  + number);
            listNamesOfFiles.add(nameOfFile);
        }
        Log.d(TAG, " sizeOfList  = "  + listNamesOfFiles.size());
        return listNamesOfFiles;
    }

}
