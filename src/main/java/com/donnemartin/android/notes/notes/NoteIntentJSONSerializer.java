package com.donnemartin.android.notes.notes;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.*;
import java.util.ArrayList;

public class NoteIntentJSONSerializer {

    private Context mContext;
    private String mFilename;

    public NoteIntentJSONSerializer(Context context, String filename) {
        mContext = context;
        mFilename = filename;
    }

    public void saveNotes(ArrayList<Note> notes)
        throws JSONException, IOException {

        // Build an array in JSON
        JSONArray array = new JSONArray();

        for (Note note : notes) {
            array.put(note.toJSON());
        }

        // Write the file to disk
        Writer writer = null;

        try {
            OutputStream out = mContext.openFileOutput(mFilename,
                                                       Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            writer.write(array.toString());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public ArrayList<Note> loadNotes() throws IOException, JSONException {
        ArrayList<Note> notes = new ArrayList<Note>();
        BufferedReader reader = null;

        try {
            // Open and read the file into a StringBuilder
            InputStream in = mContext.openFileInput(mFilename);
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line = null;

            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }

            // Parse the JSON using JSONTokener
            JSONArray array = (JSONArray) new JSONTokener(jsonString.toString())
                .nextValue();

            // Build the array of notes from JSONObjects
            for (int i = 0; i < array.length(); ++i) {
                notes.add(new Note(array.getJSONObject(i)));
            }
        } catch (FileNotFoundException e) {
            // Ignore, happens when starting fresh
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return notes;
    }
}
