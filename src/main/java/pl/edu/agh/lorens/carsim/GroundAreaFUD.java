package pl.edu.agh.lorens.carsim;

public class GroundAreaFUD extends FixtureUserData {
	public float frictionModifier;
	public boolean outOfCourse;
	
	public GroundAreaFUD(float frictionModifier, boolean outOfCourse) {
		super(FixtureUserDataType.FUD_GROUND_AREA);
		this.frictionModifier = frictionModifier;
		this.outOfCourse = outOfCourse;
	}
}
