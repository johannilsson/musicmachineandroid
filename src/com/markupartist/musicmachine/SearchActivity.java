package com.markupartist.musicmachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView.OnEditorActionListener;

import com.markupartist.musicmachine.gateway.SpotifyGateway;
import com.markupartist.musicmachine.gateway.SpotifyGateway.Track;

public class SearchActivity extends ListActivity implements OnClickListener, OnEditorActionListener {
    private static final String TAG = "Search";
    private AutoCompleteTextView mSearchView;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        mSearchView = (AutoCompleteTextView) findViewById(R.id.search_text);
        mSearchView.setOnEditorActionListener(this);
        ImageButton searchButton = (ImageButton) findViewById(R.id.btn_search);
        searchButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_search:
            doSearch();
            break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if(v.length() > 0) {
            boolean isEnterKey = (null != event && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            if(actionId == EditorInfo.IME_ACTION_SEARCH || true == isEnterKey) {
                doSearch();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Map<String, Object> item = (Map<String, Object>) ((SimpleAdapter)getListAdapter()).getItem(position);
        SpotifyGateway.Track track = (SpotifyGateway.Track) item.get("track");
        Log.d(TAG, "track: " + track.getArtist());

        Intent i = new Intent(this, VoteActivity.class);
        startActivity(i);

        super.onListItemClick(l, v, position, id);
    }

    private void doSearch() {
        String searchText = mSearchView.getText().toString();
        SpotifyGateway gateway = new SpotifyGateway();
        List<SpotifyGateway.Track> searchResult = gateway.searchTrack(searchText);

        setListAdapter(createResultAdapter(searchResult));
    }

    private SimpleAdapter createResultAdapter(List<SpotifyGateway.Track> tracks) {
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        for (SpotifyGateway.Track track : tracks) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("artist", track.getArtist());
            map.put("title", track.getTitle());
            map.put("track", track);
            list.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, list, 
                R.layout.track_row,
                new String[] { "artist", "title" },
                new int[] { 
                    R.id.artist,
                    R.id.title
                }
        );

        adapter.setViewBinder(new ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data,
                    String textRepresentation) {
                switch (view.getId()) {
                case R.id.artist:
                case R.id.title:
                    ((TextView)view).setText(textRepresentation);
                    return true;
                }
                return false;
            }
        });

        return adapter;
    }
}