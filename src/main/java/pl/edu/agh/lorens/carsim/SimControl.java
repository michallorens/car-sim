package pl.edu.agh.lorens.carsim;

public enum SimControl {
    LEFT (0x1),
    RIGHT (0x2),
    UP (0x4),
    DOWN (0x8);
    
    private int direction;
    
    SimControl(int direction) {
    	this.setDirection(direction);
    }

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}
}
