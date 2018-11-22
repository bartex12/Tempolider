package ru.bartex.p010_train.ru.bartex.p010_train.data;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class P {

    public static final String TAG ="33333";

    //идентификатор интента : Откуда пришёл?
    public static final String FROM_ACTIVITY = "ru.bartex.p010_train_from_activity";
    //пришёл от MainActivity
    public static final int MAIN_ACTIVITY = 111;
    //пришёл от TimeGrafactivity
    public static final int TIME_GRAF_ACTIVITY = 222;
    //пришёл от TabBarActivity
    public static final int TAB_BAR_ACTIVITY = 333;
    //пришёл от DetailActivity
    public static final int DETAIL_ACTIVITY = 444;
    //пришёл от DetailActivity
    public static final int NEW_EXERCISE_ACTIVITY = 555;
    // MainActivity =111   TIME_GRAF_ACTIVITY = 222    TabBarActivity = 333 DetailActivity =444
    //NewExerciseActivity = 555

    // идёт к расчёту количества прожитых дней
    public final static int TO_SEC = 1111;
    //идёт к расчёту биоритмов`
    public final static int TO_TEMP = 2222;
    //идёт новой персоне и затем диплоговое окно
    public final static int TO_ADD = 3333;
    //плюсик в тулбаре темполидера
    public final static int TO_ADD_SET = 4444;
    //плюсик в тулбаре редактора
    public final static int TO_ADD_FRAG = 5555;
    //контекстное меню в редакторе -вставить после
    public final static int TO_INSERT_AFTER_FRAG = 7777;
    //контекстное меню в редакторе -вставить до
    public final static int TO_INSERT_BEFORE_FRAG = 8888;

    //идентификатор интента : пришёл от Main
    public final static String FROM_MAIN = "ru.bartex.p010_train.FROM_MAIN";

    public static final String DETAIL_DATA_SET = "ru.bartex.p010_train.DetailActivity.DATA_SET";
    public static final String DETAIL_CHANGE_REQUEST = "ru.bartex.p010_train.DetailActivity.change_request";


    //откуда идёт запрос на справку
    public final static String HELP_FROM = "ru.bartex.p010_train.HELP_FROM";
    //варианты запросов на справку


    //риквест код для startActivityForResult
    public final static String REQUEST_CODE = "ru.bartex.p010_train.REQUEST_CODE";



    //имя файла для записи раскладки темполидера  по умолчанию - когда имя - пустая строка
    public static final String FILENAME_OTSECHKI_TEMP ="автосохранение_темполидера";
    //имя файла для записи раскладки секундомера по умолчанию - когда имя - пустая строка
    public static final String FILENAME_OTSECHKI_SEC ="автосохранение_секундомера";
    //имя файла для записи раскладки Избранное по умолчанию - когда имя - пустая строка
    public static final String FILENAME_OTSECHKI_LIKE ="автосохранение_избранное";


    //имя файла для записи текущей раскладки секундомера - когда не стали сохранять (или случайно)
    // перезаписывается при новой порции отсечек
    public static final String TYPE_OF_FILE = "ru.bartex.p010_train.TYPE_OF_FILE";

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
    public static final String FINISH_FILE_ID = "ru.bartex.p010_train_date_fileId";
    //имя файла для имени последнего сохранённого файла
    public static final String NAME_OF_LAST_FILE = "ru.bartex.p010_train.name_of_last_file";
    // ключ для имени последнего сохранённого файла
    public static final String LAST_FILE = "ru.bartex.p010_train.last_file";
    //имя в строке имени, если нет сохранённых файлов
    public static final String NAME_OF_LAST_FILE_ZERO = "У вас нет сохранённых файлов";

    public static final String KEY_DELAY = "DELAY";
    public static final String KEY_FILENAME = "FILENAME";

    public final static String ATTR_TIME = "ru.bartex.p010_train.ATTR_TIME";
    public final static String ATTR_REP = "ru.bartex.p010_train.ATTR_REP";
    public final static String ATTR_NUMBER = "ru.bartex.p010_train.ATTR_NUMBER";


    public final static String ATTR_ITEM_GRAF = "ru.bartex.p010_train.item";
    public final static String ATTR_TIME_GRAF = "ru.bartex.p010_train.time";
    public final static String ATTR_DELTA_GRAF = "ru.bartex.p010_train.delta";

    public static final String CHANGE_TEMP_POSITION = "ru.bartex.p010_train.position_ChangeTempActivity";
    public static final String CHANGE_TEMP_NAME_OF_FILE = "ru.bartex.p010_train.nameOfFile_ChangeTempActivity";
    public static final String CHANGE_TEMP_CHANGE_REQUEST = "ru.bartex.p010_train.changeRequest_ChangeTempActivity.";

    public static final String ARG_NAME_OF_FILE = "ru.bartex.p010_train.NameOfFile";
    public static final String ARG_NUMBER_ITEM_SEC = "ru.bartex.p010_train.NumberItemSec";
    public static final String ARG_NUMBER_ITEM_TEMP = "ru.bartex.p010_train.NumberItemTemp";
    public static final String ARG_NUMBER_ITEM_LIKE = "ru.bartex.p010_train.NumberItemLike";
    public static final String ARG_DELAY = "ru.bartex.p010_train.delay";

    public static final String INTENT_TO_DETILE_FILE_ID = "ru.bartex.p010_train.IntentToDetileFileId";
    public static final String INTENT_TO_CHANGE_TEMP_FILE_ID = "ru.bartex.p010_train.IntentToChangeTempFileId";
    public static final String INTENT_TO_CHANGE_TEMP_FILE_NAME = "ru.bartex.p010_train.IntentToChangeTempFileName";
    public static final String INTENT_TO_SINGLE_FROM_DETAIL_SAVE = "ru.bartex.p010_train.INTENT_TO_SINGLE_FROM_DETAIL_SAVE";
    public static final String INTENT_SAVE_VISION = "ru.bartex.p010_train.INTENT_SAVE_VISION";
    public static final String INTENT_SAVE_VISION_REPEAT = "ru.bartex.p010_train.INTENT_SAVE_VISION_REPEAT";
    public static final String INTENT_TO_DETILE_FILE_POSITION = "ru.bartex.p010_train.INTENT_TO_DETILE_FILE_POSITION";

    public final static int HELP_FROM_MAIN = 1;
    public final static int HELP_FROM_LIST_PERSONS = 2;
    public final static int HELP_FROM_BIORITM = 3;
    public final static int HELP_FROM_TIME = 4;
    public final static int HELP_FROM_TABLE = 5;
    public final static int HELP_FROM_NEW_PERSON = 6;
    public final static int HELP_FROM_JOINT = 7;
    public final static int HELP_FROM_FIND_DATE = 8;
    public final static int HELP_ALL = 9;

    public final static int DETAIL_CHANGE_REQUEST_KODE = 10;
    public final static int DETAIL_CHANGE_TEMP_REQUEST_CODE = 20;
    public final static int TEMP_REQUEST_CODE = 30;

    public static final int DELETE_ACTION_SEC = 1;
    public static final int CHANGE_ACTION_SEC = 2;
    public static final int CANCEL_ACTION_SEC = 3;
    public static final int DETAIL_ACTION_SEC = 4;
    public static final int MOVE_LIKE_ACTION_SEC = 5;
    public static final int MOVE_SEC_ACTION_SEC = 6;
    public static final int MOVE_TEMP_ACTION_SEC = 7;

    public static final int DELETE_ACTION_TEMP = 11;
    public static final int CHANGE_ACTION_TEMP = 12;
    public static final int CANCEL_ACTION_TEMP = 13;
    public static final int DETAIL_ACTION_TEMP = 14;
    public static final int MOVE_LIKE_ACTION_TEMP = 15;
    public static final int MOVE_SEC_ACTION_TEMP = 16;
    public static final int MOVE_TEMP_ACTION_TEMP = 17;

    public static final int DELETE_ACTION_LIKE = 21;
    public static final int CHANGE_ACTION_LIKE = 22;
    public static final int CANCEL_ACTION_LIKE = 23;
    public static final int DETAIL_ACTION = 24;
    public static final int MOVE_LIKE_ACTION = 25;
    public static final int MOVE_SEC_ACTION_LIKE = 26;
    public static final int MOVE_TEMP_ACTION_LIKE = 27;

    public static final int SAVE_ICON_REQUEST = 28;
    public static final int SAVE_ICON_REQUEST_CHANGE_TEMP = 29;

    public static final int DELETE_CHANGETEMP = 31;
    public static final int CHANGE_CHANGETEMP = 32;
    public static final int CANCEL_CHANGETEMP = 33;
    public static final int CHANGE_TEMP_CHANGETEMP = 34;

    public static final int ADD_NEW_FRAG_REQUEST = 35;
    public static final int REDACT_REQUEST = 36;

    public static final int INSERT_BEFORE_CHANGETEMP = 37;
    public static final int INSERT_AFTER_CHANGETEMP = 38;
    public static final int INSERT_AFTER_CHANGETEMP_REQUEST = 39;
    public static final int INSERT_BEFORE_CHANGETEMP_REQUEST = 40;

    //====================================================================//

    public static String setDateString() {
        Calendar calendar = new GregorianCalendar();
        int date = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);
        return  String.format(Locale.ENGLISH,"%02d.%02d.%04d_%02d:%02d:%02d",
                date, month+1, year, hour, min, sec);
    }

    public static String getTimeString1 (Long timeInMillis){

        //формируем формат строки показа времени
        int minut = (int)((timeInMillis/60000)%60);
        int second = (int)((timeInMillis/1000)%60);
        int decim = Math.round((timeInMillis%1000)/100);
        String time;

        if(minut<1) {
            time = String.format(Locale.ENGLISH,"%d.%01d", second, decim);
        }else if (minut<60){
            time = String.format(Locale.ENGLISH,"%d:%02d.%01d",minut,second,decim);
        }else {
            int hour = (int)((timeInMillis/3600000)%24);
            time = String.format(Locale.ENGLISH,"%d:%02d:%02d.%01d",hour,minut,second,decim);
        }
        return time;
    }

    public static String getTimeString2 (Long timeInMillis){

        //формируем формат строки показа времени
        int minut = (int)((timeInMillis/60000)%60);
        int second = (int)((timeInMillis/1000)%60);
        int decim = Math.round((timeInMillis%1000)/10);
        String time;

        if(minut<1) {
            time = String.format(Locale.ENGLISH,"%d.%02d", second, decim);
        }else if (minut<60){
            time = String.format(Locale.ENGLISH,"%d:%02d.%02d",minut,second,decim);
        }else {
            int hour = (int)((timeInMillis/3600000)%24);
            time = String.format(Locale.ENGLISH,"%d:%02d:%02d.%02d",hour,minut,second,decim);
        }
        return time;
    }

    public static String getTimeString3 (Long timeInMillis){

        //формируем формат строки показа времени
        int minut = (int)((timeInMillis/60000)%60);
        int second = (int)((timeInMillis/1000)%60);
        int decim = (int)(timeInMillis%1000);
        String time;

        if(minut<1) {
            time = String.format(Locale.ENGLISH,"%d.%03d", second, decim);
        }else if (minut<60){
            time = String.format(Locale.ENGLISH,"%d:%02d.%03d",minut,second,decim);
        }else {
            int hour = (int)((timeInMillis/3600000)%24);
            time = String.format(Locale.ENGLISH,"%d:%02d:%02d.%03d",hour,minut,second,decim);
        }
        return time;
    }

    //Заполнение списка адаптера данными курсора
    //потом заменить на CursorAdaptor
    public static ArrayList<Map<String, Object>>
    getDataFromCursor(Cursor cursor, ArrayList<Map<String, Object>> data, int accurancy){
        //проходим по курсору и берём данные
        if (cursor.moveToFirst()) {
            // Узнаем индекс каждого столбца
            int idColumnIndex = cursor.getColumnIndex(TabSet.COLUMN_SET_TIME);
            do {
                long time_total = 0;
                // Используем индекс для получения строки или числа и переводим в милисекунды
                //чтобы использовать ранее написанные функции getTimeString1
                long time_now = (long) (cursor.getFloat(idColumnIndex) * 1000);
                time_total += time_now;

                Log.d(TAG, "TimeGrafActivity cursor.getPosition()+1 = " +
                        (cursor.getPosition() + 1) + "  time_now = " + time_now +
                        "  time_total = " + time_total);

                //Делаем данные для адаптера
                String s_item = Integer.toString(cursor.getPosition() + 1);
                String s_time;
                String s_delta;

                switch (accurancy) {
                    case 1:
                        s_time = getTimeString1(time_total);
                        s_delta = getTimeString1(time_now);
                        break;
                    case 2:
                        s_time = getTimeString2(time_total);
                        s_delta = getTimeString2(time_now);
                        break;
                    case 3:
                        s_time = getTimeString3(time_total);
                        s_delta = getTimeString3(time_now);
                        break;
                    default:
                        s_time = getTimeString1(time_total);
                        s_delta = getTimeString1(time_now);
                }
                //заводим данные для одной строки списка
                Map<String, Object> m;
                m = new HashMap<>();
                m.put(ATTR_ITEM_GRAF, s_item);
                m.put(ATTR_TIME_GRAF, s_time);
                m.put(ATTR_DELTA_GRAF, s_delta);
                //добавляем данные в конец ArrayList
                data.add(m);
            } while (cursor.moveToNext());
        }
                return  data;
    }

    public static String getTimeString1_Float (float timeFloat){

        float timeInMillis = timeFloat*1000;

        //формируем формат строки показа времени
        int minut = (int)((timeInMillis/60000)%60);
        int second = (int)((timeInMillis/1000)%60);
        int decim = Math.round((timeInMillis%1000)/100);
        String time;

        if(minut<1) {
            time = String.format(Locale.ENGLISH,"%d.%01d", second, decim);
        }else if (minut<60){
            time = String.format(Locale.ENGLISH,"%d:%02d.%01d",minut,second,decim);
        }else {
            int hour = (int)((timeInMillis/3600000)%24);
            time = String.format(Locale.ENGLISH,"%d:%02d:%02d.%01d",hour,minut,second,decim);
        }
        return time;
    }

    public static String getTimeString2_Float (float timeFloat){

        float timeInMillis = timeFloat*1000;

        //формируем формат строки показа времени
        int minut = (int)((timeInMillis/60000)%60);
        int second = (int)((timeInMillis/1000)%60);
        int decim = Math.round((timeInMillis%1000)/10);
        String time;

        if(minut<1) {
            time = String.format(Locale.ENGLISH,"%d.%02d", second, decim);
        }else if (minut<60){
            time = String.format(Locale.ENGLISH,"%d:%02d.%02d",minut,second,decim);
        }else {
            int hour = (int)((timeInMillis/3600000)%24);
            time = String.format(Locale.ENGLISH,"%d:%02d:%02d.%02d",hour,minut,second,decim);
        }
        return time;
    }

    public static String getTimeString3_Float (float timeFloat){

        float timeInMillis = timeFloat*1000;

        //формируем формат строки показа времени
        int minut = (int)((timeInMillis/60000)%60);
        int second = (int)((timeInMillis/1000)%60);
        int decim = (int)(timeInMillis%1000);
        String time;

        if(minut<1) {
            time = String.format(Locale.ENGLISH,"%d.%03d", second, decim);
        }else if (minut<60){
            time = String.format(Locale.ENGLISH,"%d:%02d.%03d",minut,second,decim);
        }else {
            int hour = (int)((timeInMillis/3600000)%24);
            time = String.format(Locale.ENGLISH,"%d:%02d:%02d.%03d",hour,minut,second,decim);
        }
        return time;
    }


}
