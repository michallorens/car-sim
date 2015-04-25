package pl.edu.agh.lorens.carsim;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jbox2d.testbed.framework.TestList;
import org.jbox2d.testbed.framework.TestbedController;
import org.jbox2d.testbed.framework.TestbedErrorHandler;
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.TestbedController.MouseBehavior;
import org.jbox2d.testbed.framework.TestbedController.UpdateBehavior;
import org.jbox2d.testbed.framework.j2d.DebugDrawJ2D;
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D;
import org.jbox2d.testbed.framework.j2d.TestbedSidePanel;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
		TestbedModel model = new TestbedModel();
		final TestbedController controller =
				new TestbedController(model, UpdateBehavior.UPDATE_CALLED, MouseBehavior.NORMAL,
						new TestbedErrorHandler() {
								public void serializationError(Exception e, String message) {
									JOptionPane.showMessageDialog(null, message, "Serialization Error",
									JOptionPane.ERROR_MESSAGE);
								}
						});
		TestPanelJ2D panel = new TestPanelJ2D(model, controller);
		model.setPanel(panel);
		model.setDebugDraw(new DebugDrawJ2D(panel, true));
		model.addCategory("Custom simulations");
		model.addTest(new TopDownCar());
		TestList.populateModel(model);
		
		JFrame testbed = new JFrame();
		testbed.setTitle("JBox2D Testbed");
		testbed.setLayout(new BorderLayout());
		TestbedSidePanel side = new TestbedSidePanel(model, controller);
		testbed.add((Component) panel, "Center");
		testbed.add(new JScrollPane(side), "East");
		testbed.pack();
		testbed.setVisible(true);
		testbed.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		System.out.println(System.getProperty("java.home"));
		
		SwingUtilities.invokeLater(new Runnable() {
		  public void run() {
		    controller.playTest(0);
		    controller.start();
		  }
		});
    }
}
