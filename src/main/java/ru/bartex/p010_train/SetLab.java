package ru.bartex.p010_train;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Андрей on 02.05.2018.
 */
public class SetLab {

    private static final String TAG = "33333";

    private static List<Set> mSets;
    private static SetLab sSetLab;
    private static Set mSet;
    private static int mDelay;

    //статический метод получения экземпляра синглета
    public static SetLab get() {
        if (sSetLab == null) {
            sSetLab = new SetLab();
        }
        return sSetLab;
    }

    //закрытый конструктор
    private SetLab() {
        mSets = new ArrayList<>();
        mDelay = 5;
    }

    //получение списка данных
    public static List<Set> getSets() {

        return mSets;
    }

    //метод получения экземпляра класса Set по его id
    public Set getSet(UUID id) {

        for (Set set : mSets) {
            if (set.getId().equals(id))
                return set;
        }
        return null;
    }

    //метод получения экземпляра класса Set (фрагмента подхода) по  номеру фрагмента подхода
    public static Set getSet(int number) {

        for (Set set : mSets) {
            if (set.getNumberOfFrag() == number) return set;
        }
        return null;
    }

    //добавление данных  фрагмента подхода в конец списка
    public void addSet(Set s) {

        mSets.add(s);
    }

    //добавление данных  фрагмента подхода в определённое место списка
    public void addSetInNumber(Set s, int numberOfFrag) {

        mSets.add(numberOfFrag, s);
    }

    //удаление данных для фрагмента подхода
    public static List<Set> removeSet(int mNumberOfFrag) {

        mSets.remove(mNumberOfFrag);
        return mSets;
    }

    //Пересчёт после удаления
    public static void reRangeSet(int pos) {

        for (int i = pos; i < mSets.size(); i++) {
            mSet = SetLab.getSet(i + 1);
            mSet.setNumberOfFrag(i);
        }
    }

    //получение задержки старта
    public static int getDelay() {

        return mDelay;
    }

    //установка задержки старта
    public static void setDelay(int delay) {

        mDelay = delay;
    }

    //считаем общее время подхода
    public static float countSetTime() {

        float timeOfSet = 0;
        int array_size = mSets.size();
        for (int i = 0; i < array_size; i++) {
            timeOfSet += (mSets.get(i).getTimeOfRep()) * (mSets.get(i).getReps());
        }
        return timeOfSet;
    }

    //считаем общее количество повторений в подходе
    public static int countTotalReps() {

        int totalReps = 0;
        int array_size = mSets.size();
        for (int i = 0; i < array_size; i++) {
            totalReps += mSets.get(i).getReps();
        }
        return totalReps;
    }

    //заполняем синглет-держатель отсечек новыми данными из списка отсечек секундомера
    public SetLab fillSetFromArrayListSecundomer(ArrayList<String> arlSec) {
        //стираем список перед его новым заполнением
        mSets.clear();
        int size = arlSec.size();
        //заполняем список данными из интента, устанавливая количество повторов =1
        for (int i = 0; i < size; i++) {
            double timeTransfer = Double.parseDouble(arlSec.get(i));
            Set newSet = new Set();
            newSet.setNumberOfFrag(i);
            newSet.setReps(1);
            newSet.setTimeOfRep((float) timeTransfer);
            sSetLab.addSet(newSet);
        }
        return sSetLab;
    }

    //заполняем синглет-держатель отсечек новыми данными из списка отсечек
    public  SetLab  addAllSetInSetLabFromFormatList(ArrayList<String> from){
        //стираем список перед его новым заполнением
        mSets.clear();
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
            sSetLab.addSet(newSet);
        }
        return sSetLab;
    }

    //копирование данных в список
    public List<Set> getSetListCopy(){
        List<Set> setListCopy = new ArrayList<>();
        for (Set set: mSets){
            setListCopy.add(set);
        }

        return setListCopy;
    }

    //заполняем синглет-держатель отсечек  данными из списка-копии отсечек
    public  static SetLab returnToOldSetList(List<Set> fromCopy){

/*
        //так не работает

        //смотрим размер списка
        int size = fromCopy.size();

        // копируем данные  из резервной копии списка отсечек
        for (int i = 0; i<size; i++){
            mSets.get(i).setTimeOfRep(fromCopy.get(i).getTimeOfRep());
            mSets.get(i).setReps(fromCopy.get(i).getReps());
            mSets.get(i).setNumberOfFrag(fromCopy.get(i).getNumberOfFrag());
        }
        */

        //стираем список перед его новым заполнением
        mSets.clear();
        //смотрим размер списка
        int size = fromCopy.size();
        // копируем данные из резервной копии списка отсечек
        for (int i = 0; i<size; i++){
            Set oldSet = new Set();
            oldSet.setNumberOfFrag(fromCopy.get(i).getNumberOfFrag());
            oldSet.setReps(fromCopy.get(i).getReps());
            oldSet.setTimeOfRep(fromCopy.get(i).getTimeOfRep());
            //Добавляем Set в SetLab
            sSetLab.addSet(oldSet);
        }
/*
        //выводим в лог список отсечек
        for (Set set:mSets){
            float time = set.getTimeOfRep();
            int reps = set.getReps();
            int i = set.getNumberOfFrag();
            Log.d(TAG, "SetLab returnToOldSetList = " + time + "  " + reps + "  " + (i+1));
        }
*/
        return sSetLab;
    }

    //заполняем синглет-держатель отсечек  данными интервалов времени из списка-копии отсечек
    public  static SetLab returnToOldTimeOrReps(List<Set> fromCopy,
                                                boolean returnTime, boolean all, int position){

        //смотрим размер списка
        int size = fromCopy.size();

        //если returnTime = true, возвращаемся к старому времени
        if (returnTime){
            //если all = true, возвращаемся к старому времени во всём списке
            if (all){
                // копируем время  из резервной копии списка отсечек во все позиции,
                // position игнорируем
                for (int i = 0; i<size; i++){
                    mSets.get(i).setTimeOfRep(fromCopy.get(i).getTimeOfRep());
                }
                //если all = false, возвращаемся к старому времени в position
            }else {
                // копируем время  из резервной копии списка отсечек в  позицию position
                mSets.get(position).setTimeOfRep(fromCopy.get(position).getTimeOfRep());
            }
            //если returnTime = false, возвращаемся к старому количеству повторений
        }else {
            //если all = true, возвращаемся к старому количеству повторений во всём списке
            if (all){
                // копируем количество повторений  из резервной копии списка отсечек во все позиции,
                // position игнорируем
                for (int i = 0; i<size; i++){
                    mSets.get(i).setReps(fromCopy.get(i).getReps());
                }
                //если all = false, возвращаемся к старому количеству повторений в position
            }else {
                // копируем количество повторений  из резервной копии списка отсечек в позицию position
                mSets.get(position).setReps(fromCopy.get(position).getReps());
            }
        }
        /*
        //выводим в лог список отсечек
        for (Set set:mSets){
            float time = set.getTimeOfRep();
            int reps = set.getReps();
            int i = set.getNumberOfFrag();
            Log.d(TAG, "SetLab returnToOldSetList = " + time + "  " + reps + "  " + (i+1));
        }
        */
        return sSetLab;
    }

}