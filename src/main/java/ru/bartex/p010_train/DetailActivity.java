package ru.bartex.p010_train;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
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

import ru.bartex.p010_train.ru.bartex.p010_train.data.P;
import ru.bartex.p010_train.ru.bartex.p010_train.data.TempDBHelper;

public class DetailActivity extends AppCompatActivity {
    private EditText mTimeOfRepFrag;  //поле для времени между повторами
    private EditText mRepsFrag;  // поле для количества повторениийдля фрагмента подхода
    private EditText mNumberOfFrag;  //порядковый номер фрагмента подхода
    private Button mButtonOk;
    private Button mButtonCancel;

    private static final String TAG = "33333";
    public static final String INTENT_SET_UUID = "DetailActivity.intent_set_uuid";
    public static final String POSITION = "positionDetailActivity";
    static final String DETAIL_REQUEST = "DetailActivity.change_request";
    static final String DETAIL_DATA_SET = "DetailActivity.DATA_SET";

    Bundle extras;
    DataSet mDataSet;
    TempDBHelper mDBHelper = new TempDBHelper(this);
    int fragmentCount;  //количество фрагментов подхода
    int fragmentNumber;  //номер фрагмента для Вставить до/после
    long fileId; //id файла, в который добавляем новый фрагмент подхода
    String finishFileName; //имя файла для обратной отправки

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
            //если Изменить из контекстного меню редактора
            if (extras.getInt(P.DETAIL_CHANGE_REQUEST) == P.DETAIL_CHANGE_TEMP_REQUEST_CODE) {
                act.setTitle("Изменить");
                finishFileName = extras.getString(P.FINISH_FILE_NAME);
                mDataSet = (DataSet) extras.getSerializable(P.DETAIL_DATA_SET);
                Log.d(TAG, "DetailActivity DETAIL_CHANGE_TEMP_REQUEST_CODE " +
                        "onCreate mDataSet № = " + mDataSet.getNumberOfFrag());

                //если вставить после  из контекстного меню редактора
            }else if (extras.getInt(P.FROM_ACTIVITY) ==P.TO_INSERT_AFTER_FRAG){
                act.setTitle("Вставить после ");
                fileId = extras.getLong(P.INTENT_TO_DETILE_FILE_ID);
                fragmentNumber = extras.getInt(P.INTENT_TO_DETILE_FILE_POSITION);
                mDataSet = new DataSet();
                mDataSet.setNumberOfFrag(fragmentNumber + 1);

                //если вставить до  из контекстного меню редактора
            }else if (extras.getInt(P.FROM_ACTIVITY) ==P.TO_INSERT_BEFORE_FRAG){
                act.setTitle("Вставить до ");
                fileId = extras.getLong(P.INTENT_TO_DETILE_FILE_ID);
                fragmentNumber = extras.getInt(P.INTENT_TO_DETILE_FILE_POSITION);
                mDataSet = new DataSet();
                mDataSet.setNumberOfFrag(fragmentNumber);

                //если Добавить с тулбара редактора  +
            }else if (extras.getInt(P.FROM_ACTIVITY) ==P.TO_ADD_FRAG){
                act.setTitle("Добавить ");
                fileId = extras.getLong(P.INTENT_TO_DETILE_FILE_ID);
                fragmentCount = mDBHelper.getSetFragmentsCount(fileId);
                mDataSet = new DataSet();
                mDataSet.setNumberOfFrag(fragmentCount + 1);
                Log.d(TAG, "DetailActivity TO_ADD_FRAG " +
                        "onCreate Добавить с тулбара редактора  fileId= " +fileId);
            }
        }

        mButtonOk = (Button) findViewById(R.id.buttonOk);
        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //если Добавить с тулбара редактора  +
               if (extras.getInt(P.FROM_ACTIVITY) == P.TO_ADD_FRAG) {
                   //Добавляем фрагмент подхода
                   mDBHelper.addSet(mDataSet, fileId);
                   Log.d(TAG, "mButtonOk (P.FROM_ACTIVITY) == P.TO_ADD_FRAG ");
                    //посылаем интент для обновления данных на экране
                   Intent intentSave = new Intent();
                   setResult(RESULT_OK, intentSave);

                   //если вставить после  из контекстного меню редактора
               }else if (extras.getInt(P.FROM_ACTIVITY) ==P.TO_INSERT_AFTER_FRAG){
                   //вставляем фрагмент подхода после позиции, на которой сделан щелчок
                   mDBHelper.addSetAfter(mDataSet, fileId, fragmentNumber);
                   Log.d(TAG, "mButtonOk (P.FROM_ACTIVITY) == P.TO_INSERT_AFTER_FRAG ");
                   //посылаем интент для обновления данных на экране
                   Intent intentInsertAfter = new Intent();
                   setResult(RESULT_OK, intentInsertAfter);

                   //если вставить до  из контекстного меню редактора
               }else if (extras.getInt(P.FROM_ACTIVITY) ==P.TO_INSERT_BEFORE_FRAG){
                   //вставляем фрагмент подхода после позиции, на которой сделан щелчок
                   mDBHelper.addSetBefore(mDataSet, fileId, fragmentNumber);
                   Log.d(TAG, "mButtonOk (P.FROM_ACTIVITY) == P.TO_INSERT_BEFORE_FRAG ");
                    //посылаем интент для обновления данных на экране
                   Intent intentInsertBefore = new Intent();
                   setResult(RESULT_OK, intentInsertBefore);

                    //если Изменить из контекстного меню редактора
                }else if(extras.getInt(P.DETAIL_CHANGE_REQUEST) == P.DETAIL_CHANGE_TEMP_REQUEST_CODE){
                   //обновляем фрагмент подхода в базе данных
                   mDBHelper.updateSetFragment(mDataSet);
                    Log.d(TAG, "mButtonOk (P.DETAIL_CHANGE_REQUEST) == P.DETAIL_CHANGE_TEMP_REQUEST_CODE");
                    //посылаем интент чтобы показать иконку Сохранить как
                    Intent intentSaveIcon = new Intent();
                    setResult(RESULT_OK,intentSaveIcon);
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

                //прячем экранную клавиатуру
                takeOffSoftInput();
                //завершаем
                finish();
            }
        });

        mTimeOfRepFrag = (EditText)findViewById(R.id.time_item_set_editText);
        //если Добавить с тулбара редактора  +
        if(extras.getInt(P.FROM_ACTIVITY) == P.TO_ADD_FRAG) {
            mTimeOfRepFrag.setText("0");
            //если вставить после  из контекстного меню редактора
        }else if (extras.getInt(P.FROM_ACTIVITY) ==P.TO_INSERT_AFTER_FRAG){
            mTimeOfRepFrag.setText("0");
            //если вставить до   из контекстного меню редактора
        }else if (extras.getInt(P.FROM_ACTIVITY) ==P.TO_INSERT_BEFORE_FRAG){
            mTimeOfRepFrag.setText("0");
            //если Изменить из контекстного меню редактора
        }else if (extras.getInt(P.DETAIL_CHANGE_REQUEST) == P.DETAIL_CHANGE_TEMP_REQUEST_CODE){
            mTimeOfRepFrag.setText(String.valueOf(mDataSet.getTimeOfRep()));
        }
        mTimeOfRepFrag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //если Изменить из контекстного меню редактора
            if (extras.getInt(P.DETAIL_CHANGE_REQUEST) == P.DETAIL_CHANGE_TEMP_REQUEST_CODE) {
                //получаем float секунд для времени между повторами из строчки mTimeOfRepFrag
                float ft = getCountSecond(mTimeOfRepFrag);
                //и присваиваем его переменной mTimeOfRep класса DataSet
                mDataSet.setTimeOfRep(ft);
                //доступность кнопки Ok, если оба значения ненулевые
                mButtonOk.setEnabled(((mDataSet.getTimeOfRep()!=0))&&((mDataSet.getReps()!=0)));
                Log.d(TAG, " DETAIL_CHANGE_TEMP_REQUEST_CODE countSecond = " + mDataSet.getTimeOfRep() +
                        "countReps = " + mDataSet.getReps());

                //если Добавить с тулбара редактора  +
            }else if (extras.getInt(P.FROM_ACTIVITY) == P.TO_ADD_FRAG){
                //получаем float секунд для времени между повторами из строчки mTimeOfRepFrag
                float ft = getCountSecond(mTimeOfRepFrag);
                //и присваиваем его переменной mTimeOfRep класса Set
                mDataSet.setTimeOfRep(ft);
                //доступность кнопки Ok, если оба значения ненулевые
                mButtonOk.setEnabled(((mDataSet.getTimeOfRep()!=0))&&((mDataSet.getReps()!=0)));
                Log.d(TAG, " TO_ADD_FRAG countSecond = " + mDataSet.getTimeOfRep() +
                        "countReps = " + mDataSet.getReps());

                //если вставить после  из контекстного меню редактора
            }else if (extras.getInt(P.FROM_ACTIVITY) ==P.TO_INSERT_AFTER_FRAG){
                //получаем float секунд для времени между повторами из строчки mTimeOfRepFrag
                float ft = getCountSecond(mTimeOfRepFrag);
                //и присваиваем его переменной mTimeOfRep класса Set
                mDataSet.setTimeOfRep(ft);
                //доступность кнопки Ok, если оба значения ненулевые
                mButtonOk.setEnabled(((mDataSet.getTimeOfRep()!=0))&&((mDataSet.getReps()!=0)));
                Log.d(TAG, " TO_INSERT_AFTER_FRAG countSecond = " + mDataSet.getTimeOfRep() +
                        "countReps = " + mDataSet.getReps());

                //если вставить до   из контекстного меню редактора
            }else if (extras.getInt(P.FROM_ACTIVITY) ==P.TO_INSERT_BEFORE_FRAG){
                //получаем float секунд для времени между повторами из строчки mTimeOfRepFrag
                float ft = getCountSecond(mTimeOfRepFrag);
                //и присваиваем его переменной mTimeOfRep класса Set
                mDataSet.setTimeOfRep(ft);
                //доступность кнопки Ok, если оба значения ненулевые
                mButtonOk.setEnabled(((mDataSet.getTimeOfRep()!=0))&&((mDataSet.getReps()!=0)));
                Log.d(TAG, " TO_INSERT_BEFORE_FRAG countSecond = " + mDataSet.getTimeOfRep() +
                        "countReps = " + mDataSet.getReps());
            }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        mRepsFrag = (EditText)findViewById(R.id.reps_item_set_editText);
        //если Добавить с тулбара редактора  +
        if(extras.getInt(P.FROM_ACTIVITY) == P.TO_ADD_FRAG){
             mRepsFrag.setText("0");
            //если вставить после  из контекстного меню редактора
        }else if (extras.getInt(P.FROM_ACTIVITY) ==P.TO_INSERT_AFTER_FRAG){
            mRepsFrag.setText("0");
            //если вставить до   из контекстного меню редактора
        }else if (extras.getInt(P.FROM_ACTIVITY) ==P.TO_INSERT_BEFORE_FRAG){
            mRepsFrag.setText("0");
            //если Изменить из контекстного меню редактора
        } else if (extras.getInt(P.DETAIL_CHANGE_REQUEST) == P.DETAIL_CHANGE_TEMP_REQUEST_CODE) {
            mRepsFrag.setText(String.valueOf(mDataSet.getReps()));
        }
        mRepsFrag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //если Изменить из контекстного меню редактора
                if (extras.getInt(P.DETAIL_CHANGE_REQUEST) == P.DETAIL_CHANGE_TEMP_REQUEST_CODE) {
                    //получаем int количество повторений для фрагмента из строчки mRepsFrag
                    int ir = getCountReps(mRepsFrag);
                    //и присваиваем его переменной mReps класса Set
                    mDataSet.setReps(ir);
                    //доступность кнопки Ok, если оба значения ненулевые
                    mButtonOk.setEnabled(((mDataSet.getTimeOfRep()!=0))&&((mDataSet.getReps()!=0)));
                    Log.d(TAG, "DETAIL_CHANGE_TEMP_REQUEST_CODE countReps = " + mDataSet.getReps());

                    //если Добавить с тулбара редактора +
                }else if (extras.getInt(P.FROM_ACTIVITY) == P.TO_ADD_FRAG) {
                    //получаем int количество повторений для фрагмента из строчки mRepsFrag
                    int ir = getCountReps(mRepsFrag);
                    //и присваиваем его переменной mReps класса Set
                    mDataSet.setReps(ir);
                    //доступность кнопки Ok, если оба значения ненулевые
                    mButtonOk.setEnabled(((mDataSet.getTimeOfRep() != 0)) && ((mDataSet.getReps() != 0)));
                    Log.d(TAG, " TO_ADD_SET countReps = " + mDataSet.getReps());

                    //если вставить после  из контекстного меню редактора
                }else if (extras.getInt(P.FROM_ACTIVITY) ==P.TO_INSERT_AFTER_FRAG){
                    //получаем int количество повторений для фрагмента из строчки mRepsFrag
                    int ir = getCountReps(mRepsFrag);
                    //и присваиваем его переменной mReps класса Set
                    mDataSet.setReps(ir);
                    //доступность кнопки Ok, если оба значения ненулевые
                    mButtonOk.setEnabled(((mDataSet.getTimeOfRep() != 0)) && ((mDataSet.getReps() != 0)));
                    Log.d(TAG, " TO_INSERT_AFTER_FRAG countReps = " + mDataSet.getReps());

                    //если вставить до   из контекстного меню редактора
                }else if (extras.getInt(P.FROM_ACTIVITY) ==P.TO_INSERT_BEFORE_FRAG){
                    //получаем int количество повторений для фрагмента из строчки mRepsFrag
                    int ir = getCountReps(mRepsFrag);
                    //и присваиваем его переменной mReps класса Set
                    mDataSet.setReps(ir);
                    //доступность кнопки Ok, если оба значения ненулевые
                    mButtonOk.setEnabled(((mDataSet.getTimeOfRep() != 0)) && ((mDataSet.getReps() != 0)));
                    Log.d(TAG, " TO_INSERT_BEFORE_FRAG countReps = " + mDataSet.getReps());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        mNumberOfFrag = (EditText)findViewById(R.id.mark_item_set_editText);
        //записываем в поле mNumberOfFrag порядковый номер фрагмента подхода +1 (на экране - с 1)

        //если Добавить с тулбара редактора +
         if(extras.getInt(P.FROM_ACTIVITY) == P.TO_ADD_FRAG){
           mNumberOfFrag.setText(String.valueOf(fragmentCount + 1));
           Log.d(TAG, " TO_ADD_FRAG mNumberOfFrag = " + (fragmentCount + 1));

             //если вставить после  из контекстного меню редактора
         }else if (extras.getInt(P.FROM_ACTIVITY) ==P.TO_INSERT_AFTER_FRAG){
             mNumberOfFrag.setText(String.valueOf(fragmentNumber + 1));
             Log.d(TAG, " TO_INSERT_AFTER_FRAG щелчок на фрагменте = " + fragmentCount);

             //если вставить до   из контекстного меню редактора
         }else if (extras.getInt(P.FROM_ACTIVITY) ==P.TO_INSERT_BEFORE_FRAG){
             mNumberOfFrag.setText(String.valueOf(fragmentNumber));
             Log.d(TAG, " TO_INSERT_BEFORE_FRAG щелчок на фрагменте = " + fragmentCount);

            //если Изменить из контекстного меню редактора
    }else if (extras.getInt(P.DETAIL_CHANGE_REQUEST) == P.DETAIL_CHANGE_TEMP_REQUEST_CODE){
        mNumberOfFrag.setText(String.valueOf(mDataSet.getNumberOfFrag()));
            Log.d(TAG, " DETAIL_CHANGE_TEMP_REQUEST_CODE mNumberOfFrag = " + mDataSet.getNumberOfFrag());
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
    private float getCountSecond(EditText time) {
        String s = time.getText().toString();
        if ((s.equals("")) ||(s.equals("."))) {
            return 0;
        } else return Float.parseFloat(time.getText().toString());
    }
    //getCountMilliSecond
    //переводим текст в цифру для количества повторений одного фрагмента
    private int getCountReps(EditText reps){
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
