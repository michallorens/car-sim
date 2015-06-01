package pl.edu.agh.lorens.carsim;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.contacts.Contact;

/**
 * Created by selmerin on 09.05.15.
 */
public class CarContactListener implements ContactListener {

	private long lastUpdate;
	private long previousTime;
	private long currentTime;

    public void beginContact(Contact contact) {
        handleContact(contact, true);
    }

    public void endContact(Contact contact) {
        handleContact(contact, false);
    }

    public void preSolve(Contact contact, Manifold manifold) { }

    public void postSolve(Contact contact, ContactImpulse contactImpulse) { }

    void handleContact(Contact contact, boolean began)
    {
        Fixture a = contact.getFixtureA();
        Fixture b = contact.getFixtureB();
        FixtureUserData fudA = (FixtureUserData) a.getUserData();
        FixtureUserData fudB = (FixtureUserData) b.getUserData();

        if(fudA == null || fudB == null)
            return;

        if((fudA.getType() == FixtureUserDataType.FUD_CAR_BODY && fudB.getType() == FixtureUserDataType.FUD_FINISH)
        		|| (fudA.getType() == FixtureUserDataType.FUD_FINISH && fudB.getType() == FixtureUserDataType.FUD_CAR_BODY)) {
	        if(System.currentTimeMillis() - lastUpdate > 5000) {
	        	lastUpdate = System.currentTimeMillis();
	        	previousTime = currentTime;
		        currentTime = System.currentTimeMillis();
	        }
        }

        if(fudA.getType() == FixtureUserDataType.FUD_CAR_TIRE && fudB.getType() == FixtureUserDataType.FUD_GROUND_AREA)
            tireVsGroundArea(a, b, began);
        else if(fudA.getType() == FixtureUserDataType.FUD_GROUND_AREA && fudB.getType() == FixtureUserDataType.FUD_CAR_TIRE)
            tireVsGroundArea(b, a, began);

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

	public long getCurrentTime() {
		return currentTime;
	}

	public long getPreviousTime() {
		return previousTime;
	}
}
