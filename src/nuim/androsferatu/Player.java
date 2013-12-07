package nuim.androsferatu;

public class Player {

	private String name;
	private String[] cards;
	private boolean first;
	private boolean renfield;
	private boolean nosferatu;
	private GameActivity gameActivity;
	private int bites;
	
	public Player(boolean renfield, GameActivity gameActivity) {
		this.gameActivity = gameActivity;
		this.renfield = renfield;
		if(renfield) {
			nosferatu = false;
			first = false;
			gameActivity.showRenfield();
		}
		this.bites = 0;
	}
	
	public void initializeRole(boolean nosferatu) {
		this.nosferatu = nosferatu;
	}
	
	public void initializeCards(String[] cards) {
		this.cards = cards;
	}
	
	public String[] actionPhase(String[] drawCards) {
		String[] resultCards = new String[2];
		resultCards = gameActivity.chooseCards(cards, drawCards);
		for(int i=0; i<2-bites; i++) {
			if(cards[i] == null) { // !! drawcards really has been changed?
				if(drawCards[0] != null) {
					cards[i] = drawCards[0];
				}
				else {
					cards[i] = drawCards[1];
				}
			}
		}
		return resultCards;
	}
	
	public boolean kill() {
		return nosferatu;
	}

}
