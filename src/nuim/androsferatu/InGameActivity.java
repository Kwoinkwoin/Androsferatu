package nuim.androsferatu;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

public class InGameActivity extends Activity
{

	public static final String PLAYER_NAME = "com.cs385.chatclient.PLAYER_NAME";
	public static final String TCP_CLIENT = "com.cs385.chatclient.TCP_CLIENT";
	private TCPClient tcpClient;
	private GameClient gameClient;
	private ArrayAdapter<String> msgList;
	private ListView msgView;
	private String playerName;
	private String msgData;
	private Player player;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		// Show the Up button in the action bar.
		setupActionBar();
	
		//get Player name from previous activity
		Intent parentIntent = getIntent();
		this.playerName = parentIntent.getStringExtra("PLAYER_NAME");
		this.player = new Player(playerName, this);
		msgView = (ListView)findViewById(R.id.listView1);
        msgList = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        msgView.setAdapter(msgList);

        final Handler handler = new Handler()
        {
        	@Override
        	public void handleMessage(Message message)
        	{
        		Bundle b;
        		b = message.getData();
        		String s = (String)b.get("android_chat_msg");
				msgList.add(s);
				msgList.notifyDataSetChanged();
				msgView.smoothScrollToPosition(msgList.getCount() - 1);
        	}
        };
        
		tcpClient = new TCPClient(new InputHandler()
		{
			
			@Override
			public void onMessageRecieve(String message)
			{
				Message m = handler.obtainMessage();
				Bundle b = m.getData();
				b.putString("android_chat_msg", message);
				handler.sendMessage(m);
				
			}
		});
		
		 final Handler handlerData = new Handler()
	        {
	        	@Override
	        	public void handleMessage(Message message)
	        	{
	        		Bundle b2;
	        		b2 = message.getData();
	        		msgData = (String)b2.get("android_data_msg");
	        		if(msgData.contains("ONLINE_PLAYER")) {
	        			int playerNumber = Integer.parseInt(msgData.split("=")[1]);
	        			msgList.add("Waiting for other players : " + playerNumber + " / 5");
	        			msgList.notifyDataSetChanged();
	    				msgView.smoothScrollToPosition(msgList.getCount() - 1);
	    				if(playerNumber == 5) {
	    					msgList.add("Game Starting!");
		        			msgList.notifyDataSetChanged();
		    				msgView.smoothScrollToPosition(msgList.getCount() - 1);
	    				}
	        		}
	        		if(msgData.equals("PA_DRAWCARDS"));
	        	}
	        };
	        
	    gameClient = new GameClient(new InputHandler()
			{
				
				@Override
				public void onMessageRecieve(String message)
				{
					Message m2 = handlerData.obtainMessage();
					Bundle b2 = m2.getData();
					b2.putString("android_data_msg", message);
					handlerData.sendMessage(m2);
					
				}
			});
		
		Thread t = new Thread(tcpClient);
		t.start();
	
		Thread gameDataThread = new Thread(gameClient);
		gameDataThread.start();
		
        Button sndButton = (Button)findViewById(R.id.sendButton);
        sndButton.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				sendMessage();
			}
		});
        
        this.sendHelloMessage();
	}
	
    public void sendMessage()
    {
    	EditText input = (EditText)findViewById(R.id.input);
		//String msg = input.getText().toString();
    	String msg = this.playerName;
    	msg += " : " + input.getText().toString();
		try
		{
			tcpClient.sendMessage(msg);
		}catch(java.lang.NullPointerException npe)
		{
			//Start ServerOffline activity if server is offline
			Intent intent = new Intent(this, ServerOfflineActivity.class);
			startActivity(intent);
			finish();
		}
		input.setText("");
    }
    
    public void sendHelloMessage()
    {
    	try
		{
    		/*
    		 * We're sleeping the main thread, giving the network Thread time to initialize itself
    		 * then we can move on to chatting.
    		 * If we remove this, we'll get an error message even if the server is running
    		 */
			Thread.sleep(100);	//50ms mini
		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	/*
         * Notify server that we're connected
         * if it fails => server is off => display offline activity
         */
        try
		{
        	tcpClient.sendMessage(TCPClient.HELLO_MSG);
		}catch(java.lang.NullPointerException npe)
		{
			//Start ServerOffline activity if server is offline
			Intent intent = new Intent(this, ServerOfflineActivity.class);
			startActivity(intent);
			finish();
		}
    }
	

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
