package com.donnemartin.android.notes.notes;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

public class NoteFragment extends Fragment {

    private Note mNote;
    private EditText mTitleField;
    private EditText mContentField;
    private Button mDateButton;
    private CheckBox mCompleteCheckBox;
    private Button mRecordButton;
    private Button mPlayButton;
    private AudioPlayer mAudioPlayer;
    private AudioRecorder mAudioRecorder;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;

    private static StringBuffer mAudioFileName;

    private static final String TAG = "NoteFragment";

    public static final String EXTRA_NOTE_ID =
        "com.donnemartin.android.notes.note_id";

    private static final String AUDIO_POS_INDEX = "audio_pos_index";
    private static final String DIALOG_IMAGE = "image";

    private static final String DIALOG_DATE = "date";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_PHOTO = 1;

    public static NoteFragment newInstance(UUID noteId) {
    // Attaching arguments to a fragment must be done after the fragment
    // is created but before it is added to an activity.
    // This function uses the standard convention, call this function
    // instead of the constructor directly.
    // TODO: Should the constructor be marked as private?
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_NOTE_ID, noteId);

        NoteFragment fragment = new NoteFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private void setPlayAudioButtonText() {
        mPlayButton.setText(getResources()
            .getString(R.string.note_play));
    }

    private void setStopAudioButtonText() {
        mPlayButton.setText(getResources()
            .getString(R.string.note_stop));
    }

    private void setStartRecordingButtonText() {
        mRecordButton.setText(getResources()
            .getString(R.string.note_record));
    }

    private void setStopRecordingButtonText() {
        mRecordButton.setText(getResources()
            .getString(R.string.note_stop));
    }

    private void setFormattedDateButton(FragmentActivity activity) {
        if (activity != null) {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        UUID noteId = (UUID)getArguments().getSerializable(EXTRA_NOTE_ID);
        mNote = Notebook.getInstance(getActivity()).getNote(noteId);
    }

    @TargetApi(11)
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup parent,
                             Bundle savedInstanceState) {
        // TODO: Might be a problem if a title contains characters that
        // are not allowed by the Android file system
        // Ask the user to supply a file name instead
        // This is also written to external storage, whereas
        // the notes are saved to internal memory
        mAudioFileName = new StringBuffer(Environment
            .getExternalStorageDirectory().getAbsolutePath());
        mAudioFileName
                .append("/")
                .append(mNote.getTitle())
                .append(".3gp");

        String audioFileName = mAudioFileName.toString();
        mNote.setAudioFilename(audioFileName);

        mAudioPlayer = new AudioPlayer(mNote.getAudioFilename());
        mAudioRecorder = new AudioRecorder(mNote.getAudioFilename());

        // Inflated view is added to parent in the activity code
        View view = inflater.inflate(R.layout.fragment_note,
                                  parent,
                                  false);

        setHasOptionsMenu(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (NavUtils.getParentActivityName(getActivity()) != null) {
                getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        mTitleField = (EditText)view.findViewById(R.id.note_title);
        mTitleField.setText(mNote.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start,
                                          int count,
                                          int after) {
                // This space intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence s,
                                      int start,
                                      int before,
                                      int count) {
                mNote.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // This space intentionally left blank
            }
        });

        mContentField = (EditText)view.findViewById(R.id.note_content);
        mContentField.setText(mNote.getContent());
        mContentField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start,
                                          int count,
                                          int after) {
                // This space intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence s,
                                      int start,
                                      int before,
                                      int count) {
                mNote.setContent(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // This space intentionally left blank
            }
        });

        mDateButton = (Button)view.findViewById(R.id.note_date);
        setFormattedDateButton(getActivity());
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                    .newInstance(mNote.getDate());

                // We want to get the selected date back from the dialog
                dialog.setTargetFragment(NoteFragment.this, REQUEST_DATE);
                dialog.show(fm, DIALOG_DATE);
            }
        });

        mCompleteCheckBox = (CheckBox)view.findViewById(R.id.note_complete);
        mCompleteCheckBox.setChecked(mNote.isComplete());
        mCompleteCheckBox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        mNote.setComplete(isChecked);
                    }
                });

        mRecordButton = (Button)view.findViewById(R.id.note_record);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PackageManager pm = getActivity().getPackageManager();

                if (pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
                    if (mAudioRecorder.isRecording()) {
                        mAudioRecorder.stopRecording();
                        setStartRecordingButtonText();
                    } else {
                        mAudioRecorder.startRecording();
                        setStopRecordingButtonText();
                    }
                } else {
                    Toast.makeText(getActivity(),
                                   getResources()
                                       .getString(R.string.error_no_mic),
                                   Toast.LENGTH_LONG).show();
                }
            }
        });

        mPlayButton = (Button)view.findViewById(R.id.note_play_pause);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAudioPlayer.isPlaying()) {
                    mAudioPlayer.stop();
                    setPlayAudioButtonText();
                } else {
                    mAudioPlayer.play(AudioPlayer.PLAY_FROM_START);
                    setStopAudioButtonText();
                }
            }
        });

        mPhotoButton = (ImageButton)view.findViewById(R.id.note_imageButton);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PackageManager pm = getActivity().getPackageManager();

                boolean hasCamera =
                    pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) ||
                    pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT) ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD &&
                    Camera.getNumberOfCameras() > 0);

                if (hasCamera) {
                    Intent intent = new Intent(getActivity(),
                                               NoteCameraActivity.class);
                    startActivityForResult(intent, REQUEST_PHOTO);
                } else {
                    Toast.makeText(getActivity(),
                                   getResources()
                                       .getString(R.string.error_no_camera),
                                   Toast.LENGTH_LONG).show();
                }
            }
        });

        mPhotoView = (ImageView) view.findViewById(R.id.note_imageView);
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Photo photo = mNote.getPhoto();

                if (photo != null) {
                    FragmentManager fm = getActivity()
                        .getSupportFragmentManager();
                    String path = getActivity().getFileStreamPath(
                        photo.getFileName()).getAbsolutePath();
                    ImageFragment.newInstance(path)
                        .show(fm, DIALOG_IMAGE);
                }
            }
        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_DATE) {
                Date date = (Date)data
                    .getSerializableExtra(DatePickerFragment.EXTRA_DATE);
                mNote.setDate(date);
                setFormattedDateButton(getActivity());
            } else if (requestCode == REQUEST_PHOTO) {
                // Create a new photo object and attach it to the note
                String fileName = data
                    .getStringExtra(NoteCameraFragment.EXTRA_PHOTO_FILENAME);
                if (fileName != null) {
                    Photo photo = new Photo(fileName);
                    mNote.setPhoto(photo);
                    showPhoto();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAudioPlayer.stop();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean selectionHandled;

        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                selectionHandled = true;
                break;
            default:
                selectionHandled = super.onOptionsItemSelected(item);
                break;
        }

        return selectionHandled;
    }

    @Override
    public void onPause() {
        super.onPause();

        // onPause() is the safest choice to save notes.
        // onStop() or onDestroy() might not work.
        // A paused activity will be destroyed if the OS needs to
        // reclaim memory, where you cannot count on onStop() or onDestroy()
        boolean success = Notebook.getInstance(getActivity()).saveNotes();

        if (!success) {
            Toast.makeText(getActivity(),
                           getResources()
                                   .getString(R.string.error_saving),
                           Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        showPhoto();
    }

    @Override
    public void onStop() {
        super.onStop();
        PictureUtils.cleanImageView(mPhotoView);
    }

    private void showPhoto() {
        // Reset the image button's image based o our photo
        Photo photo = mNote.getPhoto();
        BitmapDrawable bitmapDrawable = null;

        if (photo != null) {
            String path = getActivity()
                .getFileStreamPath(photo.getFileName()).getAbsolutePath();
            bitmapDrawable = PictureUtils.getScaledDrawable(getActivity(),
                                                            path);
        }

        mPhotoView.setImageDrawable(bitmapDrawable);
    }
}
