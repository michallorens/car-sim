package pl.edu.agh.lorens.carsim;

public class GroundAreaFUD extends FixtureUserData {
	public float frictionModifier;
	public float dragModifier;

	public GroundAreaFUD(float frictionModifier, float dragModifier) {
		super(FixtureUserDataType.FUD_GROUND_AREA);
		this.frictionModifier = frictionModifier;
		this.dragModifier = dragModifier;
	}
}
