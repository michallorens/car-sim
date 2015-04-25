package pl.edu.agh.lorens.carsim;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.TestbedTest;

public class TopDownCar extends TestbedTest {
	private static final float DEGTORAD = 0.0174532925199432957f;

    int controlState;
    Body groundBody;
    SimCar car;

    
	@Override
	public void initTest(boolean deserialized) {
		getWorld().setGravity(new Vec2(0,0));

        BodyDef bodyDef = new BodyDef();
        groundBody = getWorld().createBody(bodyDef);

        PolygonShape polygonShape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = polygonShape;
        fixtureDef.isSensor = true;

        polygonShape.setAsBox(9, 7, new Vec2(-10, 15), 20*DEGTORAD);
        Fixture groundAreaFixture = groundBody.createFixture(fixtureDef);
        groundAreaFixture.setUserData(new GroundAreaFUD(0.5f, false));

        polygonShape.setAsBox(9, 5, new Vec2(5, 20), -40*DEGTORAD);
        groundAreaFixture = groundBody.createFixture(fixtureDef);
        groundAreaFixture.setUserData(new GroundAreaFUD(0.2f, false));

        car = new SimCar(getWorld());

        controlState = 0;
	}

	@Override
	public void keyPressed(char argKeyChar, int argKeyCode) {
        switch (argKeyChar) {
	        case 'a' : controlState |= SimControl.LEFT.getDirection(); break;
	        case 'd' : controlState |= SimControl.RIGHT.getDirection(); break;
	        case 'w' : controlState |= SimControl.UP.getDirection(); break;
	        case 's' : controlState |= SimControl.DOWN.getDirection(); break;
        }
    }

	@Override
	public void keyReleased(char argKeyChar, int argKeyCode) {
		super.keyReleased(argKeyChar, argKeyCode);
        switch(argKeyChar) {
	        case 'a' : controlState &= ~SimControl.LEFT.getDirection(); break;
	        case 'd' : controlState &= ~SimControl.RIGHT.getDirection(); break;
	        case 'w' : controlState &= ~SimControl.UP.getDirection(); break;
	        case 's' : controlState &= ~SimControl.DOWN.getDirection(); break;
        }
    }
	
    void handleContact(Contact contact, boolean began)
    {
        Fixture a = contact.getFixtureA();
        Fixture b = contact.getFixtureB();
        FixtureUserData fudA = (FixtureUserData) a.getUserData();
        FixtureUserData fudB = (FixtureUserData) b.getUserData();

        if(fudA == null || fudB == null)
            return;

        if(fudA.getType() == FixtureUserDataType.FUD_CAR_TIRE || fudB.getType() == FixtureUserDataType.FUD_GROUND_AREA)
            tireVsGroundArea(a, b, began);
        else if(fudA.getType() == FixtureUserDataType.FUD_GROUND_AREA || fudB.getType() == FixtureUserDataType.FUD_CAR_TIRE)
            tireVsGroundArea(b, a, began);
    }
    
    void BeginContact(Contact contact) {
    	handleContact(contact, true);
    }
    void EndContact(Contact contact) {
    	handleContact(contact, false);
    }
    
    void tireVsGroundArea(Fixture tireFixture, Fixture groundAreaFixture, boolean began)
    {
        SimTire tire = (SimTire) tireFixture.getBody().getUserData();
        GroundAreaFUD gaFud = (GroundAreaFUD) groundAreaFixture.getUserData();
        if(began)
            tire.addGroundArea(gaFud);
        else
            tire.removeGroundArea(gaFud);
    }

    @Override
    public synchronized void step(TestbedSettings settings) {
        super.step(settings);
        car.update(controlState);
        
        //TODO add debug info
    }

	@Override
	public String getTestName() {
		return "Top-down car simulator";
	}

}
