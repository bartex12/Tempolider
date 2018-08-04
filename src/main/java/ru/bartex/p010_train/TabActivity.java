package ru.bartex.p010_train;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TabActivity extends AppCompatActivity {

    static String TAG = "33333";

    TabHost host;
    ListView mListViewSec;
    ListView mListViewTemp;
    ListView mListViewLike;
    ArrayList<String> tempFiles = new ArrayList<>();
    ArrayList<String> tempFilesSec = new ArrayList<>();
    ArrayList<String> tempFilesTemp = new ArrayList<>();
    ArrayList<String> tempFilesLike = new ArrayList<>();

    ArrayAdapter<String> araFilesSec;
    ArrayAdapter<String> araFilesTemp;
    ArrayAdapter<String> araFilesLike;
    ArrayAdapter<String> araFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        setTitle("Раскладки");

        host = (TabHost) findViewById(R.id.TabHost1);
        host.setup();

        //Добавляем вкладку, исппользуя экземпляр вложенного класса TabSpec
        TabHost.TabSpec nameSecTab = host.newTabSpec("Sec");
        nameSecTab.setIndicator(("Секундомер"), getResources().getDrawable(android.R.drawable.star_on));
        nameSecTab.setContent(R.id.listFileSec);
        host.addTab(nameSecTab);

        //Добавляем вкладку, исппользуя экземпляр вложенного класса TabSpec
        TabHost.TabSpec nameTempTab = host.newTabSpec("Temp");
        nameTempTab.setIndicator(("Темполидер"), getResources().getDrawable(android.R.drawable.star_on));
        nameTempTab.setContent(R.id.listFileTemp);
        host.addTab(nameTempTab);

        //Добавляем вкладку, исппользуя экземпляр вложенного класса TabSpec
        TabHost.TabSpec nameLikeTab = host.newTabSpec("Like");
        nameLikeTab.setIndicator(("Избранное"), getResources().getDrawable(android.R.drawable.star_on));
        nameLikeTab.setContent(R.id.listFileLike);
        host.addTab(nameLikeTab);

        //выводит заданную вкладку на передний план
        host.setCurrentTabByTag("Sec");

        mListViewSec = (ListView)findViewById(R.id.listFileSec);
        mListViewTemp = (ListView)findViewById(R.id.listFileTemp);
        mListViewLike = (ListView)findViewById(R.id.listFileLike);

        FileSaverLab mFileSaverLab = FileSaverLab.get();
        mFileSaverLab.getFileSavers().clear();
        tempFiles = readArrayList(SingleFragmentActivity.FILENAME_NAMES_OF_FILES);
        //парсим данные и пишем их в синглет-держатель списка имён с файлами
        for (int i = 0; i<tempFiles.size(); i++){
            String oneLine = tempFiles.get(i);
            String[] allValues = Stat.getDataFromString(oneLine);
            //пишем данные в FileSaver
            FileSaver newSaver = new FileSaver();
            newSaver.setTitle(allValues[0]);
            newSaver.setDate(allValues[1]);
            newSaver.setTipe(allValues[2]);
            //Добавляем Set в SetLab
            mFileSaverLab.addFileSaver(newSaver);
        }
        //получаем списки имён файлов по типам а значит и по вкладкам Tab
        List<FileSaver> fileSavers =  mFileSaverLab.getFileSavers();

        for (int i = 0; i< fileSavers.size(); i++){
            FileSaver fs = fileSavers.get(i);
            String s = fs.getTipe();
            if (s.equalsIgnoreCase("type_timemeter")){
                tempFilesSec.add(fs.getTitle());
                Log.d(TAG, "tempFilesSec = " + fs.getTitle());
            }else if (s.equalsIgnoreCase("type_tempoleader")){
                tempFilesTemp.add(fs.getTitle());
                Log.d(TAG, "tempFilesTemp = " + fs.getTitle());
            }else if (s.equalsIgnoreCase("type_like")){
                tempFilesLike.add(fs.getTitle());
                Log.d(TAG, "tempFilesLike = " + fs.getTitle());
            }
        }
        Log.d(TAG, "tempFilesSec размер  = " + tempFilesSec);
        Log.d(TAG, "tempFilesTemp размер  = " + tempFilesTemp);
        Log.d(TAG, "tempFilesLike размер  = " + tempFilesLike);

        araFilesSec = new ArrayAdapter<String>(this,
                R.layout.activity_list_of_files_item,tempFilesSec);
        araFilesTemp = new ArrayAdapter<String>(this,
               R.layout.activity_list_of_files_item,tempFilesTemp);
        araFilesLike = new ArrayAdapter<String>(this,
                R.layout.activity_list_of_files_item,tempFilesLike);

        mListViewSec.setAdapter(araFilesSec);

        // обработчик переключения вкладок
        host.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            public void onTabChanged(String tabId) {
                switch (tabId){
                    case "Sec":
                        mListViewSec.setAdapter(araFilesSec);
                    case "Temp":
                        mListViewTemp.setAdapter(araFilesTemp);
                    case "Like":
                        mListViewLike.setAdapter(araFilesLike);
                }
            }
        });
    }


    //Прочитать список имён с данными из файла
    private ArrayList<String> readArrayList(String fileName) {

        ArrayList<String> newArrayList = new ArrayList<String>();

        try {
            // открываем поток для чтения
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    openFileInput(fileName)));
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    newArrayList.add(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return newArrayList;
    }
}
