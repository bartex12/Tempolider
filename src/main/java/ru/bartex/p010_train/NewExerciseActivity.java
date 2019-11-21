package ru.bartex.p010_train;

import android.content.Intent;
import android.content.pm.ActivityInfo;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import ru.bartex.p010_train.ru.bartex.p010_train.data.P;
import ru.bartex.p010_train.ru.bartex.p010_train.data.TempDBHelper;

public class NewExerciseActivity extends AppCompatActivity {

    public static final String TAG ="33333";

    EditText fileName;
    EditText delay;

    EditText time1;
    EditText time2;
    EditText time3;
    EditText time4;
    EditText time5;
    EditText reps1;
    EditText reps2;
    EditText reps3;
    EditText reps4;
    EditText reps5;

    Button create;

    TempDBHelper mTempDBHelper = new TempDBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_exercise_con);

        //разрешить только портретную ориентацию экрана
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ActionBar acBar = getSupportActionBar();
        acBar.setTitle(R.string.new_name);
        //показать стрелку Назад
        acBar.setDisplayHomeAsUpEnabled(true );
        acBar.setHomeButtonEnabled(true);

        fileName = (EditText)findViewById(R.id.etFileName);
        delay = (EditText)findViewById(R.id.etDelay);
        time1 = (EditText)findViewById(R.id.time1);
        time2 = (EditText)findViewById(R.id.time2);
        time3 = (EditText)findViewById(R.id.time3);
        time4 = (EditText)findViewById(R.id.time4);
        time5 = (EditText)findViewById(R.id.time5);
        reps1 = (EditText)findViewById(R.id.reps1);
        reps2 = (EditText)findViewById(R.id.reps2);
        reps3 = (EditText)findViewById(R.id.reps3);
        reps4 = (EditText)findViewById(R.id.reps4);
        reps5 = (EditText)findViewById(R.id.reps5);

        create = (Button)findViewById(R.id.buttonCreate);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int delayInt = Integer.parseInt(delay.getText().toString());

                float time1Float = Float.parseFloat(time1.getText().toString());
                float time2Float = Float.parseFloat(time2.getText().toString());
                float time3Float = Float.parseFloat(time3.getText().toString());
                float time4Float = Float.parseFloat(time4.getText().toString());
                float time5Float = Float.parseFloat(time5.getText().toString());

                int reps1Int = Integer.parseInt(reps1.getText().toString());
                int reps2Int = Integer.parseInt(reps2.getText().toString());
                int reps3Int = Integer.parseInt(reps3.getText().toString());
                int reps4Int = Integer.parseInt(reps4.getText().toString());
                int reps5Int = Integer.parseInt(reps5.getText().toString());

                float[] timeArray = {time1Float,time2Float,time3Float,time4Float,time5Float};
                int[] repsArray = {reps1Int,reps2Int,reps3Int,reps4Int,reps5Int};

                double resalt = 0;
                for (int i=0; i<timeArray.length; i++){
                    resalt += timeArray[i]*repsArray[1];
                }

                String fileNameStr = fileName.getText().toString();
                long fileId = mTempDBHelper.getIdFromFileName(fileNameStr);
                Log.d(TAG, "fileNameStr = " +fileNameStr + "  fileId = " +fileId);

                //если имя - пустая строка
                if (fileNameStr.trim().isEmpty()){
                    Snackbar.make(v, R.string.InputNameOfSchelule, Snackbar.LENGTH_LONG)
                           .setAction("Action", null).show();
                    Log.d(TAG, "Введите непустое имя раскладки ");
                    return;

                    //если такое имя уже есть в базе
                }else if (fileId != -1) {
                    Snackbar.make(v, R.string.InputAnotherName, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Log.d(TAG, "Такое имя уже существует. Введите другое имя. fileId = " +fileId);
                    return;

                    //если во всех фрагментах есть нулевые поля
                }else  if (resalt == 0) {
                        Snackbar.make(v, "Заполните хотя бы один фрагмент подхода.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        Log.d(TAG, "Заполните хотя бы один фрагмент подхода  resalt = " + resalt);
                    return;

                    //если имя не повторяется, оно не пустое и заполнен хотя бы один фрагмент
                    }else {
                         Log.d(TAG, "Такое имя отсутствует fileId = " + fileId);
                        //получаем дату и время в нужном для базы данных формате
                        String dateFormat = mTempDBHelper.getDateString();
                        String timeFormat = mTempDBHelper.getTimeString();

                        //создаём экземпляр класса DataFile в конструкторе
                        DataFile file = new DataFile(fileNameStr,
                                dateFormat, timeFormat, null,
                                null, P.TYPE_LIKE, delayInt);
                        //добавляем запись в таблицу TabFile, используя данные DataFile и получаем id записи
                        long file1_id = mTempDBHelper.addFile(file);
                        Log.d(TAG, "Добавили   fileNameStr = " + fileNameStr + " file1_id = " + file1_id);

                        int j = 1;
                        for (int i = 0; i<timeArray.length; i++){
                            if ((timeArray[i]!=0)&&(repsArray[i]!=0)){
                                DataSet set = new DataSet(timeArray[i],repsArray[i], j);
                                mTempDBHelper.addSet(set, file1_id);
                                j++;
                            }
                        }
                        mTempDBHelper.rerangeSetFragments(file1_id);
                        Log.d(TAG, "MyDatabaseHelper.create count = " +
                                mTempDBHelper.getSetFragmentsCount(file1_id));
                    }

                    Intent intent = new Intent(NewExerciseActivity.this, SetListActivity.class);
                    intent.putExtra(P.FINISH_FILE_NAME, fileNameStr);
                    intent.putExtra(P.FROM_ACTIVITY, P.NEW_EXERCISE_ACTIVITY);
                    startActivity(intent);
                }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.pacemaker,menu);
        Log.d(TAG, "onCreateOptionsMenu");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                Log.d(TAG, "Домой");
                Intent intentHome = new Intent(this,MainActivity.class);
                startActivity(intentHome);
                finish();
                return true;

            case R.id.action_settings:
                //вызываем ListOfFilesActivity
                Intent intentPref = new Intent(getBaseContext(), PrefActivity.class);
                startActivity(intentPref);
                //finish();  //не нужно
                return true;


        }
        return super.onOptionsItemSelected(item);
    }

}
