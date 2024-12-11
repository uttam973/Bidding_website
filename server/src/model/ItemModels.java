package serverModel;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import serverCore.FullItem;
import serverCore.Item;

//this class get Item list as the input and create Model for creating table

public class ItemModels extends AbstractTableModel{
	
	public static final int OBJECT_COL = -1;
	private static final int SYMBOL    = 0;
	private static final int PRICE     = 1;
	private static final int PROFIT    = 2;
	private static final int CUSNAM    = 3;
	private static final int LHIGHT    = 4;
	
	private String[] colName = {"Symbol", "Price", "Profit", "Last Bid Customer", "Last High Bid Time"};
	
	private List<FullItem> symWithD;
	
	public ItemModels(List<FullItem> theSymWithD) {
		symWithD = theSymWithD;
	}
	
	
	@Override
	public int getColumnCount() {
		return colName.length;
	}

	@Override
	public int getRowCount() {
		return symWithD.size();
	}
	
	@Override
	public String getColumnName(int col) {
		return colName[col];
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		
		FullItem tmpItem = symWithD.get(row);
		
		switch (col) {
		case SYMBOL:
			return tmpItem.getSym();
		case PRICE:
			return tmpItem.getPrice();
		case PROFIT:
			return tmpItem.getProfit();
		case CUSNAM:
			return tmpItem.getCus();
		case LHIGHT:
			return tmpItem.getTime();
		case OBJECT_COL:
			return tmpItem;
		default :
			return tmpItem.getSym();
		}
		
	}
	
	@Override
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

}
