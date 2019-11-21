package ru.bartex.p010_train;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.Nullable;

import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.material.snackbar.Snackbar;

import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import ru.bartex.p010_train.ru.bartex.p010_train.data.P;
import ru.bartex.p010_train.ru.bartex.p010_train.data.TabFile;
import ru.bartex.p010_train.ru.bartex.p010_train.data.TempDBHelper;


public class TabBarSecFragment extends Fragment {

    static String TAG = "33333";
    ListView mListView;
    ViewPager mViewPager;
    private static final int REQUEST_FRAGMENT_CODE = 0;

    TempDBHelper mTempDBHelper;
    SimpleCursorAdapter scAdapter;
    Dialog dialog;

    public TabBarSecFragment() {
        // Required empty public constructor
    }

    public static TabBarSecFragment newInstance(int numberItem) {
        Bundle args = new Bundle();
        args.putInt(P.ARG_NUMBER_ITEM_SEC, numberItem);
        TabBarSecFragment fragment = new TabBarSecFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTempDBHelper = new TempDBHelper(getActivity());
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
                //строка с именем файла
                String fileName = mTempDBHelper.getFileNameFromTabFile(l);
                Log.d(TAG, "TabBarSecFragment onCreateView     имя файла = " + fileName +
                        "  long l = " + l);
                //отправляем интент с меткой 333, что значит из TabBarActivity
                Intent intent = new Intent(getActivity(), SetListActivity.class);
                intent.putExtra(P.FINISH_FILE_NAME, fileName);
                intent.putExtra(P.FINISH_FILE_ID, l);
                intent.putExtra(P.FROM_ACTIVITY, P.TAB_BAR_ACTIVITY);
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

        Cursor cursor = mTempDBHelper.getAllFilesWhithType(P.TYPE_TIMEMETER);

        // формируем столбцы сопоставления
        String[] from = new String[]{TabFile.COLUMN_FILE_NAME};
        int[] to = new int[]{R.id.text1};

        // создааем адаптер и настраиваем список
        scAdapter = new SimpleCursorAdapter(getActivity().getApplicationContext(),
                R.layout.activity_list_of_files_item, cursor, from, to, 0);
        mListView.setAdapter(scAdapter);

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

    //создаём контекстное меню для списка (сначала регистрация нужна  - здесь в onResume)
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, P.DELETE_ACTION_SEC, 0, "Удалить запись");
        menu.add(0, P.CHANGE_ACTION_SEC, 0, "Изменить запись");
        menu.add(0, P.MOVE_TEMP_ACTION_SEC, 0, "Переместить в темполидер");
        menu.add(0, P.MOVE_LIKE_ACTION_SEC, 0, "Переместить в избранное");
        menu.add(0, P.CANCEL_ACTION_SEC, 0, "Отмена");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // получаем инфу о пункте списка
        final AdapterView.AdapterContextMenuInfo acmi =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //получаем номер открытой вкладки из аргументов
        int curItem = getArguments().getInt(P.ARG_NUMBER_ITEM_SEC);
        Log.d(TAG, "TabBarSecFragment curItem = " + curItem);
        //смотрим номер текущей вкладки
        int currentItem = mViewPager.getCurrentItem();

        //если выбран пункт Удалить запись
        if (item.getItemId() == P.DELETE_ACTION_SEC) {
            Log.d(TAG, "TabBarSecFragment DELETE_ACTION");

            String fileName = mTempDBHelper.getFileNameFromTabFile(acmi.id);
            if (fileName.equals(P.FILENAME_OTSECHKI_SEC)||
                    fileName.equals(P.FILENAME_OTSECHKI_TEMP)) {
                Snackbar.make(getView(), "Системный файл. Удаление запрещено.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }else {

                AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getActivity());
                deleteDialog.setTitle("Удалить: Вы уверены?");
                deleteDialog.setPositiveButton("Нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                deleteDialog.setNegativeButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                            //Удаление записи из базы данных
                            mTempDBHelper.deleteFileAndSets(acmi.id);
                            Log.d(TAG, "PersonsListActivity удалена позиция с ID " + acmi.id);
                            //обновляем адаптер
                            mViewPager.getAdapter().notifyDataSetChanged();
                    }
                });
                //если текущий фрагмент это открытая вкладка, то показываем диалог
                if (currentItem == curItem) {
                    deleteDialog.show();
                }
            }
            return true;


            //если выбран пункт Изменить запись
        } else if (item.getItemId() == P.CHANGE_ACTION_SEC) {
            //получаем объект с данными строки с id = acmi.id из  таблицы TabFile
            final DataFile dataFile = mTempDBHelper.getAllFilesData(acmi.id);
            String fn = dataFile.getFileName();

            //если имя системного файла
            if (fn.equals(P.FILENAME_OTSECHKI_SEC) ||
                    fn.equals(P.FILENAME_OTSECHKI_TEMP)){
                Snackbar.make(getView(), "Системный файл.Изменение запрещено.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                //если НЕ имя системного файла
            }else {
                //принудительно вызываем клавиатуру - повторный вызов ее скроет
                takeOnAndOffSoftInput();

                final AlertDialog.Builder changeDialog = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View view = inflater.inflate(R.layout.fragment_dialog_chahge_name, null);

                final EditText name = view.findViewById(R.id.editTextNameOfFile);
                name.requestFocus();
                name.setInputType(InputType.TYPE_CLASS_TEXT);
                //получаем имя файла
                String nameFile = dataFile.getFileName();
                name.setText(nameFile);

                String min = dataFile.getFileNameDate() +
                        getResources().getString(R.string.LowMinus) + dataFile.getFileNameTime();
                final EditText dateAndTime = view.findViewById(R.id.editTextDateAndTime);
                dateAndTime.setText(min);
                dateAndTime.setEnabled(false);

                final CheckBox date = view.findViewById(R.id.checkBoxDate);

                changeDialog.setView(view);
                changeDialog.setTitle("Изменить имя");
                changeDialog.setIcon(R.drawable.ic_wrap_text_black_24dp);

            Button saveButYes = view.findViewById(R.id.buttonSaveYesChangeName);
            saveButYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String nameFile = name.getText().toString();

                        if(date.isChecked()){
                            nameFile = nameFile + "_" + P.setDateString();
                            Log.d(TAG, "TabBarSecFragment date.isChecked() Имя файла = " + nameFile);
                        }

                        //++++++++++++++++++   проверяем, есть ли такое имя   +++++++++++++//
                        long fileId = mTempDBHelper.getIdFromFileName(nameFile);
                        Log.d(TAG, "nameFile = " + nameFile + "  fileId = " + fileId);

                        //если имя - пустая строка
                        if (nameFile.trim().isEmpty()) {
                            Snackbar.make(view, "Введите непустое имя раскладки", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            Log.d(TAG, "Введите непустое имя раскладки ");
                            return;

                            //если такое имя уже есть в базе
                        } else if (fileId != -1) {
                            Snackbar.make(view, "Такое имя уже существует. Введите другое имя.", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            Log.d(TAG, "Такое имя уже существует. Введите другое имя. fileId = " + fileId);
                            return;

                            //если имя не повторяется, оно не пустое и не системный файл то
                        } else {
                            Log.d(TAG, "Такое имя отсутствует fileId = " + fileId);

                            //изменяем имя файла
                            mTempDBHelper.updateFileName(nameFile, acmi.id);

                            //принудительно прячем  клавиатуру - повторный вызов ее покажет
                            takeOnAndOffSoftInput();

                            mViewPager.getAdapter().notifyDataSetChanged();

                            //getActivity().finish(); //закрывает и диалог и активность
                            dialog.dismiss();  //закрывает только диалог
                        }
                }
            });

            Button saveButNo = view.findViewById(R.id.buttonSaveNoChangeName);
            saveButNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //принудительно прячем  клавиатуру - повторный вызов ее покажет
                    takeOnAndOffSoftInput();
                    //getActivity().finish(); //закрывает и диалог и активность
                    dialog.dismiss();  //закрывает только диалог
                }
            });
                //если делать запрет на закрытие окна при щелчке за пределами окна,
                // то сначала билдер создаёт диалог
                dialog = changeDialog.create();
                //запрет на закрытие окна при щелчке за пределами окна
                dialog.setCanceledOnTouchOutside(false);
                //если текущий фрагмент это открытая вкладка, то показываем диалог
                if (currentItem == curItem) {
                    dialog.show();
                }
                return true;
            }

            //если выбран пункт Переместить в избранное
        } else if (item.getItemId() == P.MOVE_LIKE_ACTION_SEC) {

            String fileName = mTempDBHelper.getFileNameFromTabFile(acmi.id);
            if (fileName.equals(P.FILENAME_OTSECHKI_SEC)||
                    fileName.equals(P.FILENAME_OTSECHKI_TEMP)) {
                Snackbar.make(getView(), "Системный файл.Перемещение запрещено.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }else {

                SQLiteDatabase db = mTempDBHelper.getWritableDatabase();
                ContentValues updatedValues = new ContentValues();
                updatedValues.put(TabFile.COLUMN_TYPE_FROM, P.TYPE_LIKE);
                db.update(TabFile.TABLE_NAME, updatedValues,
                        TabFile._ID + "=" + acmi.id, null);
                //обновляем адаптер вкладок
                mViewPager.getAdapter().notifyDataSetChanged();
            }
            return true;

            //если выбран пункт Переместить в Темполидер
        } else if (item.getItemId() == P.MOVE_TEMP_ACTION_SEC) {

            String fileName = mTempDBHelper.getFileNameFromTabFile(acmi.id);
            if (fileName.equals(P.FILENAME_OTSECHKI_SEC)||
                    fileName.equals(P.FILENAME_OTSECHKI_TEMP)) {
                Snackbar.make(getView(), "Системный файл.Перемещение запрещено.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }else {

                SQLiteDatabase db = mTempDBHelper.getWritableDatabase();
                ContentValues updatedValues = new ContentValues();
                updatedValues.put(TabFile.COLUMN_TYPE_FROM, P.TYPE_TEMPOLEADER);
                db.update(TabFile.TABLE_NAME, updatedValues,
                        TabFile._ID + "=" + acmi.id, null);
                //обновляем адаптер вкладок
                mViewPager.getAdapter().notifyDataSetChanged();
            }
            return true;

        }
            //если ничего не выбрано
            return super.onContextItemSelected(item);
        }

        //принудительно вызываем клавиатуру - повторный вызов ее скроет
        private void takeOnAndOffSoftInput(){
            InputMethodManager imm = (InputMethodManager) getActivity().
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }

}
