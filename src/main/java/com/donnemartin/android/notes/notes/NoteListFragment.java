package com.donnemartin.android.notes.notes;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NoteListFragment extends ListFragment
{
    private ArrayList<Note> mNotes;
    private boolean mSubtitleVisible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        mSubtitleVisible = false;

        getActivity().setTitle(R.string.notes_title);
        mNotes = Notebook.getInstance(getActivity()).getNotes();

        NoteAdapter adapter = new NoteAdapter(mNotes);
        setListAdapter(adapter);
    }

    @TargetApi(11)
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup parent,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, parent, savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (mSubtitleVisible) {
                getActivity().getActionBar().setSubtitle(R.string.subtitle);
            }
        }

        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Note note = ((NoteAdapter)getListAdapter()).getItem(position);

        // Start NotePagerActivity with this note
        // NoteListFragment uses getActivity() to pass its hosting
        // activity as the Context object that the Intent constructor needs
        Intent intent = new Intent(getActivity(), NotePagerActivity.class);
        intent.putExtra(NoteFragment.EXTRA_NOTE_ID, note.getId());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        // Update the list view onResume,
        // as it might have been paused not killed
        super.onResume();
        ((NoteAdapter)getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_note_list, menu);

        MenuItem showSubtitle = menu.findItem(R.id.menu_item_show_subtitle);
        if (mSubtitleVisible && showSubtitle != null) {
            showSubtitle.setTitle(R.string.hide_subtitle);
        }
    }

    @TargetApi(11)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean selectionHandled;

        switch (item.getItemId()) {
            case R.id.menu_item_new_note:
                Note note = new Note();
                Notebook.getInstance(getActivity()).addNote(note);

                Intent intent =
                    new Intent(getActivity(), NotePagerActivity.class);
                intent.putExtra(NoteFragment.EXTRA_NOTE_ID, note.getId());
                startActivityForResult(intent, 0);

                selectionHandled = true;
                break;
            case R.id.menu_item_show_subtitle:
                if (getActivity().getActionBar().getSubtitle() == null) {
                    getActivity().getActionBar().setSubtitle(R.string.subtitle);
                    mSubtitleVisible = true;
                    item.setTitle(R.string.hide_subtitle);
                } else {
                    getActivity().getActionBar().setSubtitle(null);
                    mSubtitleVisible = false;
                    item.setTitle(R.string.show_subtitle);
                }
                selectionHandled = true;
                break;
            default:
                selectionHandled = super.onOptionsItemSelected(item);
                break;
        }

        return selectionHandled;
    }

    private class NoteAdapter extends ArrayAdapter<Note> {
        public NoteAdapter(ArrayList<Note> notes) {
            // Required to properly hook up dataset of Notes
            // Not using a pre-defined layout, so pass 0 for the layout ID
            super(getActivity(), 0, notes);
        }

        private String getFormattedDate(FragmentActivity activity,
                                        Note note) {
            // XXX: Duplicated code, original in NoteFragment.java
            String formattedDate = "";

            if (activity != null) {
                Date date = note.getDate();
                DateFormat dateFormat = android.text.format.DateFormat
                        .getDateFormat(activity.getApplicationContext());
                DateFormat timeFormat = android.text.format.DateFormat
                        .getTimeFormat(activity.getApplicationContext());
                formattedDate = dateFormat.format(date) +
                                " " +
                                timeFormat.format(date);
            }

            return formattedDate;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // If we weren't give a view, inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.list_item_note, null);
            }

            // Configure the view for this Note
            Note note = getItem(position);

            TextView titleTextView = (TextView) convertView
                    .findViewById(R.id.note_list_item_titleTextView);
            titleTextView.setText(note.getTitle());

            TextView dateTextView = (TextView) convertView
                    .findViewById(R.id.note_list_item_dateTextView);
            dateTextView.setText(getFormattedDate(getActivity(), note));

            CheckBox completeCheckBox = (CheckBox) convertView
                    .findViewById(R.id.note_list_item_completeCheckBox);
            completeCheckBox.setChecked(note.isComplete());

            return convertView;
        }
    }
}
