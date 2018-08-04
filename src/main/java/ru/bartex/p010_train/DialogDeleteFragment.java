package ru.bartex.p010_train;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DialogDeleteFragment extends DialogFragment {

    private static final String TAG = "33333";
    public static String ARG_NAME = "name";
    public static final String EXTRA_SIZE = "ru.bartex.p010_train.size";

    public DialogDeleteFragment() {
        // Required empty public constructor
    }

    public static DialogDeleteFragment newInstance(String name){
        Bundle args = new Bundle();
        args.putString(ARG_NAME,name);
        DialogDeleteFragment fragment = new DialogDeleteFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void sendResult(int resultCode, int size) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_SIZE, size);
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), resultCode, intent);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String name = getArguments().getString(ARG_NAME);

        AlertDialog.Builder delDialog = new AlertDialog.Builder(getActivity());
        delDialog.setTitle("Удалить: Вы уверены?");
        delDialog.setPositiveButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        delDialog.setNegativeButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //удаление из синглета-держателя списка сохранённых файлов
                //его опрос идёт в методе интерфейса секундомера  в saveDataAndFilename
                FileSaverLab fileSaverLab = FileSaverLab.get();
                List<FileSaver> fileSavers = fileSaverLab.getFileSavers();
                Log.d(TAG, "Размер списка Файлов fileSavers до удаления = " + fileSavers.size());
                int positionInList = fileSaverLab.findIdList(name);
                fileSavers.remove(positionInList);
                Log.d(TAG, "Размер списка Файлов fileSavers после удаления = " + fileSavers.size());
                sendResult(Activity.RESULT_OK,fileSavers.size());

                //пока так, потом это надо убрать и писать только перед выходом
                ArrayList<String> names = fileSaverLab.getArrayListNames();
                //сохраняем в файл список сохранённых файлов без удалённого элемента
                writeArrayList(names, SingleFragmentActivity.FILENAME_NAMES_OF_FILES);

            }
        });
        return delDialog.create();
    }

    //Записать список имён с данными  в файл
    private void writeArrayList(ArrayList<String> arrayList, String fileName) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    getActivity().openFileOutput(fileName,  getActivity().MODE_PRIVATE)));
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

}
