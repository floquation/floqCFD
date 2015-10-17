package newCFD;

public class CFDStatus {

	public boolean isInitialized = false;
	public boolean hasAMesh = false;
	public status currentStatus = status.idle;
	
	public enum status{
		idle,
		iterating
	}

}
