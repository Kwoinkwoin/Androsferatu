package nuim.androsferatu;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

import nuim.androsferatu.R;
import nuim.androsferatu.R.id;
import nuim.androsferatu.R.layout;
import nuim.androsferatu.R.menu;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class CCMainActivity extends Activity 
{
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ccmain);
        Button submit = (Button)findViewById(R.id.submitBtn);
        
        submit.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				submit(v);
			}
		});
    }
    
    public void submit(View v)
    {
    	//get the player name from the textfield
    	EditText input = (EditText)findViewById(R.id.nameInput);
    	String name = input.getText().toString();
    	
    	//put the data in the intent
    	Intent intent = new Intent(this,InGameActivity.class);
    	intent.putExtra(/*InGameActivity.*/"PLAYER_NAME",name);
    
    	//actually start the activity
    	startActivity(intent);
    	finish();
    	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ccmain, menu);
        return true;
    } 
}
