package com.donnemartin.android.notes.notes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

public class NoteFragment extends Fragment {

    private Note mNote;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mCompleteCheckBox;
    private Button mRecordButton;
    private Button mPlayPauseButton;
    private Button mStopButton;
    private AudioPlayer mPlayer;
    private AudioRecorder mRecorder;
    private static StringBuffer mAudioFileName;

    public static final String EXTRA_NOTE_ID =
        "com.donnemartin.android.notes.note_id";

    private static final String DIALOG_DATE = "date";
    private static final int REQUEST_DATE = 0;

    public static NoteFragment newInstance(UUID noteId)
    // Attaching arguments to a fragment must be done after the fragment
    // is created but before it is added to an activity.
    // This function uses the standard convention, call this function
    // instead of the constructor directly.
    // XXX: Should the constructor be marked as private?
    {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_NOTE_ID, noteId);

        NoteFragment fragment = new NoteFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private void setPlayAudioButtonText()
    {
        mPlayPauseButton.setText(getResources()
            .getString(R.string.note_play));
    }

    private void setPauseAudioButtonText()
    {
        mPlayPauseButton.setText(getResources()
            .getString(R.string.note_pause));
    }

    private void setStartRecordingButtonText()
    {
        mRecordButton.setText(getResources()
            .getString(R.string.note_record));
    }

    private void setStopRecordingButtonText()
    {
        mRecordButton.setText(getResources()
            .getString(R.string.note_stop));
    }

    private void setFormattedDateButton(FragmentActivity activity)
    {
        if (activity != null)
        {
            Date date = mNote.getDate();
            DateFormat dateFormat = android.text.format.DateFormat
                .getDateFormat(activity.getApplicationContext());
            DateFormat timeFormat = android.text.format.DateFormat
                    .getTimeFormat(activity.getApplicationContext());
            mDateButton.setText(dateFormat.format(date) +
                                " " +
                                timeFormat.format(date));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        UUID noteId = (UUID)getArguments().getSerializable(EXTRA_NOTE_ID);
        mNote = Notebook.getInstance(getActivity()).getNote(noteId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup parent,
                             Bundle savedInstanceState)
    {
        // XXX: Might be a problem if a title contains characters that
        // are not allowed by the Android file system
        // XXX: Use a StringBuffer instead
        mAudioFileName = new StringBuffer(Environment
            .getExternalStorageDirectory().getAbsolutePath());
        mAudioFileName.append("/" + mNote.getTitle() + ".3gp");

        mPlayer = new AudioPlayer(mAudioFileName.toString());
        mRecorder = new AudioRecorder(mAudioFileName.toString());

        // Inflated view is added to parent in the activity code
        boolean addInflatedViewToParent = false;
        View v = inflater.inflate(R.layout.fragment_note,
                                  parent,
                                  addInflatedViewToParent);

        mTitleField = (EditText)v.findViewById(R.id.note_title);
        mTitleField.setText(mNote.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start,
                                          int count,
                                          int after)
            {
                // This space intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence s,
                                      int start,
                                      int before,
                                      int count)
            {
                mNote.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                // This space intentionally left blank
            }
        });

        mDateButton = (Button)v.findViewById(R.id.note_date);
        setFormattedDateButton(getActivity());
        mDateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                    .newInstance(mNote.getDate());

                // We want to get the selected date back from the dialog
                dialog.setTargetFragment(NoteFragment.this, REQUEST_DATE);
                dialog.show(fm, DIALOG_DATE);
            }
        });

        mCompleteCheckBox = (CheckBox)v.findViewById(R.id.note_complete);
        mCompleteCheckBox.setChecked(mNote.isComplete());
        mCompleteCheckBox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        // Set the Note's complete property
                        mNote.setComplete(isChecked);
                    }
                });

        mRecordButton = (Button)v.findViewById(R.id.note_record);
        mRecordButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mRecorder.isRecording())
                {
                    mRecorder.stopRecording();
                    setStartRecordingButtonText();
                }
                else
                {
                    mRecorder.startRecording();
                    setStopRecordingButtonText();
                }
            }
        });

        mPlayPauseButton = (Button)v.findViewById(R.id.note_play_pause);
        mPlayPauseButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mPlayer.playOrPause(getActivity());
            }
        });

        mStopButton = (Button)v.findViewById(R.id.note_stop);
        mStopButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mPlayer.stop();
                setPlayAudioButtonText();
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == REQUEST_DATE)
            {
                Date date = (Date)data
                    .getSerializableExtra(DatePickerFragment.EXTRA_DATE);
                mNote.setDate(date);
                setFormattedDateButton(getActivity());
            }
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mPlayer.stop();
    }
}
