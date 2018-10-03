package ru.bartex.p010_train;

import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

/**
 * Created by Андрей on 07.05.2018.
 */
public class FileSaver {
    private String mTitle;
    private static String mDate;
    private String mTipe;
    private UUID mId;
    private int numberFile;

    //имя файла для записи раскладки темполидера  по умолчанию - когда имя - пустая строка
    public static final String FILENAME_OTSECHKI_TEMP ="автосохранение__темполидера";
    //имя файла для записи раскладки секундомера по умолчанию - когда имя - пустая строка
    public static final String FILENAME_OTSECHKI_SEC ="автосохранение_секундомера";

    //имя файла для записи текущей раскладки секундомера - когда не стали сохранять (или случайно)
    // перезаписывается при новой порции отсечек
    public static final String FILENAME_TIMEMETER = "ru.bartex.p010_train.filename_timemeter";

    public static final String TYPE_TIMEMETER ="type_timemeter";
    public static final String TYPE_TEMPOLEADER ="type_tempoleader";
    public static final String TYPE_LIKE ="type_like";
    //имя файла для хранения имён файлов с раскладками
    public static final String FILENAME_NAMES_OF_FILES ="ru.bartex.p010_train_names_of_files";
    public static final String FILENAME_TYPE ="ru.bartex.p010_train_type";
    public static final String FILENAME_DATE ="ru.bartex.p010_train_date";

    //имя файла из строки в TabBarActivity
    public static final String NAME_OF_FILE = "ru.bartex.p010_train.name_of_file";
    public static final String FINISH_FILE_NAME = "Раскладка без имени";
    //имя файла для имени последнего сохранённого файла
    public static final String NAME_OF_LAST_FILE = "ru.bartex.p010_train.name_of_last_file";
    // ключ для имени последнего сохранённого файла
    public static final String LAST_FILE = "ru.bartex.p010_train.last_file";
    //имя в строке имени, если нет сохранённых файлов
    public static final String NAME_OF_LAST_FILE_ZERO = "У вас нет сохранённых файлов";


    public FileSaver(){
        mId = UUID.randomUUID();
    }

    public String getTipe() {
        return mTipe;
    }

    public void setTipe(String tipe) {
        mTipe = tipe;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public void setDate() {
        Calendar calendar = new GregorianCalendar();
        int date = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        mDate = String.format("%s.%s.%s_%s.%s",
                date, month+1, year, hour, min);
    }

    public static String setDateString() {
        Calendar calendar = new GregorianCalendar();
        int date = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        return  String.format("%02d.%02d.%04d_%02d:%02d",
                date, month+1, year, hour, min);
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        mId = id;
    }

    public int getNumberFile() {
        return numberFile;
    }

    public void setNumberFile(int numberFile) {
        this.numberFile = numberFile;
    }

}
