package pl.edu.agh.lorens.carsim;

public class FixtureUserData {
	private FixtureUserDataType type;
	
	public FixtureUserData(FixtureUserDataType type) {
		this.type = type;
	}

	public FixtureUserData() {
	}

	public FixtureUserDataType getType() {
		return type;
	}
}
