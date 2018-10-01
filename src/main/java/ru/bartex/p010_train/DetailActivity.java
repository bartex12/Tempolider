package ru.bartex.p010_train;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.UUID;

import ru.bartex.p010_train.ru.bartex.p010_train.data.P;

public class DetailActivity extends AppCompatActivity {
    private EditText mTimeOfRepFrag;  //поле для времени между повторами
    private EditText mRepsFrag;  // поле для количества повторениийдля фрагмента подхода
    private EditText mNumberOfFrag;  //порядковый номер фрагмента подхода
    private Button mButtonOk;
    private Button mButtonCancel;

    private static final String TAG = "33333";
    private Set mSet;
    public static final String INTENT_SET_UUID = "DetailActivity.intent_set_uuid";
    public static final String POSITION = "positionDetailActivity";
    static final String DETAIL_REQUEST = "DetailActivity.change_request";

    UUID uuid;
    int position =0;
    Bundle extras;

    //редактируемая запись появляется в списке, только если нажата кнопка Принять
    //кнопка Назад в панели инструментов, кнопка отмена и кнопка Обратно на телефоне - отменяют
    //редактирование строки

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ActionBar act = getSupportActionBar();
        //act.setTitle("Редактирование");

        act.setDisplayHomeAsUpEnabled(true );
        act.setHomeButtonEnabled(true);

        SetLab setLab = SetLab.get();
        //получаем extras из интента
        extras = getIntent().getExtras();
        if(extras != null) {
            //если Изменить
            act.setTitle("Изменить");
            if (extras.getInt(DETAIL_REQUEST) == 111) {
                position = extras.getInt(POSITION);
                mSet = setLab.getSet(position);
                Log.d(TAG, "DetailActivity onCreate position) = " + position);
                Log.d(TAG, "DetailActivity onCreate mSet.getNumberOfFrag() = " + mSet.getNumberOfFrag());
            }
            //если Добавить
            else if (extras.getInt(P.FROM_MAIN) ==P.TO_ADD){
                act.setTitle("Создать");
                //пока так

            }else {
                act.setTitle("Добавить");
                Intent intentUuid =getIntent();
                uuid = (UUID)intentUuid.getSerializableExtra(INTENT_SET_UUID);
                mSet = setLab.getSet(uuid);
                Log.d(TAG, "DetailActivity onCreate uuid = " + uuid);
                Log.d(TAG, "DetailActivity onCreate mSet.getNumberOfFrag() = " + mSet.getNumberOfFrag());
            }
        }

        mButtonOk = (Button) findViewById(R.id.buttonOk);
        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if ((mSet.getTimeOfRep()==0) &&((mSet.getReps()==0))){
                    int number = mSet.getNumberOfFrag();
                    SetLab.removeSet(number);
                }
                //прячем экранную клавиатуру
                takeOffSoftInput();
                //завершаем
                finish();
            }
        });

        mButtonCancel = (Button) findViewById(R.id.buttonCancel);
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(extras != null) {
                    //если Изменить
                    if (extras.getInt(DETAIL_REQUEST) == 111) {
                        //прячем экранную клавиатуру
                        takeOffSoftInput();
                        //завершаем
                        finish();
                        //если Добавить
                    } else {
                        int number = mSet.getNumberOfFrag();
                        SetLab.removeSet(number);
                        //прячем экранную клавиатуру
                        takeOffSoftInput();
                        //завершаем
                        finish();
                    }
                }
            }
        });

        mTimeOfRepFrag = (EditText)findViewById(R.id.time_item_set_editText);
        if (extras.getInt(P.FROM_MAIN) ==P.TO_ADD){
            mTimeOfRepFrag.setText("0");
        }else {
            mTimeOfRepFrag.setText(Float.toString(mSet.getTimeOfRep()));
        }
        mTimeOfRepFrag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //получаем float миллисекунд для времени между повторами из строчки mTimeOfRepFrag
                float ft = getСountMilliSecond(mTimeOfRepFrag);
                //и присваиваем его переменной mTimeOfRep класса Set
                mSet.setTimeOfRep(ft);
                //доступность кнопки Ok, если оба значения ненулевые
                mButtonOk.setEnabled(((mSet.getTimeOfRep()!=0))&&((mSet.getReps()!=0)));

                Log.d(TAG, "countMilliSecond = " + mSet.getTimeOfRep());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mRepsFrag = (EditText)findViewById(R.id.reps_item_set_editText);
        if (extras.getInt(P.FROM_MAIN) ==P.TO_ADD){
            mRepsFrag.setText("0");
        }else {
            mRepsFrag.setText(Integer.toString(mSet.getReps()));
        }
        mRepsFrag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //получаем int количество повторений для фрагмента из строчки mRepsFrag
                int ir = getСountReps(mRepsFrag);
                //и присваиваем его переменной mReps класса Set
                mSet.setReps(ir);
                //доступность кнопки Ok, если оба значения ненулевые
                mButtonOk.setEnabled(((mSet.getTimeOfRep()!=0))&&((mSet.getReps()!=0)));

                Log.d(TAG, "countReps = " + mSet.getReps());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mNumberOfFrag = (EditText)findViewById(R.id.mark_item_set_editText);
        //записываем в поле mNumberOfFrag порядковый номер фрагмента подхода +1 (на экране - с 1)
        //Log.d(TAG, "NumberOfFrag = " + (mSet.getNumberOfFrag() + 1));
        if (extras.getInt(P.FROM_MAIN) ==P.TO_ADD){
            mNumberOfFrag.setText("1");
        }else {
            mNumberOfFrag.setText(Integer.toString(mSet.getNumberOfFrag() + 1));
        }


        //Устанавливаем фокус ввода в поле mTimeOfRepFrag
        mTimeOfRepFrag.requestFocus();
        //Вызываем экранную клавиатуру -метод работает как в 4.0.3, так и в 6.0
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mTimeOfRepFrag, 0);

        //доступность кнопки Ok в момент появления экрана редактирования (если изменить - доступна)
        if ((Integer.parseInt( mTimeOfRepFrag.getText().toString())==0) &&
                ((Integer.parseInt( mRepsFrag.getText().toString())==0))){
            mButtonOk.setEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //взываем действия, аналогичные нажатию на кнопку Отмена(прямой вызов слушателя кнопки)
        mButtonCancel.callOnClick();
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
        getMenuInflater().inflate(R.menu.detail,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            //чтобы работала стрелка Назад, а не происходил крах приложения
            case android.R.id.home:
                Log.d(TAG, "Домой");
                //onBackPressed();
                //взываем действия, аналогичные нажатию на кнопку Отмена (прямой вызов слушателя кнопки)
                mButtonCancel.callOnClick();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //перевоим текст в миллисекунды для времени между повторениями одного фрагмента
    private float getСountMilliSecond(EditText time) {
        String s = time.getText().toString();
        if ((s.equals("")) ||(s.equals("."))) {
            return 0;
        } else return Float.parseFloat(time.getText().toString());
    }

    //переводим текст в цифру для количества повторений одного фрагмента
    private int getСountReps(EditText reps){
        String s = reps.getText().toString();
        if ((s.equals("")) ||(s.equals("."))) {
            return 0;
        }else return Integer.parseInt(reps.getText().toString());
    }

    private void takeOffSoftInput(){
        //прячем экранную клавиатуру
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
