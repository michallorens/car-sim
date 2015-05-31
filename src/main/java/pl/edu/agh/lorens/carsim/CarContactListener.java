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

        System.out.println(a.toString()+ " " + b.toString() + " " + fudA.toString() + " " + fudB.toString());

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
}
