package lvl2advanced.p01gui.p03swing;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Callable;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class AppWindow extends JFrame{
	private JPanel contentPane;
	private int demoId = 1;
	static String[] names = { "lvl2advanced.p01gui.p01simple", "lvl5others.p02fractal.Renderer" };
	static int[] countMenuItems = { 1, 1 };
	static String[] nameMenuItem = { "basic", "advanced" };
	Callable<Integer> setApp;
	/*private void setApp(Frame testFrame, String name) {
		if (thread != null) {
			glfwWaitEvents();
			quit.countDown();

			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			thread.dispose();
			// make new window with renderer in new thread
		}
		quit = new CountDownLatch(1);
		thread = new LwjglWindowThread(0, quit, new lvl2advanced.p01gui.p01simple.Renderer());
		window = thread.getWindow();
		thread.start();
	}*/

public AppWindow(Callable<Integer> setApp) {
	this.setApp = setApp;
	ActionListener actionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent ae) {
			demoId = Integer
					.valueOf(ae.getActionCommand().substring(0, ae.getActionCommand().lastIndexOf('-') - 1).trim());
			try {
				setApp.call();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	MenuBar menuBar = new MenuBar();
	int menuIndex = 0;
	for (int itemMenu = 0; itemMenu < nameMenuItem.length; itemMenu++) {
		Menu menu1 = new Menu(nameMenuItem[itemMenu]);
		MenuItem m;
		for (int i = 0; i < names.length && i < countMenuItems[itemMenu]; i++) {
			m = new MenuItem(new Integer(menuIndex + 1).toString() + " - " + names[menuIndex]);
			m.addActionListener(actionListener);
			menu1.add(m);
			menuIndex++;
		}
		menuBar.add(menu1);
	}

	/*
	 * keyAdapter = new KeyAdapter() {
	 * 
	 * @Override public void keyPressed(KeyEvent e) { if ((e.getModifiers()
	 * & KeyEvent.ALT_MASK) != 0) { switch (e.getKeyCode()) { case
	 * KeyEvent.VK_HOME: demoId = 1; setApp(testFrame, names[demoId - 1]);
	 * 
	 * break; case KeyEvent.VK_END: demoId = names.length; setApp(testFrame,
	 * names[demoId - 1]); break; case KeyEvent.VK_LEFT: if (demoId > 1)
	 * demoId--; setApp(testFrame, names[demoId - 1]); break; case
	 * KeyEvent.VK_RIGHT: if (demoId < names.length) demoId++;
	 * setApp(testFrame, names[demoId - 1]); break; } } }
	 * 
	 * };
	 */
	setMenuBar(menuBar);
	addWindowListener(new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
			new Thread() {
				@Override
				public void run() {
					// if (animator.isStarted())
					// animator.stop();
					System.exit(0);
				}
			}.start();
		}
	});

	// testFrame.setTitle(ren.getClass().getName());

	pack();
	setVisible(true);
	
	}
}

