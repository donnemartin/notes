package com.donnemartin.android.notes.notes;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;

public class Note {

    private UUID mId;
    private String mTitle;
    private String mContent;
    private String mAudioFilename;
    private Date mDate;
    private Photo mPhoto;
    private boolean mComplete;

    private static final String JSON_ID = "id";
    private static final String JSON_TITLE = "title";
    private static final String JSON_CONTENT = "content";
    private static final String JSON_AUDIO_FILENAME = "audio_filename";
    private static final String JSON_COMPLETE = "complete";
    private static final String JSON_DATE = "date";
    private static final String JSON_PHOTO = "photo";

    public Note() {
        mId = UUID.randomUUID();
        mDate = new Date();
    }

    public Note(JSONObject json) throws JSONException {
        mId  = UUID.fromString(json.getString(JSON_ID));

        if (json.has(JSON_TITLE)) {
            mTitle = json.getString(JSON_TITLE);
        }
        if (json.has(JSON_CONTENT)) {
            mContent = json.getString(JSON_CONTENT);
        }
        if (json.has(JSON_AUDIO_FILENAME)) {
            mAudioFilename = json.getString(JSON_AUDIO_FILENAME);
        }
        if (json.has(JSON_PHOTO)) {
            mPhoto = new Photo(json.getJSONObject(JSON_PHOTO));
        }

        mComplete = json.getBoolean(JSON_COMPLETE);
        mDate = new Date(json.getLong(JSON_DATE));
    }

    @Override
    public String toString() {
        return getTitle();
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_ID, mId.toString());
        json.put(JSON_TITLE, mTitle);
        json.put(JSON_CONTENT, mContent);
        json.put(JSON_AUDIO_FILENAME, mAudioFilename);
        json.put(JSON_COMPLETE, mComplete);
        json.put(JSON_DATE, mDate.getTime());
        if (mPhoto != null) {
            json.put(JSON_PHOTO, mPhoto.toJSON());
        }
        return json;
    }

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

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getAudioFilename() {
        return mAudioFilename;
    }

    public void setAudioFilename(String audioFilename) {
        mAudioFilename = audioFilename;
    }

    public Photo getPhoto() {
        return mPhoto;
    }

    public void setPhoto(Photo photo) {
        mPhoto = photo;
    }
}