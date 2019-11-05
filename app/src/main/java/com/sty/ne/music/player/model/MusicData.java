package com.sty.ne.music.player.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MusicData implements Parcelable{
    //音乐资源id
    private int mMusicRes;
    //专辑图片ID
    private int mMusicPicRes;
    //音乐名称
    private String mMusicName;
    //作者
    private String mMusicAuthor;

    public MusicData(int mMusicRes, int mMusicPicRes, String mMusicName, String mMusicAuthor) {
        this.mMusicRes = mMusicRes;
        this.mMusicPicRes = mMusicPicRes;
        this.mMusicName = mMusicName;
        this.mMusicAuthor = mMusicAuthor;
    }

    protected MusicData(Parcel in) {
        mMusicRes = in.readInt();
        mMusicPicRes = in.readInt();
        mMusicName = in.readString();
        mMusicAuthor = in.readString();
    }

    public static final Creator<MusicData> CREATOR = new Creator<MusicData>() {
        @Override
        public MusicData createFromParcel(Parcel in) {
            return new MusicData(in);
        }

        @Override
        public MusicData[] newArray(int size) {
            return new MusicData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mMusicRes);
        dest.writeInt(mMusicPicRes);
        dest.writeString(mMusicName);
        dest.writeString(mMusicAuthor);
    }

    public int getmMusicRes() {
        return mMusicRes;
    }

    public void setmMusicRes(int mMusicRes) {
        this.mMusicRes = mMusicRes;
    }

    public int getmMusicPicRes() {
        return mMusicPicRes;
    }

    public void setmMusicPicRes(int mMusicPicRes) {
        this.mMusicPicRes = mMusicPicRes;
    }

    public String getmMusicName() {
        return mMusicName;
    }

    public void setmMusicName(String mMusicName) {
        this.mMusicName = mMusicName;
    }

    public String getmMusicAuthor() {
        return mMusicAuthor;
    }

    public void setmMusicAuthor(String mMusicAuthor) {
        this.mMusicAuthor = mMusicAuthor;
    }
}
