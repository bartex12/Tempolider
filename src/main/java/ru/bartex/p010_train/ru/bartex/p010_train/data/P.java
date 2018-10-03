package ru.bartex.p010_train.ru.bartex.p010_train.data;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class P {
    //идентификатор интента : пришёл от Main
    public final static String FROM_MAIN = "ru.bartex.p010_train.FROM_MAIN";
    // идёт к расчёту количества прожитых дней
    public final static int TO_SEC = 1111;
    //идёт к расчёту биоритмов`
    public final static int TO_TEMP = 2222;
    //идёт новой персоне и затем диплоговое окно
    public final static int TO_ADD = 3333;
    //откуда идёт запрос на справку
    public final static String HELP_FROM = "ru.bartex.p010_train.HELP_FROM";
    //варианты запросов на справку
    public final static int HELP_FROM_MAIN = 1;
    public final static int HELP_FROM_LIST_PERSONS = 2;
    public final static int HELP_FROM_BIORITM = 3;
    public final static int HELP_FROM_TIME = 4;
    public final static int HELP_FROM_TABLE = 5;
    public final static int HELP_FROM_NEW_PERSON = 6;
    public final static int HELP_FROM_JOINT = 7;
    public final static int HELP_FROM_FIND_DATE = 8;
    public final static int HELP_ALL = 9;

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

    //====================================================================//

    public static String setDateString() {
        Calendar calendar = new GregorianCalendar();
        int date = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);
        return  String.format("%02d.%02d.%04d_%02d:%02d:%02d",
                date, month+1, year, hour, min, sec);
    }

    public static String getTimeString1 (float deltaTime){

        //формируем формат строки показа времени
        int minut = (int)((deltaTime/60)%60);
        int second = (int)((deltaTime)%60);
        int decim = Math.round((deltaTime%1)/100);
        String time = "";

        if(minut<1) {
            time = String.format("%d.%01d", second, decim);
        }else if (minut<60){
            time = String.format("%d:%02d.%01d",minut,second,decim);
        }else {
            int hour = (int)((deltaTime/3600)%24);
            time = String.format("%d:%02d:%02d.%01d",hour,minut,second,decim);
        }
        return time;
    }

    public static String getTimeString2 (float deltaTime){

        //формируем формат строки показа времени
        int minut = (int)((deltaTime/60)%60);
        int second = (int)((deltaTime)%60);
        int decim = Math.round((deltaTime%1)/10);
        String time;

        if(minut<1) {
            time = String.format("%d.%02d", second, decim);
        }else if (minut<60){
            time = String.format("%d:%02d.%02d",minut,second,decim);
        }else {
            int hour = (int)((deltaTime/3600)%24);
            time = String.format("%d:%02d:%02d.%02d",hour,minut,second,decim);
        }
        return time;
    }

    public static String getTimeString3 (float deltaTime){

        //формируем формат строки показа времени
        int minut = (int)((deltaTime/60)%60);
        int second = (int)((deltaTime)%60);
        int decim = (int)(deltaTime%1);
        String time;

        if(minut<1) {
            time = String.format("%d.%03d", second, decim);
        }else if (minut<60){
            time = String.format("%d:%02d.%03d",minut,second,decim);
        }else {
            int hour = (int)((deltaTime/3600)%24);
            time = String.format("%d:%02d:%02d.%03d",hour,minut,second,decim);
        }
        return time;
    }
}
