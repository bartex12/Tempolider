package ru.bartex.p010_train;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class TabBarSecFragment extends StatFrag {

    static String TAG = "33333";
    ListView mListView;
    ViewPager mViewPager;
    private static final int REQUEST_FRAGMENT_CODE = 0;
    private static final String DIALOG_DELETE = "DialogDelete";
    private static final String ARG_NUMBER_ITEM = "NumberItem";
    private static final String DIALOG_CHANGE_NAME = "ChangeNamePicker";

    public TabBarSecFragment() {
        // Required empty public constructor
    }

    public static TabBarSecFragment newInstance(int numberItem){
        Bundle args = new Bundle();
        args.putInt(ARG_NUMBER_ITEM,numberItem);
        TabBarSecFragment fragment = new TabBarSecFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "TabBarSecFragment onCreateView");


        // создаём View для этого фрагмента
        View v = inflater.inflate(R.layout.fragment_tab_bar_sec, container, false);

        mViewPager = getActivity().findViewById(R.id.container);

        mListView = v.findViewById(R.id.listViewSec);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String data = adapterView.getItemAtPosition(i).toString();
                Log.d(TAG, "TabBarSecFragment onCreateView adapterView.getItemAtPosition(i) = " +
                        data);  //содержимое строки

                //получаем тип файла по его имени в списке файлов
                FileSaverLab saverLab = FileSaverLab.get();
                FileSaver saver = saverLab.getFileSaver(data);
                String type = saver.getTipe();
                Log.d(TAG, "TabBarSecFragment onCreateView    String type = " +  type +
                "   data = " + data);

                //отправляем интент с меткой 333, что значит из TabBarActivity
                Intent intent = new Intent(getActivity(), SetListActivity.class);
                intent.putExtra(FileSaver.FINISH_FILE_NAME, data);
                intent.putExtra(FileSaver.FILENAME_TYPE, type);
                intent.putExtra(SingleFragmentActivity.FROM_ACTIVITY, LIST_OF_FILE_ACTIVITY);
                startActivity(intent);
                getActivity().finish();
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "TabBarSecFragment onResume");

        //устанавливаем адаптер списка вкладки
        setAdapterTabList(FileSaver.TYPE_TIMEMETER,
                R.layout.activity_list_of_files_item,  mListView);

        //объявляем о регистрации контекстного меню
        registerForContextMenu(mListView);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "TabBarSecFragment onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "TabBarSecFragment onStop");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_FRAGMENT_CODE) {

            //обновляем адаптер вкладок
            mViewPager.getAdapter().notifyDataSetChanged();
        }
    }

    //создаём контекстное меню для списка (сначала регистрация нужна в onCreateView)
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, "Удалить запись");
        menu.add(0, CHANGE_ID, 0, "Изменить запись");
        menu.add(0, LIKE_ID, 0, "Переместить в избранное");
        menu.add(0, CANCEL_ID, 0, "Отмена");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        //получаем номер открытой вкладки из аргументов
        int curItem = getArguments().getInt(ARG_NUMBER_ITEM);
        Log.d(TAG, "TabBarSecFragment curItem = " + curItem );

        // получаем инфу о пункте списка
        final AdapterView.AdapterContextMenuInfo acmi =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        View v = acmi.targetView;
        TextView tv = v.findViewById(R.id.text1);
        final String name = tv.getText().toString();
        Log.d(TAG, "acmi.position = " + acmi.position +
                "  acmi.id = " + acmi.id +"  String name = " + name );

        //если выбран пункт Удалить запись
        if (item.getItemId() == DELETE_ID) {

            //смотрим номер текущей вкладки
            int currentItem = mViewPager.getCurrentItem();
            //если текущий фрагмент это открытая вкладка, то показываем диалог
        if (currentItem == curItem){
            Log.d(TAG, "TabBarSecFragment Удалить запись currentItem = " +
                    currentItem +" curItem = " + curItem );
            DialogFragment fragment = DialogDeleteFragment.newInstance(name);
            fragment.setTargetFragment(TabBarSecFragment.this, REQUEST_FRAGMENT_CODE);
            fragment.show(getFragmentManager(),DIALOG_DELETE);
        }

            //если выбран пункт Изменить запись
        } else if (item.getItemId() == CHANGE_ID) {

            //смотрим номер текущей вкладки
            int currentItem = mViewPager.getCurrentItem();
            //если текущий фрагмент это открытая вкладка, то показываем диалог
            if (currentItem == curItem){
                Log.d(TAG, "TabBarSecFragment Изменить запись currentItem = " +
                        currentItem +" curItem = " + curItem );
                FileSaverLab fileSaverLab = FileSaverLab.get();
                FileSaver saver = fileSaverLab.getFileSaver(name);
                String date = saver.getDate();

                DialogFragment dialogFragment = DialogChangeNameFragment.newInstance(name,date);
                dialogFragment.setTargetFragment(TabBarSecFragment.this, REQUEST_FRAGMENT_CODE);
                dialogFragment.show(getFragmentManager(),DIALOG_CHANGE_NAME);
            }

            //если выбран пункт Переместить в избранное
        } else if (item.getItemId() == LIKE_ID) {

            //получаем тип файла по его имени в списке файлов и меняем тип на TYPE_LIKE
            FileSaverLab.get().getFileSaver(name).setTipe(FileSaver.TYPE_LIKE);
            //обновляем адаптер вкладок
            mViewPager.getAdapter().notifyDataSetChanged();
        }
        //если ничего не выбрано
        return super.onContextItemSelected(item);
    }


    private   void setAdapterTabList(String type,int layout,ListView listTab){
        ArrayList<String> tempFilesSec =Stat.getFilesWithType(type);
        //загружаем список в адаптер
        ArrayAdapter mAdapter =  new ArrayAdapter<String>(getContext(),
                layout, tempFilesSec);
        //подключаем адаптер к listTab
        listTab.setAdapter(mAdapter);
    }

}
