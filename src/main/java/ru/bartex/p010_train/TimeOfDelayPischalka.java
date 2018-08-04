package ru.bartex.p010_train;

/**
 * Created by Андрей on 24.04.2018.
 * класс сделан только для работоы с задержкой старта
 * задержку можно было бы определять в SetLab, но тогда в onCreate SingleFragmentActivity нужно
 * инициировать SetLab, который держит информацию для фрагмента
 */

public class TimeOfDelayPischalka {

    private static TimeOfDelayPischalka mDelay;  //экземпляр синглета
    private static int mTimeDelay;  //время задержки

    //статический метод синглета, возвращающий или создающий сам себя
    public static TimeOfDelayPischalka get(){
        if (mDelay == null){
            mDelay = new TimeOfDelayPischalka();
        }
        return mDelay;
    }
    //закрытый конструктор
    private TimeOfDelayPischalka(){
        mTimeDelay = 5;
    }

    public int getTimeDelay() {
        return mTimeDelay;
    }

    public void setTimeDelay(int timeDelay) {
        mTimeDelay = timeDelay;
    }
}

