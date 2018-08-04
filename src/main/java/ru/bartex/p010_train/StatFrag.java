package ru.bartex.p010_train;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Андрей on 14.05.2018.
 */
public class StatFrag extends Fragment {

    public static final int LIST_OF_FILE_ACTIVITY = 333;
    public static final int DELETE_ID = 1;
    public static final int CHANGE_ID = 2;
    public static final int CANCEL_ID = 3;
    public static final int DETAIL_ID = 4;
    public static final int LIKE_ID = 5;
    public static final int MOVE_SEC_ID = 6;
    public static final int MOVE_TEMP_ID = 7;




    public StatFrag(){
    //конструктор
    }

    //Прочитать список имён с данными из файла
    public  ArrayList<String> readArrayList(String fileName) {

        ArrayList<String> newArrayList = new ArrayList<String>();

        try {
            // открываем поток для чтения
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    getContext().openFileInput(fileName)));
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
