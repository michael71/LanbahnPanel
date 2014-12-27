package de.blankedv.lanbahnpanel;

public class Loco {
	public int adr;
	public String desc;
	
	public Loco(int adr, String desc) {
		super();
		this.adr = adr;
		this.desc = desc;
	}
	
	public Loco(int adr) {
		super();
		this.adr = adr;
		this.desc = "Adr="+adr;
	}
}
