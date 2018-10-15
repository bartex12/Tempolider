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
    private String mType_From;
    private int mDelay;

    public DataFile( ){
        //пустой конструктор
    }

    //основной конструктор
    public DataFile(String FileName,String FileNameDate,String FileNameTime, String KindOfSport,
                    String DescriptionOfSport, String Type_From, int Delay){
        mFileName = FileName;
        mFileNameDate = FileNameDate;
        mFileNameTime = FileNameTime;
        mKindOfSport = KindOfSport;
        mDescriptionOfSport = DescriptionOfSport;
        mType_From = Type_From;
        mDelay = Delay;
    }

    //конструктор
    public DataFile(long Person_id, String FileName, String FileNameDate,String FileNameTime,
                    String KindOfSport, String DescriptionOfSport, String Type_From, int Delay){
        mPerson_id = Person_id;
        mFileName = FileName;
        mFileNameDate = FileNameDate;
        mFileNameTime = FileNameTime;
        mKindOfSport = KindOfSport;
        mDescriptionOfSport = DescriptionOfSport;
        mType_From = Type_From;
        mDelay = Delay;
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

    public String getType_From() {
        return mType_From;
    }

    public void setType_From(String type_From) {
        mType_From = type_From;
    }

}
