package nuim.androsferatu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class GameActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}
	
	public String[] chooseCards(String[] cards, String[] drawCards) {
		String[] resultCards = new String[2];
		//graphical stuff to do
		return resultCards;
	}

	public void showRenfield() {
		// show Renfield portrait and ok button
	}
	
	public void showRoles(String[] roles) {
		
	}
	
	

}
