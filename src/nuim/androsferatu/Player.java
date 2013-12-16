package nuim.androsferatu;

public class Player {

	private String name;
	private String[] cards;
	private boolean first;
	private boolean renfield;
	private boolean nosferatu;
	private InGameActivity inGameActivity;
	private int bites;
	
	public Player(String name, InGameActivity inGameActivity) {
		this.name = name;
		this.cards = new String[4];
		this.first = false;
		this.renfield = false;
		this.nosferatu = false;
		this.inGameActivity = inGameActivity;
		this.bites = 0;
	}
	
	public void renfield() {
		this.renfield = true;
	}
	
	public void initializeRole(boolean nosferatu) {
		this.nosferatu = nosferatu;
	}
	
	public void initializeCards(String[] cards) {
		this.cards = cards;
	}
	/*
	public String[] actionPhase(String[] drawCards) {
		String[] resultCards = new String[2];
		resultCards = inGameActivity.chooseCards(cards, drawCards);
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
	}*/
	
	public boolean kill() {
		return nosferatu;
	}

}
