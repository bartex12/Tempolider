package ru.bartex.p010_train.ru.bartex.p010_train.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;

import ru.bartex.p010_train.DataFile;
import ru.bartex.p010_train.DataSet;

import static ru.bartex.p010_train.ru.bartex.p010_train.data.TabFile.COLUMN_DELAY;
import static ru.bartex.p010_train.ru.bartex.p010_train.data.TabFile.COLUMN_DESCRIPTION_OF_SPORT;
import static ru.bartex.p010_train.ru.bartex.p010_train.data.TabFile.COLUMN_FILE_NAME;
import static ru.bartex.p010_train.ru.bartex.p010_train.data.TabFile.COLUMN_FILE_NAME_DATE;
import static ru.bartex.p010_train.ru.bartex.p010_train.data.TabFile.COLUMN_FILE_NAME_TIME;
import static ru.bartex.p010_train.ru.bartex.p010_train.data.TabFile.COLUMN_KIND_OF_SPORT;
import static ru.bartex.p010_train.ru.bartex.p010_train.data.TabFile.COLUMN_LIKED;
import static ru.bartex.p010_train.ru.bartex.p010_train.data.TabFile.COLUMN_TYPE_FROM;
import static ru.bartex.p010_train.ru.bartex.p010_train.data.TabSet.COLUMN_SET_FILE_ID;
import static ru.bartex.p010_train.ru.bartex.p010_train.data.TabSet.COLUMN_SET_FRAG_NUMBER;
import static ru.bartex.p010_train.ru.bartex.p010_train.data.TabSet.COLUMN_SET_REPS;
import static ru.bartex.p010_train.ru.bartex.p010_train.data.TabSet.COLUMN_SET_TIME;


public class TempDBHelper extends SQLiteOpenHelper {

    public static final String TAG = "33333";

    //Имя файла базы данных
    private static final String DATABASE_NAME = "Tempolider.db";
    // Версия базы данных. При изменении схемы увеличить на единицу
    private static final int DATABASE_VERSION = 1;


    public TempDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
// Строка для создания таблицы FileData
        String SQL_CREATE_TAB_FILE = "CREATE TABLE " + TabFile.TABLE_NAME + " ("
                + TabFile._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FILE_NAME + " TEXT NOT NULL, "
                + COLUMN_FILE_NAME_DATE + " TEXT NOT NULL, "
                + COLUMN_FILE_NAME_TIME + " TEXT NOT NULL, "
                + COLUMN_KIND_OF_SPORT + " TEXT, "
                + COLUMN_DESCRIPTION_OF_SPORT + " TEXT, "
                + COLUMN_DELAY + " INTEGER NOT NULL DEFAULT 6, "
                + COLUMN_TYPE_FROM + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_LIKED + " INTEGER NOT NULL DEFAULT 0);";

        // Строка для создания таблицы TimeReps
        String SQL_CREATE_TAB_SET = "CREATE TABLE " + TabSet.TABLE_NAME + " ("
                + TabSet._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SET_FILE_ID + " INTEGER NOT NULL, "
                + COLUMN_SET_FRAG_NUMBER + " INTEGER NOT NULL, "
                + COLUMN_SET_TIME + " REAL NOT NULL, "
                + COLUMN_SET_REPS + " INTEGER NOT NULL DEFAULT 1);";

        // Запускаем создание таблицы
        db.execSQL(SQL_CREATE_TAB_FILE);
        db.execSQL(SQL_CREATE_TAB_SET);
        Log.d(TAG, "Создана база данных  " + DATABASE_NAME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Запишем в журнал
        Log.d(TAG, "Обновляемся с версии " + oldVersion + " на версию " + newVersion);
    }


    // Если записей в базе нет, вносим запись
    public void createDefaultSetIfNeed()  {
        int count = this.getFilesCount();
        if(count ==0 ) {

            Calendar calendar = new GregorianCalendar();

            String dateFormat  = String.format("%s-%s-%s",
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH)+1,
                    calendar.get(Calendar.DAY_OF_MONTH));
            String timeFormat  = String.format("%s:%s:%s",
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND));
            DataFile file1 = new DataFile("Подтягивание 50раз за 4мин",
                    dateFormat, timeFormat,"Подтягивание",
                    "Подтягивание на перекладине", 6, 0,0);
            this.addFile(file1);

            long file1_id = getIdFromFileName("Подтягивание 50раз за 4мин");

            DataSet set1 = new DataSet(3,10,1);
            this.addSet(set1, file1_id);
            DataSet set2 = new DataSet(4,10,2);
            this.addSet(set2, file1_id);
            DataSet set3 = new DataSet(5,10,3);
            this.addSet(set3, file1_id);
            DataSet set4 = new DataSet(6,20,4);
            this.addSet(set4, file1_id);
            Log.d(TAG, "MyDatabaseHelper.createDefaultPersonIfNeed ... count = " +
                    this.getFilesCount());
        }
    }

    //получаем количество файлов (подходоа) в базе
    public int getFilesCount() {
        Log.i(TAG, "TempDBHelper.getSetCount ... " );
        String countQuery = "SELECT  * FROM " + TabFile.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    //Метод для добавления нового фрагмента подхода в список
    public void addSet(DataSet set, long file_id) {
        Log.d(TAG, "MyDatabaseHelper.addSet ... " + set.getNumberOfFrag());

        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(TabSet.COLUMN_SET_FILE_ID, file_id);
        cv.put(TabSet.COLUMN_SET_TIME, set.getTimeOfRep());
        cv.put(TabSet.COLUMN_SET_REPS, set.getReps());
        cv.put(TabSet.COLUMN_SET_FRAG_NUMBER, set.getNumberOfFrag());
        // вставляем строку
        db.insert(TabSet.TABLE_NAME, null, cv);
        // закрываем соединение с базой
        db.close();
    }

    //Метод для добавления нового файла с подходом в список
    public void addFile(DataFile file) {
        Log.d(TAG, "TempDBHelper.addFile ... " + file.getFileName());

        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(TabFile.COLUMN_FILE_NAME, file.getFileName());
        cv.put(TabFile.COLUMN_FILE_NAME_DATE, file.getFileNameDate());
        cv.put(TabFile.COLUMN_FILE_NAME_TIME, file.getFileNameTime());
        cv.put(TabFile.COLUMN_KIND_OF_SPORT, file.getKindOfSport());
        cv.put(TabFile.COLUMN_DESCRIPTION_OF_SPORT, file.getDescriptionOfSport());
        cv.put(TabFile.COLUMN_DELAY, file.getDelay());
        cv.put(TabFile.COLUMN_TYPE_FROM, file.getFileName());
        cv.put(TabFile.COLUMN_LIKED, file.getFileName());
        // вставляем строку
        db.insert(TabFile.TABLE_NAME, null, cv);
        // закрываем соединение с базой
        db.close();
    }

    //Удалить запись в таблице TabFile и все записи в таблице TabSet с id удалённой записи в TabFile
    public void deleteFileAndSets(long rowId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TabFile.TABLE_NAME, TabFile._ID + "=" + rowId, null);
        db.delete(TabSet.TABLE_NAME, TabSet.COLUMN_SET_FILE_ID + "=" + rowId, null);
        db.close();
    }
/*
    public void deleteSet(long rowId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, PersonTable._ID + "=" + rowId, null);
        db.close();
    }
    */
    //получаем ID по имени
    public long getIdFromFileName(String name){
        long currentID;
        // Создадим и откроем для чтения базу данных
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TabFile.TABLE_NAME,   // таблица
                new String[] { TabFile._ID},            // столбцы
                TabFile.COLUMN_FILE_NAME + "=?" ,                  // столбцы для условия WHERE
                new String[] {name},                  // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null);                   // порядок сортировки

        if (cursor != null) {
            cursor.moveToFirst();
            // Узнаем индекс каждого столбца
            int idColumnIndex = cursor.getColumnIndex(TabFile._ID);
            // Используем индекс для получения строки или числа
            currentID = cursor.getLong(idColumnIndex);
        }else {
            currentID = -1;
        }
        Log.d(TAG, "getIdFromName currentID = " + currentID);
        if (cursor != null) {
            cursor.close();
        }
        return currentID;
    }

    //вывод в лог всех строк базы
    public void displayDatabaseInfo() {
        // Создадим и откроем для чтения базу данных
        SQLiteDatabase db = this.getReadableDatabase();

        // Зададим условие для выборки - список столбцов
        String[] projection = {
                TabSet._ID,
                TabSet.COLUMN_SET_FILE_ID,
                TabSet.COLUMN_SET_TIME,
                TabSet.COLUMN_SET_REPS,
                TabSet.COLUMN_SET_FRAG_NUMBER};

        // Делаем запрос
        Cursor cursor = db.query(
                TabSet.TABLE_NAME,   // таблица
                projection,            // столбцы
                null,                  // столбцы для условия WHERE
                null,                  // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null);                   // порядок сортировки
        // Зададим условие для выборки - список столбцов
        String[] projectionFile = {
                TabFile._ID,
                TabFile.COLUMN_FILE_NAME,
                TabFile.COLUMN_FILE_NAME_DATE,
                TabFile.COLUMN_FILE_NAME_TIME,
                TabFile.COLUMN_KIND_OF_SPORT,
                TabFile.COLUMN_DESCRIPTION_OF_SPORT,
                TabFile.COLUMN_DELAY,
                TabFile.COLUMN_TYPE_FROM,
                TabFile.COLUMN_LIKED};

        // Делаем запрос
        Cursor cursorFile = db.query(
                TabFile.TABLE_NAME,   // таблица
                projectionFile,            // столбцы
                null,                  // столбцы для условия WHERE
                null,                  // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null);                   // порядок сортировки

        try {
            // Проходим через все ряды в таблице TabFile
            while (cursorFile.moveToNext()) {
                // Используем индекс для получения строки или числа
                int current_ID = cursorFile.getInt(
                        cursorFile.getColumnIndex(TabFile._ID));
                String current_nameFile = cursorFile.getString(
                        cursorFile.getColumnIndex(TabFile.COLUMN_FILE_NAME));
                String current_nameFileDate = cursorFile.getString(
                        cursorFile.getColumnIndex(TabFile.COLUMN_FILE_NAME_DATE));
                String current_nameFileTime = cursorFile.getString(
                        cursorFile.getColumnIndex(TabFile.COLUMN_FILE_NAME_TIME));
                String current_kindSport = cursorFile.getString(
                        cursorFile.getColumnIndex(TabFile.COLUMN_KIND_OF_SPORT));
                String current_descript = cursorFile.getString(
                        cursorFile.getColumnIndex(TabFile.COLUMN_DESCRIPTION_OF_SPORT));
                int current_delay = cursorFile.getInt(
                        cursorFile.getColumnIndex(TabFile.COLUMN_DELAY));
                int current_typeFrom = cursorFile.getInt(
                        cursorFile.getColumnIndex(TabFile.COLUMN_TYPE_FROM));
                int current_liked = cursorFile.getInt(
                        cursorFile.getColumnIndex(TabFile.COLUMN_LIKED));
                // Выводим построчно значения каждого столбца
                Log.d(TAG, "\n" + current_ID + " - " +
                        current_nameFile + " - " +
                        current_nameFileDate + " - " +
                        current_nameFileTime + " - " +
                        current_kindSport + " - " +
                        current_descript + " - " +
                        current_delay + " - " +
                        current_typeFrom + " - " +
                        current_liked);
            }
            // Проходим через все ряды в таблице TabSet
            while (cursor.moveToNext()) {
                // Используем индекс для получения строки или числа
                int currentID = cursor.getInt(
                        cursor.getColumnIndex(TabSet._ID));
                String current_FILE_ID = cursor.getString(
                        cursor.getColumnIndex(TabSet.COLUMN_SET_FILE_ID));
                String current_SET_TIME = cursor.getString(
                        cursor.getColumnIndex(TabSet.COLUMN_SET_TIME));
                int current_SET_REPS = cursor.getInt(
                        cursor.getColumnIndex(TabSet.COLUMN_SET_REPS));
                int current_SET_FRAG_NUMBER = cursor.getInt(
                        cursor.getColumnIndex(TabSet.COLUMN_SET_FRAG_NUMBER));
                // Выводим построчно значения каждого столбца
                Log.d(TAG, "\n" + currentID + " - " +
                        current_FILE_ID + " - " +
                        current_SET_TIME + " - " +
                        current_SET_REPS + " - " +
                        current_SET_FRAG_NUMBER);
            }
        } finally {
            // Всегда закрываем курсор после чтения
            cursor.close();
            cursorFile.close();
        }
    }

}
