package com.donnemartin.android.notes.notes;

import java.util.Date;
import java.util.UUID;

public class Note {

    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mComplete;

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isComplete() {
        return mComplete;
    }

    public void setComplete(boolean complete) {
        mComplete = complete;
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Note() {
        mId = UUID.randomUUID();
        mDate = new Date();
    }

    @Override
    public String toString() {
        return getTitle();
    }
}