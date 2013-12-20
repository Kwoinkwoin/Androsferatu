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
	private CharSequence[] playerCards;
	private CharSequence[] magics;
	private String dialogInfo;
	private DialogInterface.OnClickListener onClickListener;
	private String confirmMessage;
	private int draws;
	private boolean kill;
	
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
		this.confirmMessage = "PA_CONFIRM";
		this.draws = 0;
		this.kill = false;
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
	        			player.addCard("BITE");
	        			draws++;
	        			if(draws == 2) {
	        				confirmMessage = "END_DRAW";
	        				draws = 0;
	        			}
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_bite);
	    				b.putString("android_text_info", "You draw a bite.");
	    				handler.sendMessage(m);
	        			
        			
	        		}
	        		else if(msgData.equals("nosferatu_component")) {
	        			player.addCard("COMPONENT");
	        			draws++;
	        			if(draws == 2) {
	        				confirmMessage = "END_DRAW";
	        				draws = 0;
	        			}
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_component);
	    				b.putString("android_text_info", "You draw a component.");
	    				handler.sendMessage(m);
	        			
		        	}
	        		else if(msgData.equals("nosferatu_rumor")) {
	        			draws++;
	        			if(draws == 2) {
	        				confirmMessage = "END_DRAW";
	        				draws = 0;
	        			}
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_gossip);
	    				b.putString("android_text_info", "You draw a gossip.");
	    				handler.sendMessage(m);
	        			player.addCard("GOSSIP");
		        	}
	        		else if(msgData.equals("nosferatu_night")) {
	        			player.addCard("NIGHT");
	        			draws++;
	        			if(draws == 2) {
	        				confirmMessage = "END_DRAW";
	        				draws = 0;
	        			}
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_night);
	    				b.putString("android_text_info", "You draw a night.");
	    				handler.sendMessage(m);
	        		}
	        		else if(msgData.equals("CURRENT_PLAYER")) {
	        			confirmMessage = "PA_DRAWCARDS";
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_night);
	    				b.putString("android_text_info", "It's your turn.");
	    				handler.sendMessage(m);
	        		}
	        		else if(msgData.equals("ASK_CARD_RENFIELD")) {
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_renfield);
	    				b.putString("android_text_info", "Chose a card for Renfield.");
	    				handler.sendMessage(m);
	        			playerCards = player.getRenfieldCards();
	        			dialogInfo = "Chose a card for Renfield";
	        			onClickListener = new CardRenfieldListener();
	        			showDialog(DIALOG_CARD);
	        		}
	        		else if(msgData.contains("RENFIELD_SHOW_CARD")) {
	        			if(player.isRenfield()) {
		        			String[] temp = msgData.split(":");
		        			String renfieldShowCard = temp[1];
		        			String from = temp[2];
		        			Message m = handler.obtainMessage();
		    				Bundle b = m.getData();
		    				if(renfieldShowCard.equals("nosferatu_bite")) {
		    					b.putString("android_image_id", "" + R.drawable.nosferatu_bite);
		    					b.putString("android_text_info", from + " has played a bite");
		    				}
		    				else if(renfieldShowCard.equals("nosferatu_rumor")) {
		    					b.putString("android_image_id", "" + R.drawable.nosferatu_gossip);
		    					b.putString("android_text_info", from + " has played a gossip");
		    				}
		    				else if(renfieldShowCard.equals("nosferatu_component")) {
		    					b.putString("android_image_id", "" + R.drawable.nosferatu_component);
		    					b.putString("android_text_info", from + " has played a component");
		    				}
		    				else if(renfieldShowCard.equals("nosferatu_night")) {
		    					b.putString("android_image_id", "" + R.drawable.nosferatu_night);
		    					b.putString("android_text_info", from + " has played a night");
		    				}
		    				handler.sendMessage(m);
	        			}
	        		}
	        		else if(msgData.contains("ASK_CARD_DISCARD")) {
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_night);
	    				b.putString("android_text_info", "Chose a card to discard.");
	    				handler.sendMessage(m);
	        			playerCards = player.getDiscardCards();
	        			dialogInfo = "Chose a card to discard";
	        			onClickListener = new CardDiscardListener();
	        			showDialog(DIALOG_CARD);
	        		}
	        		else if(msgData.contains("ALL_SHOW_CARD")) {
	        			confirmMessage = "PA_CONFIRM_NEXT_TURN";
	        			String[] temp = msgData.split(":");
	        			String allShowCard = temp[1];
	        			String from = temp[2];
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				if(allShowCard.equals("nosferatu_bite")) {
	    					b.putString("android_image_id", "" + R.drawable.nosferatu_bite);
	    					b.putString("android_text_info", from + " has discarded a bite");
	    				}
	    				else if(allShowCard.equals("nosferatu_rumor")) {
	    					b.putString("android_image_id", "" + R.drawable.nosferatu_gossip);
	    					b.putString("android_text_info", from + " has discarded a gossip");
	    				}
	    				else if(allShowCard.equals("nosferatu_component")) {
	    					b.putString("android_image_id", "" + R.drawable.nosferatu_component);
	    					b.putString("android_text_info", from + " has discarded a component");
	    				}
	    				else if(allShowCard.equals("nosferatu_night")) {
	    					b.putString("android_image_id", "" + R.drawable.nosferatu_night);
	    					b.putString("android_text_info", from + " has discarded a night");
	    				}
	    				handler.sendMessage(m);
	        		}
	        		else if(msgData.contains("END_TURN")) {
	        			confirmMessage="PA_CONFIRM_CLOCK";
	        			String clock = msgData.split(":")[1];
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	        			if(clock.equals("nosferatu_night")) {
	        				b.putString("android_image_id", "" + R.drawable.nosferatu_night);
	        				b.putString("android_text_info", "It's still night.");
	        			}
	        			else if(clock.equals("nosferatu_dawn")) {
	        				b.putString("android_image_id", "" + R.drawable.nosferatu_dawn);
	        				b.putString("android_text_info", "The sun is rising.");
	        			}
	        			handler.sendMessage(m);
	        		}
	        		else if(msgData.equals("MAGIC_OCCURS")) {
	        			confirmMessage = "kwoin";
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_component);
        				b.putString("android_text_info", "Full Component : magic occurs!");
        				handler.sendMessage(m);
	        		}
	        		else if(msgData.contains("WHICH_MAGIC")) {
	        			String[] temp = msgData.split(";");
	        			magics = new CharSequence[temp.length-1];
	        			for(int i=1; i<temp.length; i++) {
	        				magics[i-1] = temp[i];
	        			}
	        			dialogInfo = "Magic occurs!";
	        			onClickListener = new MagicListener();
	        			showDialog(DIALOG_MAGIC);
	        		}
	        		else if(msgData.contains("VAMPIRE_BITES")) {
	        			int bites = Integer.parseInt(msgData.split(":")[1]);
	        			confirmMessage = "kwoin";
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_bite);
        				b.putString("android_text_info", "The vampire has bitten : " + bites + "x .");
        				handler.sendMessage(m);
	        		}
	        		else if(msgData.contains("WHICH_TARGET_BITTEN")) {
	        			String[] temp = msgData.split(";");
	        			int nBites = Integer.parseInt(temp[1]);
	        			playersNames = new CharSequence[temp.length-2];
	        			for(int i=2; i<temp.length; i++) {
	        				playersNames[i-2] = temp[i];
	        			}
	        			if(nBites < 2) {
	        				dialogInfo = "Which target to bite?";
	        				onClickListener = new BiteListener();
	        				showDialog(DIALOG_PLAYER);
	        			}
	        			else {
	        				for(int i=0; i<nBites; i++) {
	        					dialogInfo = "Target " + (i+1) + " to bite?";
		        				onClickListener = new BiteListener();
		        				showDialog(DIALOG_PLAYER);
	        				}
	        			}
	        		}
	        		else if(msgData.contains("HAS_BEEN_BITTEN")) {
	        			confirmMessage = "PA_CONFIRM_END_TURN";
	        			String target = msgData.split(":")[1];
	        			if(player.getName().equals(target)) {
        					player.addBite();
        				}
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_bite);
        				b.putString("android_text_info", target + " has been bitten!");
        				handler.sendMessage(m);
	        		}
	        		else if(msgData.equals("VAMPIRE_WIN")) {
	        			confirmMessage = "kwoin";
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_vampire);
        				b.putString("android_text_info", "The Vampire has bitten 5 times. VAMPIRE WINS!");
        				handler.sendMessage(m);
	        		}
	        		else if(msgData.contains("VAMPIRE_SECOND_WIN")) {
	        			confirmMessage = "kwoin";
	        			String target = msgData.split(":")[1];
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_vampire);
        				b.putString("android_text_info", target + " has been killed but he was a hunter!. VAMPIRE WINS!");
        				handler.sendMessage(m);
	        		}
	        		else if(msgData.equals("NOTHING_HAPPENS")) {
	        			confirmMessage = "PA_CONFIRM_END_TURN";
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				if(player.isRenfield()) {
	    					b.putString("android_image_id", "" + R.drawable.nosferatu_renfield);
	    				}
	    				else if(player.isNosferatu()) {
	    					b.putString("android_image_id", "" + R.drawable.nosferatu_vampire);
	    				}
	    				else if(!player.isNosferatu()) {
	    					b.putString("android_image_id", "" + R.drawable.nosferatu_hunter);
	    				}
        				b.putString("android_text_info", "Nothing happens...");
        				handler.sendMessage(m);
	        		}
	        		else if(msgData.contains("WHICH_IDENTITY")) {
	        			String[] temp = msgData.split(";");
	        			playersNames = new CharSequence[temp.length-1];
	        			for(int i=1; i<temp.length; i++) {
	        				playersNames[i-1] = temp[i];
	        			}
	        			dialogInfo = "Which identity to reveal?";
	        			onClickListener = new IdentityListener();
	        			showDialog(DIALOG_PLAYER);
	        		}
	        		else if(msgData.contains("TARGET_IDENTITY_IS")) {
	        			confirmMessage = "PA_CONFIRM_END_TURN";
	        			String target = msgData.split(":")[1];
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_hunter);
	    				b.putString("android_text_info", target + " is a hunter!");
        				handler.sendMessage(m);
	        		}
	        		else if(msgData.contains("WHICH_TRANSFUSION")) {
	        			String[] temp = msgData.split(";");
	        			playersNames = new CharSequence[temp.length-1];
	        			for(int i=1; i<temp.length; i++) {
	        				playersNames[i-1] = temp[i];
	        			}
	        			dialogInfo = "Who transfuse?";
	        			onClickListener = new TransfusionListener();
	        			showDialog(DIALOG_PLAYER);
	        		}
	        		else if(msgData.contains("TARGET_TRANSFUSION_IS")) {
	        			confirmMessage = "PA_CONFIRM_END_TURN";
	        			String target = msgData.split(":")[1];
	        			if(player.getName().equals(target)) {
        					player.removeBite(msgData.split(":")[2]);
        				}
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_ambiance4);
	    				b.putString("android_text_info", target + " has been transfused! He draws a card");
        				handler.sendMessage(m);
	        		}
	        		else if(msgData.contains("END_ALL_TURN")) {
	        			String[] temp = msgData.split(";");
	        			if(!player.isNosferatu()) {
		        			playersNames = new CharSequence[temp.length];
		        			playersNames[0] = "No thx";
		        			for(int i=1; i<temp.length; i++) {
		        				playersNames[i] = temp[i];
		        			}
		        			dialogInfo = "Kill somebody?";
		        			onClickListener = new KillListener();
		        			showDialog(DIALOG_PLAYER);
	        			}
	        			else {
	        				sendDataMsg("PA_TARGET_KILL:No thx");
	        			}
	        		}
	        		else if(msgData.equals("HUNTERS_WIN")) {
	        			confirmMessage = "kwoin";
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_hunter);
        				b.putString("android_text_info", "The Vampire has been killed. HUNTERS WIN!");
        				handler.sendMessage(m);
	        		}
	        		else if(msgData.equals("NIGHT_REMOVED")) {
	        			confirmMessage = "PA_CONFIRM_END_TURN";
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_ambiance4);
	    				b.putString("android_text_info", "A night has been removed from the clock.");
        				handler.sendMessage(m);
	        		}
	        		else if(msgData.equals("CLOCK_NIGHT")) {
	        			confirmMessage = "kwoin";
	        			Message m = handler.obtainMessage();
	    				Bundle b = m.getData();
	    				b.putString("android_image_id", "" + R.drawable.nosferatu_night);
	    				b.putString("android_text_info", "it's still night");
        				handler.sendMessage(m);
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
		sendDataMsg(confirmMessage);
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
			break;
			
		case DIALOG_CARD:
			Builder builder2 = new AlertDialog.Builder(this);
			builder2.setTitle(dialogInfo);
			builder2.setCancelable(true);
			builder2.setItems(playerCards, onClickListener);
			AlertDialog dialog2 = builder2.create();
			dialog2.show();
			break;
			
		case DIALOG_MAGIC:
			Builder builder3 = new AlertDialog.Builder(this);
			builder3.setTitle(dialogInfo);
			builder3.setCancelable(true);
			builder3.setItems(magics, onClickListener);
			AlertDialog dialog3 = builder3.create();
			dialog3.show();
			break;
			
		}
	return super.onCreateDialog(id);
	}
	
	private final class FirstPlayerListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			sendDataMsg("PA_FIRSTPLAYER:" + playersNames[which].toString());
		}
	}
	
	private final class CardRenfieldListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			player.removeCard(which);
			player.compactCards();
			sendDataMsg("PA_CARD_RENFIELD:" + playerCards[which].toString());
		}
	}
	
	private final class CardDiscardListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			player.removeCard(which);
			player.compactCards();
			sendDataMsg("PA_CARD_DISCARD:" + playerCards[which].toString());
		}
	}
	
	private final class MagicListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			sendDataMsg("PA_MAGIC:" + magics[which].toString());
		}
	}
	
	private final class BiteListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			sendDataMsg("PA_TARGET_BITTEN:" + playersNames[which].toString());
		}
	}
	
	private final class IdentityListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			sendDataMsg("PA_TARGET_IDENTITY:" + playersNames[which].toString());
		}
	}
	
	private final class TransfusionListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			sendDataMsg("PA_TARGET_TRANSFUSION:" + playersNames[which].toString());
		}
	}

	private final class KillListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			sendDataMsg("PA_TARGET_KILL:" + playersNames[which].toString());
		}
	}
}
