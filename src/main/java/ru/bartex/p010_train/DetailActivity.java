package ru.bartex.p010_train;

import android.content.Context;
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
import ru.bartex.p010_train.ru.bartex.p010_train.data.TempDBHelper;

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
    static final String DETAIL_DATA_SET = "DetailActivity.DATA_SET";

    Bundle extras;
    DataSet mDataSet;
    TempDBHelper mDBHelper = new TempDBHelper(this);
    int fragmentCount;  //количество фрагментов подхода
    long fileId; //id файла, в который добавляем новый фрагмент подхода

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

        //получаем extras из интента
        extras = getIntent().getExtras();
        if(extras != null) {

            //если Изменить из контекстного меню темполидера
            if (extras.getInt(P.DETAIL_CHANGE_REQUEST) == P.DETAIL_CHANGE_REQUEST_KODE) {
                act.setTitle("Изменить");
                mDataSet = (DataSet) extras.getSerializable(P.DETAIL_DATA_SET);
                Log.d(TAG, "DetailActivity onCreate mDataSet № = " + mDataSet.getNumberOfFrag());

                //если Добавить с тулбара темполидера +
            }else if (extras.getInt(P.FROM_ACTIVITY) ==P.TO_ADD_SET){
                act.setTitle("Добавить ");
                fileId = extras.getLong(P.INTENT_TO_DETILE_FILE_ID);
                fragmentCount = mDBHelper.getSetFragmentsCount(fileId);
                mDataSet = new DataSet();
                mDataSet.setNumberOfFrag(fragmentCount + 1);

                //если плавающая кнопка из главного меню
            }else if (extras.getInt(P.FROM_MAIN) ==P.TO_ADD){
                act.setTitle("Создать");
            }
        }

        mButtonOk = (Button) findViewById(R.id.buttonOk);
        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //если плавающая кнопка из главного меню
                if (extras.getInt(P.FROM_MAIN) == P.TO_ADD){
                    Log.d(TAG, "mButtonOk (P.FROM_MAIN) == P.TO_ADD ");

                    //если Добавить с тулбара темполидера +
                }else if (extras.getInt(P.FROM_ACTIVITY) == P.TO_ADD_SET){
                    //Добавляем фрагмент подхода
                    mDBHelper.addSet(mDataSet, fileId);
                    Log.d(TAG, "mButtonOk (P.FROM_ACTIVITY) == P.TO_ADD_SET ");

                    //если Изменить из контекстного меню темполидера
                }else if (extras.getInt(P.DETAIL_CHANGE_REQUEST) == P.DETAIL_CHANGE_REQUEST_KODE){
                    //обновляем фрагмент подхода в базе данных
                    mDBHelper.updateSetFragment(mDataSet);
                    Log.d(TAG, "mButtonOk (P.DETAIL_CHANGE_REQUEST) == P.DETAIL_CHANGE_REQUEST_KODE ");
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
                        //int number = mSet.getNumberOfFrag();
                        //SetLab.removeSet(number);
                        //прячем экранную клавиатуру
                        takeOffSoftInput();
                        //завершаем
                        finish();
                    }
                }
            }
        });

        mTimeOfRepFrag = (EditText)findViewById(R.id.time_item_set_editText);
        //если плавающая кнопка из главного меню
        if (extras.getInt(P.FROM_MAIN) == P.TO_ADD){
            mTimeOfRepFrag.setText("0");
            //если Добавить с тулбара темполидера +
        }else if (extras.getInt(P.FROM_ACTIVITY) == P.TO_ADD_SET){
            mTimeOfRepFrag.setText("0");
            //если Изменить из контекстного меню темполидера
        }else if (extras.getInt(P.DETAIL_CHANGE_REQUEST) == P.DETAIL_CHANGE_REQUEST_KODE){
            mTimeOfRepFrag.setText(String.valueOf(mDataSet.getTimeOfRep()));
        }
        mTimeOfRepFrag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //если плавающая кнопка из главного меню
                if (extras.getInt(P.FROM_MAIN) == P.TO_ADD){

                    //если Изменить из контекстного меню темполидера
                }else if (extras.getInt(P.DETAIL_CHANGE_REQUEST) == P.DETAIL_CHANGE_REQUEST_KODE) {

                    //получаем float миллисекунд для времени между повторами из строчки mTimeOfRepFrag
                    float ft = getСountMilliSecond(mTimeOfRepFrag);
                    //и присваиваем его переменной mTimeOfRep класса Set
                    mDataSet.setTimeOfRep(ft);
                    //доступность кнопки Ok, если оба значения ненулевые
                    mButtonOk.setEnabled(((mDataSet.getTimeOfRep()!=0))&&((mDataSet.getReps()!=0)));
                    Log.d(TAG, "countSecond = " + mDataSet.getTimeOfRep() +
                            "countReps = " + mDataSet.getReps());

                    //если Добавить с тулбара темполидера +
            }else if (extras.getInt(P.FROM_ACTIVITY) == P.TO_ADD_SET){
                    //получаем float миллисекунд для времени между повторами из строчки mTimeOfRepFrag
                    float ft = getСountMilliSecond(mTimeOfRepFrag);
                    //и присваиваем его переменной mTimeOfRep класса Set
                    mDataSet.setTimeOfRep(ft);
                    //доступность кнопки Ok, если оба значения ненулевые
                    mButtonOk.setEnabled(((mDataSet.getTimeOfRep()!=0))&&((mDataSet.getReps()!=0)));
                    Log.d(TAG, "countSecond = " + mDataSet.getTimeOfRep() +
                            "countReps = " + mDataSet.getReps());
            }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        mRepsFrag = (EditText)findViewById(R.id.reps_item_set_editText);
        //если плавающая кнопка из главного меню
        if (extras.getInt(P.FROM_MAIN) ==P.TO_ADD){
            mRepsFrag.setText("0");
            //если Добавить с тулбара темполидера +
        }else if (extras.getInt(P.FROM_ACTIVITY) == P.TO_ADD_SET) {
            mRepsFrag.setText("0");
            //если Изменить из контекстного меню темполидера
        } else if (extras.getInt(P.DETAIL_CHANGE_REQUEST) == P.DETAIL_CHANGE_REQUEST_KODE) {
            mRepsFrag.setText(String.valueOf(mDataSet.getReps()));
        }
        mRepsFrag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //если плавающая кнопка из главного меню
                if (extras.getInt(P.FROM_MAIN) == P.TO_ADD){

                    //если Изменить из контекстного меню темполидера
                }else if (extras.getInt(P.DETAIL_CHANGE_REQUEST) == P.DETAIL_CHANGE_REQUEST_KODE) {
                    //получаем int количество повторений для фрагмента из строчки mRepsFrag
                    int ir = getСountReps(mRepsFrag);
                    //и присваиваем его переменной mReps класса Set
                    mDataSet.setReps(ir);
                    //доступность кнопки Ok, если оба значения ненулевые
                    mButtonOk.setEnabled(((mDataSet.getTimeOfRep()!=0))&&((mDataSet.getReps()!=0)));
                    Log.d(TAG, "countReps = " + mDataSet.getReps());

                    //если Добавить с тулбара темполидера +
                }else if (extras.getInt(P.FROM_ACTIVITY) == P.TO_ADD_SET){
                    //получаем int количество повторений для фрагмента из строчки mRepsFrag
                    int ir = getСountReps(mRepsFrag);
                    //и присваиваем его переменной mReps класса Set
                    mDataSet.setReps(ir);
                    //доступность кнопки Ok, если оба значения ненулевые
                    mButtonOk.setEnabled(((mDataSet.getTimeOfRep()!=0))&&((mDataSet.getReps()!=0)));
                    Log.d(TAG, "countReps = " + mDataSet.getReps());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        mNumberOfFrag = (EditText)findViewById(R.id.mark_item_set_editText);
        //записываем в поле mNumberOfFrag порядковый номер фрагмента подхода +1 (на экране - с 1)

        //если плавающая кнопка из главного меню
        if (extras.getInt(P.FROM_MAIN) ==P.TO_ADD){
            mNumberOfFrag.setText("1");

            //если Добавить с тулбара темполидера +
        }else if (extras.getInt(P.FROM_ACTIVITY) == P.TO_ADD_SET){
            mNumberOfFrag.setText(String.valueOf(fragmentCount+1));

         //если Изменить из контекстного меню темполидера
    }else if (extras.getInt(P.DETAIL_CHANGE_REQUEST) == P.DETAIL_CHANGE_REQUEST_KODE){
        mNumberOfFrag.setText(String.valueOf(mDataSet.getNumberOfFrag()));
        }

        //Устанавливаем фокус ввода в поле mTimeOfRepFrag
        mTimeOfRepFrag.requestFocus();
        //Вызываем экранную клавиатуру -метод работает как в 4.0.3, так и в 6.0
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mTimeOfRepFrag, 0);

        //доступность кнопки Ok в момент появления экрана редактирования (если изменить - доступна)
        if ((Float.parseFloat( mTimeOfRepFrag.getText().toString())==0) &&
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
