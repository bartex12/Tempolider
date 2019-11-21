package ru.bartex.p010_train;

import android.content.Context;
import android.media.AudioManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HelpMainActivity extends AppCompatActivity {

    private static final String TAG = "33333";
    TextView tvHelp;
    TextView tvTittle;
    ImageView left;
    ImageView right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_main);

        ActionBar acBar = getSupportActionBar();
        acBar.setTitle("");
        //показать стрелку Назад
        acBar.setDisplayHomeAsUpEnabled(true );
        acBar.setHomeButtonEnabled(true);

        tvHelp = (TextView) findViewById(R.id.textViewHelpMain);

        InputStream iFile = getResources().openRawResource(R.raw.help_trener_plus);
        StringBuilder strFile = inputStreamToString(iFile);
        tvHelp.setText(strFile);

        left = (ImageView)findViewById(R.id.imageView2);
        left.setImageResource(R.drawable.help_magistr);

        right = (ImageView)findViewById(R.id.imageView3);
        right.setImageResource(R.drawable.help_magistr);
    }

    private StringBuilder inputStreamToString(InputStream iFile) {
        StringBuilder strFull = new StringBuilder();
        String str = "";
        try {
            // открываем поток для чтения
            InputStreamReader ir = new InputStreamReader(iFile);
            BufferedReader br = new BufferedReader(ir);
            // читаем содержимое
            while ((str = br.readLine()) != null) {
                //Log.d(TAG, str);
                //Чтобы не было в одну строку, ставим символ новой строки
                strFull.append(str + "\n");
            }
            //закрываем потоки
            iFile.close();
            ir.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strFull;
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
        getMenuInflater().inflate(R.menu.activity_help_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            //чтобы работала стрелка Назад, а не происходил крах приложения
            case android.R.id.home:
                Log.d(TAG, "Домой");
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}