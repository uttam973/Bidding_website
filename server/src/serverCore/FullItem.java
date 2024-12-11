package serverCore;
// this extend from Item class and 
//additional with customer name and bidding time

public class FullItem extends Item{
	
	private String higBCus;
	private String lBidTime;
	
	public FullItem(String theSym, float thePrice, int theProfit  , String hbCus, String t) {
		super(theSym,thePrice, theProfit);
		this.higBCus   = hbCus;
		this.lBidTime  = t;
	}
	
	public String getCus() {
		return higBCus;
	}
	
	public String getTime() {
		return lBidTime;
	}

}
