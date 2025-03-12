import java.util.*;

public class Station {
	
	protected String name;
	protected String lineColor;
	protected boolean flag;
	//protected ___ connection;
	
	public Station(String lineColor, String name) {
		this.name = name;
		this.lineColor = lineColor;
		this.flag = true;
	}
}