package ru.bartex.p010_train.ru.bartex.p010_train.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
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
                + COLUMN_TYPE_FROM + " TEXT NOT NULL DEFAULT 'type_timemeter', "
                + COLUMN_DELAY + " INTEGER NOT NULL DEFAULT 6);";

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
    public void createDefaultSetIfNeed() {
        int count = this.getFilesCount();
        if (count == 0) {

            //получаем дату и время в нужном для базы данных формате
            String dateFormat = getDateString();
            String timeFormat = getTimeString();

            //создаём экземпляр класса DataFile в конструкторе
            DataFile file1 = new DataFile(P.FILENAME_OTSECHKI_SEC,
                    dateFormat, timeFormat, "Подтягивание",
                    "Подтягивание на перекладине", P.TYPE_TIMEMETER, 6);

            //добавляем запись в таблицу TabFile, используя данные DataFile и получаем id записи
            long file1_id = this.addFile(file1);

            //создаём экземпляр класса DataSet в конструкторе
            DataSet set1 = new DataSet(2f, 1, 1);
            //добавляем запись в таблицу TabSet, используя данные DataSet
            this.addSet(set1, file1_id);
            // повторяем для всех фрагментов подхода
            DataSet set2 = new DataSet(2.5f, 1, 2);
            this.addSet(set2, file1_id);
            DataSet set3 = new DataSet(3f, 1, 3);
            this.addSet(set3, file1_id);
            DataSet set4 = new DataSet(3.5f, 1, 4);
            this.addSet(set4, file1_id);

            Log.d(TAG, "MyDatabaseHelper.createDefaultPersonIfNeed ... count = " +
                    this.getFilesCount());

            //создаём экземпляр класса DataFile в конструкторе
            DataFile file2 = new DataFile(P.FILENAME_OTSECHKI_TEMP,
                    dateFormat, timeFormat, "Пистолетики",
                    "Полиатлон", P.TYPE_TEMPOLEADER, 6);

            //добавляем запись в таблицу TabFile, используя данные DataFile и получаем id записи
            long file2_id = this.addFile(file2);

            //создаём экземпляр класса DataSet в конструкторе
            DataSet set11 = new DataSet(2.2f, 3, 1);
            //добавляем запись в таблицу TabSet, используя данные DataSet
            this.addSet(set11, file2_id);
            // повторяем для всех фрагментов подхода
            DataSet set22 = new DataSet(2.3f, 3, 2);
            this.addSet(set22, file2_id);
            DataSet set33 = new DataSet(2.5f, 4, 3);
            this.addSet(set33, file2_id);
            DataSet set44 = new DataSet(2.7f, 2, 4);
            this.addSet(set44, file2_id);

            Log.d(TAG, "MyDatabaseHelper.createDefaultPersonIfNeed ... count = " +
                    this.getFilesCount());
        }
    }

    public String getDateString() {
        Calendar calendar = new GregorianCalendar();
        return String.format("%s-%s-%s",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    public String getTimeString() {
        Calendar calendar = new GregorianCalendar();
        return String.format("%s:%s:%s",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
    }

    //получаем количество файлов (сохранённых подходов) в базе
    public int getFilesCount() {
        Log.i(TAG, "TempDBHelper.getFilesCount ... ");
        String countQuery = "SELECT  * FROM " + TabFile.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    //получаем количество фрагментов подхода в подходе
    public int getSetFragmentsCount(long fileId) {

        Log.i(TAG, "TempDBHelper.getSetFragmentsCount");
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "select " + TabSet.COLUMN_SET_TIME + " from " + TabSet.TABLE_NAME +
                " where " + TabSet.COLUMN_SET_FILE_ID + " = ? ";
        Cursor mCursor = db.rawQuery(query, new String[]{String.valueOf(fileId)});

        int count = mCursor.getCount();
        mCursor.close();
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

    //Метод для вставки нового фрагмента подхода в список ПОСЛЕ выделенного фрагмента
    public void addSetAfter(DataSet set, long file_id, int numberOfString) {
        Log.d(TAG, "MyDatabaseHelper.addSetAfter ... " + set.getNumberOfFrag());

        SQLiteDatabase db = this.getWritableDatabase();
        //получаем  фрагменты подхода с ID = file_id
        String query = "select " + TabSet._ID + " , " +  TabSet.COLUMN_SET_FRAG_NUMBER +
                " from " + TabSet.TABLE_NAME +
                " where " + TabSet.COLUMN_SET_FILE_ID + " = ? " +
                " order by " + TabSet.COLUMN_SET_FRAG_NUMBER;
        Cursor mCursor = db.rawQuery(query, new String[]{String.valueOf(file_id)});
        mCursor.moveToPosition(numberOfString-1);
        int fragCount = mCursor.getCount();
        Log.d(TAG, "MyDatabaseHelper.addSetAfter mCursor.getCount()= " + fragCount +
                " numberOfString-1 = " + (numberOfString-1));
        try {
                // Проходим через все строки в курсоре начиная с position
            while (mCursor.moveToNext()){
                    //вычисляем id текущей строки курсора
                    long id = mCursor.getLong(mCursor.getColumnIndex(TabSet._ID));

                    ContentValues updatedValues = new ContentValues();
                    updatedValues.put(TabSet.COLUMN_SET_FRAG_NUMBER, (mCursor.getPosition() + 2));
                    db.update(TabSet.TABLE_NAME, updatedValues,
                            TabSet.COLUMN_SET_FILE_ID + "=" + file_id +
                                    " AND " + TabSet._ID + "=" + id, null);
                    Log.d(TAG, "addSetAfter mCursor.getPosition() = " +
                            mCursor.getPosition() + " id строки =" + id);
                }
            //Добавляем фрагмент подхода созданный в DetailActiviti
            this.addSet(set, file_id);
            //пересчитывает  номера фрагментов подхода
            //this.rerangeSetFragments(file_id);

        } finally {
            //закрываем курсор
            mCursor.close();
            // закрываем соединение с базой
            db.close();
        }
    }

    //Метод для вставки нового фрагмента подхода в список ДО  выделенного фрагмента
    public void addSetBefore(DataSet set, long file_id, int numberOfString) {
        Log.d(TAG, "MyDatabaseHelper.addSetBefore ... getNumberOfFrag = " + set.getNumberOfFrag());

        SQLiteDatabase db = this.getWritableDatabase();
        //получаем  фрагменты подхода с ID = file_id
        String query = "select " + TabSet._ID + " , " +  TabSet.COLUMN_SET_FRAG_NUMBER +
                " from " + TabSet.TABLE_NAME +
                " where " + TabSet.COLUMN_SET_FILE_ID + " = ? " +
                " order by " + TabSet.COLUMN_SET_FRAG_NUMBER;
        Cursor mCursor = db.rawQuery(query, new String[]{String.valueOf(file_id)});
        mCursor.moveToPosition(numberOfString-2);
        Log.d(TAG, "MyDatabaseHelper.addSetBefore isBeforeFirst() =" + mCursor.isBeforeFirst());
        int fragCount = mCursor.getCount();
        Log.d(TAG, "MyDatabaseHelper.addSetBefore mCursor.getCount()= " + fragCount +
                " numberOfString = " + (numberOfString-2));
        try {
            // Проходим через все строки в курсоре начиная с position
            while (mCursor.moveToNext()){
                //вычисляем id текущей строки курсора
                long id = mCursor.getLong(mCursor.getColumnIndex(TabSet._ID));

                ContentValues updatedValues = new ContentValues();
                updatedValues.put(TabSet.COLUMN_SET_FRAG_NUMBER, (mCursor.getPosition() + 2));
                db.update(TabSet.TABLE_NAME, updatedValues,
                        TabSet.COLUMN_SET_FILE_ID + "=" + file_id +
                                " AND " + TabSet._ID + "=" + id, null);
                Log.d(TAG, "addSetBefore mCursor.getPosition() = " +
                        mCursor.getPosition() + " id строки =" + id);
            }
            //Добавляем фрагмент подхода созданный в DetailActiviti
            this.addSet(set, file_id);
            //пересчитывает  номера фрагментов подхода
            //this.rerangeSetFragments(file_id);

        } finally {
            //закрываем курсор
            mCursor.close();
            // закрываем соединение с базой
            db.close();
        }
    }

    /**
     * Пересчитывает  номера фрагментов подхода после удаления какого либо фрагмента подхода
     */
    public void rerangeSetFragments(long fileId) throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select " + TabSet._ID + " , " +  TabSet.COLUMN_SET_FRAG_NUMBER +
                " from " + TabSet.TABLE_NAME +
                " where " + TabSet.COLUMN_SET_FILE_ID + " = ? " +
                " order by " + TabSet.COLUMN_SET_FRAG_NUMBER;
        Cursor mCursor = db.rawQuery(query, new String[]{String.valueOf(fileId)});

        try {
            // Проходим через все строки в курсоре
            while (mCursor.moveToNext()) {

                //вычисляем id текущей строки курсора
                long id = mCursor.getLong(mCursor.getColumnIndex(TabSet._ID));

                ContentValues updatedValues = new ContentValues();
                updatedValues.put(TabSet.COLUMN_SET_FRAG_NUMBER, (mCursor.getPosition()+1));
                db.update(TabSet.TABLE_NAME, updatedValues,
                        TabSet.COLUMN_SET_FILE_ID + "=" + fileId +
                                " AND " + TabSet._ID + "=" + id, null);
                Log.d(TAG, "rerangeSetFragments mCursor.getPosition() = " +
                        mCursor.getPosition() + " id строки =" + id);
            }
        } finally {
            // Всегда закрываем курсор после чтения
            mCursor.close();
        }
    }


    //Метод для добавления нового файла с подходом в список
    public long addFile(DataFile file) {
        Log.d(TAG, "TempDBHelper.addFile ... " + file.getFileName());

        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(TabFile.COLUMN_FILE_NAME, file.getFileName());
        cv.put(TabFile.COLUMN_FILE_NAME_DATE, file.getFileNameDate());
        cv.put(TabFile.COLUMN_FILE_NAME_TIME, file.getFileNameTime());
        cv.put(TabFile.COLUMN_KIND_OF_SPORT, file.getKindOfSport());
        cv.put(TabFile.COLUMN_DESCRIPTION_OF_SPORT, file.getDescriptionOfSport());
        cv.put(TabFile.COLUMN_TYPE_FROM, file.getType_From());
        cv.put(TabFile.COLUMN_DELAY, file.getDelay());
        // вставляем строку
        long ID = db.insert(TabFile.TABLE_NAME, null, cv);
        // закрываем соединение с базой
        db.close();
        return ID;
    }

    //Метод для изменения имени файла
    public void updateFileName(String nameFile, long fileId) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues updatedValues = new ContentValues();
        updatedValues.put(TabFile.COLUMN_FILE_NAME, nameFile);
        db.update(TabFile.TABLE_NAME, updatedValues,
                TabFile._ID + "=" + fileId, null);
    }

    //Метод для изменения времени и количества повторений фрагмента подхода  по известному DataSet
    public void updateSetFragment(DataSet mDataSet) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues updatedValues = new ContentValues();
        updatedValues.put(TabSet.COLUMN_SET_TIME, mDataSet.getTimeOfRep());
        updatedValues.put(TabSet.COLUMN_SET_REPS, mDataSet.getReps());

        db.update(TabSet.TABLE_NAME, updatedValues,
                TabSet.COLUMN_SET_FILE_ID + "=" + mDataSet.getFile_id()+
                        " AND " + TabSet._ID + "=" + mDataSet.getSet_id(), null);
/*
        Log.d(TAG, "updateSetFragment COLUMN_SET_TIME  = " + mDataSet.getTimeOfRep()+
                "  COLUMN_SET_REPS = " +  mDataSet.getReps() +
                "  mDataSet.getFile_id() = " + mDataSet.getFile_id()+
                "  mDataSet.getSet_id() = " + mDataSet.getSet_id());
        */
    }

    // создать копию файла  в базе данных и получить его id
    public long createFileCopy(String finishFileName, long fileId, String endName){
        //количество фрагментов подхода
        int countOfSet =this.getSetFragmentsCount(fileId);
        String newFileName = finishFileName + endName;
        Log.d(TAG, "createFileCopy newFileName = " + newFileName);
        //создаём и записываем в базу копию файла на случай отмены изменений
        DataFile dataFileCopy = this.getAllFilesData(fileId);
        dataFileCopy.setFileName(newFileName);
        long fileIdCopy = this.addFile(dataFileCopy);
        //записываем фрагменты подхода в файл-копию
        for (int i = 0; i<countOfSet; i++ ){
            DataSet dataSet = this.getOneSetFragmentData(fileId, i);
            this.addSet(dataSet,fileIdCopy);
        }
        Log.d(TAG, "createFileCopy фрагментов  = " + this.getSetFragmentsCount(fileIdCopy));
        return  fileIdCopy;
    }

    //Удалить запись в таблице TabFile и все записи в таблице TabSet с id удалённой записи в TabFile
    public void deleteFileAndSets(long rowId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TabFile.TABLE_NAME, TabFile._ID + "=" + rowId, null);
        db.delete(TabSet.TABLE_NAME, TabSet.COLUMN_SET_FILE_ID + "=" + rowId, null);
        db.close();
    }

    //удаляем строку с номером fragmentNumber, относящуюся к файлу с id =fileId
        public void deleteSet(long fileId, long fragmentNumber) {

            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TabSet.TABLE_NAME, TabSet.COLUMN_SET_FILE_ID +
                    " =? " + " AND " + TabSet.COLUMN_SET_FRAG_NUMBER +
                    " =? ", new String[]{String.valueOf(fileId),String.valueOf(fragmentNumber)});
            db.close();
        }



    //получаем ID по имени
    public long getIdFromFileName(String name) {
        long currentID;
        // Создадим и откроем для чтения базу данных
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TabFile.TABLE_NAME,   // таблица
                new String[]{TabFile._ID},            // столбцы
                TabFile.COLUMN_FILE_NAME + "=?",                  // столбцы для условия WHERE
                new String[]{name},                  // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null);                   // порядок сортировки

        if ((cursor != null) && (cursor.getCount() != 0)) {
            cursor.moveToFirst();
            // Узнаем индекс каждого столбца
            int idColumnIndex = cursor.getColumnIndex(TabFile._ID);
            // Используем индекс для получения строки или числа
            currentID = cursor.getLong(idColumnIndex);
        } else {
            currentID = -1;
        }
        Log.d(TAG, "getIdFromName currentID = " + currentID);
        if (cursor != null) {
            cursor.close();
        }
        return currentID;
    }

    /**
     * Возвращает задержку старта файла по его ID
     */
    public int  getFileDelayFromTabFile( String name) throws SQLException {

        long rowId = this.getIdFromFileName(name);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor = db.query(true, TabFile.TABLE_NAME,
                null,
                TabFile._ID + "=" + rowId,
                null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        int delay = mCursor.getInt(mCursor.getColumnIndex(TabFile.COLUMN_DELAY));

        if (mCursor != null) {
            mCursor.close();
        }
        return delay;
    }

    /**
     * Возвращает имя файла по его ID
     */
    public String getFileNameFromTabFile( long rowId) throws SQLException {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor mCursor = db.query(true, TabFile.TABLE_NAME,
                null,
                TabFile._ID + "=" + rowId,
                null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        String fileName = mCursor.getString(mCursor.getColumnIndex(TabFile.COLUMN_FILE_NAME));
        Log.d(TAG, "getFileNameFromTabFile fileName = " + fileName);
        mCursor.close();

        return fileName;
    }

    /**
     * Возвращает тип файла по его ID
     */
    public String getFileTypeFromTabFile( long rowId) throws SQLException {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor mCursor = db.query(true, TabFile.TABLE_NAME,
                null,
                TabFile._ID + "=" + rowId,
                null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        String fileType = mCursor.getString(mCursor.getColumnIndex(TabFile.COLUMN_TYPE_FROM));

        mCursor.close();

        return fileType;
    }



    /**
     * Возвращает курсор с набором данных времени, количества повторений
     * и порядкового номера фрагмента для всех фрагментов одного подхода с id = rowId
     * отсортировано по COLUMN_SET_FRAG_NUMBER, иначе в Андроид 4 работает неправильно
     */
    public Cursor getAllSetFragments(long rowId) throws SQLException {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor = db.query(true, TabSet.TABLE_NAME,
                new String[]{TabSet.COLUMN_SET_TIME, TabSet.COLUMN_SET_REPS, TabSet.COLUMN_SET_FRAG_NUMBER},
                TabSet.COLUMN_SET_FILE_ID + "=" + rowId,
                null, null, null, TabSet.COLUMN_SET_FRAG_NUMBER, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * Возвращает объект DataFile с данными файла из таблицы TabFile с номмером ID = rowId
     */
    public DataFile getAllFilesData(long rowId) throws SQLException {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursorFile = db.query(true, TabFile.TABLE_NAME,
                new String[]{TabFile._ID, TabFile.COLUMN_FILE_NAME, TabFile.COLUMN_FILE_NAME_DATE,
                        TabFile.COLUMN_FILE_NAME_TIME, TabFile.COLUMN_KIND_OF_SPORT,
                        TabFile.COLUMN_DESCRIPTION_OF_SPORT, TabFile.COLUMN_TYPE_FROM,
                        TabFile.COLUMN_DELAY},
                TabFile._ID + "=" + rowId,
                null, null, null, null, null);
        if ((cursorFile != null)&& (cursorFile.getCount()>0)) {
            cursorFile.moveToFirst();
        }
            Log.d(TAG, "getAllFilesData cursorFile.getCount() = " + cursorFile.getCount());

            // Используем индекс для получения строки или числа
            long current_ID = cursorFile.getLong(
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
            String current_typeFrom = cursorFile.getString(
                    cursorFile.getColumnIndex(TabFile.COLUMN_TYPE_FROM));
            int current_delay = cursorFile.getInt(
                    cursorFile.getColumnIndex(TabFile.COLUMN_DELAY));

            DataFile dataFile = new DataFile(current_ID,current_nameFile,
                    current_nameFileDate,current_nameFileTime,current_kindSport,
                    current_descript,current_typeFrom,current_delay);

            cursorFile.close();
            return dataFile;

    }

    /**
     * Возвращает DataSet с номером фрагмента подхода position из файла с номером ID = fileId
     */
    public DataSet getOneSetFragmentData(long fileId, int position) throws SQLException {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursorFile = db.query(true, TabSet.TABLE_NAME,
                new String[]{TabSet._ID, TabSet.COLUMN_SET_FILE_ID, TabSet.COLUMN_SET_FRAG_NUMBER,
                        TabSet.COLUMN_SET_TIME, TabSet.COLUMN_SET_REPS},
                TabSet.COLUMN_SET_FILE_ID + "=" + fileId,
                null, null, null, TabSet.COLUMN_SET_FRAG_NUMBER, null);
        if ((cursorFile != null)&& (cursorFile.getCount()>=position)) {
            cursorFile.moveToPosition(position);
        }else {
            Log.d(TAG, "getAllSetData cursorFile.getCount() = " + cursorFile.getCount()+
            "  position" + position);
        }
        // Используем индекс для получения строки или числа
        long current_ID = cursorFile.getLong(
                cursorFile.getColumnIndex(TabSet._ID));
        long current_fileId = cursorFile.getLong(
                cursorFile.getColumnIndex(TabSet.COLUMN_SET_FILE_ID));
        float current_setTime = cursorFile.getFloat(
                cursorFile.getColumnIndex(TabSet.COLUMN_SET_TIME));
        int current_setReps = cursorFile.getInt(
                cursorFile.getColumnIndex(TabSet.COLUMN_SET_REPS));
        int current_nameFragNumber = cursorFile.getInt(
                cursorFile.getColumnIndex(TabSet.COLUMN_SET_FRAG_NUMBER));

        DataSet dataset = new DataSet(current_ID, current_fileId,
                current_setTime, current_setReps, current_nameFragNumber);

        cursorFile.close();
        return dataset;
    }


    /**
     * Возвращает курсор с набором данных времени всех фрагментов одного подхода с id = rowId
     */
    public Cursor getAllSetFragmentsRaw(long rowId) throws SQLException {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select " + TabSet.COLUMN_SET_TIME + " from " + TabSet.TABLE_NAME +
                " where " + TabSet.COLUMN_SET_FILE_ID + " = ? ";
        Cursor mCursor = db.rawQuery(query, new String[]{String.valueOf(rowId)});
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }


    /**
     * Возвращает время фрагмента подхода с номером position для файла подхода с id = rowId
     */
    public float getTimeOfRepInPosition(long rowId, int position) throws SQLException {

        SQLiteDatabase db = this.getReadableDatabase();

        String query = "select " + TabSet.COLUMN_SET_TIME + " from " + TabSet.TABLE_NAME +
                " where " + TabSet.COLUMN_SET_FILE_ID + " = ? ";
        Cursor mCursor = db.rawQuery(query, new String[]{String.valueOf(rowId)});

        if ((mCursor != null) && (mCursor.getCount() != 0)) {
            mCursor.moveToPosition(position);
            // Используем индекс для получения времени фрагмента подхода с номером position
            float currentTime = mCursor.getFloat(mCursor.getColumnIndex(TabSet.COLUMN_SET_TIME));
            Log.d(TAG, "getTimeOfRepInPosition position = " + position +
                    " currentTime = " + currentTime);

            return currentTime;
        }
        if (mCursor != null) {
            mCursor.close();
        }
        //если курсор = null или в курсоре нет данных, то
        return -1;
    }

    /**
     * Возвращает количество повторений для фрагмента подхода с номером position
     * для файла подхода с id = rowId
     */
    public int getRepsInPosition(long rowId, int position) throws SQLException {

        SQLiteDatabase db = this.getReadableDatabase();

        String query = "select " + TabSet.COLUMN_SET_REPS + " from " + TabSet.TABLE_NAME +
                " where " + TabSet.COLUMN_SET_FILE_ID + " = ? ";
        Cursor mCursor = db.rawQuery(query, new String[]{String.valueOf(rowId)});

        if ((mCursor != null) && (mCursor.getCount() != 0)) {
            mCursor.moveToPosition(position);
            // Используем индекс для получения времени фрагмента подхода с номером position
            int currentReps = mCursor.getInt(mCursor.getColumnIndex(TabSet.COLUMN_SET_REPS));
            Log.d(TAG, "getTimeOfRepInPosition position = " + position +
                    " currentReps = " + currentReps);

            return currentReps;
        }
        if (mCursor != null) {
            mCursor.close();
        }
        //если курсор = null или в курсоре нет данных, то
        return -1;
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
                TabFile.COLUMN_TYPE_FROM,
                TabFile.COLUMN_DELAY};

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
                String current_typeFrom = cursorFile.getString(
                        cursorFile.getColumnIndex(TabFile.COLUMN_TYPE_FROM));
                int current_delay = cursorFile.getInt(
                        cursorFile.getColumnIndex(TabFile.COLUMN_DELAY));
                // Выводим построчно значения каждого столбца
                Log.d(TAG, "\n" + current_ID + " - " +
                        current_nameFile + " - " +
                        current_nameFileDate + " - " +
                        current_nameFileTime + " - " +
                        current_kindSport + " - " +
                        current_descript + " - " +
                        current_typeFrom + " - " +
                        current_delay);
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

    public float getSumOfTimeSet(long rowId) throws SQLException {

        SQLiteDatabase db = this.getReadableDatabase();

        String query = " select " + " sum( " + TabSet.COLUMN_SET_TIME +
        " * " + TabSet.COLUMN_SET_REPS + " ) " +
                " from " + TabSet.TABLE_NAME + " where " + TabSet.COLUMN_SET_FILE_ID + " = ?";

        Cursor mCursor = db.rawQuery(query, new String[]{String.valueOf(rowId)});
        float sum = -1;
        if ((mCursor != null) && (mCursor.getCount() != 0)) {
            mCursor.moveToFirst();

            // Используем индекс для получения строки или числа
            sum = mCursor.getFloat(0);
        }
        Log.d(TAG, "getSumOfTimeSet sum = " + sum);
        if (mCursor != null) {
            mCursor.close();
        }
        return sum;
    }

    public int getSumOfRepsSet(long rowId) throws SQLException {

        SQLiteDatabase db = this.getReadableDatabase();

        String query = " select " + " sum( " + TabSet.COLUMN_SET_REPS + " ) " +
                " from " + TabSet.TABLE_NAME + " where " + TabSet.COLUMN_SET_FILE_ID + " = ?";

        Cursor mCursor = db.rawQuery(query, new String[]{String.valueOf(rowId)});
        int sum = -1;
        if ((mCursor != null) && (mCursor.getCount() != 0)) {
            mCursor.moveToFirst();

            // Используем индекс для получения строки или числа
            sum = mCursor.getInt(0);
        }
        Log.d(TAG, "getSumOfRepsSet sum = " + sum);
        if (mCursor != null) {
            mCursor.close();
        }
        return sum;
    }

    //получить названия всех файлов с типом файла P.TYPE_TIMEMETER
    public Cursor getAllFilesFromTimemeter() throws SQLException {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select " + TabFile._ID + " , " + TabFile.COLUMN_FILE_NAME  + " from " + TabFile.TABLE_NAME +
                " where " + TabFile.COLUMN_TYPE_FROM + " = ? ";
        Cursor mCursor = db.rawQuery(query, new String[]{P.TYPE_TIMEMETER});
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    //получить названия всех файлов с типом файла P.TYPE_TIMEMETER
    public Cursor getAllFilesWhithType(String type) throws SQLException {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select " + TabFile._ID + " , " + TabFile.COLUMN_FILE_NAME  + " from " + TabFile.TABLE_NAME +
                " where " + TabFile.COLUMN_TYPE_FROM + " = ? ";
        Cursor mCursor = db.rawQuery(query, new String[]{type});
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
}