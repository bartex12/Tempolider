package ru.bartex.p010_train;

import java.io.Serializable;

public class DataSet implements Serializable {

    private float mTimeOfRep;
    private int mReps;
    private int mNumberOfFrag = 0;

    public DataSet( ){
        //пустой конструктор
    }

    //основной конструктор
    public DataSet(float TimeOfRep, int Reps, int NumberOfFrag){
        mTimeOfRep = TimeOfRep;
        mReps = Reps;
        mNumberOfFrag = NumberOfFrag;
    }

    public float getTimeOfRep() {
        return mTimeOfRep;
    }

    public void setTimeOfRep(float timeOfRep) {
        mTimeOfRep = timeOfRep;
    }

    public int getReps() {
        return mReps;
    }

    public void setReps(int reps) {
        mReps = reps;
    }

    public int getNumberOfFrag(){
        return mNumberOfFrag;
    }

    public void setNumberOfFrag(int numberOfFrag){
        mNumberOfFrag = numberOfFrag;
    }

}
