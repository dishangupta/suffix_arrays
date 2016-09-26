package empsyn.sa;

public class SuffixArrayRange {
	private int s;
	private int e;
	
	public SuffixArrayRange() {
		this(0,0);
		return;
	}
	public SuffixArrayRange(int _s, int _e) {
		s = _s;
		e = _e;
		return;
	}
	
	public int getStart() {
		return s;
	}
	public void setStart(int _s) {
		s = _s;
		return;
	}
	
	public int getEnd() {
		return e;
	}
	public void setEnd(int _e) {
		e = _e;
		return;
	}
	
	public int getFrequency() {
		return e - s + 1;
	}
}
