package ru.bartex.p010_train;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class TimeMeterActivity extends AppCompatActivity
        implements DialogSaveSecFragment.SaverFragmentSecundomerListener {

    public static final String TAG ="33333";

    Button mButtonStart;
    Button mButtonStop;
    Button mButtonReset;
    Button mButtonNext;
    TextView mCurrentTime;
    ListView mListViewRep;

    private Timer mTimer;
    private TimerTask mTimerTask;
    private ToneGenerator mToneGenerator;
    private long mKvant = 100;//время в мс между срабатываниями TimerTask
    private long mTotalTime = 0;//текущее суммарное время для фрагмента подхода
    private long mTimeLast = 0;   // время на предыдущей отсечке
    private long mTimeStart = 0;  //время в момент нажатия на Старт
    private long mTimeExersize = 0;  //время от старта
    private long mTimeNext = 0;   //время на последующей отсечке
    private long mDelta = 0;   //время между соседними отсечками
    private boolean mRestart = false; //признак повторного старта (true)
    private boolean start = false;//признак нажатия на старт
    int ii = 0; //порядковый номер отсечки
    private String finishFileName = FileSaver.FINISH_FILE_NAME;

    ArrayList<String> repTimeList = new ArrayList<>();//список отсечек времени для записи в файл

    ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(); //список данных для показа на экране
    SimpleAdapter sara;
    final String ATTR_ITEM = "ru.bartex.p008_complex_imit_real.item";
    final String ATTR_TIME = "ru.bartex.p008_complex_imit_real.time";
    final String ATTR_DELTA = "ru.bartex.p008_complex_imit_real.delta";

    int accurancy; //точность отсечек - количество знаков после запятой - от MainActivity
    boolean sound = true; // включение / выключение звука
    private SharedPreferences prefSetting;// предпочтения из PrefActivity
    private SharedPreferences prefNameOfLastFile;// предпочтения - имя последнего сохранённого файла

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

            //переносим данные отсечек из repTimeList в arlSave для записи в файл
            ArrayList<String> arlSave = new ArrayList<String>();
            // если индекс =0, то первое значение
            for (int ii = 0; ii<repTimeList.size(); ii++ ) {
                double time_now;
                //получаем время в секундах между измерениями
                //если первое значение, то так
                if (ii == 0){
                    time_now = (double) (Long.parseLong(repTimeList.get(ii)) )/1000;
                    //если не первое значение, то как разницу
                }else{
                    time_now = (double) (Long.parseLong(repTimeList.get(ii)) -
                            Long.parseLong(repTimeList.get(ii - 1)))/1000;
                }
                String time = String.format("%s",time_now);;
                String reps = "1";
                String number = Integer.toString(ii);
                String trn = String.format("%s:%s:%s",time,reps,number);
                arlSave.add(trn);
            }

            // 1)вызываем метод saveDataAndFilename() записи в файл данных от секундомера
            // и записи имени файла в список сохранённых файлов, получаем записанное имя файла
            finishFileName =  saveDataAndFilename(arlSave, nameFile,
                    FileSaver.FILENAME_OTSECHKI_SEC ,FileSaver.TYPE_TIMEMETER);

            //  2) сохраняем в файл  c  именем FILENAME_TIMEMETER список меток времени для каждой отсечки
            //этот файл переписывается при записи новой порции отсечек секундомера
            // Используется в TimeGrafActivity для построения графика и таблицы данных
            writeArrayList(repTimeList,FileSaver.FILENAME_TIMEMETER );

            //  3) сохраняем имя файла в предпочтениях
            //получаем файл предпочтений
            prefNameOfLastFile = getPreferences(MODE_PRIVATE);
            // получаем Editor
            SharedPreferences.Editor ed = prefNameOfLastFile.edit();
            //пишем имя последнего сохранённого файла в предпочтения
            ed.putString(FileSaver.LAST_FILE, finishFileName);
            ed.apply();

            //если нужно записать и показать
            //то 1) пишем в список файлов и в список файлов имён, а также 2) во временный файл, а
            //затем вызываем интент, в котором передаём список с 0 в начале
            if (showGraf){
                //открываем экран с графиком только что записанных данных
                Intent intentTiming = new Intent(TimeMeterActivity.this, TimeGrafActivity.class);
                intentTiming.putStringArrayListExtra(TimeGrafActivity.REP_TIME_LIST,repTimeList);
                intentTiming.putExtra(FileSaver.FINISH_FILE_NAME, finishFileName);
                startActivity(intentTiming);
                //если нужно только записать, а потом, возможно показать через тулбар
                //то интент не вызываем
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
        acBar.setTitle("Секундомер");
        //показать стрелку Назад
        acBar.setDisplayHomeAsUpEnabled(true );
        acBar.setHomeButtonEnabled(true);
        //или заголовок в ActionBar устанавливается так
        //getSupportActionBar().setTitle("Секундомер");

        //установить портретную ориентацию экрана
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mCurrentTime = (TextView) findViewById(R.id.textViewTime);
        //для  mListViewRep в макете стоит запрет на выключение экрана android:keepScreenOn="true"
        mListViewRep = (ListView) findViewById(R.id.listViewRep);
        mButtonStart = (Button) findViewById(R.id.buttonStart);
        mButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!mRestart){
                    mTimeStart = System.currentTimeMillis();
                    mTimeLast = mTimeStart;
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
                mTimeExersize = mTimeNext-mTimeStart;
                mDelta = mTimeNext - mTimeLast;

                String s_item = Integer.toString(ii+1);
                String s_time = "";
                String s_delta = "";

                switch (accurancy){
                    case 1:
                        s_time = Stat.getTimeString1(mTimeExersize);
                        s_delta = Stat.getTimeString1 (mDelta);
                        break;
                    case 2:
                        s_time = Stat.getTimeString2(mTimeExersize);
                        s_delta = Stat.getTimeString2 (mDelta);
                        break;
                    case 3:
                        s_time = Stat.getTimeString3(mTimeExersize);
                        s_delta = Stat.getTimeString3 (mDelta);
                        break;
                    default:
                        s_time = Stat.getTimeString1(mTimeExersize);
                        s_delta = Stat.getTimeString1 (mDelta);
                }
                //заводим данные для одной строки списка
                Map<String, Object> m;
                m = new HashMap<>();
                m.put(ATTR_ITEM, s_item);
                m.put(ATTR_TIME, s_time);
                m.put(ATTR_DELTA, s_delta);
                //добавляем данные в начало ArrayList
                data.add(0,m);
                //добавляем время отсечки в список (в конец) для записи в файл
                repTimeList.add(Long.toString(mTimeExersize));

                //добавляем время отсечки  в строковом формате в список (в конец) для записи в файл
                //repTimeList.add(s_time);  //так нельзя- потом число не прочитать


                Log.d(TAG,"mTimeExersize = " + mTimeExersize + "  mDelta = " + mDelta);

                //обновляем список на экране
                sara.notifyDataSetChanged();
                //увеличиваем порядковый номер отсечки
                ii++;
                mTimeLast = mTimeNext;
            }
        });

        mButtonStop = (Button) findViewById(R.id.buttonStop);
        mButtonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //останавливаем часы и подаём сигнал
                if (mTimer!=null)mTimer.cancel();
                mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 100);

                mButtonStart.setEnabled(true);
                mButtonStart.setText("Продолжить" );
                mButtonStart.setVisibility(View.VISIBLE);
                mButtonReset.setEnabled(true);
                mButtonReset.setVisibility(View.VISIBLE);
                mButtonStop.setEnabled(false);
                mButtonStop.setVisibility(View.GONE);
                mButtonNext.setEnabled(false);
                mButtonNext.setVisibility(View.GONE);

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
                mCurrentTime.setText("00:00");
                mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 300);

                ii = 0;
                mTimeLast = 0;   // время на предыдущей отсечке
                mTimeStart = 0;  //время в момент нажатия на Старт
                mTimeNext = 0;   //время на последующей отсечке
                mDelta = 0;   //время между соседними отсечками
                mRestart = false;


                data.clear();
                sara.notifyDataSetChanged();

                mButtonReset.setEnabled(false);
                mButtonReset.setVisibility(View.GONE);
                mButtonStart.setEnabled(true);
                mButtonStart.setText("Старт" );
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

    //отслеживаем нажатие аппаратной кнопки Back и запрещаем, если секундомер работает
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

            case R.id.action_settings:
                Log.d(TAG, "OptionsItem = action_settings");
                Intent intentSettings = new Intent(this, PrefActivity.class);
                startActivity(intentSettings);
                return true;

            case R.id.action_timing:
                Log.d(TAG, "OptionsItem = action_timing");
                //получаем из предпочтений имя файла и отправляем его в интенте
                prefNameOfLastFile = getPreferences(MODE_PRIVATE);
                finishFileName = prefNameOfLastFile.getString(FileSaver.LAST_FILE,
                        FileSaver.NAME_OF_LAST_FILE_ZERO);

                Intent intentTiming = new Intent(this, TimeGrafActivity.class);
                intentTiming.putStringArrayListExtra(TimeGrafActivity.REP_TIME_LIST,repTimeList);
                intentTiming.putExtra(FileSaver.FINISH_FILE_NAME, finishFileName);
                startActivity(intentTiming);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }




    //======================class MyTimerTask=================================//
    public class MyTimerTask extends TimerTask{
        @Override
        public void run() {  //запускаем MyTimerTask в методе run()
            mTotalTime = System.currentTimeMillis() - mTimeStart;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String time = Stat.getTimeString1(mTotalTime);
                    //показ текущ времени
                    mCurrentTime.setText(time);
                }
            });
        }
    }
    //====================  end class MyTimerTask  =================================//

    //диалог сохранения, оформленный как класс с указанием имени файла
    private void openSaveInFileDialogFragment() {
        DialogFragment dialogFragment = new DialogSaveSecFragment();
        dialogFragment.show(getSupportFragmentManager(),"SavePickerSecundomer");
    }

    //запись данных в файл и запись имени файла в список сохранённых файлов
    //в зависимости от имени, введённого в диалоге сохранения и типа данных

    public  String saveDataAndFilename(ArrayList<String> dataSave,
                                     String nameFile, String fileNameDefoult,
                                     String typeData){

        String finishFileName = "";
        //добавляем в синглет списка файлов новое имя
        //получаем ссылку на экземпляр FileSaverLab
        FileSaverLab fileSaverLab = FileSaverLab.get();
        FileSaver fileSaver = new FileSaver();
        //получаем список данных о сохранённых файлов
        List<FileSaver> listOfFiles = fileSaverLab.getFileSavers();
        Log.d(TAG, "TimeMeterActivity saveDataAndFilename " +
                " Разммер списка файлов = " + listOfFiles.size() );
        //если имя файла- пустая строка, то пишем отсечки в файл
        // с именем по умолчанию fileNameDefoult, а в синглет ничего не добавляем если
        // запись с именем fileNameDefoult уже была
        if (nameFile.isEmpty()) {
            finishFileName = fileNameDefoult;
            //пишем отсечки dataSave в файл с именем fileNameDefoult
            writeArrayList(dataSave, fileNameDefoult);
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
                Log.d(TAG, "TimeMeterActivity saveDataAndFilename Пропускаем запись имени файла ");
            }else{
                //если записи с таким именем нет, пишем имя файла в файл
                //даём имя по умолчанию
                fileSaver.setTitle(fileNameDefoult);
                fileSaver.setTipe(typeData);  //даём тип файлу
                fileSaver.setDate(); //пишем текущую дату и время, их можно прочитать где-то
                //добавляем данные файла в список-хранитель данных
                fileSaverLab.addFileSaver(fileSaver,0);
                //получаем весь список имён файлов из FileSaverLab
                ArrayList<String> listNamesOfFiles = fileSaverLab.getListFullNamesOfFiles();
                Log.d(TAG, "TimeMeterActivity saveDataAndFilename " +
                        "После Записи В listNamesOfFiles строк =  " + listNamesOfFiles.size());
                //пишем список имён в файл имён
                writeArrayList(listNamesOfFiles,FileSaver.FILENAME_NAMES_OF_FILES);
            }
            //если имя файла-  НЕ пустая строка, то пишем отсечки в файл
            // с именем nameFile, а в синглет  добавляем FileSaver с данными о файле
        } else {
            finishFileName = nameFile;
            //пишем отсечки dataSave в файл с именем nameFile
            writeArrayList(dataSave, nameFile);
            //даём имя, считанное из строки
            fileSaver.setTitle(nameFile);
            fileSaver.setTipe(typeData);  //даём тип файлу
            fileSaver.setDate(); //ишем текущую дату и время, их можно прочитать где-то
            //добавляем данные файла в список-хранитель данных
            fileSaverLab.addFileSaver(fileSaver);

            //получаем весь список имён файлов из FileSaverLab
            ArrayList<String> listNamesOfFiles = fileSaverLab.getListFullNamesOfFiles();
            Log.d(TAG, "TimeMeterActivity saveDataAndFilename " +
                    "В listNamesOfFiles строк =  " + listNamesOfFiles.size());
            //пишем список имён в файл имён
            writeArrayList(listNamesOfFiles,FileSaver.FILENAME_NAMES_OF_FILES);
        }
        Log.d(TAG, "TimeMeterActivity saveDataAndFilename " +
                "В списке FileSaverLab сохранённых файлов  Количество файлов = " +
                fileSaverLab.getFileSavers().size());

        return finishFileName;
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
            Log.d(TAG, "TimeMeterActivity writeArrayList Файл ArrayList записан ");
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
