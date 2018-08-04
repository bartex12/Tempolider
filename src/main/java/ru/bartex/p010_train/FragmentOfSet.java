package ru.bartex.p010_train;

public class FragmentOfSet {

    private float mTimeOfRep;
    private int mReps;
    private int mTimeDelay;

    FragmentOfSet(float timeOfRep,int reps){
        mTimeOfRep = timeOfRep;
        mReps = reps;
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

    public int getTimeDelay() {return mTimeDelay;}

    public void setTimeDelay(int timeDelay) {mTimeDelay = timeDelay;}
}

