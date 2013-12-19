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
	
	public void addCard(String card) {
		if(bites == 0) {
			if(cards[0] == null) {
				cards[0] = card;
				return;
			}
			else if(cards[1] == null) {
				cards[1] = card;
				return;
			}
		}
		if(cards[2] == null) {
			cards[2] = card;
		}
		else if(cards[3] == null) {
			cards[3] = card;
		}
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
