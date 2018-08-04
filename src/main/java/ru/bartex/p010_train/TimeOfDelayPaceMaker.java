package ru.bartex.p010_train;

/**
 * Created by Андрей on 02.05.2018.
 */
public class TimeOfDelayPaceMaker {

    private int mTimeDelay;

    TimeOfDelayPaceMaker(int timeDelay){
        mTimeDelay = timeDelay;
    }

    public int getTimeDelay() {
        return mTimeDelay;
    }

    public void setTimeDelay(int timeDelay) {
        mTimeDelay = timeDelay;
    }

}
