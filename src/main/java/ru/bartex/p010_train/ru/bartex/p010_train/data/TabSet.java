package ru.bartex.p010_train.ru.bartex.p010_train.data;

import android.provider.BaseColumns;

public class TabSet {

    public TabSet(){
        //пустой конструктор
    }

    public final static String TABLE_NAME = "TimeReps";

    public final static String _ID = BaseColumns._ID;
    public final static String COLUMN_SET_FILE_ID = "File_ID";
    public final static String COLUMN_SET_TIME = "Set_Time";
    public final static String COLUMN_SET_REPS = "Set_Reps";
    public final static String COLUMN_SET_FRAG_NUMBER = "FragNumber";
}
