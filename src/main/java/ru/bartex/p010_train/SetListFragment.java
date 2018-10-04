package ru.bartex.p010_train;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ru.bartex.p010_train.ru.bartex.p010_train.data.TabSet;
import ru.bartex.p010_train.ru.bartex.p010_train.data.TempDBHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class SetListFragment extends Fragment {

    private static final String TAG = "33333";
    private static final int DELETE_ID = 1;
    private static final int CHANGE_ID = 2;
    private static final int CANCEL_ID = 3;
    private static final int CHANGE_TEMP_ID = 4;

    static final int request_code = 111;

    Set mSet;
    SetLab mSetLab;
    List<Set> mSets;
    int array_size;
    ArrayList<Map<String, Object>> data;
    Map<String,Object> m;
    SimpleAdapter sara;
    ListView mListView;

    private float mTimeOfSet = 0;   //общее время выполнения подхода в секундах
    private int mTotalReps = 0;  //общее количество повторений в подходе
    //private final long mKvant = 100;//время в мс между срабатываниями TimerTask
    private int mCountFragment = 0;  //номер фрагмента подхода
    boolean end = false;  //признак окончания работы

    final String ATTR_TIME = "time";
    final String ATTR_REP = "rep";
    final String ATTR_NUMBER = "number";

    private static final String ARG_NAME_OF_FILE = "NameOfFile";
    private String finishFileName = FileSaver.FINISH_FILE_NAME;

    TempDBHelper mTempDBHelper;


    public interface OnShowTotalValuesListener {
        void onShowTotalValues( String time,String reps);
    }

    private OnShowTotalValuesListener mShowTotalValuesListener;

    public static SetListFragment newInstance(String nameOfFile){
        Bundle args = new Bundle();
        args.putString(ARG_NAME_OF_FILE,nameOfFile);
        SetListFragment fragment = new SetListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SetListFragment() {
        // Required empty public constructor
    }
//onCreateView использовать не нужно, так как extends ListFragment предполагает, что где-то
//внутри есть список (ListView),  и метод setListAdapter сам знает, как до него добраться.
// В принципе, это и есть основная фишка ListFragment - нам не надо работать с ListView.
//подробности:
// http://startandroid.ru/ru/uroki/vse-uroki-spiskom/179-urok-109-android-3-fragments-listfragment-spisok.html

//если используется своя разметка для списка, тогда её НУЖНО определять в onCreateView
    //http://developer.alexanderklimov.ru/android/listfragment.php
    //Тогда в шаблоне для списка нужно разместить ListView с обязательным идентификатором @id/android:list.
    // Компонент TextView будет показан в том случае, если нет данных для списка.
    // Он также должен иметь обязательный идентификатор @id/android:empty.


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mShowTotalValuesListener = (OnShowTotalValuesListener)context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "SetListFragment: onCreate  ");

        finishFileName = getArguments().getString(ARG_NAME_OF_FILE);
        //для фрагментов требуется так разрешить появление меню
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTempDBHelper = new TempDBHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_set_list, container, false);
        Log.d(TAG, "SetListFragment: onCreateView  ");
        //находим список фрагмента по id
        mListView = v.findViewById(R.id.listViewFrag);
        //накладываем жёлтый задний фон строки списка fragment_set_list
        // на градиентный фон макета разметки  самОй строки list_item_set_textview
        mListView.setBackgroundColor(Color.YELLOW);

        //находим View, которое выводит текст Список пуст
        View empty = v.findViewById(R.id.emptyList);
        mListView.setEmptyView(empty);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //если в активности нажата Старт = true , ничего не делать
                if (SingleFragmentActivity.start){
                    Log.d(TAG, "SetListFragment onItemClick  start = " + SingleFragmentActivity.start);
                    Toast.makeText(getContext(),
                            "Сначала нажмите Стоп", Toast.LENGTH_SHORT).show();

                    //если в активности нажата Стоп, то Старт = false
                }else{
                    Intent intent = new Intent(getContext(), DetailActivity.class);
                    intent.putExtra(DetailActivity.POSITION, i);
                    intent.putExtra(DetailActivity.DETAIL_REQUEST, request_code);
                    Log.d(TAG, "SetListFragment onItemClick   i = " + i);
                    startActivity(intent);
                }
            }
        });
        //объявляем о регистрации контекстного меню
        registerForContextMenu(mListView);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "SetListFragment: onStart  ");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "SetListFragment: onResume  ");

        //readArrayList(SingleFragmentActivity.FILENAME_OTSECHKI);
        //обновляем данные списка фрагмента активности
        //если здесь не обновлять, то список не обновляется при возврате из DetailActivity
        updateAdapter();
        //вычисляем и показываем общее время выполнения подхода и количество повторов в подходе
        //calculateAndShowTotalValues();
    }
    
    //создаём контекстное меню для списка (сначала регистрация нужна в onCreateView)
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, "Удалить строку");
        menu.add(0, CHANGE_ID, 0, "Изменить строку");
        menu.add(0, CHANGE_TEMP_ID, 0, "Изменить раскладку");
        menu.add(0, CANCEL_ID, 0, "Отмена");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // получаем инфу о пункте списка
        final AdapterView.AdapterContextMenuInfo acmi =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        //если в активности нажата Старт = true , ничего не делать
        if (SingleFragmentActivity.start) {
            Log.d(TAG, "SetListFragment onItemClick  start = " + SingleFragmentActivity.start);
            Toast.makeText(getContext(),
                    "Сначала нажмите Стоп", Toast.LENGTH_SHORT).show();
        }else {
            //если выбран пункт Удалить запись
            if (item.getItemId() == DELETE_ID) {
                Log.d(TAG, "SetListFragment CM_DELETE_ID");

                AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getContext());
                deleteDialog.setTitle("Удалить: Вы уверены?");
                deleteDialog.setPositiveButton("Нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                deleteDialog.setNegativeButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // находим позицию строки, на которую сделано нажатие
                        int pos = acmi.position;
                        //получаем список
                        mSets = SetLab.getSets();
                        // удаляем нажатую строку
                        mSets = SetLab.removeSet(pos);
                        //пересчитывваем номера фрагментов подхода
                        SetLab.reRangeSet(pos);
                        //обновляем данные списка фрагмента активности
                        updateAdapter();
                        //вычисляем и показываем общее время выполнения подхода и количество повторов в подходе
                        calculateAndShowTotalValues();
                    }
                });

                deleteDialog.show();
                return true;

                //если выбран пункт Изменить запись
            } else if (item.getItemId() == CHANGE_ID) {

                Intent intent = new Intent(getContext(), DetailActivity.class);
                intent.putExtra(DetailActivity.POSITION, acmi.position);
                intent.putExtra(DetailActivity.DETAIL_REQUEST, request_code);
                Log.d(TAG, "SetListFragment CM_CHANGE_ID acmi.position = " + acmi.position);
                startActivity(intent);
                return true;

            } else if (item.getItemId() == CHANGE_TEMP_ID) {

                Log.d(TAG, "SetListFragment getCheckedItemPosition = " +
                        mListView.getCheckedItemPosition());
                //ищем в активности TextView с именем файла
                TextView tv = getActivity().findViewById(R.id.textViewName);
                //читаем имя
                String name = tv.getText().toString();
                Intent intent = new Intent(getContext(), ChangeTempActivity.class);
                //передаём позицию списка, на которой сделано нажатие
                intent.putExtra(ChangeTempActivity.POSITION, acmi.position);
                //передаём имя файла
                intent.putExtra(ChangeTempActivity.NAME_OF_FILE, name);
                //передаём request_code
                intent.putExtra(ChangeTempActivity.CHANGE_REQUEST, request_code);
                Log.d(TAG, "SetListFragment CHANGE_TEMP_ID acmi.position = " + acmi.position);
                startActivity(intent);
                return true;
            }
        }
        //если ничего не выбрано
        return super.onContextItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "SetListFragment: onPause  ");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "SetListFragment: onStop  ");
    }

    public void updateAdapter() {
        Log.d(TAG, "SetListFragment: updateAdapter() ");
        //получаем экземпляр SetLab
        //mSetLab = SetLab.get();
        //получаем список объектов класса Set
        //mSets =  mSetLab.getSets();
        //получаем размер списка
        //array_size = mSets.size();



            //получаем id записи с таким именем
            long finishFileId = mTempDBHelper.getIdFromFileName (finishFileName);
            Log.d(TAG,"SetListFragment  имя =" + finishFileName + "  Id = " + finishFileId );

            //получаем курсор с данными подхода с id = finishFileId
            Cursor cursor = mTempDBHelper.getAllSetFragments(finishFileId);
            //Список с данными для адаптера
            data = new ArrayList<Map<String, Object>>(cursor.getCount());
            //проходим по курсору и берём данные
            if (cursor.moveToFirst()) {
                do {
                    float time  = cursor.getFloat(cursor.getColumnIndex(TabSet.COLUMN_SET_TIME));
                    String timeFormat = String.format("%.2f",time);
                    int reps_now = cursor.getInt(cursor.getColumnIndex(TabSet.COLUMN_SET_REPS));
                    int number_now = cursor.getInt(cursor.getColumnIndex(TabSet.COLUMN_SET_FRAG_NUMBER));

                    Log.d(TAG,"SetListFragment time_now = " + timeFormat +
                            "  reps_now = " + reps_now + "  number_now = " + number_now);

                    m = new HashMap<>();
                    m.put(ATTR_TIME, timeFormat);
                    m.put(ATTR_REP, reps_now);
                    m.put(ATTR_NUMBER, number_now);
                    data.add(m);

                } while (cursor.moveToNext());

                String[] from = {ATTR_TIME, ATTR_REP, ATTR_NUMBER};
                int[] to = {R.id.time_item_set_textview, R.id.reps_item_set_textview,
                        R.id.mark_item_set_textview};
                //заводим данные в адаптер и присваиваем его встроенному списку ListFragment
                sara = new SimpleAdapter(getContext(), data, R.layout.list_item_set_textview, from, to);
                //устанавливаем свой биндер
                sara.setViewBinder(new MyViewBinder());
                mListView.setAdapter(sara);
                //setListAdapter(sara);
                //Чтобы сделать что-то при щелчке на галке, нужно расширить адаптер и сделать
                // слушатель внутри View на флажок
            }
    }

    // класс для изменения цвета элемента строки - маркера номера фрагмента подхода
    private class MyViewBinder implements SimpleAdapter.ViewBinder{
        @Override
        public boolean setViewValue(View view, Object o, String s) {
            int i;
            switch (view.getId()) {
                case R.id.mark_item_set_textview:
                    i = ((Integer) o).intValue();
                    //Log.d(TAG, "SetListFragment:  i == mCountFragment =  " + i + "  " + mCountFragment);
                    if (!end) {
                        if (i >= mCountFragment) {
                            //оставляем как есть, если нужно сделать другого цвета, то
                        } else view.setBackgroundColor(Color.YELLOW);
                    }else view.setBackgroundColor(Color.YELLOW);
            }

            //если поставить true? почемуто работает неправильно
            return false;
        }
    }

    private void calculateAndShowTotalValues(){

        //посчитаем общее врямя выполнения подхода в секундах
        mTimeOfSet = SetLab.countSetTime();
        //посчитаем общее количество повторений в подходе
        mTotalReps = SetLab.countTotalReps();
        //покажем общее время подхода и общее число повторений в подходе
        showTotalValues(mTimeOfSet, mTotalReps, SingleFragmentActivity.mKvant);
    }

    //покажем общее время подхода и общее число повторений в подходе
    public void showTotalValues(float timeOfSet,int totalReps, long kvant){

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

        // общее количество повторений в подходе
        String reps = String.format("Количество  %d", totalReps);
        mShowTotalValuesListener.onShowTotalValues(time,reps);
    }

    //
    public void   changeBackColor(int numberFraction, boolean endOfSet){
        //Log.d(TAG, "SetListFragment:  changeBackColor numberFraction =  " + (numberFraction+1));
        mCountFragment =  numberFraction +1;
        end = endOfSet;
        updateAdapter();
       // Log.d(TAG, "SetListFragment: changeBackColor До  mCountFragment =" + mCountFragment);
        //mListView.smoothScrollToPosition(mCountFragment);
       // Log.d(TAG, "SetListFragment: changeBackColor  После mCountFragment =" + mCountFragment);
        mListView.setSelection(numberFraction-1);
    }


}
