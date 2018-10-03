package ru.bartex.p010_train.ru.bartex.p010_train.data;

import android.provider.BaseColumns;

public class TabFile {

    private TabFile(){
        //пустой конструктор
    }

    public final static String TABLE_NAME = "FileData";

    public final static String _ID = BaseColumns._ID;
    public final static String COLUMN_FILE_NAME = "FileName";
    public final static String COLUMN_FILE_NAME_DATE = "FileNameDate";
    public final static String COLUMN_FILE_NAME_TIME = "FileNameTime";
    public final static String COLUMN_KIND_OF_SPORT = "KindOfSport";
    public final static String COLUMN_DESCRIPTION_OF_SPORT = "DescriptionOfSport";
    public final static String COLUMN_TYPE_FROM = "Type_From";
    public final static String COLUMN_DELAY = "Delay";

}
