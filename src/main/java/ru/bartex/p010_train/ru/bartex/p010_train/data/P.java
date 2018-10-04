package ru.bartex.p010_train.ru.bartex.p010_train.data;

import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class P {

    public static final String TAG ="33333";

    //идентификатор интента : Откуда пришёл?
    public static final String FROM_ACTIVITY = "ru.bartex.p010_train_from_activity";
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

    public static final String KEY_DELAY = "DELAY";

    public final static String ATTR_ITEM_GRAF = "ru.bartex.p008_complex_imit_real.item";
    public final static String ATTR_TIME_GRAF = "ru.bartex.p008_complex_imit_real.time";
    public final static String ATTR_DELTA_GRAF = "ru.bartex.p008_complex_imit_real.delta";

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

    public static String getTimeString1 (Long timeInMillis){

        //формируем формат строки показа времени
        int minut = (int)((timeInMillis/60000)%60);
        int second = (int)((timeInMillis/1000)%60);
        int decim = Math.round((timeInMillis%1000)/100);
        String time;

        if(minut<1) {
            time = String.format("%d.%01d", second, decim);
        }else if (minut<60){
            time = String.format("%d:%02d.%01d",minut,second,decim);
        }else {
            int hour = (int)((timeInMillis/3600000)%24);
            time = String.format("%d:%02d:%02d.%01d",hour,minut,second,decim);
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
            time = String.format("%d.%02d", second, decim);
        }else if (minut<60){
            time = String.format("%d:%02d.%02d",minut,second,decim);
        }else {
            int hour = (int)((timeInMillis/3600000)%24);
            time = String.format("%d:%02d:%02d.%02d",hour,minut,second,decim);
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
            time = String.format("%d.%03d", second, decim);
        }else if (minut<60){
            time = String.format("%d:%02d.%03d",minut,second,decim);
        }else {
            int hour = (int)((timeInMillis/3600000)%24);
            time = String.format("%d:%02d:%02d.%03d",hour,minut,second,decim);
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

}
