package pl.edu.agh.lorens.carsim;

import org.iforce2d.Jb2dJson;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.FrictionJointDef;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.TestbedTest;

public class TopDownCar extends TestbedTest {
	private static final float DEGTORAD = 0.0174532925199432957f;

    int controlState;
    Body groundBody;
    SimCar car;

    public void createRaceTrack(){
        Jb2dJson json = new Jb2dJson();
        StringBuilder stringBuilder = new StringBuilder();
        json.readFromFile("racetrack.json", stringBuilder, getWorld());
        System.out.println("error: "+stringBuilder.toString());
        getWorld().setContactListener(new CarContactListener());
        getWorld().setDebugDraw(getDebugDraw());

        BodyDef bodyDef = new BodyDef();
        groundBody = getWorld().createBody(bodyDef);

        FrictionJointDef frictionJointDef = new FrictionJointDef();
        frictionJointDef.localAnchorA.setZero();
        frictionJointDef.localAnchorB.setZero();
        frictionJointDef.bodyA = groundBody;
        frictionJointDef.maxForce = 400;
        frictionJointDef.maxTorque = 400;
        frictionJointDef.collideConnected = true;



        Body[] barrelBodies;
        barrelBodies = json.getBodiesByName("barrel");
        for (Body barrelBody : barrelBodies) {
            frictionJointDef.bodyB = barrelBody;
            m_world.createJoint(frictionJointDef);
        }

        Fixture[] waterFixtures;
        waterFixtures = json.getFixturesByName("water");
        for (Fixture waterFixture : waterFixtures) {
            waterFixture.setUserData(new GroundAreaFUD(1, 30));
        }

        getWorld().setGravity(new Vec2(0,0));
    }
    
	@Override
	public void initTest(boolean deserialized) {
//		getWorld().setGravity(new Vec2(0,0));
//        getWorld().setContactListener(new CarContactListener());
//
//        BodyDef bodyDef = new BodyDef();
//        groundBody = getWorld().createBody(bodyDef);
//
//        PolygonShape polygonShape = new PolygonShape();
//        FixtureDef fixtureDef = new FixtureDef();
//        fixtureDef.shape = polygonShape;
//        fixtureDef.isSensor = true;
//
//        polygonShape.setAsBox(9, 7, new Vec2(-10, 15), 20*DEGTORAD);
//        Fixture groundAreaFixture = groundBody.createFixture(fixtureDef);
//        groundAreaFixture.setUserData(new GroundAreaFUD(0.5f, 30));
//
//        polygonShape.setAsBox(9, 5, new Vec2(5, 20), -40*DEGTORAD);
//        groundAreaFixture = groundBody.createFixture(fixtureDef);
//        groundAreaFixture.setUserData(new GroundAreaFUD(0.2f, 30));

        createRaceTrack();
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
