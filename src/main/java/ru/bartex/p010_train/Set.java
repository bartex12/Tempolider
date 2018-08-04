package ru.bartex.p010_train;

import java.util.UUID;

/**
 * Created by Андрей on 02.05.2018.
 */
public class Set {

    private String mTitle;
    private UUID mId;
    private float mTimeOfRep;
    private int mReps;
    private int mNumberOfFrag = 0;

    Set( ){

        mId = UUID.randomUUID();
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public UUID getId() {return mId;}

    public void setId(UUID id) {mId = id;}

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
