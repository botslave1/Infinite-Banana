package scripts;

import java.awt.Graphics;
import java.awt.Point;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.tribot.api.Timing;
import org.tribot.api.util.Screenshots;
import org.tribot.api2007.Login;
import org.tribot.api2007.Player;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Ending;
import org.tribot.script.interfaces.MousePainting;
import org.tribot.script.interfaces.MouseSplinePainting;
import org.tribot.script.interfaces.Painting;

@ScriptManifest(authors = "BOTSLAVE", category = "Money making", name = SUPER.SCRIPT_NAME, version = 1.0, description = "Collects bananas from Wydin's storeroom and banks them.", gameMode = 1)

public class InfiniteBanana extends Script implements Painting, Ending, MousePainting, MouseSplinePainting
{
	private State m_state;
	private Collector m_collector;
	private Banker m_banker;
	private Walker m_walker;
	
	// Overridden functions
	@Override
	public void run()
	{
		initialise();
		
		logName();

		while (SUPER.RUNNING)
		{
			if (Login.getLoginState() == Login.STATE.INGAME)
			{
				switch (m_state.getState())
				{
					case COLLECTING_BANANAS :
						m_collector.run();
						break;
					case INTERACTING_WITH_DEPOSIT_BOX :
						m_banker.run();
						break;
					case TRAVELLING_TO_DEPOSIT_BOX :
						m_walker.run(SUPER.DESTINATION.DEPOSIT_BOX);
						break;
					case TRAVELLING_TO_SHOP :
						m_walker.run(SUPER.DESTINATION.SHOP);
						break;
					case INITIALISING :
						break;
				}

				if ((System.currentTimeMillis() - SUPER.TIME_LAST_PRICE_CHECK) > SUPER.MILIS_IN_HOUR)
				{
					SUPER.priceCheckBanana();
				}
			}
			
			sleep(SUPER.SLEEP_TIME);
		}
	}
	
	
	@Override
	public void onEnd()
	{
		shutDownMessage();
	}
	
	// Member functions
	
	private void initialise()
	{
		PAINT.init();
		SUPER.init(this);
		ANTIBAN.init();
		m_state = new State();
		m_collector = new Collector();
		m_banker = new Banker();
		m_walker = new Walker();
	}

	private void logName()
	{
		if (Login.getLoginState() == Login.STATE.INGAME)
		{
			if (Player.getRSPlayer().getName() != SUPER.ACC_NAME)
			{
				SUPER.ACC_NAME = Player.getRSPlayer().getName();
			}
		}
	}
	
	private void shutDownMessage() 
	{	
		if (SUPER.SAVE_PROGS)
		{
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			
			String name = (SUPER.ACC_NAME == null) ? "UNKNOWN" : SUPER.ACC_NAME;
			
			name += "_PROG_" + dateFormat.format(date).replace(" ", "_").replace("/", "-").replaceAll(":", "-") + ".png";
			
			try
			{
				Screenshots.take(name , false, true);
			}
			catch (Exception e)
			{
				println("Couldn't save screenshot: " + e.getMessage());
			}

			println("A progress screenshot was taken and saved in your default TRiBot screenshot folder");
		}
		
		if (!SUPER.RUNNING) Login.logout(); 
		
		String timeRan = Timing.msToString(System.currentTimeMillis() - SUPER.SCRIPT_START_TIME);
		println("Thank you for using Infinite Banana by BOTSLAVE!");
		println("Ran for [ " + timeRan + " ]" + " and collected [ " + SUPER.TOTAL_BANANAS + " bananas ] making [ " + SUPER.TOTAL_PROFIT + "gp ] profit");
		println("Remember to sell your bananas responsibly! Try to leave them in at med-price for a while and see if they sell");
	}
	
	public void print (String s) { println(s); }
	public void print (long l) { println(l); }
	public void print(boolean b) { println(b); }
	public void print(double d) { println(d); }


	@Override
	public void paintMouseSpline(Graphics g, ArrayList<Point> p) {} // Nothing as we dont want to draw the trail

	@Override
	public void paintMouse(Graphics g, Point p1, Point p2) { PAINT.paintMouse(g, p1); }


	@Override
	public void onPaint(Graphics g) { PAINT.paint(g); }


}
