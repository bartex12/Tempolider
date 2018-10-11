package ru.bartex.p010_train;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ru.bartex.p010_train.ru.bartex.p010_train.data.P;
import ru.bartex.p010_train.ru.bartex.p010_train.data.TabSet;
import ru.bartex.p010_train.ru.bartex.p010_train.data.TempDBHelper;

public class ChangeTempActivity extends AppCompatActivity implements
        DialogChangeTemp.ChangeTempUpDownListener{

    public static final String POSITION = "position_ChangeTempActivity";
    public static final String NAME_OF_FILE = "nameOfFile_ChangeTempActivity";
    static final String CHANGE_REQUEST = "ChangeTempActivity.change_request";
    public static final int  VALUE = 10;
    private static final String TAG = "33333";

    final String ATTR_TIME = "time";
    final String ATTR_REP = "rep";
    final String ATTR_NUMBER = "number";

    ListView changeTemp_listView;
    TextView changeTemp_textViewName;

    TextView timeTotal;
    TextView repsTotal;
    TextView deltaValue;

    Button changeTemp_buttonMinus5;
    Button changeTemp_buttonMinus1;
    ImageButton changeReps_imageButtonRevert;
    Button changeTemp_buttonPlus1;
    Button changeTemp_buttonPlus5;

    CheckBox mCheckBoxAll;
    RadioGroup mRadioGroupTimeCount;
    RadioButton mRadioButtonTime;
    RadioButton mRadioButtonCount;
    boolean redactTime = true;

    int positionOfList = 0;
    int pos;
    int offset = 0;
    float time = 0f; //размер изменений времени
    int count = 0; //размер изменений количества
    //количество фрагментов подхода
    int countOfSet ;

    List<Set> listSetCopy; //резервный список отсечек

    TempDBHelper mDBHelper = new TempDBHelper(this);
    ArrayList<Map<String, Object>> data;
    Map<String,Object> m;
    SimpleAdapter sara;
    String newFileName;  // имя файла - копии
    DataFile dataFileCopy; //экземпляр объекта "фрагмент подхода"

    int accurancy; //точность отсечек - количество знаков после запятой - от MainActivity
    private SharedPreferences prefSetting;// предпочтения из PrefActivity

    private float mTimeOfSet = 0;   //общее время выполнения подхода в секундах
    private int mTotalReps = 0;  //общее количество повторений в подходе

    long fileId; //имя файла на редактировании
    long fileIdCopy;  //имя копии файла для отмены
    String finishFileName;

    @Override
    public void changeTempUpDown(int valueDelta, boolean up) {

        //если повысить темп up = true
        float ff = (up == true) ? (1 - ((float)valueDelta/100)) : (1 + ((float)valueDelta/100));
        Log.d(TAG, "ChangeTempActivity changeTempUpDown ff = " + ff);

        List<Set> sets = SetLab.getSets();
        for (Set s:sets){
            s.setTimeOfRep((s.getTimeOfRep())*ff);
        }

        updateAdapter();
        calculateAndShowTotalValues();
        changeTemp_listView.setSelection(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_temp);
        Log.d(TAG, "ChangeTempActivity onCreate------!!!-------");

        //разрешить только портретную ориентацию экрана
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ActionBar acBar = getSupportActionBar();
        acBar.setTitle("Изменить");
        //показать стрелку Назад
        acBar.setDisplayHomeAsUpEnabled(true );
        acBar.setHomeButtonEnabled(true);

        Intent intent = getIntent();
        //получаем имя файла из интента
        finishFileName = intent.getStringExtra(P.INTENT_TO_CHANGE_TEMP_FILE_NAME);
        Log.d(TAG, "ChangeTempActivity finishFileName = " + finishFileName);
        //получаем id файла
        fileId = mDBHelper.getIdFromFileName(finishFileName);

        //количество фрагментов подхода
        countOfSet =mDBHelper.getSetFragmentsCount(fileId);

         //создаём и записываем в базу копию файла на случай отмены изменений
        fileIdCopy = mDBHelper.createFileCopy(finishFileName, fileId);

        deltaValue = (TextView)findViewById(R.id.deltaValue);
        deltaValue.setVisibility(View.INVISIBLE);
        //deltaValue.setText("-00%");
        //deltaValue.setBackground(R.drawable.ramka);

        mRadioButtonTime = (RadioButton) findViewById(R.id.radioButtonTime);
        mRadioButtonCount = (RadioButton) findViewById(R.id.radioButtonCount);
        mRadioGroupTimeCount = (RadioGroup) findViewById(R.id.radioGroupTimeCount);
        mRadioGroupTimeCount.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.radioButtonTime:
                        redactTime = true;
                        changeTemp_buttonMinus5.setText("-5%");
                        changeTemp_buttonMinus1.setText("-1%");
                        changeTemp_buttonPlus1.setText("+1%");
                        changeTemp_buttonPlus5.setText("+5%");
                        break;
                    case R.id.radioButtonCount:
                        redactTime = false;
                        changeTemp_buttonMinus5.setText("-5");
                        changeTemp_buttonMinus1.setText("-1");
                        changeTemp_buttonPlus1.setText("+1");
                        changeTemp_buttonPlus5.setText("+5");
                        break;
                }
            }
        });

        mCheckBoxAll = (CheckBox) findViewById(R.id.checkBox);
        mCheckBoxAll.setChecked(true);
        mCheckBoxAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                updateAdapter();
                calculateAndShowTotalValues();
                changeTemp_listView.setSelectionFromTop(pos, offset);

                //делаем индикатор невидимым
                deltaValue.setVisibility(View.INVISIBLE);
                //обнуляем показатели разности значений
                time = 0f;
                count = 0;
            }
        });

        changeTemp_listView = (ListView)findViewById(R.id.changeTemp_listView);
        //накладываем жёлтый задний фон строки списка
        // на градиентный фон макета разметки  самОй строки list_item_set_textview
        changeTemp_listView.setBackgroundColor(Color.YELLOW);
        //разрешаем выбор в списке (по умолчанию - NONE , тип - в макете)
        changeTemp_listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //слушатель нажатий
        changeTemp_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //выделенная строка
                positionOfList = i;
                Log.d(TAG, "ChangeTempActivity i = " + i);

                //выделяем строку и удерживаем её позицию в списке
                pos = changeTemp_listView.getFirstVisiblePosition();
                View v = changeTemp_listView.getChildAt(0);
                if (v != null) {
                    offset = v.getTop() - changeTemp_listView.getPaddingTop();
                }

                updateAdapter();
                calculateAndShowTotalValues();
                changeTemp_listView.setSelectionFromTop(pos, offset);

                //делаем индикатор невидимым
                deltaValue.setVisibility(View.INVISIBLE);
                //обнуляем показатели разности значений
                time = 0f;
                count = 0;
            }
        });

        changeTemp_textViewName =(TextView)findViewById(R.id.changeTemp_textViewName);
        changeTemp_textViewName.setText("Имя:  " + finishFileName);

        timeTotal = (TextView)findViewById(R.id.timeTotal);
        repsTotal = (TextView)findViewById(R.id.repsTotal);

        changeTemp_buttonMinus5 = (Button) findViewById(R.id.changeTemp_buttonMinus5);
        changeTemp_buttonMinus5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //String s = reductAction(0.95f,-5);
                reductAction(0.95f,-5);
                updateAdapter();
                calculateAndShowTotalValues();
                changeTemp_listView.setSelectionFromTop(pos, offset);

                //deltaValue.setVisibility(View.VISIBLE);
                //String ss = redactTime == true ? s+"%":s;
               // deltaValue.setText(ss);

            }
        });
        changeTemp_buttonMinus1 = (Button) findViewById(R.id.changeTemp_buttonMinus1);
        changeTemp_buttonMinus1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //String s = reductAction(0.99f, -1);
                reductAction(0.99f, -1);
                updateAdapter();
                calculateAndShowTotalValues();
                changeTemp_listView.setSelectionFromTop(pos, offset);

                //deltaValue.setVisibility(View.VISIBLE);
                //String ss = redactTime == true ? s+"%":s;
                //deltaValue.setText(ss);
            }
        });

        changeReps_imageButtonRevert = (ImageButton) findViewById(R.id.changeTemp_imageButtonRevert);
        changeReps_imageButtonRevert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //удаляем изменённый файл
                mDBHelper.deleteFileAndSets(fileId);
                //теперь первоначальный файл содержится в копии
                fileId = fileIdCopy;
                //изменяем имя у копии файла на первоначальное имя
                mDBHelper.updateFileName(finishFileName,fileIdCopy);
                // снова создаём и записываем в базу копию файла на случай отмены изменений
                fileIdCopy = mDBHelper.createFileCopy(finishFileName, fileId);

                updateAdapter();
                calculateAndShowTotalValues();
                changeTemp_listView.setSelectionFromTop(pos, offset);

                //делаем индикатор невидимым
                //deltaValue.setVisibility(View.INVISIBLE);
                //обнуляем показатели разности значений
                //time = 0f;
                //count = 0;
            }
        });

        changeTemp_buttonPlus1 = (Button) findViewById(R.id.changeTemp_buttonPlus1);
        changeTemp_buttonPlus1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //String s = reductAction(1.01f, 1);
                reductAction(1.01f, 1);
                updateAdapter();
                calculateAndShowTotalValues();
                changeTemp_listView.setSelectionFromTop(pos, offset);

                //deltaValue.setVisibility(View.VISIBLE);
                //String ss = redactTime == true ? s+"%":s;
                //deltaValue.setText(ss);
            }
        });
        changeTemp_buttonPlus5 = (Button) findViewById(R.id.changeTemp_buttonPlus5);
        changeTemp_buttonPlus5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //String s = reductAction(1.05f, 5);
                reductAction(1.05f, 5);
                updateAdapter();
                calculateAndShowTotalValues();
                changeTemp_listView.setSelectionFromTop(pos, offset);

                //deltaValue.setVisibility(View.VISIBLE);
               // String ss = redactTime == true ? s+"%":s;
               // deltaValue.setText(ss);
            }
        });

        //Выставляем надписи на кнопках перед началом редактирования
        if(redactTime){
            changeTemp_buttonMinus5.setText("-5%");
            changeTemp_buttonMinus1.setText("-1%");;
            changeTemp_buttonPlus1.setText("+1%");;
            changeTemp_buttonPlus5.setText("+5%");;
        }else {
            changeTemp_buttonMinus5.setText("-5");
            changeTemp_buttonMinus1.setText("-1");;
            changeTemp_buttonPlus1.setText("+1");;
            changeTemp_buttonPlus5.setText("+5");;
        }

        //получаем настройки из активности настроек
        prefSetting = PreferenceManager.getDefaultSharedPreferences(this);
        //получаем из файла настроек количество знаков после запятой
        accurancy = Integer.parseInt(prefSetting.getString("accurancy", "1"));
        Log.d(TAG,"TimeMeterActivity accurancy = " + accurancy);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "ChangeTempActivity onResume");

        //выводим список, суммарные время и количество, устанавливаем выделение цветом
        updateAdapter();
        calculateAndShowTotalValues();
        changeTemp_listView.setSelection(positionOfList);
    }

    @Override
    public View findViewById(@IdRes int id) {
        return super.findViewById(id);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "ChangeTempActivity onPause");


    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "ChangeTempActivity onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ChangeTempActivity onDestroy");
        //удаляем  файл - копию
        mDBHelper.deleteFileAndSets(fileIdCopy);
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
        getMenuInflater().inflate(R.menu.activity_change_temp, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                Log.d(TAG, "Домой");
                //Intent intentHome = new Intent(this,MainActivity.class);
                //startActivity(intentHome);
                finish();
                return true;

            case R.id.change_temp_up_down:
                Log.d(TAG, "change_temp_up_down");

                DialogFragment dialogFragmentChange = DialogChangeTemp.newInstance(VALUE);
                dialogFragmentChange.show(getSupportFragmentManager(), "dialogFragmentChange");

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void calculateAndShowTotalValues(){

        //посчитаем общее врямя выполнения подхода в секундах
        mTimeOfSet = mDBHelper.getSumOfTimeSet(fileId);
        Log.d(TAG, "Суммарное время подхода  = " + mTimeOfSet);

        //посчитаем общее количество повторений в подходе
        mTotalReps = mDBHelper.getSumOfRepsSet(fileId);
        Log.d(TAG, "Суммарное количество повторений  = " + mTotalReps);

        //покажем общее время подхода и общее число повторений в подходе

        timeTotal.setText(showTotalTime(mTimeOfSet, SingleFragmentActivity.mKvant));
        repsTotal.setText(String.format("Количество  %d", mTotalReps));
    }

    //покажем общее время подхода
    public String showTotalTime(float timeOfSet, long kvant){

        float millisTime = timeOfSet*1000;
        //покажем суммарное время подхода
        int minut = ((int)millisTime/60000)%60;
        int second = ((int)millisTime/1000)%60;
        int decim = (int)((millisTime%1000)/kvant);
        int hour = (int)((millisTime/3600000)%24);

        // общее время подхода
        String time = "";
        if (hour<1){
            if(minut<10) {
                time = String.format("Время  %d:%02d.%d",minut, second, decim);
            }else if (minut<60){
                time = String.format("Время  %02d:%02d.%d",minut,second,decim);
            }
        }else {
            time = String.format("Время  %d:%02d:%02d.%d",hour,minut,second,decim);
        }
        return time;
    }


    private String reductAction(float ff, int ii){

        String delta = "";
        if (redactTime){
            time += (ff-1f)*100;
            if (mCheckBoxAll.isChecked()){
                //обновляем фрагменты по очереди
                for (int i = 0; i<countOfSet; i++ ){
                    DataSet dataSet = mDBHelper.getOneSetFragmentData(fileId, i);
                    dataSet.setTimeOfRep((dataSet.getTimeOfRep())*ff);
                    mDBHelper.updateSetFragment(dataSet);
                    Log.d(TAG, "ChangeTempActivity dataSet Time = " + dataSet.getTimeOfRep());
                }
            }else {
                DataSet dataSet = mDBHelper.getOneSetFragmentData(fileId, positionOfList);
                dataSet.setTimeOfRep((dataSet.getTimeOfRep())*ff);
                mDBHelper.updateSetFragment(dataSet);
                Log.d(TAG, "ChangeTempActivity dataSet Time = " + dataSet.getTimeOfRep());
            }
            delta = String.format("%+3.0f", time);

        }else {
            count +=ii;
            //если сразу во всех строках
            if (mCheckBoxAll.isChecked()){
                //обновляем фрагменты по очереди
                for (int i = 0; i<countOfSet; i++ ){
                    DataSet dataSet = mDBHelper.getOneSetFragmentData(fileId, i);
                    dataSet.setReps((dataSet.getReps())+ii);
                    //если число повторений < 0, пишем 0
                    if (dataSet.getReps() < 0){
                        dataSet.setReps(0);
                    }
                    mDBHelper.updateSetFragment(dataSet);
                    Log.d(TAG, "ChangeTempActivity dataSet Reps = " + dataSet.getReps());
                }
                //если только в одной - выбранной -  строке
            }else {
                DataSet dataSet = mDBHelper.getOneSetFragmentData(fileId, positionOfList);
                dataSet.setReps((dataSet.getReps())+ii);
                if (dataSet.getReps() < 0){
                    dataSet.setReps(0);
                }
                mDBHelper.updateSetFragment(dataSet);
                Log.d(TAG, "ChangeTempActivity dataSet Time = " + dataSet.getTimeOfRep());
            }
            delta = String.format("%+3d", count);

        }
        return delta;
    }

    private void  reductActionProcent(int procent, boolean up){

        float ff = up ==true ? (1+ procent/100) : (1- procent/100);

        List<Set> sets = SetLab.getSets();
        for (Set s:sets){
            s.setTimeOfRep((s.getTimeOfRep())*ff);
        }
    }

    public void updateAdapter() {
        Log.d(TAG, "SetListFragment: updateAdapter() ");
        //получаем id записи с таким именем
        long finishFileId = mDBHelper.getIdFromFileName (finishFileName);
        Log.d(TAG,"SetListFragment  имя =" + finishFileName + "  Id = " + finishFileId );

        //получаем курсор с данными подхода с id = finishFileId
        Cursor cursor = mDBHelper.getAllSetFragments(finishFileId);
        Log.d(TAG, "SetListFragment: updateAdapter() cursor.getCount() = " + cursor.getCount());

        //Список с данными для адаптера
        data = new ArrayList<Map<String, Object>>(cursor.getCount());
        //проходим по курсору и берём данные
        if (cursor.moveToFirst()) {
            do {
                float time  = cursor.getFloat(cursor.getColumnIndex(TabSet.COLUMN_SET_TIME));
                int reps_now = cursor.getInt(cursor.getColumnIndex(TabSet.COLUMN_SET_REPS));
                int number_now = cursor.getInt(cursor.getColumnIndex(TabSet.COLUMN_SET_FRAG_NUMBER));

                Log.d(TAG,"SetListFragment time_now = " + time +
                        "  reps_now = " + reps_now + "  number_now = " + number_now);

                String s_delta;

                switch (accurancy){
                    case 1:
                        s_delta = String.format("%.01f",time);
                        break;
                    case 2:
                        s_delta = String.format("%.02f",time);
                        break;
                    case 3:
                        s_delta = String.format("%.03f",time);
                        break;
                    default:
                        s_delta =String.format("%.01f",time);
                }

                m = new HashMap<>();
                //m.put(P.ATTR_TIME, timeFormat);
                m.put(P.ATTR_TIME, s_delta);
                m.put(P.ATTR_REP, reps_now);
                m.put(P.ATTR_NUMBER, number_now);
                data.add(m);

            } while (cursor.moveToNext());
        }else {
            data.clear();
        }
        String[] from = {P.ATTR_TIME, P.ATTR_REP, P.ATTR_NUMBER};
        int[] to = {R.id.time_item_set_textview, R.id.reps_item_set_textview,
                R.id.mark_item_set_textview};
        //заводим данные в адаптер и присваиваем его встроенному списку ListFragment
        sara = new SimpleAdapter(this, data, R.layout.list_item_set_textview, from, to);
        //устанавливаем свой биндер
        sara.setViewBinder(new MyViewBinder());
        changeTemp_listView.setAdapter(sara);
        //Чтобы сделать что-то при щелчке на галке, нужно расширить адаптер и сделать
        // слушатель внутри View на флажок
    }

    // класс для изменения цвета элемента строки - маркера номера фрагмента подхода
    private class MyViewBinder implements SimpleAdapter.ViewBinder{
        @Override
        public boolean setViewValue(View view, Object o, String s) {

            int i = 0;

            switch (view.getId()) {

                case R.id.mark_item_set_textview:

                    i = ((Integer) o).intValue()-1;

                    if (mCheckBoxAll.isChecked()){
                        view.setBackgroundColor(Color.YELLOW);

                    }else {
                        if (i!= positionOfList) {
                            //оставляем как есть
                            view.setBackgroundResource(R.drawable.rect);

                        }else {
                            //меняем цвет Background
                            view.setBackgroundColor(Color.YELLOW);
                        }
                    }
            }
            //если поставить true? почему то работает неправильно
            return false;
        }
    }
}
