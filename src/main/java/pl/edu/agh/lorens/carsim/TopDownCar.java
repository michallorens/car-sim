package pl.edu.agh.lorens.carsim;

import org.iforce2d.Jb2dJson;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.FrictionJointDef;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.TestbedTest;

public class TopDownCar extends TestbedTest {
	CarContactListener ccl;
	float bestTime = Float.POSITIVE_INFINITY;
	int controlState;
    Body groundBody;
    SimCar car;

    public void createRaceTrack(){
        Jb2dJson json = new Jb2dJson();
        StringBuilder stringBuilder = new StringBuilder();
        json.readFromFile("racetrack.json", stringBuilder, getWorld());
        System.out.println("error: "+stringBuilder.toString());
        getWorld().setContactListener(ccl = new CarContactListener());
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
    private static final float DEGTORAD = 0.0174532925199432957f;

    @Override
	public void initTest(boolean deserialized) {
        createRaceTrack();

        PolygonShape polygonShape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = polygonShape;
        fixtureDef.isSensor = true;
        polygonShape.setAsBox(1,30, new Vec2(), 68*DEGTORAD);
        Fixture groundAreaFixture = groundBody.createFixture(fixtureDef);
        groundAreaFixture.setUserData(new FinishLineFUD());

        car = new SimCar(getWorld());

        controlState = 0;
	}

	@Override
	public void keyPressed(char argKeyChar, int argKeyCode) {
        switch (argKeyCode) {
	        case 37 : controlState |= SimControl.LEFT.getDirection(); break;
	        case 38 : controlState |= SimControl.UP.getDirection(); break;
	        case 39 : controlState |= SimControl.RIGHT.getDirection(); break;
	        case 40 : controlState |= SimControl.DOWN.getDirection(); break;
        }
    }

	@Override
	public void keyReleased(char argKeyChar, int argKeyCode) {
		super.keyReleased(argKeyChar, argKeyCode);
        switch(argKeyCode) {
	        case 37 : controlState &= ~SimControl.LEFT.getDirection(); break;
	        case 38 : controlState &= ~SimControl.UP.getDirection(); break;
	        case 39 : controlState &= ~SimControl.RIGHT.getDirection(); break;
	        case 40 : controlState &= ~SimControl.DOWN.getDirection(); break;
        }
    }

    @Override
    public synchronized void step(TestbedSettings settings) {
        super.step(settings);
        car.update(controlState);

        int rearImpulse = settings.getSetting("Rear wheel impulse").value;
        int frontImpulse = settings.getSetting("Front wheel impulse").value;
        int rearDrive = settings.getSetting("Rear wheel drive force").value;
        int frontDrive = settings.getSetting("Front wheel drive force").value;
        int backwardSpeed = settings.getSetting("Backward speed").value;
        int forwardSpeed = settings.getSetting("Forward speed").value;

        car.getTires().get(0).setMaxLateralImpulse(rearImpulse);
        car.getTires().get(0).setMaxDriveForce(rearDrive);
        car.getTires().get(0).setMaxBackwardSpeed(backwardSpeed);
        car.getTires().get(0).setMaxForwardSpeed(forwardSpeed);
        car.getTires().get(1).setMaxLateralImpulse(rearImpulse);
        car.getTires().get(1).setMaxDriveForce(rearDrive);
        car.getTires().get(1).setMaxBackwardSpeed(backwardSpeed);
        car.getTires().get(1).setMaxForwardSpeed(forwardSpeed);
        car.getTires().get(2).setMaxLateralImpulse(frontImpulse);
        car.getTires().get(2).setMaxDriveForce(frontDrive);
        car.getTires().get(2).setMaxBackwardSpeed(backwardSpeed);
        car.getTires().get(2).setMaxForwardSpeed(forwardSpeed);
        car.getTires().get(3).setMaxLateralImpulse(frontImpulse);
        car.getTires().get(3).setMaxDriveForce(rearDrive);
        car.getTires().get(3).setMaxBackwardSpeed(backwardSpeed);
        car.getTires().get(3).setMaxForwardSpeed(forwardSpeed);

        Vec2 oldViewCenter = getCamera().getTransform().getCenter();
        Vec2 posOfCarVerySoon = car.body.getPosition().add(car.body.getLinearVelocity().mul(0.25f));
        getDebugDraw().drawString(new Vec2(5, 75), "car velocity:  " + String.valueOf(
        		(int) car.getForwardVelocity().length()), Color3f.BLUE);
        getDebugDraw().drawString(new Vec2(5, 95), "tire lateral speed:", Color3f.WHITE);
        getDebugDraw().drawString(new Vec2(5, 115), "rear-left     " + String.valueOf(
        		(int) car.getTires().get(0).getFriction()), Color3f.WHITE);
        getDebugDraw().drawString(new Vec2(5, 130), "rear-right    " + String.valueOf(
        		(int) car.getTires().get(1).getFriction()), Color3f.WHITE);
        getDebugDraw().drawString(new Vec2(5, 145), "front-left      " + String.valueOf(
        		(int) car.getTires().get(2).getFriction()), Color3f.WHITE);
        getDebugDraw().drawString(new Vec2(5, 160), "front-right     " + String.valueOf(
        		(int) car.getTires().get(3).getFriction()), Color3f.WHITE);
        		
        float prevTime = (ccl.getCurrentTime()  - ccl.getPreviousTime()) / 1000.0f;
        if(prevTime < bestTime)
        	bestTime = prevTime;
        getDebugDraw().drawString(new Vec2(5, 180), "best time:      " + String.valueOf(bestTime), Color3f.WHITE);
        getDebugDraw().drawString(new Vec2(5, 195), "previous time:  " + String.valueOf(prevTime), Color3f.WHITE);
        getDebugDraw().drawString(new Vec2(5, 215), "lap time:       " + String.valueOf(
        		(System.currentTimeMillis() - ccl.getCurrentTime()) / 1000.0f), Color3f.GREEN);

        getCamera().setCamera(oldViewCenter.mul(0.9f).add(posOfCarVerySoon.mul(0.1f)), 2.8f);
    }

	@Override
	public String getTestName() {
		return "Top-down car simulator";
	}

}
