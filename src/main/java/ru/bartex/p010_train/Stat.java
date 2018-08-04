package ru.bartex.p010_train;

import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Андрей on 13.05.2018.
 */
public class Stat {

    public static final int LIST_OF_FILE_ACTIVITY = 333;

    //метод получения данных из строки  (из строки списка - в PersonsListActivity)
    public static String[] getDataFromString(String ss){

        //индекс последнего двоеточия в строке ss
        int i1 =ss.lastIndexOf (":");
        //Log.d(TAG, "lastIndexOf (\":\") =  " + i1);
        //номер строки списка
        String stringNumber = ss.substring(i1+1, ss.length());
        //Log.d(TAG, "stringNumber =  " + stringNumber);
        //строка без номера
        String stringNoNumber = ss.substring(0, i1);
        //Log.d(TAG, "stringNoNumber =  " + stringNoNumber);
        //индекс последнего двоеточия в строке stringNoNumber
        int i2 =stringNoNumber.lastIndexOf (":");
        //Log.d(TAG, "lastIndexOf (\":\") =  " + i2);
        //количество повторений
        String stringReps = stringNoNumber.substring(i2+1, stringNoNumber.length());
        //Log.d(TAG, "stringReps =  " + stringReps);
        //Время между повторениями
        String stringTime = stringNoNumber.substring(0, i2);
        //Log.d(TAG, "stringTime =  " + stringTime);

        //Возвращаемый массив
        String[]data = {stringTime,stringReps,stringNumber};

        return data;
    }


    public static String getTimeString1 (Long timeInMillis){

        //формируем формат строки показа времени
        int minut = (int)((timeInMillis/60000)%60);
        int second = (int)((timeInMillis/1000)%60);
        int decim = Math.round((timeInMillis%1000)/100);
        String time = "";

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
        String time = "";

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
        String time = "";

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

    //Сортировка FileSaver свойству Тип  = формируем ArrayList  именами файлов определённого типа
    public  static ArrayList<String> getFilesWithType (String type){

    FileSaverLab mFileSaverLab = FileSaverLab.get();
    List<FileSaver> fileSavers =  mFileSaverLab.getFileSavers();
    ArrayList<String> tempFiles = new ArrayList<>();

    for (int i = 0; i< fileSavers.size(); i++){
        FileSaver fs = fileSavers.get(i);
        String s = fs.getTipe();
        if (s.equalsIgnoreCase(type)){
            tempFiles.add(fs.getTitle());
        }
    }
        return tempFiles;
    }
    //заполняем синглет-держатель отсечек новыми данными из списка отсечек
    public  static void addAllSetToSetLabFromList(ArrayList<String> from){
        //получаем ссылку на экземпляр SetLab
        SetLab setLab = SetLab.get();
        //стираем старые данные
        SetLab.getSets().clear();
        //смотрим размер списка
        int size = from.size();
        //парсим данные
        for (int i = 0; i<size; i++){
            String oneLine = from.get(i);
            String[] allValues = Stat.getDataFromString(oneLine);
            //пишем данные в Set
            Set newSet = new Set();
            newSet.setNumberOfFrag(Integer.parseInt(allValues[2]));
            newSet.setReps(Integer.parseInt(allValues[1]));
            newSet.setTimeOfRep(Float.parseFloat(allValues[0]));
            //Добавляем Set в SetLab
            setLab.addSet(newSet);
        }
    }

    //заполняем синглет-держатель имён файлов новыми данными из списка имён файлов
    public  static void addAllFileSaverToFileSaverLabFromList(ArrayList<String> from){

    //получаем экземпляр синглета-держателя списка имён файлов и  стираем список,
    // после чего читаем список из файла, парсим его и заполняем синглет данными
    FileSaverLab mFileSaverLab = FileSaverLab.get();
    mFileSaverLab.getFileSavers().clear();

    //парсим данные и пишем их в синглет-держатель списка имён с файлами
    for (int i = 0; i<from.size(); i++){
        String oneLine = from.get(i);
        String[] allValues = Stat.getDataFromString(oneLine);
        //пишем данные в FileSaver
        FileSaver newSaver = new FileSaver();
        newSaver.setTitle(allValues[0]);
        newSaver.setDate(allValues[1]);
        newSaver.setTipe(allValues[2]);
        newSaver.setNumberFile(i);
        //Добавляем FileSaver в FileSaverLab
        mFileSaverLab.addFileSaver(newSaver);
    }
    }

    public  static String getTypeOtsechki(String fromName){
        //получаем тип файла по его имени в списке файлов
        FileSaverLab saverLab = FileSaverLab.get();
        FileSaver saver = saverLab.getFileSaver(fromName);
        return saver.getTipe();
    }

    //покажем общее время подхода
    public static String countTotalTime(float timeOfSet, long kvant){
        //переводим секунды в милисекунды
        float millisTime = timeOfSet*1000;
        //покажем суммарное время подхода
        int minut = ((int)millisTime/60000)%60;
        int second = ((int)millisTime/1000)%60;
        int decim = (int)((millisTime%1000)/kvant);
        int hour = (int)((millisTime/3600000)%24);
        // общее время подхода
        String time = "";
        if (hour<1){
            if(minut<10) {
                time = String.format("Время  %d:%02d.%d",minut, second, decim);
            }else if (minut<60){
                time = String.format("Время  %02d:%02d.%d",minut,second,decim);
            }
        }else {
            time = String.format("Время  %d:%02d:%02d.%d",hour,minut,second,decim);
        }
        return time;
    }

}
