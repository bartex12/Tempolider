package ru.bartex.p010_train;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Андрей on 07.05.2018.
 */
public class FileSaverLab {

    private List<FileSaver> mFSavers;
    private static FileSaverLab sFileSaverLab;
    private static final String TAG = "33333";

    //статический метод получения экземпляра синглета
    public static FileSaverLab get(){
        if (sFileSaverLab == null){
            sFileSaverLab = new FileSaverLab();
        }
        return sFileSaverLab;
    }

    //закрытый конструктор
    private FileSaverLab(){
        mFSavers = new ArrayList<>();
    }

    //получение списка сохранённых файлов
    public List<FileSaver> getFileSavers(){
        return mFSavers;
    }

    //получение позиции в списке по имени  /если нет, то возвращает -1
    public int findIdList(String name){
        List<FileSaver> savers = getFileSavers();
        for (int i = 0; i < savers.size(); i++){
            String s = savers.get(i).getTitle();
            if (s.equalsIgnoreCase(name)) return i;
        }
        return -1;
    }

    //получаем список имён, дат, типов  из списка FileSaver в формате "s:s:s"
    public ArrayList<String> getArrayListNames(){
        ArrayList<String> arl = new ArrayList<>();
        String s = "";
        String s1 = "";
        String s2 = "";
        for(FileSaver list:mFSavers){
            s = list.getTitle();
            s1 =list.getDate();
            s2 =  list.getTipe();
            String ss = String.format("%s:%s:%s",s,s1,s2);
            arl.add(ss);
        }
        return arl;
    }

    //метод получения экземпляра класса FileSaver по его id
    public FileSaver getFileSaver(UUID id){

        for(FileSaver list:mFSavers){
            if (list.getId().equals(id))
                return list;
        }
        return null;
    }

    //метод получения экземпляра класса FileSaver  по  номеру
    public FileSaver getFileSaver(int number){

        for(FileSaver list:mFSavers){
            if (list.getNumberFile()== number)
                return list;
        }
        return null;
    }

    //метод получения экземпляра класса FileSaver  по имени файла
    public FileSaver getFileSaver(String nameFile){

        for(FileSaver list:mFSavers){
            if ((list.getTitle()).equalsIgnoreCase(nameFile))
                return list;
        }
        return null;
    }

    //добавление нового имени файла в список файлов
    public void addFileSaver(FileSaver fileSaver){

        mFSavers.add(fileSaver);
    }

    //добавление нового имени файла в список файлов в произвольную позицию
    public void addFileSaver(FileSaver fileSaver, int position){

        mFSavers.add(position, fileSaver);
    }

    //получаем список имён файлов из FileSaverLab
    public ArrayList<String> getListFullNamesOfFiles(){

        ArrayList<String> listNamesOfFiles = new ArrayList<>();
        //заводим список сохранённых файлов ArrayList и пишем в лог
        for (FileSaver set: mFSavers){
            String name =set.getTitle();
            String date = set.getDate();
            String type = set.getTipe();
            String nameOfFile = String.format("%s:%s:%s",name,date,type);
            Log.d(TAG, " FileSaver nameOfFile = "  + nameOfFile);
            //Log.d(TAG, " number = "  + number);
            listNamesOfFiles.add(nameOfFile);
        }
        return listNamesOfFiles;
    }
}
