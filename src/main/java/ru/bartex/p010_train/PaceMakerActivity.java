package ru.bartex.p010_train;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class PaceMakerActivity extends AppCompatActivity implements TextWatcher {

    SharedPreferences shp;

    final String SAVED_DELAY = "saved_delay";
    final String SAVED_TIME1 = "saved_time1";
    final String SAVED_TIME2 = "saved_time2";
    final String SAVED_TIME3 = "saved_time3";
    final String SAVED_TIME4 = "saved_time4";
    final String SAVED_TIME5 = "saved_time5";

    final String SAVED_REPS1 = "saved_reps1";
    final String SAVED_REPS2 = "saved_reps2";
    final String SAVED_REPS3 = "saved_reps3";
    final String SAVED_REPS4 = "saved_reps4";
    final String SAVED_REPS5 = "saved_reps5";

    private EditText mEditTextTime1;
    private EditText mEditTextTime2;
    private EditText mEditTextTime3;
    private EditText mEditTextTime4;
    private EditText mEditTextTime5;
    private EditText mEditTextDelay;

    private EditText mEditTextReps1;
    private EditText mEditTextReps2;
    private EditText mEditTextReps3;
    private EditText mEditTextReps4;
    private EditText mEditTextReps5;

    private EditText mMark1;
    private EditText mMark2;
    private EditText mMark3;
    private EditText mMark4;
    private EditText mMark5;

    private TextView mCurrentTime;
    private TextView mCurrentReps;
    private TextView mTimeLabel;
    private TextView mRepsLabel;
    private TextView mtextViewCountDown;

    private Button mButtonStart;
    private Button mButtonStop;
    private Button mButtonReset;

    private ProgressBar mProgressBarTime;
    //private ProgressBar mProgressBarTotal;



    private float countMilliSecond =1000; //колич миллисекунд, получаемое из mEditTextTime
    private float countMillisDelay =5000; //колич миллисекунд, получаемое из mEditTextDelay
    private float mTimeOfSet = 0;   //общее время выполнения подхода в секундах
    private int countReps = 2; //количество повторений,получаемое из текста mEditTextReps

    private long mKvant = 100;//время в мс между срабатываниями TimerTask
    private long mTotalKvant = 0;//текущее суммарное время для фрагмента подхода
    private long mTotalTime = 0; //текущее суммарное время для подхода
    private long mCurrentDelay = 0; //текущее время для задержки
    private int mCurrentRep = 0; //счётчик повторений
    private int mTotalReps = 0;  //общее количество повторений в подходе
    private int mCurrentTotalReps = 0; //суммарное текущее количество повторений
    private int mCountFragment = 0;  //номер фрагмента подхода

    private boolean workOn = false;

    //FragmentOfSet[i] - это и mTimeOfRep и mReps класса FragmentOfSet
    //заполним массив такими вот значениями - они отличны от тех, что указаны в строковых ресурсах
    private FragmentOfSet[] mFragmentOfSet = new FragmentOfSet[]{
            new FragmentOfSet((float)1.0, 3),
            new FragmentOfSet((float)2.0, 2),
            new FragmentOfSet((float)2.5, 2),
            new FragmentOfSet((float)3.0, 1),
            new FragmentOfSet((float)3.5, 1)
    };
    //Определим время задержки как 5 сек
    private TimeOfDelayPaceMaker mTimeOfDelay = new TimeOfDelayPaceMaker(5);

    // объявим массив фрагментов времени подхода - заполним в onCreate после findViewById
    private EditText[] mEditTextTimeArray;
    // объявим массив фрагментов кол повторений в подходе - заполним в onCreate после findViewById
    private EditText[] mEditTextRepsArray;
    // объявим массив маркеров цвета - заполним в onCreate после findViewById
    private EditText[] mMarkArray;

    private Timer mTimer;
    private TimerTask mTimerTask;
    private ToneGenerator mToneGenerator;

    public static final String TAG ="33333";
    private SharedPreferences prefSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pace_maker);

        Log.d(TAG,"PaceMakerActivity onCreate");

        ActionBar acBar = getSupportActionBar();
        acBar.setTitle("Шаблон 5к");
        //показать стрелку Назад
        acBar.setDisplayHomeAsUpEnabled(true );
        acBar.setHomeButtonEnabled(true);
        //или заголовок в ActionBar устанавливается так
        //getSupportActionBar().setTitle("Секундомер");

        //установить портретную ориентацию экрана
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mButtonStart = (Button) findViewById(R.id.start_button);
        mButtonStop = (Button) findViewById(R.id.stop_button);
        mButtonReset = (Button) findViewById(R.id.reset_button);

        mEditTextTime1 = (EditText)findViewById(R.id.time_item1_editText);
        mEditTextTime2 = (EditText)findViewById(R.id.time_item2_editText);
        mEditTextTime3 = (EditText)findViewById(R.id.time_item3_editText);
        mEditTextTime4 = (EditText)findViewById(R.id.time_item4_editText);
        mEditTextTime5 = (EditText)findViewById(R.id.time_item5_editText);
        mEditTextDelay = (EditText)findViewById(R.id.editTextDelay);

        mEditTextReps1 = (EditText)findViewById(R.id.reps_item1_editText);
        mEditTextReps2 = (EditText)findViewById(R.id.reps_item2_editText);
        mEditTextReps3 = (EditText)findViewById(R.id.reps_item3_editText);
        mEditTextReps4 = (EditText)findViewById(R.id.reps_item4_editText);
        mEditTextReps5 = (EditText)findViewById(R.id.reps_item5_editText);

        mMark1 = (EditText)findViewById(R.id.mark1);
        mMark2 = (EditText)findViewById(R.id.mark2);
        mMark3 = (EditText)findViewById(R.id.mark3);
        mMark4 = (EditText)findViewById(R.id.mark4);
        mMark5 = (EditText)findViewById(R.id.mark5);

        mCurrentTime = (TextView)findViewById(R.id.current_time);
        mCurrentReps = (TextView)findViewById(R.id.current_reps);
        mTimeLabel = (TextView)findViewById(R.id.time_item1_label);
        mRepsLabel = (TextView)findViewById(R.id.reps_item1_label);
        mtextViewCountDown = (TextView)findViewById(R.id.textViewCountDown);

        mProgressBarTime = (ProgressBar)findViewById(R.id.progressBarTime);
        //mProgressBarTotal = (ProgressBar)findViewById(R.id.progressBarTotal);

        //массив EditText для отображения времени между повторами во фрагменте
        mEditTextTimeArray = new EditText[] {mEditTextTime1,mEditTextTime2,
                mEditTextTime3,mEditTextTime4,mEditTextTime5};
        //массив EditText для отображения количества повторов фрагмента подхода
        mEditTextRepsArray = new EditText[]{mEditTextReps1,mEditTextReps2,
                mEditTextReps3,mEditTextReps4,mEditTextReps5};
        //массив EditText для отображения продвижения по фрагментам
        mMarkArray = new EditText[]{mMark1,mMark2,mMark3,mMark4,mMark5};

        for (int i = 0; i < mEditTextTimeArray.length; i++){
            // Устанавливаем начальные значения в полях "время" и "количество"
            //берём Float значение mTimeOfRep из i фрагмента класса FragmentOfSet,
            // переводим его в строку и устанавливаем как текст в i строке фрагментов времени
            String s11 = Float.toString(mFragmentOfSet[i].getTimeOfRep());
            String s22 = Integer.toString(mFragmentOfSet[i].getReps());
            mEditTextTimeArray[i].setText(s11);
            mEditTextRepsArray[i].setText(s22);
            Log.d(TAG, "mFragmentOfSet ==" + i +"==  "+ s11 + "  " + s22);

            //назначаем MainActivity слушателем для EditText времени и количества повторов
            mEditTextTimeArray[i].addTextChangedListener(this);
            mEditTextRepsArray[i].addTextChangedListener(this);
        }

        //Устанавливаем значение задержки в поле EditText
        String sDelay = Integer.toString (mTimeOfDelay.getTimeDelay());
        mEditTextDelay.setText(sDelay);
        Log.d(TAG, "mTimeOfDelay-before ==" + mEditTextDelay.getText().toString());

        //назначаем MainActivity слушателем для EditText задержки
        mEditTextDelay.addTextChangedListener(this);

        //выставляем доступность кнопок
        buttonsEnable (true,false,false);

        //вычисляем и показываем общее время выполнения подхода и количество повторов в подходе
        calculateAndShowTotalValues();

        //--------------------------------START---------------------------//
        //назначаем слушатель кнопке Start
        mButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //устанавливаем фокус на поле ввода и делаем курсор невидимым до нажатия Стоп
                mEditTextDelay.requestFocus();
                mEditTextDelay.setCursorVisible(false);

                if (mTimer!=null)mTimer.cancel();
                mTimer =new Timer();
                mTimerTask = new MyTimerTask();
                mToneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC,100);

                //Выполняем начальные установки параметров, которые могли измениться
                //для  mCurrentRep в макете стоит запрет на выключение экрана android:keepScreenOn="true"
                mCurrentRep = 0;  //устанавливаем счётчик повторений во фрагменте в 0
                mCurrentTotalReps = 0; //устанавливаем счётчик выполненных в подходе повторений в 0
                mCurrentDelay = 0;  //устанавливаем текущее время задержки в 0
                mTotalKvant = 0;  //устанавливаем текущее время между повторениями в 0
                mTotalTime = 0; //обнуляем счётчик суммарного времени
                mCountFragment = 0; //обнуляем счётчик фрагментов подхода
                mProgressBarTime.setProgress(100);
                //mProgressBarTotal.setProgress(100);

                //устанавливаем белый цвет фона маркера
                changeBackColor(mMarkArray,255,255,255,0);

                //Получаем время задержки
                countMillisDelay = getСountDelay(mEditTextDelay);

                //получаем время между повторениями в мс из текста
                countMilliSecond = getСountMilliSecond(mEditTextTimeArray[mCountFragment]);

                //получаем количество повторений из текста
                countReps = getСountReps(mEditTextRepsArray[mCountFragment]);

                //посчитаем общее врямя выполнения подхода в секундах
                mTimeOfSet = countSetTime(mEditTextTimeArray,mEditTextRepsArray);

                //посчитаем общее количество повторений в подходе
                mTotalReps = countTotalReps(mEditTextRepsArray);

                //покажем общее время подхода и общее число повторений в подходе
                showTotalValues();
                //Покажем таймер задержки
                mtextViewCountDown.setText(mEditTextDelay.getText().toString());

                //Выставляем флаг "работа"
                workOn = false;

                //запускаем TimerTask на выполнение с периодом mKvant
                mTimer.scheduleAtFixedRate(mTimerTask,mKvant,mKvant);

                //играем мелодию начала подхода  с задержкой
                //mToneGenerator.startTone(ToneGenerator.TONE_SUP_PIP,400);
                mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 200);
                // WaveGeneratorStackOverflow.playTwoSound(1000,100,0,0,0);

                //выставляем доступность кнопок
                buttonsEnable (false,true,false);

                Log.d(TAG, "mTimeOfSet = " + mTimeOfSet);
                Log.d(TAG, "mTotalReps = " + mTotalReps);
                Log.d(TAG, "countMilliSecond = " + countMilliSecond +"  countReps = " + countReps);
            }
        });

        //--------------------------------STOP---------------------------//
        //назначаем слушатель кнопке Stop
        mButtonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //устанавливаем курсор и делаем его видимым для редактирования
                mEditTextDelay.requestFocus();
                mEditTextDelay.setCursorVisible(true);

                if (mTimer!=null)mTimer.cancel();
                //выставляем доступность кнопок
                buttonsEnable (true,false,true);
                mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 500);
                //WaveGeneratorStackOverflow.playTwoSound(1000,100,50,1000,300);
            }
        });
        //--------------------------------RESET---------------------------//
        //назначаем слушатель кнопке Reset
        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                //восствнавливаем белый цвет фона маркера
                changeBackColor(mMarkArray,255,255,255,0);
                mtextViewCountDown.setText("");
                workOn = false;
            }
        });

        shp = getPreferences(MODE_PRIVATE);
        if (!((shp.getString(SAVED_TIME1,"").equals("") && shp.getString(SAVED_REPS1,"").equals("")))) {
            loadText();
            Log.d(TAG, "Load_Saved");
        }else Log.d(TAG, "Load_Default");
        Log.d(TAG, "mTimeOfDelay-after ==" + mEditTextDelay.getText().toString());
    }
    //================= Конец метода onCreate ===========================

    //реализация методов обработки события изменения текста в EditText
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
    @Override
    public void afterTextChanged(Editable s) {
        calculateAndShowTotalValues();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"PaceMakerActivity onPause");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"PaceMakerActivity onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"PaceMakerActivity onResume");
        //получаем настройки из активности настроек
        prefSetting = PreferenceManager.getDefaultSharedPreferences(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pacemaker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            //чтобы работала стрелка Назад, а не происходил крах приложения
            case android.R.id.home:
                Log.d(TAG, "Домой");
                onBackPressed();
                return true;

            case R.id.action_settings:
                Log.d(TAG, "OptionsItem = action_settings");
                Intent intentSettings = new Intent(this, PrefActivity.class);
                startActivity(intentSettings);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //====================================ФУНКЦИИ========================================//
    //считаем общее время подхода
    private float countSetTime(EditText[] timeArray,EditText[] repsArray){

        float timeOfSet=0;
        for (int i = 0; i < timeArray.length; i++){
            timeOfSet +=  getСountMilliSecond (timeArray[i])
                    /1000*getСountReps(repsArray[i]);
        }
        return timeOfSet;
    }
    //считаем общее количество повторений в подходе
    private int countTotalReps(EditText[] repsArray){

        int totalReps = 0;
        for (int i = 0; i < repsArray.length; i++) {
            String s = repsArray[i].getText().toString();
            if (s.equals("")){s = "0";}
            totalReps += Integer.parseInt(s);
        }
        return totalReps;
    }
    //перевоим текст в миллисекунды для времени между повторениями одного фрагмента
    private float getСountMilliSecond(EditText time){

        if (time.getText().toString().equals("")){
            //Log.d(TAG, "countMilliSecond = 0");
            return 0;
        } else return 1000 * Float.parseFloat(time.getText().toString());
    }
    //переводим текст в цифру для количества повторений одного фрагмента
    private int getСountReps(EditText reps){

        if (reps.getText().toString().equals("")){
            //Log.d(TAG, "countReps = 0");
            return 0;
        }else return Integer.parseInt(reps.getText().toString());
    }

    private int getСountDelay(EditText delay){

        if (delay.getText().toString().equals("")){
            //Log.d(TAG, "countDelay = 0");
            return 0;
        }else return 1000*Integer.parseInt(delay.getText().toString());
    }

    //покажем общее время подхода и общее число повторений в подходе
    private void showTotalValues(){

        float millisTime = mTimeOfSet*1000;
        //покажем суммарное время подхода
        int minut = ((int)millisTime/60000)%60;
        int second = ((int)millisTime/1000)%60;
        int decim = (int)((millisTime%1000)/mKvant);
        String time = String.format("Время  %d:%02d.%d",minut,second,decim);
        mTimeLabel.setText(time);
        //покажем общее количество повторений в подходе
        String reps = String.format("Количество  %02d", mTotalReps);
        mRepsLabel.setText(reps);

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
                String time = String.format("%d:%02d.%d",minut,second,decim);
                //показ текущ времени
                mCurrentTime.setText(time);

                //показ текущ количества повторений
                mCurrentReps.setText(Integer.toString(mCurrentTotalReps));

                //показываем текущую задержку
                float f =(countMillisDelay - mCurrentDelay)/1000;
                if (f>=0) {
                    mtextViewCountDown.setText(Float.toString((f)));
                }else mtextViewCountDown.setText("");

                //меняем цвет фона для  фрагментов, предварительно сбросив всё в белый
                changeBackColor(mMarkArray,255,255,255,0);
                changeBackColor(mMarkArray,255,211,108,mCountFragment);

                //Показываем прогресс текущего времени фрагмента подхода
                if (countMilliSecond == 0){
                    mProgressBarTime.setProgress(100);
                }else {
                    String s = Float.toString(((float) mTotalKvant) * 100 / countMilliSecond);
                    float i = Float.parseFloat(s);
                    //прогресс текущ времени
                    mProgressBarTime.setProgress(100 - (int) i);
                }

                /*
                if(mTotalReps == 0){
                    mProgressBarTotal.setProgress(100);
                }else {
                    //прогресс общего  колич
                    mProgressBarTotal.setProgress(100 - 100/mTotalReps*mCurrentTotalReps);
                }
                */
            }
        });
    }

    private void buttonsEnable(boolean start,boolean stop,boolean reset){
        mButtonStart.setEnabled(start);
        mButtonStop.setEnabled(stop);
        mButtonReset.setEnabled(reset);
    }

    private void changeBackColor(EditText[] markArray,int r,int g,int b,int iter){
        for (int i = iter; i < markArray.length; i++)
            markArray[i].setBackgroundColor(Color.rgb(r, g, b));
    }

    private void calculateAndShowTotalValues(){
        //посчитаем общее время выполнения подхода в секундах
        mTimeOfSet = countSetTime(mEditTextTimeArray,mEditTextRepsArray);
        //посчитаем общее количество повторений в подходе
        mTotalReps = countTotalReps(mEditTextRepsArray);
        //покажем общее время подхода и общее число повторений в подходе
        showTotalValues();
    }

    void  saveText(){
        shp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor edit = shp.edit();
        edit.putString(SAVED_DELAY, mEditTextDelay.getText().toString());
        edit.apply();

        edit.putString(SAVED_TIME1, mEditTextTime1.getText().toString());
        edit.apply();
        edit.putString(SAVED_TIME2, mEditTextTime2.getText().toString());
        edit.apply();
        edit.putString(SAVED_TIME3, mEditTextTime3.getText().toString());
        edit.apply();
        edit.putString(SAVED_TIME4, mEditTextTime4.getText().toString());
        edit.apply();
        edit.putString(SAVED_TIME5, mEditTextTime5.getText().toString());
        edit.apply();

        edit.putString(SAVED_REPS1, mEditTextReps1.getText().toString());
        edit.apply();
        edit.putString(SAVED_REPS2, mEditTextReps2.getText().toString());
        edit.apply();
        edit.putString(SAVED_REPS3, mEditTextReps3.getText().toString());
        edit.apply();
        edit.putString(SAVED_REPS4, mEditTextReps4.getText().toString());
        edit.apply();
        edit.putString(SAVED_REPS5, mEditTextReps5.getText().toString());
        edit.apply();
/*
        for (int i = 0; i<5; i++){
            edit.putString(SAVED_TIME[i], mEditTextTimeArray[i].getText().toString());
            edit.commit();
            edit.putString(SAVED_REPS[i], mEditTextRepsArray[i].getText().toString());
            edit.commit();
            Log.d(TAG, "mEditTextTimeArray-Save " + i + " = " + mEditTextTimeArray[i].getText().toString()
            + "   mEditTextRepsArray-Save " + i + " = " + mEditTextRepsArray[i].getText().toString());
        }
        */
        Toast.makeText(this, "Сохранение данных", Toast.LENGTH_SHORT).show();
    }

    void loadText(){
        shp = getPreferences(MODE_PRIVATE);
        String sDelay = shp.getString(SAVED_DELAY,"");
        mEditTextDelay.setText(sDelay);

        mEditTextTime1.setText(shp.getString(SAVED_TIME1,""));
        mEditTextTime2.setText(shp.getString(SAVED_TIME2,""));
        mEditTextTime3.setText(shp.getString(SAVED_TIME3,""));
        mEditTextTime4.setText(shp.getString(SAVED_TIME4,""));
        mEditTextTime5.setText(shp.getString(SAVED_TIME5,""));

        mEditTextReps1.setText(shp.getString(SAVED_REPS1,""));
        mEditTextReps2.setText(shp.getString(SAVED_REPS2,""));
        mEditTextReps3.setText(shp.getString(SAVED_REPS3,""));
        mEditTextReps4.setText(shp.getString(SAVED_REPS4,""));
        mEditTextReps5.setText(shp.getString(SAVED_REPS5,""));

/*
        for (int i = 0; i<5; i++){
        Log.d(TAG, "mEditTextTimeArray-Load " + i + " = " +shp.getString(SAVED_TIME[i],"")
            + "   mEditTextRepsArray-Load " + i + " = " + shp.getString(SAVED_REPS[i],""));
            String sTime = shp.getString(SAVED_TIME[i],"");
            String sReps = shp.getString(SAVED_REPS[i],"");
            mEditTextTimeArray[i].setText(sTime);
            mEditTextRepsArray[i].setText(sReps);
        }
*/
        Toast.makeText(this, "Загрузка данных", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "PaceMakerActivity onDestroy");
        //Сохраняем текст
        saveText();
        if (mTimer != null) mTimer.cancel();

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
                //WaveGeneratorStackOverflow.playTwoSound(1000,100,50,1000,300);
                mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 150);
                SystemClock.sleep(250);
                mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 300);
                //SystemClock.sleep(400);     //задержка
                //mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 200);
            }
            if (mCurrentDelay>=countMillisDelay){
                workOn = true;
                //Log.d(TAG, "workOn = "  + workOn);
            }

            //TONE_SUP_PIP
            //TONE_SUP_CONFIRM
            //TONE_CDMA_INTERCEPT
            //TONE_CDMA_DIAL_TONE_LITE

            if (workOn) {

                mTotalKvant += mKvant;  // добавляем 100мс пока не будет больше времени между повторами
                mTotalTime += mKvant;  //добавляем 100мс к текущему времени подхода
                if (mTotalKvant >= countMilliSecond) {
                    mCurrentRep++; // если стало больше, переходим к следующему повтору
                    Log.d(TAG, "mCurrentRep = " + mCurrentRep);
                    mCurrentTotalReps++; //считаем количество выполненных повторений
                    mTotalKvant = 0;  //при этом обнуляя текущее время между повторами
                    mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 200);
                    //WaveGeneratorStackOverflow.playTwoSound(1000,100,0,0,0);
                }
                if ((mCurrentRep >= countReps) && (mCountFragment < mEditTextTimeArray.length - 1)) {
                    mCountFragment++;
                    countMilliSecond = getСountMilliSecond(mEditTextTimeArray[mCountFragment]);
                    countReps = getСountReps(mEditTextRepsArray[mCountFragment]);
                    Log.d(TAG, "countMilliSecond = " + countMilliSecond + "  countReps = " + countReps);
                    mTotalKvant = 0;
                    mCurrentRep = 0;
                }
                //если в последнем фрагменте - прекращаем выполнение
                if ((mCurrentRep >= countReps) && (mCountFragment >= mEditTextTimeArray.length - 1)) {
                    if (mTimer != null) mTimer.cancel();
                    Log.d(TAG, "mTimer.cancel");
                    //WaveGeneratorStackOverflow.playTwoSound(1000,100,50,1000,400);
                    //.startTone(ToneGenerator.TONE_SUP_PIP, 600);
                    mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 500);
                    //выставляем начальные значения - для повторного старта
                    mProgressBarTime.setProgress(0);
                    //mProgressBarTotal.setProgress(0);
                    mCurrentRep = 0;
                    mTotalKvant = 0;
                    mTotalReps = 0;
                    mCountFragment = 0;
                    //изменяем свойство кнопок и сбрасываем цвет в белый в пользовательском потоке
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            changeBackColor(mMarkArray, 255, 255, 255,0);
                            buttonsEnable(true, false, true);
                        }
                    });
                }

            }
        }
    }

}
