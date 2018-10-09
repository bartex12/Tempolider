package ru.bartex.p010_train;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
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

    List<Set> listSetCopy; //резервный список отсечек

    @Override
    public void changeTempUpDown(int valueDelta, boolean up) {

        //если повысить темп up = true
        float ff = (up == true) ? (1 - ((float)valueDelta/100)) : (1 + ((float)valueDelta/100));
        Log.d(TAG, "ChangeTempActivity changeTempUpDown ff = " + ff);

        List<Set> sets = SetLab.getSets();
        for (Set s:sets){
            s.setTimeOfRep((s.getTimeOfRep())*ff);
        }

        updateAdapter(changeTemp_listView);
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
        positionOfList = intent.getIntExtra(P.CHANGE_TEMP_POSITION,0);
        pos = positionOfList;
        Log.d(TAG, "ChangeTempActivity positionOfList = " + (positionOfList+1));

        //сделаем копию списка отсечек для случая отмены изменений
        SetLab setLab = SetLab.get();
        listSetCopy = setLab.getSetListCopy();
        //без этой строки возврат почему-то не работает!!! а listSetCopy обновляется сам
        SetLab.returnToOldSetList(listSetCopy);

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

                updateAdapter(changeTemp_listView);
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

                updateAdapter(changeTemp_listView);
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
        changeTemp_textViewName.setText("Имя:  " + intent.getStringExtra(NAME_OF_FILE));

        timeTotal = (TextView)findViewById(R.id.timeTotal);
        repsTotal = (TextView)findViewById(R.id.repsTotal);

        changeTemp_buttonMinus5 = (Button) findViewById(R.id.changeTemp_buttonMinus5);
        changeTemp_buttonMinus5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //String s = reductAction(0.95f,-5);
                reductAction(0.95f,-5);
                updateAdapter(changeTemp_listView);
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
                updateAdapter(changeTemp_listView);
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

                //возвращаемся к старому списку отсечек
                SetLab.returnToOldTimeOrReps(listSetCopy,redactTime,
                        mCheckBoxAll.isChecked(),positionOfList);

                updateAdapter(changeTemp_listView);
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
                updateAdapter(changeTemp_listView);
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
                updateAdapter(changeTemp_listView);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "ChangeTempActivity onResume");

        //выводим список, суммарные время и количество, устанавливаем выделение цветом
        updateAdapter(changeTemp_listView);
        calculateAndShowTotalValues();
        changeTemp_listView.setSelection(positionOfList);
    }

    @Override
    public View findViewById(@IdRes int id) {
        return super.findViewById(id);

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


    public void updateAdapter(ListView listView) {
        Log.d(TAG, "ChangeTempActivity: updateAdapter() ");
        //получаем экземпляр SetLab
        SetLab setLab = SetLab.get();
        //получаем список объектов класса Set
        List<Set> sets =  setLab.getSets();
        //получаем размер списка
        int array_size = sets.size();
        //готовим данные для SimpleAdapter
        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(array_size);
        Map<String,Object> m;
        for (int i = 0; i < array_size; i++) {
            Set set = sets.get(i);
            float time  = set.getTimeOfRep();
            String timeFormat = String.format("%.2f",time);
            m = new HashMap<>();
            m.put(ATTR_TIME, timeFormat);
            m.put(ATTR_REP, set.getReps());
            m.put(ATTR_NUMBER, set.getNumberOfFrag()+1); //чтобы начинался список  с 1, а не с 0
            data.add(m);
        }
        String[] from = {ATTR_TIME, ATTR_REP, ATTR_NUMBER};
        int[] to = {R.id.time_item_set_textview, R.id.reps_item_set_textview,
                R.id.mark_item_set_textview};
        //заводим данные в адаптер и присваиваем его встроенному списку ListFragment
        SimpleAdapter sara = new SimpleAdapter(this, data, R.layout.list_item_set_textview, from, to);
        //устанавливаем свой биндер
        sara.setViewBinder(new MyViewBinder());
        //присваиваем адаптер списку
        listView.setAdapter(sara);
        //проверяем состояние listSetCopy
        for (Set set:listSetCopy){
            float time = set.getTimeOfRep();
            int reps = set.getReps();
            int i = set.getNumberOfFrag();
            Log.d(TAG, "ChangeTempActivity updateAdapter listSetCopy = " +
                    time + "  " + reps + "  " + (i+1));
    }
    }

    private void calculateAndShowTotalValues(){

        //посчитаем общее врямя выполнения подхода в секундах
        float mTimeOfSet = SetLab.countSetTime();
        String totalTime = Stat.countTotalTime(mTimeOfSet, SingleFragmentActivity.mKvant);
        timeTotal.setText(totalTime);
        //посчитаем общее количество повторений в подходе
        int  mTotalReps = SetLab.countTotalReps();
        // общее количество повторений в подходе
        String totalReps = String.format("Количество  %d", mTotalReps);
        repsTotal.setText(totalReps);
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

    private String reductAction(float ff, int ii){

        String delta = "";
        if (redactTime){
            time += (ff-1f)*100;
            if (mCheckBoxAll.isChecked()){
                List<Set> sets = SetLab.getSets();
                for (Set s:sets){
                    s.setTimeOfRep((s.getTimeOfRep())*ff);
                }
            }else {
                Set set = SetLab.getSet(positionOfList);
                set.setTimeOfRep((set.getTimeOfRep())*ff);
            }
            delta = String.format("%+3.0f", time);

        }else {
            count +=ii;
            if (mCheckBoxAll.isChecked()){
                List<Set> sets = SetLab.getSets();
                for (Set s:sets){
                    s.setReps((s.getReps()+ii));
                    if (s.getReps()<=0)  s.setReps(0);
                }
            }else {
                Set set = SetLab.getSet(positionOfList);
                set.setReps((set.getReps()+ii));
                if (set.getReps()<=0)  set.setReps(0);
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


}
