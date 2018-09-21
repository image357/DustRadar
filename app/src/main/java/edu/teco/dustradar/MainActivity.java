package edu.teco.dustradar;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import edu.teco.dustradar.blebridge.BLEBridge;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ViewPager mViewPager = null;


    // event handlers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.getCurrentItem();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void OnStartButtonClick(View view) {
        int section = mViewPager.getCurrentItem();
        Log.d(TAG, "Start button click in section: " + String.valueOf(section));

        switch (section) {
            case 1:
                // BLE Bridge
                Button button = findViewById(R.id.button_mode);
                button.setText(R.string.button_mode_starting);
                Intent intent = new Intent(this, BLEBridge.class);
                startActivity(intent);
                break;

            case 2:
                // Local Display
                Snackbar.make(view, "comming soon ...", Snackbar.LENGTH_LONG).setAction(
                        "Action", null).show();
                break;

            default:
                Snackbar.make(view, "not supported", Snackbar.LENGTH_LONG).setAction(
                        "Action", null).show();
        }
    }


    // A placeholder fragment containing a simple view.

    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        private Button startButton = null;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        // event handlers

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView;
            int section = getArguments().getInt(ARG_SECTION_NUMBER);

            if (section == 0) {
                rootView = inflater.inflate(R.layout.fragment_videotitle, container, false);

                VideoView videoView = rootView.findViewById(R.id.videoViewTitle);
                Uri videoUri = Uri.parse("android.resource://" + getActivity().getPackageName()
                        + "/" + R.raw.whitesmoke);
                videoView.setVideoURI(videoUri);

                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setLooping(true);
                    }
                });

                videoView.start();
                videoView.requestFocus();
            }
            else {
                rootView = inflater.inflate(R.layout.fragment_mode, container, false);
                startButton = rootView.findViewById(R.id.button_mode);
            }

            // adjust TextViews
            TextView title = rootView.findViewById(R.id.textView_mode_title);
            TextView description = rootView.findViewById(R.id.textView_mode_description);
            switch (section) {
                case 0:
                    // no text for video title
                    break;

                case 1:
                    title.setText(R.string.mode_blebride_title);
                    description.setText(R.string.mode_blebride_description);
                    break;

                case 2:
                    title.setText(R.string.mode_localdisplay_title);
                    description.setText(R.string.mode_localdisplay_description);
                    break;

                default:
                    title.setText(R.string.mode_emptytitle);
                    description.setText(R.string.mode_emptydescription);
                    Log.w(TAG, "Trying to access more fragments than possible.");
            }

            return rootView;
        }

        @Override
        public void onResume() {
            if (startButton != null) {
                startButton.setText(R.string.button_mode_start);
            }

            super.onResume();
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private int numberOfSections = 3;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Define number of sections.
            return numberOfSections;
        }
    }
}
