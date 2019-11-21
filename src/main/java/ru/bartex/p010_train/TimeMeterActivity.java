package ru.bartex.p010_train;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.preference.PreferenceManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.fragment.app.DialogFragment;
import ru.bartex.p010_train.ru.bartex.p010_train.data.P;
import ru.bartex.p010_train.ru.bartex.p010_train.data.TempDBHelper;

public class TimeMeterActivity extends AppCompatActivity
        implements DialogSaveSecFragment.SaverFragmentSecundomerListener {

    public static final String TAG ="33333";

    private Button mButtonStart;
    private Button mButtonStop;
    private Button mButtonReset;
    private Button mButtonNext;
    private TextView mCurrentTime;
    private TextView mCurrentTimePause;
    private ListView mListViewRep;

    private Timer mTimer;
    private TimerTask mTimerTask;
    private ToneGenerator mToneGenerator;
    private final long mKvant = 100;//время в мс между срабатываниями TimerTask
    private long mTotalTime = 0;//текущее суммарное время для фрагмента подхода
    private long mTimeLast = 0;   // время на предыдущей отсечке
    private long mTimeStart = 0;  //время в момент нажатия на Старт
    private long mTimeExersize = 0;  //время от старта
    private long mTimeNext = 0;   //время на последующей отсечке
    private long mDelta = 0;   //время между соседними отсечками
    private long mTimeStop = 0; //время в момент нажатия на стоп
    private long mTimeRestart = 0; //время в момент нажатия на Продолжить (Рестарт)
    private long mDeltaDelaySummary = 0; //Суммарное время приостановки секундомера
    private long mDeltaDelayCurrent = 0; //Текущее время приостановки секундомера
    private long mPauseTime = 0;  //Текущее время в паузе секундомера
    private boolean mRestart = false; //признак повторного старта (true)
    private boolean mIsStop  = false;  //признак нажатия на Стоп (Пауза)
    private boolean mIsStopped = false; //признак нахождения в режиме паузы
    private boolean start = false;//признак нажатия на старт
    private int ii = 0; //порядковый номер отсечки

    //Временный список отсечек по кругам для записи в базу, если будет нужно (в диалоге сохранения)
    private ArrayList<String> repTimeList = new ArrayList<>();
    //список данных для показа на экране
    private ArrayList<Map<String, Object>> data = new ArrayList<>();
    private SimpleAdapter sara;

    private final String ATTR_ITEM = "ru.bartex.p008_complex_imit_real.item";
    private final String ATTR_TIME = "ru.bartex.p008_complex_imit_real.time";
    private final String ATTR_DELTA = "ru.bartex.p008_complex_imit_real.delta";

    private int accurancy; //точность отсечек - количество знаков после запятой - от MainActivity
    private int pause;  //режим паузы 1-остановка времени 2- остановка индикации
    private boolean sound = true; // включение / выключение звука
    private SharedPreferences prefSetting;// предпочтения из PrefActivity
    private SharedPreferences prefNameOfLastFile;// предпочтения - имя последнего сохранённого файла

    private TempDBHelper mTempDBHelper = new TempDBHelper(this);
    private String finishNameFile;//имя файла - или из метода интерфейса или по умолчанию

    // Метод интерфейса, в котором передаём имя,тип сохранения данных (показать или нет)
    //и пометить как избранные
    @Override
    public void onNameAndGrafTransmit(String nameFile, boolean showGraf, boolean cancel) {

        //showGraf -флаг : показывать график отсечек или нет
        //cancel - флаг : диалог отменён пользователем? true - да

        if (cancel){
            //если диалог был отменён, просто стираем список с отсечками
            repTimeList.clear();

            //в противном случае
        }else{
            //получаем дату и время в нужном для базы данных формате
            String dateFormat  = mTempDBHelper.getDateString();
            String timeFormat  = mTempDBHelper.getTimeString();
            //если строка имени пустая
            if (nameFile.isEmpty()) {
                //имя будет "Автосохранение секундомера"
                finishNameFile = P.FILENAME_OTSECHKI_SEC;

            Log.d(TAG, "onNameAndGrafTransmit nameFile = " + nameFile.isEmpty()+
                    "  finishNameFile = " + finishNameFile);

            //проверяем, есть ли в базе запись с таким именем FILENAME_OTSECHKI_SEC
            long repeatId = mTempDBHelper.getIdFromFileName (finishNameFile);
            Log.d(TAG,"onNameAndGrafTransmit repeatId = " + repeatId);
            //если есть (repeatId не равно -1), стираем её и потом пишем новые данные под таким именем
            if (repeatId != -1){mTempDBHelper.deleteFileAndSets(repeatId);}

            }else {
                //проверяем, есть ли в базе запись с именем nameFile< чтобы избежать дублирования
                long checkRepeatId = mTempDBHelper.getIdFromFileName (nameFile);
                Log.d(TAG,"onNameAndGrafTransmit checkRepeatId = " + checkRepeatId);
                //если есть (repeatId не равно -1), добавляем к имени +
                if (checkRepeatId != -1){
                    finishNameFile = nameFile + R.string.LowMinus + mTempDBHelper.getTimeString() ;
                }else {finishNameFile = nameFile;}
            }
            Log.d(TAG, " После finishNameFile = " + finishNameFile);
            //======Начало добавления записей в таблицы DataFile и DataSet=========//
            //если имя файла не пустое или "Автосохранение секундомера"
            //создаём экземпляр класса DataFile в конструкторе
            DataFile file1 = new DataFile(finishNameFile, dateFormat, timeFormat,
                    null,null,P.TYPE_TIMEMETER, 6);
            //добавляем запись в таблицу TabFile, используя данные DataFile
            long file1_id =  mTempDBHelper.addFile(file1);

            //готовим данные фрагментов подхода
            // если индекс =0, то первое значение
            for (int j = 0; j<repTimeList.size(); j++ ) {
                float time_now;
                //получаем время в секундах между измерениями
                //если первое значение, то так
                if (j == 0){
                    time_now = (float) (Long.parseLong(repTimeList.get(j)) )/1000;
                    //если не первое значение, то как разницу
                }else{
                    time_now = (float) (Long.parseLong(repTimeList.get(j)) -
                            Long.parseLong(repTimeList.get(j - 1)))/1000;
                }
                //создаём экземпляр класса DataSet в конструкторе
                DataSet set = new DataSet(time_now,1,j+1);
                //добавляем запись в таблицу TabSet, используя данные DataSet
                mTempDBHelper.addSet(set, file1_id);
                //======Окончание добавления записей в таблицы DataFile и DataSet=========//
            }
            // Cохраняем имя файла в предпочтениях (ИСПОЛЬЗУЕМ  при переходе в график с тулбара)
            //получаем файл предпочтений
            prefNameOfLastFile = getPreferences(MODE_PRIVATE);
            // получаем Editor
            SharedPreferences.Editor ed = prefNameOfLastFile.edit();
            //пишем имя последнего сохранённого файла в предпочтения
            ed.putString(P.LAST_FILE, finishNameFile);
            ed.apply();

            //если нужно записать и показать
            //вызываем интент, в котором передаём имя последнего файла в TimeGrafActivity
            if (showGraf){
                //открываем экран с графиком только что записанных данных
                Intent intentTiming = new Intent(TimeMeterActivity.this, TimeGrafActivity.class);
                intentTiming.putExtra(P.FINISH_FILE_NAME, finishNameFile);
                startActivity(intentTiming);
            }
            //стираем список с отсечками
            repTimeList.clear();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_meter);

        ActionBar acBar = getSupportActionBar();
        acBar.setTitle(R.string.Timemeter);
        //показать стрелку Назад
        acBar.setDisplayHomeAsUpEnabled(true );
        acBar.setHomeButtonEnabled(true);
        //или заголовок в ActionBar устанавливается так
        //getSupportActionBar().setTitle("Секундомер");

        //установить портретную ориентацию экрана
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mCurrentTime = (TextView) findViewById(R.id.textViewTime);
        mCurrentTimePause = (TextView) findViewById(R.id.textViewTimePause);
        //для  mListViewRep в макете стоит запрет на выключение экрана android:keepScreenOn="true"
        mListViewRep = (ListView) findViewById(R.id.listViewRep);
        mButtonStart = (Button) findViewById(R.id.buttonStart);
        mButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //признак нахождения в режиме паузы
                mIsStopped = false;

                if (!mRestart){
                    mTimeStart = System.currentTimeMillis();
                    mTimeLast = mTimeStart;
                }else {
                    mTimeRestart = System.currentTimeMillis();
                    mDeltaDelaySummary += mTimeRestart - mTimeStop;
                    if (mIsStop){
                        mDeltaDelayCurrent += mTimeRestart - mTimeStop;
                    }
                    Log.d(TAG, " mDeltaDelaySummary = " + mDeltaDelaySummary +
                            "  mDeltaDelayCurrent = " + mDeltaDelayCurrent);
                }

                if (mTimer!=null)mTimer.cancel();
                mTimer =new Timer();
                mTimerTask = new MyTimerTask();
                mToneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC,100);
                //запускаем TimerTask на выполнение с периодом mKvant
                mTimer.scheduleAtFixedRate(mTimerTask,mKvant,mKvant);
                //играем мелодию начала подхода  с задержкой
                mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 50);

                mButtonStop.setEnabled(true);
                mButtonStop.setVisibility(View.VISIBLE);
                mButtonReset.setEnabled(false);
                mButtonReset.setVisibility(View.GONE);
                mButtonStart.setEnabled(false);
                mButtonStart.setVisibility(View.GONE);
                mButtonNext.setEnabled(true);
                mButtonNext.setVisibility(View.VISIBLE);
                //выставляем признак рестарта
                mRestart = true;
                //выставляем флаг нажатия на Старт = да
                start = true;
                //вызываем onPrepareOptionsMenu для создания недоступномсти значков ToolBar
                invalidateOptionsMenu();

            }
        });

        //кнопка "Круг"
        mButtonNext = (Button) findViewById(R.id.buttonNext);
        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 50);

                mTimeNext = System.currentTimeMillis();
                switch (pause){
                    case 1:
                        mTimeExersize = mTimeNext-mTimeStart - mDeltaDelaySummary;
                        mDelta = mTimeNext - mTimeLast - mDeltaDelayCurrent;
                        break;
                    case 2:
                        mTimeExersize = mTimeNext-mTimeStart;
                        mDelta = mTimeNext - mTimeLast;
                        break;
                }


                Log.d(TAG, " mTimeExersize = " + mTimeExersize +
                        "  mDelta = " + mDelta +
                        " mDeltaDelaySummary = " + mDeltaDelaySummary +
                        "  mDeltaDelayCurrent = " + mDeltaDelayCurrent);

                String s_item = Integer.toString(ii+1);
                String s_time;
                String s_delta;

                switch (accurancy){
                    case 1:
                        s_time = P.getTimeString1(mTimeExersize);
                        s_delta = P.getTimeString1 (mDelta);
                        break;
                    case 2:
                        s_time = P.getTimeString2(mTimeExersize);
                        s_delta = P.getTimeString2 (mDelta);
                        break;
                    case 3:
                        s_time = P.getTimeString3(mTimeExersize);
                        s_delta = P.getTimeString3 (mDelta);
                        break;
                    default:
                        s_time = P.getTimeString1(mTimeExersize);
                        s_delta = P.getTimeString1 (mDelta);
                }
                //заводим данные для одной строки списка
                Map<String, Object> m;
                m = new HashMap<>();
                m.put(ATTR_ITEM, s_item);
                m.put(ATTR_TIME, s_time);
                m.put(ATTR_DELTA, s_delta);
                //добавляем данные для вывода на экран в начало ArrayList
                data.add(0,m);
                //добавляем время отсечки в список (в конец) для записи в базу в диалоге сохранения
                repTimeList.add(Long.toString(mTimeExersize));

                //обновляем список на экране
                sara.notifyDataSetChanged();
                //увеличиваем порядковый номер отсечки
                ii++;
                //обновляем время на предыдущей отсечке
                mTimeLast = mTimeNext;
                //признак нажатия стоп
                mIsStop = false;
                //обнуление текущей приостановки секундомера (между отсечками ее не было)
                mDeltaDelayCurrent = 0;
            }
        });

        //Кнопка "Пауза"
        mButtonStop = (Button) findViewById(R.id.buttonStop);
        mButtonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //останавливаем часы и подаём сигнал
                if (mTimer!=null)mTimer.cancel();
                //засекаем время в момент приостановки секундомера
                mTimeStop = System.currentTimeMillis();
                //подаём звуковой сигнал
                mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 100);

                mButtonStart.setEnabled(true);
                mButtonStart.setText(R.string.continue_task);
                mButtonStart.setVisibility(View.VISIBLE);
                mButtonReset.setEnabled(true);
                mButtonReset.setVisibility(View.VISIBLE);
                mButtonStop.setEnabled(false);
                mButtonStop.setVisibility(View.GONE);
                mButtonNext.setEnabled(false);
                mButtonNext.setVisibility(View.GONE);

                //признак нажатия стоп
                mIsStop = true;
                //признак нахождения в режиме паузы
                mIsStopped = true;
            }
        });

        mButtonReset = (Button) findViewById(R.id.buttonReset);
        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //если число отсечек не меньше 1, вызываем диалог записи в файл
                if (ii>0) {
                    //диалог сохранения данных в файл с именем, указанным пользователем и типом сохранения
                    openSaveInFileDialogFragment();
                }else {
                    // стираем repTimeList
                    repTimeList.clear();
                }
                Log.d(TAG,"mButtonReset count = " + ii);

                //Останавливаем часы и обнуляем их, подаём звуковой сигнал
                if (mTimer!=null)mTimer.cancel();
                mTotalTime = 0;
                mCurrentTime.setText(R.string.CountZeroTime);
                mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 300);

                ii = 0;
                mTimeLast = 0;   // время на предыдущей отсечке
                mTimeStart = 0;  //время в момент нажатия на Старт
                mTimeNext = 0;   //время на последующей отсечке
                mDelta = 0;   //время между соседними отсечками
                mDeltaDelaySummary = 0; //суммарное время приостановки секундомера
                mDeltaDelayCurrent = 0; //Текущее время приостановки секундомера
                mRestart = false; //признак повторного, а не первого старта
                mIsStop = false; //признак нажатия кнопки стоп для отсчёта приостановки времени


                data.clear();
                sara.notifyDataSetChanged();

                mButtonReset.setEnabled(false);
                mButtonReset.setVisibility(View.GONE);
                mButtonStart.setEnabled(true);
                mButtonStart.setText(R.string.start_button );
                mButtonStart.setVisibility(View.VISIBLE);
                mButtonStop.setEnabled(false);
                mButtonStop.setVisibility(View.GONE);
                mButtonNext.setEnabled(false);
                mButtonNext.setVisibility(View.GONE);

                //выставляем флаг нажатия на Старт = нет
                start = false;
                //вызываем onPrepareOptionsMenu для создания недоступномсти значков ToolBar
                invalidateOptionsMenu();

            }
        });

        //состояние кнопок при запуске программы
        mButtonStart.setEnabled(true);
        mButtonStart.setVisibility(View.VISIBLE);
        mButtonStop.setEnabled(false);
        mButtonStop.setVisibility(View.GONE);
        mButtonReset.setEnabled(false);
        mButtonReset.setVisibility(View.GONE);
        mButtonNext.setEnabled(false);
        mButtonNext.setVisibility(View.GONE);

        Map<String, Object> m = new HashMap<>();
        data = new ArrayList<Map<String, Object>>();
        //не нужно добавлять data, иначе будет пустая строка
        //data.add(m);
        //Делаем массивы откуда-куда
        String[] from = {ATTR_ITEM, ATTR_TIME, ATTR_DELTA};
        int[] to = {R.id.item_list, R.id.time_list, R.id.delta_list};
        sara = new SimpleAdapter(this, data, R.layout.list_rep, from, to);
        mListViewRep.setAdapter(sara);

        //получаем настройки из активности настроек
        prefSetting = PreferenceManager.getDefaultSharedPreferences(this);
        //получаем из файла настроек количество знаков после запятой
        accurancy = Integer.parseInt(prefSetting.getString("accurancy", "1"));
        Log.d(TAG,"TimeMeterActivity accurancy = " + accurancy);
    }
    //======================конец OnCreate=================================//

    //======================class MyTimerTask=================================//
    public class MyTimerTask extends TimerTask{
        @Override
        public void run() {  //запускаем MyTimerTask в методе run()
            //Рассчитываем общее время в зависимости от режима паузы секундомера
            switch (pause){
                case 1:
                    //Остановка времени
                    //Общее время
                    mTotalTime = System.currentTimeMillis() - mTimeStart - mDeltaDelaySummary;
                    // если в режиме паузы, показать текущее время паузы
                    if (mIsStopped){
                        mPauseTime =  System.currentTimeMillis() - mTimeStop;
                    }
                    break;
                case 2:
                    //Остановка индикации времени
                    mTotalTime = System.currentTimeMillis() - mTimeStart;
                    break;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String time = P.getTimeString1(mTotalTime);
                    String timePause = P.getTimeString1(mPauseTime);
                    //показ текущ времени
                    mCurrentTime.setText(time);
                    if (mIsStopped){
                        mCurrentTimePause.setText(timePause);
                    }
                }
            });
        }
    }
    //====================  end class MyTimerTask  =================================//

    //отслеживаем нажатие аппаратной кнопки Back и запрещаем, если секундомер работает
    @Override
    public void onBackPressed() {
        if (start){
            Log.d(TAG,"TimeMeterActivity onBackPressed if (start)");
             Toast.makeText(getApplicationContext(),
                        R.string.PressStopBefore, Toast.LENGTH_SHORT).show();
        }else{
            super.onBackPressed();
            Log.d(TAG,"TimeMeterActivity onBackPressed if (!start)");
        }
    }

    //отслеживание нажатия кнопки HOME
    @Override
    protected void onUserLeaveHint() {
        //включаем звук
        AudioManager audioManager =(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        super.onUserLeaveHint();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"TimeMeterActivity onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"TimeMeterActivity onDestroy");
        //стираем список
        repTimeList.clear();
        //выключаем таймер
        if (mTimer!=null)mTimer.cancel();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"TimeMeterActivity onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"TimeMeterActivity onResume");
        //получаем настройки из активности настроек
        prefSetting = PreferenceManager.getDefaultSharedPreferences(this);
        //получаем из файла настроек количество знаков после запятой
        accurancy = Integer.parseInt(prefSetting.getString("accurancy", "1"));
        Log.d(TAG,"TimeMeterActivity onResume accurancy = " + accurancy);
        //получаем режим работы секундомера в паузе
        pause = Integer.parseInt(prefSetting.getString("pause_type", "1"));
        Log.d(TAG,"TimeMeterActivity onResume pause = " + pause);
        //получаем из файла настроек наличие звука
        sound = prefSetting.getBoolean("cbSound",true);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.timemeter, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        ActionBar acBar = getSupportActionBar();
        acBar.setDisplayHomeAsUpEnabled(!start );
    /*
        //можно прятать всю панель
        if (start){
            acBar.hide();
        }else acBar.show();
    */
        //отключаем видимость на время от Старт до Стоп
        //acBar.setHomeButtonEnabled(!start);  //не работает
        menu.findItem(R.id.action_timing).setVisible(!start);
        menu.findItem(R.id.action_settings).setVisible(!start);
        return super.onPrepareOptionsMenu(menu);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            //чтобы работала стрелка Назад, а не происходил крах приложения
            case android.R.id.home:
                Log.d(TAG, "Домой");
                onBackPressed();
                return true;

            case R.id.action_timing:
                Log.d(TAG, "OptionsItem = action_timing");
                //получаем из предпочтений имя файла и отправляем его в интенте
                prefNameOfLastFile = getPreferences(MODE_PRIVATE);
                finishNameFile = prefNameOfLastFile.getString(P.LAST_FILE,
                        P.FILENAME_OTSECHKI_SEC);
                Log.d(TAG, "action_timing finishFileName = " + finishNameFile);
                //если файл был удалён, fileId = -1 и тогда вместо finishNameFile
                // передаём Автосохранение секундомера
                if ((mTempDBHelper.getIdFromFileName(finishNameFile)) == -1){
                    finishNameFile = P.FILENAME_OTSECHKI_SEC;
                }
                Log.d(TAG, "После action_timing finishFileName = " + finishNameFile);
                Intent intentTiming = new Intent(this, TimeGrafActivity.class);
                intentTiming.putStringArrayListExtra(TimeGrafActivity.REP_TIME_LIST,repTimeList);
                intentTiming.putExtra(P.FINISH_FILE_NAME, finishNameFile);
                startActivity(intentTiming);
                return true;

            case R.id.action_settings:
                Log.d(TAG, "OptionsItem = action_settings");
                Intent intentSettings = new Intent(this, PrefActivity.class);
                startActivity(intentSettings);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //диалог сохранения, оформленный как класс с указанием имени файла
    private void openSaveInFileDialogFragment() {
        DialogFragment dialogFragment = new DialogSaveSecFragment();
        dialogFragment.show(getSupportFragmentManager(),"SavePickerSecundomer");
    }
}
