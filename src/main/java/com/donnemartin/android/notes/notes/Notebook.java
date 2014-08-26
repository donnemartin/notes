package com.donnemartin.android.notes.notes;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

public class Notebook {

    private ArrayList<Note> mNotes;
    private static Notebook sNotebook;
    private Context mAppContext;
    private NoteIntentJSONSerializer mSerializer;


    private static final String TAG = "Notebook";
    private static final String FILENAME = "notes.json";

    private Notebook(Context appContext) {
        mAppContext = appContext;
        mSerializer = new NoteIntentJSONSerializer(mAppContext, FILENAME);

        try {
            mNotes = mSerializer.loadNotes();
        } catch (Exception e) {
            mNotes = new ArrayList<Note>();
            Log.e(TAG, "Error loading notes: ", e);
        }
    }

    public static Notebook getInstance(Context context) {
        if (sNotebook == null) {
            // Ensure the singleton has a long-term Context to work with
            // Application context is global to the application
            // Application-wide singleton should always use the
            // application context
            sNotebook = new Notebook(context.getApplicationContext());
        }

        return sNotebook;
    }

    public boolean saveNotes() {
        boolean success = false;

        try {
            mSerializer.saveNotes(mNotes);
            Log.d(TAG, "Notes saved to file");
            success = true;
        } catch (Exception e) {
            Log.e(TAG, "Error saving notes: ", e);
        }

        return success;
    }

    public ArrayList<Note> getNotes() {
        return mNotes;
    }

    public void addNote(Note note) {
        mNotes.add(note);
    }

    public void deleteNote(Note note) {
        mNotes.remove(note);
    }

    public Note getNote(UUID id) {
        Note matchNote = null;

        for (Note note : mNotes) {
            if (note.getId().equals(id)) {
                matchNote = note;
                break;
            }
        }

        return matchNote;
    }
}
