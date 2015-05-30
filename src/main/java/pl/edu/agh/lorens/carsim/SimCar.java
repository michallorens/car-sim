package pl.edu.agh.lorens.carsim;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

public class SimCar {
	Body body;
	private List<SimTire> tires = new ArrayList<SimTire>();
	private RevoluteJoint flJoint, frJoint;
	private static final float DEGTORAD = 0.0174532925199432957f;
	
	public SimCar(World world) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DYNAMIC;
		body = world.createBody(bodyDef);
		body.setAngularDamping(5);
		
		Vec2[] vertices = new Vec2[8];
        vertices[0] = new Vec2(1.5f,   0f);
        vertices[1] = new Vec2(3f, 2.5f);
        vertices[2] = new Vec2(2.8f, 5.5f);
        vertices[3] = new Vec2(1f,  10f);
        vertices[4] = new Vec2(-1f,  10f);
        vertices[5] = new Vec2(-2.8f, 5.5f);
        vertices[6] = new Vec2(-3f, 2.5f);
        vertices[7] = new Vec2(-1.5f,   0f);
        
        PolygonShape polygonShape = new PolygonShape();
        polygonShape.set(vertices, 8);
        body.createFixture(polygonShape, 0.1f);
        
        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.bodyA = body;
        jointDef.enableLimit = true;
        jointDef.lowerAngle = 0;
        jointDef.upperAngle = 0;
        jointDef.localAnchorB.setZero();

        float maxForwardSpeed = 300;
        float maxBackwardSpeed = -40;
        float backTireMaxDriveForce = 600;
        float frontTireMaxDriveForce = 400;
        float backTireMaxLateralImpulse = 8.5f;
        float frontTireMaxLateralImpulse = 7.5f;
        
        SimTire tire = new SimTire(world);
        tire.setCharacteristics(maxForwardSpeed, maxBackwardSpeed, backTireMaxDriveForce, backTireMaxLateralImpulse);
        jointDef.bodyB = tire.body;
        jointDef.localAnchorA.set(-3f, 0.75f);
        world.createJoint(jointDef);
        tires.add(tire);
        
        tire = new SimTire(world);
        tire.setCharacteristics(maxForwardSpeed, maxBackwardSpeed, backTireMaxDriveForce, backTireMaxLateralImpulse);
        jointDef.bodyB = tire.body;
        jointDef.localAnchorA.set(3f, 0.75f);
        world.createJoint(jointDef);
        tires.add(tire);
        
        tire = new SimTire(world);
        tire.setCharacteristics(maxForwardSpeed, maxBackwardSpeed, frontTireMaxDriveForce, frontTireMaxLateralImpulse);
        jointDef.bodyB = tire.body;
        jointDef.localAnchorA.set(-3, 8.5f);
        flJoint = (RevoluteJoint) world.createJoint(jointDef);
        tires.add(tire);
        
        tire = new SimTire(world);
        tire.setCharacteristics(maxForwardSpeed, maxBackwardSpeed, frontTireMaxDriveForce, frontTireMaxLateralImpulse);
        jointDef.bodyB = tire.body;
        jointDef.localAnchorA.set(3f, 8.5f);
        frJoint = (RevoluteJoint) world.createJoint(jointDef);
        tires.add(tire);
	}
	
	void update(int controlState) {
        for(SimTire tire : tires)
            tire.updateFriction();
        for(SimTire tire : tires)
            tire.updateDrive(controlState);

        float lockAngle = 35 * DEGTORAD;
        float turnSpeedPerSec = 160 * DEGTORAD;//from lock to lock in 0.5 sec
        float turnPerTimeStep = turnSpeedPerSec / 60.0f;
        float desiredAngle = 0;
        int control = controlState & (SimControl.LEFT.getDirection()|SimControl.RIGHT.getDirection());
        if(SimControl.LEFT.getDirection() == control) desiredAngle = lockAngle;
        if(SimControl.RIGHT.getDirection() == control) desiredAngle = -lockAngle;

        float angleNow = flJoint.getJointAngle();
        float angleToTurn = desiredAngle - angleNow;
        angleToTurn = MathUtils.clamp(angleToTurn, -turnPerTimeStep, turnPerTimeStep);
        float newAngle = angleNow + angleToTurn;
        flJoint.setLimits(newAngle, newAngle);
        frJoint.setLimits(newAngle, newAngle);
    }

    public Vec2 getForwardVelocity(){
        Vec2 currentForwardNormal = body.getWorldVector(new Vec2(0,1));
        return currentForwardNormal.mul(Vec2.dot(currentForwardNormal, body.getLinearVelocity()));
    }
}
