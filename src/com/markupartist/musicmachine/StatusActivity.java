package com.markupartist.musicmachine;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.markupartist.musicmachine.gateway.SpotifyGateway;

import java.util.List;

public class StatusActivity extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(this);

        SpotifyGateway gateway = new SpotifyGateway();
        List<SpotifyGateway.Track> searchResult = gateway.searchTrack("Foo");
        Log.d("FOO", searchResult.get(0).toString());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.search_button:
            Intent i = new Intent(this, SearchActivity.class);
            startActivity(i);
            break;
        }
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.myprefspane:
            	startActivity(new Intent(this, PrefsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}