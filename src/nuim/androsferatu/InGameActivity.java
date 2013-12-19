package nuim.androsferatu;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

public class InGameActivity extends Activity
{

	public static final String PLAYER_NAME = "com.cs385.chatclient.PLAYER_NAME";
	public static final String TCP_CLIENT = "com.cs385.chatclient.TCP_CLIENT";
	public static final int DIALOG_PLAYER = 10;
	public static final int DIALOG_MAGIC = 20;
	public static final int DIALOG_CARD = 30;
	private TCPClient tcpClient;
	private GameClient gameClient;
	private ArrayAdapter<String> msgList;
	private ListView msgView;
	private String playerName;
	private String msgData;
	private Player player;
	private ImageView card;
	private TextView info;
	private Thread gameDataThread;
	private CharSequence[] playersNames;
	private String dialogInfo;
	private DialogInterface.OnClickListener onClickListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		// Show the Up button in the action bar.
		setupActionBar();
	
		//get Player name from previous activity
		Intent parentIntent = getIntent();
		this.card = (ImageView) findViewById(R.id.card);
		this.info = (TextView) findViewById(R.id.text_info);
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
        		String cardId = (String)b.get("android_image_id");
        		String text = (String)b.get("android_text_info");
        		if(s != null) {
					msgList.add(s);
					msgList.notifyDataSetChanged();
					msgView.smoothScrollToPosition(msgList.getCount() - 1);
        		}
        		if(cardId != null) {
        			card.setImageResource(Integer.parseInt(cardId));
        		}
        		if(text != null) {
        			info.setText("" + text);
        		}
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
	        		System.out.println(message);
	        		Bundle b2;
	        		b2 = message.getData();
	        		msgData = (String)b2.get("android_data_msg");
	        		if(msgData.contains("ONLINE_PLAYER")) {
	        			int playerNumber = Integer.parseInt(msgData.split("=")[1]);
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_text_info", "Waiting for other players : " + playerNumber + " / 5");
	    				handler.sendMessage(m);
	        			/*msgList.add("Waiting for other players : " + playerNumber + " / 5");
	        			msgList.notifyDataSetChanged();
	    				msgView.smoothScrollToPosition(msgList.getCount() - 1);*/
	    				if(playerNumber == 5) {
	    					m = handler.obtainMessage();
		    				b = m.getData();
		    				b.putString("android_chat_msg", "Game Starting!");
		    				handler.sendMessage(m);
	    					/*msgList.add("Game Starting!");
		        			msgList.notifyDataSetChanged();
		    				msgView.smoothScrollToPosition(msgList.getCount() - 1);*/
	    				}
	        		}
	        		else if(msgData.equals("START_GAME")) {
	        			sendDataMsg("PA_ASKROLE");
	        		}
	        		else if(msgData.contains("WHO_FIRST_PLAYER")) {
	        			String[] temp = msgData.split(";");
	        			playersNames = new CharSequence[temp.length-1];
	        			for(int i=0; i<playersNames.length; i++) {
	        				playersNames[i] = temp[i+1];
	        				Message m = handler.obtainMessage();
		    				Bundle b = m.getData();
		    				b.putString("android_chat_msg", "" + playersNames[i]);
		    				handler.sendMessage(m);
	        			}
	        			dialogInfo = "Who will be the first player?";
	        			onClickListener = new FirstPlayerListener();
	        			showDialog(DIALOG_PLAYER);
	        		}
	        		else if(msgData.equals("nosferatu_renfield")) {
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_renfield);
	    				b.putString("android_text_info", "You are Renfield.");
	    				handler.sendMessage(m);
	        			player.renfield();
	        		}
	        		else if(msgData.equals("nosferatu_vampire")) {
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_vampire);
	    				b.putString("android_text_info", "You are the vampire.");
	    				handler.sendMessage(m);
	        			player.initializeRole(true);
	        			
	        			//sendDataMsg("PA_DRAWCARDS");
	        		}
	        		else if(msgData.equals("nosferatu_hunter")) {
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_hunter);
	    				b.putString("android_text_info", "You are a hunter.");
	    				handler.sendMessage(m);
	        			player.initializeRole(false);
	        			
	        			//sendDataMsg("PA_DRAWCARDS");
	        		}
	        		else if(msgData.contains("VAMPIRE_IS")) {
	        			String vampireName = msgData.split(":")[1];
	        			Message m = handler.obtainMessage();
	        			Bundle b = m.getData();
	        			b.putString("android_text_info", "The Vampire is " + vampireName + ".");
	        			handler.sendMessage(m);
	        		}
	        		else if(msgData.equals("nosferatu_bite")) {
        				Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_bite);
	    				b.putString("android_text_info", "You draw a bite.");
	    				handler.sendMessage(m);
	        			player.addCard("BITE");
	        			
        			
	        		}
	        		else if(msgData.equals("nosferatu_component")) {
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_component);
	    				b.putString("android_text_info", "You draw a component.");
	    				handler.sendMessage(m);
	        			player.addCard("COMPONENT");
	        			
		        	}
	        		else if(msgData.equals("nosferatu_rumor")) {
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_gossip);
	    				b.putString("android_text_info", "You draw a gossip.");
	    				handler.sendMessage(m);
	        			player.addCard("GOSSIP");
	        			
		        	}
	        		else if(msgData.equals("nosferatu_night")) {
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_night);
	    				b.putString("android_text_info", "You draw a night.");
	    				handler.sendMessage(m);
	        			player.addCard("NIGHT");
	        			
	        		}
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
	
		gameDataThread = new Thread(gameClient);
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
    public void confirm(View view) {
		sendDataMsg("PA_CONFIRM");
	}
    
    public void sendDataMsg(String msg) {
    	try {
    		gameClient.sendMessage(msg);
    	}catch(java.lang.NullPointerException npe)
		{
			//Start ServerOffline activity if server is offline
    		npe.printStackTrace();
			Intent intent = new Intent(this, ServerOfflineActivity.class);
			startActivity(intent);
			finish();
		}
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
        	tcpClient.sendMessage("PLAYER_NAME=" + playerName);
        	gameClient.sendMessage("PLAYER_NAME=" + playerName);
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
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_PLAYER:
			Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(dialogInfo);
			builder.setCancelable(true);
			builder.setItems(playersNames, onClickListener);
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	return super.onCreateDialog(id);
	}
	 
	private final class CancelOnClickListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			Toast.makeText(getApplicationContext(), "Activity will continue", Toast.LENGTH_LONG).show();
		}
	}
	 
	private final class OkOnClickListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			Toast.makeText(getApplicationContext(), "I was just kidding", Toast.LENGTH_LONG).show();
		}
	}
	
	private final class FirstPlayerListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			sendDataMsg(playersNames[which].toString());
		}
		
	}

}
