package ru.bartex.p010_train;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.bartex.p010_train.ru.bartex.p010_train.data.P;
import ru.bartex.p010_train.ru.bartex.p010_train.data.TabSet;
import ru.bartex.p010_train.ru.bartex.p010_train.data.TempDBHelper;

public class TimeGrafActivity extends AppCompatActivity {

    public static final String TAG ="33333";

    static final String REP_TIME_LIST = "ru.bartex.p010_train.repTimeList";

    ListView mListViewTiming;
    TextView nameOfFile;

    private GraphView graphPace;
    LineGraphSeries<DataPoint> seriesPace;
    DataPoint[] pointPace1;

    //список данных для показа на экране
    ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
    SimpleAdapter sara;
    int accurancy; //точность отсечек - количество знаков после запятой - от MainActivity
    private SharedPreferences prefSetting;// предпочтения из PrefActivity
    private String finishFileName = P.FINISH_FILE_NAME;

    TempDBHelper mTempDBHelper = new TempDBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timegraf);

        ActionBar act = getSupportActionBar();
        act.setTitle(R.string.graph);
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
        finishFileName =intent.getStringExtra(P.FINISH_FILE_NAME);
        Log.d(TAG,"TimeGrafActivity имя = " + finishFileName);

        //имя файла из интента = null, если не было записи в преференсис-
        // это возможно при первом запуске, когда из секундомера сразу идем на график
        // тогда присваиваем имя единственной записи в базе, сделанной в onCreate MainActivity
        if (finishFileName==null){
            finishFileName = P.FILENAME_OTSECHKI_SEC;
        }

        nameOfFile = (TextView)findViewById(R.id.textViewName_TimeGraf);
        nameOfFile.setText(finishFileName);

        //получаем id записи с таким именем
        long finishFileId = mTempDBHelper.getIdFromFileName (finishFileName);
        Log.d(TAG,"TimeGrafActivity имя =" + finishFileName + "  Id = " + finishFileId );

        //получаем курсор с данными подхода с id = finishFileId
        Cursor cursor = mTempDBHelper.getAllSetFragmentsRaw(finishFileId);
        // Узнаем индекс каждого столбца
        int idColumnIndex = cursor.getColumnIndex(TabSet.COLUMN_SET_TIME);

        graphPace = (GraphView) findViewById(R.id.graphPace);
        //размер массива данных для линии графика = размер курсора
        pointPace1 = new DataPoint[cursor.getCount()];
        //суммарное время подхода сначала = 0
        long time_total = 0;  //суммарное время подхода
        long time_now;  //время на отрезке

        //проходим по курсору и берём данные
        if (cursor.moveToFirst()) {
            do {
                // Используем индекс для получения строки или числа и переводим в милисекунды
                //чтобы использовать ранее написанные функции getTimeString1
                time_now = (long) (cursor.getFloat(idColumnIndex)*1000);
                time_total += time_now;
                Log.d(TAG,"TimeGrafActivity cursor.getPosition()+1 = " +
                        (cursor.getPosition()+1) + "  time_now = " + time_now +
                        "  time_total = " + time_total);

                //Делаем данные для адаптера
                String s_item = Integer.toString(cursor.getPosition()+1);
                String s_time;
                String s_delta;

                switch (accurancy){
                    case 1:
                        s_time = P.getTimeString1(time_total);
                        s_delta = P.getTimeString1 (time_now);
                        break;
                    case 2:
                        s_time = P.getTimeString2(time_total);
                        s_delta = P.getTimeString2 (time_now);
                        break;
                    case 3:
                        s_time = P.getTimeString3(time_total);
                        s_delta = P.getTimeString3 (time_now);
                        break;
                    default:
                        s_time = P.getTimeString1(time_total);
                        s_delta = P.getTimeString1 (time_now);
                }
                //заводим данные для одной строки списка
                Map<String, Object> m;
                m = new HashMap<>();
                m.put(P.ATTR_ITEM_GRAF, s_item);
                m.put(P.ATTR_TIME_GRAF, s_time);
                m.put(P.ATTR_DELTA_GRAF, s_delta);
                //добавляем данные в конец ArrayList
                data.add(m);

                //Переводим время в секунды
                double time_tran = (double)time_now/1000;
                //добавляем точку для графика, используя double time_tran
                pointPace1[cursor.getPosition()] = new DataPoint(
                        (cursor.getPosition()+1), time_tran);

            } while (cursor.moveToNext());
        }
        //заполняем линию графика точками
        seriesPace = new LineGraphSeries<>(pointPace1);
        //устснавливаем параметры кривой графика
        setParamSeries(seriesPace);
        //добавляем кривую на график
        graphPace.addSeries(seriesPace);
        //устанавливаем параметры графика
        setParamGraph(graphPace, cursor.getCount());
        //Название графика
        graphPace.setTitle ("Длительность круга, с");

        //Делаем массивы откуда-куда
        String[] from = {P.ATTR_ITEM_GRAF, P.ATTR_TIME_GRAF, P.ATTR_DELTA_GRAF};
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
                intent.putExtra(P.FROM_ACTIVITY, P.TIME_GRAF_ACTIVITY);
                intent.putExtra(P.FINISH_FILE_NAME, finishFileName);
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
