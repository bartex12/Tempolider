package ru.bartex.p010_train;

import android.provider.BaseColumns;

import java.io.Serializable;

public class DataFile implements Serializable {

    private long mPerson_id;
    private String mFileName;
    private String mFileNameDate;
    private String mFileNameTime;
    private String mKindOfSport;
    private String mDescriptionOfSport;
    private int mDelay = 6;
    private int mType_From;
    private int mLiked;

    public DataFile( ){
        //пустой конструктор
    }

    //основной конструктор
    public DataFile(String FileName,String FileNameDate,String FileNameTime, String KindOfSport,
                    String DescriptionOfSport, int Delay, int Type_From, int Liked){
        mFileName = FileName;
        mFileNameDate = FileNameDate;
        mFileNameTime = FileNameTime;
        mKindOfSport = KindOfSport;
        mDescriptionOfSport = DescriptionOfSport;
        mDelay = Delay;
        mType_From = Type_From;
        mLiked = Liked;
    }

    //конструктор
    public DataFile(long Person_id, String FileName, String FileNameDate,String FileNameTime,
                    String KindOfSport, String DescriptionOfSport,
                    int Delay, int Type_From, int Liked){
        mPerson_id = Person_id;
        mFileName = FileName;
        mFileNameDate = FileNameDate;
        mFileNameTime = FileNameTime;
        mKindOfSport = KindOfSport;
        mDescriptionOfSport = DescriptionOfSport;
        mDelay = Delay;
        mType_From = Type_From;
        mLiked = Liked;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public String getFileNameDate() {
        return mFileNameDate;
    }

    public void setFileNameDate(String fileNameDate) {
        mFileNameDate = fileNameDate;
    }

    public String getFileNameTime() {
        return mFileNameTime;
    }

    public void setFileNameTime(String fileNameTime) {
        mFileNameTime = fileNameTime;
    }

    public String getKindOfSport() {
        return mKindOfSport;
    }

    public void setKindOfSport(String kindOfSport) {
        mKindOfSport = kindOfSport;
    }

    public String getDescriptionOfSport() {
        return mDescriptionOfSport;
    }

    public void setDescriptionOfSport(String descriptionOfSport) {
        mDescriptionOfSport = descriptionOfSport;
    }

    public int getDelay() {
        return mDelay;
    }

    public void setDelay(int delay) {
        mDelay = delay;
    }

    public int getType_From() {
        return mType_From;
    }

    public void setType_From(int type_From) {
        mType_From = type_From;
    }

    public int getLiked() {
        return mLiked;
    }

    public void setLiked(int liked) {
        mLiked = liked;
    }
}
