package com.cognactionlab.PsyGlass;

import com.google.android.glass.app.Card;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * PsyGlass: An Open-Source Framework for Implementing Google Glass in Research Settings
 *
 * For more, see "PsyGlass: Capitalizing on Google Glass for Naturalistic Data Collection"
 * (Paxton, Rodriguez, & Dale, submitted).
 *
 * Written by K. Rodriguez, A. Paxton, & R. Dale.
 * Created on 13 October 2014.
 * Last modified on 16 December 2014.
 *
 */

public class SettingsActivity extends Activity implements AdapterView.OnItemClickListener {

    private static final int CARD_DATA = 0;

    private List<Card> cards;
    private CardScrollView scrollView;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        createCards();

        scrollView = new CardScrollView(this);
        scrollView.setAdapter(new SettingsAdapter());
        scrollView.setHorizontalScrollBarEnabled(true);
        scrollView.setOnItemClickListener(this);
        setContentView(scrollView);
    }

    public void createCards() {
        cards = new ArrayList<Card>(1);

        Card card = new Card(this);
        card.addImage(R.drawable.upload);
        long timestamp = getPrefTimestamp() * 1000;
        if (timestamp == -1) {
            card.setText("Data\nNone");
        } else {
            int interval = (int) (getPrefDuration());
            int minute = interval / 60;
            int second = interval % 60;
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy\nHH:mm");
            card.setText("DATA\n" +
                    "DeviceId: " + getPrefDeviceId() + "\n" +
                    format.format(new Date(timestamp)) + " MT\n" +
                    String.format("%02dm%02ds", minute, second));
        }
        cards.add(card);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scrollView.activate();
    }

    @Override
    protected void onPause() {
        scrollView.deactivate();
        super.onPause();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Audio manager used to play system sound effects
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (position == CARD_DATA && getPrefTimestamp() == -1) { // data
            audioManager.playSoundEffect(Sounds.DISALLOWED);
        } else {
            audioManager.playSoundEffect(Sounds.TAP);
            openOptionsMenu();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        switch(scrollView.getSelectedItemPosition()) {
            case CARD_DATA:
                getMenuInflater().inflate(R.menu.data, menu);
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.upload_data:
                Intent intent = new Intent(this, UploadActivity.class);
                startActivity(intent);
                break;
            default:
                return false;
        }
        return true;
    }

    private long getPrefTimestamp() {
        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
        );
        return preferences.getLong("Timestamp", -1);
    }

    private long getPrefDuration() {
        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
        );
        return preferences.getLong("Duration", -1);
    }

    private int getPrefDeviceId() {
        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
        );
        return preferences.getInt("DeviceId", -1);
    }



    private class SettingsAdapter extends CardScrollAdapter {

        @Override
        public int getCount() {
            return cards.size();
        }

        @Override
        public Object getItem(int i) {
            return cards.get(i);
        }

        @Override
        public int getPosition(Object object) {
            return cards.indexOf(object);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            return cards.get(i).getView(view, viewGroup);
        }
    }
}
