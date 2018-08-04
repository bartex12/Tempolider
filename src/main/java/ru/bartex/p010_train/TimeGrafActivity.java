package ru.bartex.p010_train;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TimeGrafActivity extends AppCompatActivity {

    public static final String TAG ="33333";

    static final String REP_TIME_LIST = "ru.bartex.p010_train.repTimeList";
    static final int TIME_GRAF_ACTIVITY = 222;

    ListView mListViewTiming;
    TextView nameOfFile;

    ArrayList<String> mListTiming = new ArrayList<>();//список из сохранённого файла
    ArrayList<String> mListTimeTransfer = new ArrayList<>();// список интервалов для загрузки в темполидер

    private GraphView graphPace;
    LineGraphSeries<DataPoint> seriesPace;
    DataPoint[] pointPace1;

    final String ATTR_ITEM = "ru.bartex.p008_complex_imit_real.item";
    final String ATTR_TIME = "ru.bartex.p008_complex_imit_real.time";
    final String ATTR_DELTA = "ru.bartex.p008_complex_imit_real.delta";
    ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(); //список данных для показа на экране
    SimpleAdapter sara;
    int accurancy; //точность отсечек - количество знаков после запятой - от MainActivity
    private SharedPreferences prefSetting;// предпочтения из PrefActivity
    private String finishFileName = FileSaver.FINISH_FILE_NAME;
    private SharedPreferences prefNameOfLastFile;// предпочтения - имя последнего сохранённого файла

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timegraf);

        ActionBar act = getSupportActionBar();
        act.setTitle("Графики");
        act.setDisplayHomeAsUpEnabled(true );
        act.setHomeButtonEnabled(true);

        //получаем настройки из активности настроек
        prefSetting = PreferenceManager.getDefaultSharedPreferences(this);
        //получаем из файла настроек количество знаков после запятой
        accurancy = Integer.parseInt(prefSetting.getString("accurancy", "1"));
        Log.d(TAG,"TimeGrafActivity accurancy = " + accurancy);

        mListViewTiming = (ListView)findViewById(R.id.listViewTiming);

        //получаем интент от TimeMeterActivity
        Intent intent = getIntent();
        //получаем имя файла из интента
        finishFileName =intent.getStringExtra(FileSaver.FINISH_FILE_NAME);
        nameOfFile = (TextView)findViewById(R.id.textViewName_TimeGraf);
        nameOfFile.setText(finishFileName);
        Log.d(TAG,"TimeGrafActivity finishFileName = " + finishFileName);
        Log.d(TAG,"TimeGrafActivity REP_TIME_LIST = " + intent.getStringArrayListExtra(REP_TIME_LIST));

        if (intent.getStringArrayListExtra(REP_TIME_LIST).size()==0){
            //читаем список отсечек времени из файла в ArrayList
            mListTiming = readArrayList(FileSaver.FILENAME_TIMEMETER);
        }else {
            //читаем список отсечек времени из интента  в ArrayList
            mListTiming = intent.getStringArrayListExtra(REP_TIME_LIST);
        }

        graphPace = (GraphView) findViewById(R.id.graphPace);
        //размер массива данных для линии графика = размер списка
        pointPace1 = new DataPoint[ mListTiming.size()];

        //суммарное время подхода = 0
        long time_total = 0;

        //заполняем список для вывода на экран и массив данных для линии графика
        if (mListTiming.size()>0) {
            //заполняем список экрана
            for (int i = 0; i < mListTiming.size(); i++) {

                long time_now;
                //получаем время в мс между измерениями
                //если первое значение, то так
                if (i == 0){
                    time_now = Long.parseLong(mListTiming.get(i)) ;
                    //если не первое значение, то как разницу
                }else{
                    time_now = Long.parseLong(mListTiming.get(i)) -
                            Long.parseLong(mListTiming.get(i - 1));
                }
                time_total += time_now;

                //Переводим время в секунды
                double time_tran = (double)time_now/1000;
                String time_transfer = String.format("%s",time_tran);
                //добавляем строку  в список для передачи раскладки в темполидер
                mListTimeTransfer.add(time_transfer);

                //Делаем данные для адаптера
                String s_item = Integer.toString(i+1);
                String s_time = "";
                String s_delta = "";

                switch (accurancy){
                    case 1:
                        s_time = Stat.getTimeString1(time_total);
                        s_delta = Stat.getTimeString1 (time_now);
                        break;
                    case 2:
                        s_time = Stat.getTimeString2(time_total);
                        s_delta = Stat.getTimeString2 (time_now);
                        break;
                    case 3:
                        s_time = Stat.getTimeString3(time_total);
                        s_delta = Stat.getTimeString3 (time_now);
                        break;
                    default:
                        s_time = Stat.getTimeString1(time_total);
                        s_delta = Stat.getTimeString1 (time_now);
                }
                //заводим данные для одной строки списка
                Map<String, Object> m;
                m = new HashMap<>();
                m.put(ATTR_ITEM, s_item);
                m.put(ATTR_TIME, s_time);
                m.put(ATTR_DELTA, s_delta);
                //добавляем данные в конец ArrayList
                data.add(m);

                //добавляем точку для графика, используя double time_tran
                pointPace1[i] = new DataPoint(i+1,time_tran);
            }

            Log.d(TAG,"mListTimeTransfer.get(0) = " + mListTimeTransfer.get(0));

            //заполняем линию графика точками
            seriesPace = new LineGraphSeries<>(pointPace1);
            //устснавливаем параметры кривой графика
            setParamSeries(seriesPace);
            //добавляем кривую на график
            graphPace.addSeries(seriesPace);
            //устанавливаем параметры графика
            setParamGraph(graphPace,(mListTiming.size()));
            //Название графика
            graphPace.setTitle ("Длительность круга, с");
            /*
            if (kindOfGraf){
                //Название графика
                graphPace.setTitle ("Длительность итервала, с");
            }else {
                //Название графика
                graphPace.setTitle ("Темп движений, раз в мин");
            }
             */
        }
        //Делаем массивы откуда-куда
        String[] from = {ATTR_ITEM, ATTR_TIME, ATTR_DELTA};
        int[] to = {R.id.item_list, R.id.time_list, R.id.delta_list};
        //заполняем адаптер данными списка
        sara = new SimpleAdapter(this, data, R.layout.list_rep, from, to);
        //подключаем адаптер к списку
        mListViewTiming.setAdapter(sara);

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
        getMenuInflater().inflate(R.menu.timegraf, menu);
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
            case R.id.action_transfer:
                Log.d(TAG, "action_transfer");
                Intent intent = new Intent(this, SetListActivity.class);
                intent.putExtra(SingleFragmentActivity.FROM_ACTIVITY,TIME_GRAF_ACTIVITY);
                intent.putStringArrayListExtra(SingleFragmentActivity.ARRAY_STRING_TRANSFER,
                        mListTimeTransfer);
                intent.putExtra(FileSaver.FINISH_FILE_NAME, finishFileName);
                startActivity(intent);
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    //Прочитать список  с данными из файла
    private ArrayList<String> readArrayList( String fileName) {

        ArrayList<String> arrayList = new ArrayList<>();

        try {
            // открываем поток для чтения
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    openFileInput(fileName)));
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    arrayList.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public void setParamSeries(LineGraphSeries<DataPoint> series){
        series.setColor(Color.BLUE);
        //ручная установка параметров
        series.setDrawDataPoints(true);
        //радиус точки
        series.setDataPointsRadius(8);
        // толщина линии
        series.setThickness(4);
    }

    public void setParamGraph(GraphView graph, int size){
        graph.getGridLabelRenderer().setTextSize(50);
        //  ручная установка горизонт пределов
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(size);
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Номер цикла");
        // разрешение горизонт прокрутки
        //graph.getViewport().setScrollable(true);
    }


}
