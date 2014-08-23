package com.donnemartin.android.notes.notes;

import android.content.Context;

import java.util.ArrayList;
import java.util.UUID;

public class Notebook {

    private ArrayList<Note> mNotes;
    private static Notebook sNotebook;
    private Context mAppContext;

    private Notebook(Context appContext) {
        mAppContext = appContext;
        mNotes = new ArrayList<Note>();
        generateTestNotes();
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

    private void generateTestNotes() {
        for (int i = 0; i < 5; ++i) {
            Note note = new Note();
            note.setTitle("Note " + i);
            note.setComplete(i % 2 == 0);
            mNotes.add(note);
        }
    }

    public ArrayList<Note> getNotes() {
        return mNotes;
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
