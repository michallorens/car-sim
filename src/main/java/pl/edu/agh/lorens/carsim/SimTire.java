package pl.edu.agh.lorens.carsim;

import java.util.HashSet;
import java.util.Set;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;

public class SimTire {
	Body body;
	private float maxForwardSpeed;
	private float maxBackwardSpeed;
	private float maxDriveForce;
	private float currentTraction;
	private float maxLateralImpulse;
    Set<GroundAreaFUD> groundAreas = new HashSet<GroundAreaFUD>();
    private float currentDrag;

    private float lastDriveImpulse;
    private float lastLateralFrictionImpulse;
    
    private float friction;
	
	SimTire(World world) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DYNAMIC;
		body = world.createBody(bodyDef);
		
		PolygonShape polygonShape = new PolygonShape();
		polygonShape.setAsBox(0.5f, 1.25f);
		body.createFixture(polygonShape, 1).setUserData(new CarTireFUD());;
		
		body.setUserData(this);
		currentTraction = 1;
        currentDrag = 1;
	}
	
	void setCharacteristics(float maxForwardSpeed, float maxBackwardSpeed, float maxDriveForce, float maxLateralImpulse) {
        this.maxForwardSpeed = maxForwardSpeed;
        this.maxBackwardSpeed = maxBackwardSpeed;
        this.maxDriveForce = maxDriveForce;
        this.maxLateralImpulse = maxLateralImpulse;
    }
	
	void addGroundArea(GroundAreaFUD ga) {
		groundAreas.add(ga);
        updateTraction();
	}
	
    void removeGroundArea(GroundAreaFUD ga) {
    	groundAreas.remove(ga);
        updateTraction();
    }
    
    void updateTraction() {
        if(groundAreas.isEmpty()){
            currentTraction = 1;
        } else {
            currentTraction = 0;
            for(GroundAreaFUD ga : groundAreas) {
                if(ga.frictionModifier > currentTraction)
                    currentTraction = ga.frictionModifier;
            }
        }
    }
	
	Vec2 getLateralVelocity() {
		Vec2 currentRightNormal = body.getWorldVector(new Vec2(1, 0));
		return currentRightNormal.mul(Vec2.dot(currentRightNormal, body.getLinearVelocity()));
	}
	

    Vec2 getForwardVelocity() {
        Vec2 currentForwardNormal = body.getWorldVector(new Vec2(0,1));
		return currentForwardNormal.mul(Vec2.dot(currentForwardNormal, body.getLinearVelocity()));
    }
	
	void updateFriction() {
		//lateral linear velocity
		Vec2 impulse = getLateralVelocity().negate().mul(body.getMass());
        if(impulse.length() > maxLateralImpulse)
        	impulse = impulse.mul(maxLateralImpulse / impulse.length());
		body.applyLinearImpulse(impulse.mul(currentTraction), body.getWorldCenter(), true);

		//angular velocity
		body.applyAngularImpulse(currentTraction * 0.1f * body.getInertia() * -body.getAngularVelocity());
		
		//forward linear velocity
		Vec2 currentForwardNormal = getForwardVelocity();
		float currentForwardSpeed = currentForwardNormal.normalize();
		float dragForceMagnitude = -2f * currentForwardSpeed;
		
		body.applyForce(currentForwardNormal.mul(currentTraction * dragForceMagnitude), body.getWorldCenter());

		friction = getLateralVelocity().length();
	}
	
	void updateDrive(int controlState) {
        //find desired speed
        float desiredSpeed = 0;
        int control = controlState & (SimControl.UP.getDirection()|SimControl.DOWN.getDirection());

        if(SimControl.UP.getDirection() == control)
            desiredSpeed = maxForwardSpeed;
        else if(SimControl.DOWN.getDirection() == control)
            desiredSpeed = maxBackwardSpeed;

        //find current speed in forward direction
        Vec2 currentForwardNormal = body.getWorldVector(new Vec2(0,1));
        float currentSpeed = Vec2.dot(getForwardVelocity(), currentForwardNormal);

        //apply necessary force
        float force = 0;
        if(control > 0) {
	        if(desiredSpeed > currentSpeed)
	            force = maxDriveForce;
	        else if(desiredSpeed < currentSpeed)
	            force = -maxDriveForce * 0.5f;
	        else
	        	return;
        }
        
        body.applyForce(currentForwardNormal.mul(currentTraction * force), body.getWorldCenter());

//        float speedFactor = currentSpeed / 120;
//        Vec2 driveImpulse = currentForwardNormal.mul(force / 60.0f);
//
//        if(driveImpulse.length() > maxLateralImpulse){
//            driveImpulse.mul(maxLateralImpulse / driveImpulse.length());
//        }
//
//        Vec2 lateralFrictionImpulse = getLateralVelocity().mul(-body.getMass());
//        float lateralImpulseAvailable = maxLateralImpulse;
//        lateralImpulseAvailable *= 2.0f * speedFactor;
//
//        if ( lateralImpulseAvailable < 0.5f * maxLateralImpulse )
//            lateralImpulseAvailable = 0.5f * maxLateralImpulse;
//        /*else if ( lateralImpulseAvailable > m_maxLateralImpulse )
//            lateralImpulseAvailable = m_maxLateralImpulse;*/
//        if ( lateralFrictionImpulse.length() > lateralImpulseAvailable )
//            lateralFrictionImpulse.mul(lateralImpulseAvailable / lateralFrictionImpulse.length());
//
//        lastDriveImpulse = driveImpulse.length();
//        lastLateralFrictionImpulse = lateralFrictionImpulse.length();
//
//        Vec2 impulse = driveImpulse.add(lateralFrictionImpulse);
//        if ( impulse.length() > maxLateralImpulse )
//            impulse.mul(maxLateralImpulse / impulse.length());
//
//        body.applyLinearImpulse(impulse.mul(currentTraction), body.getWorldCenter(), true); //TODO: zmienic
    }

    void updateTurn(int controlState) {
        float desiredTorque = 0;
        int control = controlState & (SimControl.LEFT.getDirection() | SimControl.RIGHT.getDirection());
        if(SimControl.LEFT.getDirection() == control) desiredTorque = 15;
        if(SimControl.RIGHT.getDirection() == control) desiredTorque = -15;
        body.applyTorque(desiredTorque);
    }
    
    public float getFriction() {
    	return friction;
    }
    
    public void setMaxLateralImpulse(int max) {
    	this.maxLateralImpulse = max / 10.0f;
    }
    
    public void setMaxDriveForce(int max) {
    	this.maxDriveForce = max;
    }
    
    public void setMaxForwardSpeed(int max) {
    	this.maxForwardSpeed = max;
    }
    
    public void setMaxBackwardSpeed(int max) {
    	this.maxBackwardSpeed = max * -1.0f;
    }
}