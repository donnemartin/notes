package com.donnemartin.android.notes.notes;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.UUID;

public class NotePagerActivity extends FragmentActivity
{
    private ViewPager mViewPager;
    private ArrayList<Note> mNotes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // FragmentManager requires that any view used as a fragment container
        // must have an Id.  ViewPager is a fragment container.
        // Since this is just a simple single view,
        // Create it in code rather than XML
        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.viewPager);
        setContentView(mViewPager);

        mNotes = Notebook.getInstance(this).getNotes();

        FragmentManager fm = getSupportFragmentManager();

        // FragmentStatePagerAdapter is the agent managing the conversation
        // with ViewPager.  It adds the fragments returned to the activity
        // and helps ViewPager id the fragments' views for placement.
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public Fragment getItem(int position) {
                Note note = mNotes.get(position);
                return NoteFragment.newInstance((note.getId()));
            }

            @Override
            public int getCount() {
                return mNotes.size();
            }
        });

        // By default, ViewPager shows the first item in its PagerAdapter.
        // We have to loop through to find a matching index to show
        // the selected item
        UUID noteId = (UUID)getIntent()
            .getSerializableExtra(NoteFragment.EXTRA_NOTE_ID);

        int numNotes = mNotes.size();
        Note note;

        for (int i = 0; i < numNotes; ++i) {
            note = mNotes.get(i);

            if (note.getId().equals(noteId)) {
                mViewPager.setCurrentItem(i);
                setTitle(note.getTitle());
                break;
            }
        }

        // Replace the activity's title that appears on the action bar
        // with the title of the current Note
        // OnPageChangeListener is how you listen for changes in the page
        // currently being displayed by ViewPager
        mViewPager.setOnPageChangeListener
            (new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
                // This space intentionally left blank
            }

            public void onPageScrolled(int pos,
                                       float posOffset,
                                       int posOffsetPixels) {
                // This space intentionally left blank
            }

            public void onPageSelected(int pos) {
                Note note = mNotes.get(pos);

                if (note.getTitle() != null) {
                    setTitle(note.getTitle());
                }
            }
        });
    }
}
