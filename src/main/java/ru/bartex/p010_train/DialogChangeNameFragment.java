package ru.bartex.p010_train;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Андрей on 19.05.2018.
 */
public class DialogChangeNameFragment extends DialogFragment {

    static String TAG = "33333";
    private static final String ARG_NAME_OF_FILE = "NameOfFile";
    private static final String ARG_DATE_AND_TIME = "DateAndTime";

    StatFrag mStatFrag = new StatFrag();
    ArrayList<String> mListTimeTransfer = new ArrayList<>();

    public static DialogChangeNameFragment newInstance(String nameOfFile, String dateAndTime){
        Bundle args = new Bundle();
        args.putString(ARG_NAME_OF_FILE, nameOfFile);
        args.putString(ARG_DATE_AND_TIME, dateAndTime);
        DialogChangeNameFragment fragment = new DialogChangeNameFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //принудительно вызываем клавиатуру - повторный вызов ее скроет
        takeOnAndOffSoftInput();

        AlertDialog.Builder bilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_dialog_chahge_name, null);
        final EditText name = (EditText)view.findViewById(R.id.editTextNameOfFile);
        name.requestFocus();
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        String nameFile = (String) getArguments().get(ARG_NAME_OF_FILE);
        name.setText(nameFile);
        FileSaverLab fileSaverLab = FileSaverLab.get();
        final FileSaver saver = fileSaverLab.getFileSaver(nameFile);
        Log.d(TAG, "FileSaver saver  = " + saver);

        //читаем сохранённый файл с ОТСЕЧКАМИ
        mListTimeTransfer = readArrayList(nameFile);

        final EditText dateAndTime = (EditText)view.findViewById(R.id.editTextDateAndTime);
        dateAndTime.setText((String) getArguments().get(ARG_DATE_AND_TIME));
        dateAndTime.setEnabled(false);

        final CheckBox date = (CheckBox)view.findViewById(R.id.checkBoxDate);

        bilder.setView(view);
        bilder.setTitle("Изменить имя");
        bilder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String nameFile = name.getText().toString();
                String dateAndTimeFile = dateAndTime.getText().toString();

                if(date.isChecked()){
                    nameFile = nameFile + "_" + dateAndTimeFile;
                }
                saver.setTitle(nameFile);

                //пишем те же отсечки в файл с новым именем nameFile
                writeArrayList(mListTimeTransfer,nameFile);
                //вызываем onActivityResult, где обновляем адаптер
                sendResult(Activity.RESULT_OK);

                //принудительно прячем  клавиатуру - повторный вызов ее покажет
                takeOnAndOffSoftInput();
            }
        });
        bilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //принудительно прячем  клавиатуру - повторный вызов ее покажет
                takeOnAndOffSoftInput();

            }
        });
        //если не делать запрет на закрытие окна при щелчке за пределами окна, то можно так
        //return bilder.create();
        //А если делать запрет, то так
        Dialog  dialog = bilder.create();
        //запрет на закрытие окна при щелчке за пределами окна
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    //Прочитать список имён с данными из файла
    private ArrayList<String> readArrayList(String fileName) {

        ArrayList<String> newArrayList = new ArrayList<String>();

        try {
            // открываем поток для чтения
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    getActivity().openFileInput(fileName)));
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

    //Записать список имён с данными  в файл
    private void writeArrayList(ArrayList<String> arrayList, String fileName) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    getActivity().openFileOutput(fileName, getContext().MODE_PRIVATE)));
            for (String line : arrayList) {
                //функция write не работает для CharSequence, поэтому String
                bw.write(line);
                // тут мог бы быть пробел если надо в одну строку
                //сли не включать эту строку, то в файле будет всего одна строчка, а нужен массив
                bw.write(System.getProperty("line.separator"));
            }
            Log.d(TAG, "Файл ArrayList записан ");
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), resultCode, intent);
    }

    //принудительно вызываем клавиатуру - повторный вызов ее скроет
    private void takeOnAndOffSoftInput(){
        InputMethodManager imm = (InputMethodManager) getActivity().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

}
