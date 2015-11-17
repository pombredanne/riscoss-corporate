package eu.riscoss.shared;


public class JAHPComparison {
	
	public int value = 1;
	
	String[] ids = new String[] { "", "" };
	
	public JAHPComparison() {}
	
	public JAHPComparison( String id1, String id2, int value ) {
		this.ids[0] = id1;
		this.ids[1] = id2;
		this.value = value;
	}
	
	public String getId1() {
		return ids[0];
	}
	
	public String getId2() {
		return ids[1];
	}
	
	public void setId1( String id ) {
		ids[0] = id;
	}
	
	public void setId2( String id ) {
		ids[1] = id;
	}
	
}