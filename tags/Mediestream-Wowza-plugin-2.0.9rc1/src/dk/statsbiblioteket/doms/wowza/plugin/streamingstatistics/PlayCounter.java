package dk.statsbiblioteket.doms.wowza.plugin.streamingstatistics;

public class PlayCounter {

	private int playCount;
	
	public PlayCounter() {
		super();
		this.playCount = 0;
	}

	public void add(int count) {
		playCount+=count;
	}
	
	public int getCount() {
		return playCount;
	}
}
