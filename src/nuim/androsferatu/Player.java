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
	
	public boolean isRenfield() {
		return this.renfield;
	}
	
	public boolean isNosferatu() {
		return this.nosferatu;
	}
	
	public void initializeRole(boolean nosferatu) {
		this.nosferatu = nosferatu;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void addBite() {
		if(bites < 2) {
			bites++;
		}
		cards[bites-1] = null;
	}
	
	public void removeBite(String card) {
		String temp = "";
		if(card.equals("nosferatu_bite")) {
			temp = "BITE";
		}
		else if(card.equals("nosferatu_rumor")) {
			temp = "GOSSIP";
		}
		else if(card.equals("nosferatu_component")) {
			temp = "COMPONENT";
		}
		if(card.equals("nosferatu_night")) {
			temp = "NIGHT";
		}
		if(bites == 1) {
			bites--;
			cards[0] = temp;
		}
		else if(bites == 2) {
			bites--;
			cards[1] = temp;
		}
	}
	
	public void addCard(String card) {
		if(bites == 0) {
			if(cards[0] == null) {
				cards[0] = card;
			}
			else if(cards[1] == null) {
				cards[1] = card;
			}
			else if(cards[2] == null) {
				cards[2] = card;
			}
			else if(cards[3] == null) {
				cards[3] = card;
			}
		}
		else if(bites == 1) {
			if(cards[1] == null) {
				cards[1] = card;
			}
			else if(cards[2] == null) {
				cards[2] = card;
			}
			else if(cards[3] == null) {
				cards[3] = card;
			}
		}
		else if(bites == 2) {
			if(cards[2] == null) {
				cards[2] = card;
			}
			else if(cards[3] == null) {
				cards[3] = card;
			}
		}

	}
	
	public CharSequence[] getRenfieldCards() {
		CharSequence[] res = new CharSequence[4-bites];
		int k = 0;
		for(int i=0; k<res.length; i++) {
			while(cards[i] == null) {
				i++;
			}
			res[k] = cards[i];
			k++;
		}
		return res;
	}
	
	public CharSequence[] getDiscardCards() {
		CharSequence[] res = new CharSequence[4-bites-1];
		int k = 0;
		for(int i=0; k<res.length; i++) {
			while(cards[i] == null) {
				i++;
			}
			res[k] = cards[i];
			k++;
		}
		return res;
	}
	
	
	public void removeCard(int n) {
		n += bites;
		cards[n] = null;
	}
	
	public void compactCards() {
		if(bites < 1) {
			if(cards[0] == null && cards[2] != null) {
				cards[0] = cards[2];
				cards[2] = null;
			}
			else if(cards[0] == null && cards[3] != null) {
				cards[0] = cards[3];
				cards[3] = null;
			}
		}
		if(bites < 2) {
			if(cards[1] == null && cards[2] != null) {
				cards[1] = cards[2];
				cards[2] = null;
			}
			else if(cards[1] == null && cards[3] != null) {
				cards[1] = cards[3];
				cards[3] = null;
			}
		}
		if(cards[2] == null && cards[3] != null) {
			cards[2] = cards[3];
			cards[3] = null;
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
