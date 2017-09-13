package scripts;

import java.util.*;

import org.tribot.api.util.abc.*;
import org.tribot.api2007.Banking;

// https://tribot.org/forums/topic/60720-guide-to-implementing-abc2/
public class ANTIBAN
{
	private static List<WaitedActivity> m_waitedActivities;
	private static Map<String, Long> m_waitStarts;
	private static ABCUtil m_abc;
	private static double m_abLevel;
	
	public static void run()
	{
		if (m_abc.shouldExamineEntity() && !Banking.isDepositBoxOpen())
		{
			if (SUPER.ANTIBAN_VERBOSITY.ordinal() > SUPER.LOG_VERBOSITY.NONE.ordinal()) SUPER.print("ABC2: Examing something...");
			m_abc.examineEntity();
		}

		if (m_abc.shouldMoveMouse())
		{
			if (SUPER.ANTIBAN_VERBOSITY.ordinal() > SUPER.LOG_VERBOSITY.NONE.ordinal()) SUPER.print("ABC2: Moving mouse...");
			m_abc.moveMouse();
		}

		if (m_abc.shouldPickupMouse())
		{
			if (SUPER.ANTIBAN_VERBOSITY.ordinal() > SUPER.LOG_VERBOSITY.NONE.ordinal()) SUPER.print("ABC2: Picking up mouse...");
			m_abc.pickupMouse();
		}

		if (m_abc.shouldRightClick())
		{
			if (SUPER.ANTIBAN_VERBOSITY.ordinal() > SUPER.LOG_VERBOSITY.NONE.ordinal()) SUPER.print("ABC2: Right clicking...");
			m_abc.rightClick();
		}

		if (m_abc.shouldRotateCamera())
		{
			if (SUPER.ANTIBAN_VERBOSITY.ordinal() > SUPER.LOG_VERBOSITY.NONE.ordinal()) SUPER.print("ABC2: Rotating camera...");
			m_abc.rotateCamera();
		}

		if (m_abc.shouldLeaveGame())
		{
			if (SUPER.ANTIBAN_VERBOSITY.ordinal() > SUPER.LOG_VERBOSITY.NONE.ordinal()) SUPER.print("ABC2: Moving mouse off screen");
			m_abc.leaveGame();
		}
	}
	
	public static void init() 
	{
		m_abLevel = SUPER.ANTIBAN_LEVEL;
		m_waitedActivities = new LinkedList<WaitedActivity>();
		m_waitStarts = new HashMap<String, Long>();
		m_abc = new ABCUtil();
	}
	
	public static void resetStarts ()
	{
		m_waitStarts.clear();
		if (SUPER.ANTIBAN_VERBOSITY.ordinal() > SUPER.LOG_VERBOSITY.LOW.ordinal()) SUPER.print("ABC2: STATE has changed. Cleaning antiban cache");
	}
	
	public static void startTimer(String activity)
	{
		m_waitStarts.put(activity, System.currentTimeMillis());
		if (SUPER.ANTIBAN_VERBOSITY.ordinal() > SUPER.LOG_VERBOSITY.LOW.ordinal()) SUPER.print("ABC2: Added timeStart for " + activity + " [ " + System.currentTimeMillis() + " ]");
	}
	
	// Generally, you'd call this after generating a reaction time using ABC2, after we call ABCUtil#sleep for that reaction time, and after we perform the action which we are reacting to.
	public static void updateTracker(String activity)
	{
		long averageWait = getAverageWait(activity);
		if (averageWait == -1) averageWait = 200;
		
		m_abc.generateTrackers(m_abc.generateBitFlags((int)averageWait));
	}
	
	public static void waitForReactionTime(String activity)
	{
		final int reactionTime = getNextWait(activity);
		
		try
		{
			if (SUPER.ANTIBAN_VERBOSITY.ordinal() > SUPER.LOG_VERBOSITY.LOW.ordinal()) SUPER.print("ABC2 Sleep [ " + activity + " ]: " + reactionTime + "ms");
			m_abc.sleep(reactionTime);
		}
		catch (final InterruptedException e)
		{
			
		}
	}

	private static int getNextWait(String activity) 
	{
		long averageWait = getAverageWait(activity);
		
		if (SUPER.ANTIBAN_VERBOSITY.ordinal() > SUPER.LOG_VERBOSITY.LOW.ordinal()) SUPER.print("ABC2: Average wait time for activity [" + activity + "] is " + averageWait);
		
		final boolean menuOpen = m_abc.shouldOpenMenu() && m_abc.shouldHover();
		final boolean hovering = m_abc.shouldHover();
		
		final long hoverOp = hovering ? ABCUtil.OPTION_HOVERING : 0;
		final long menuOpenOp = menuOpen ? ABCUtil.OPTION_MENU_OPEN : 0;

		final int reactionTime = (int)(m_abLevel * m_abc.generateReactionTime(m_abc.generateBitFlags((int)averageWait, hoverOp, menuOpenOp)));
		return reactionTime;
	}
	
	public static void updateActivityStats(String activity)
	{
		if (m_waitStarts.get(activity) == null)	return;
		
		long time = System.currentTimeMillis() - m_waitStarts.get(activity);
		
		if (SUPER.ANTIBAN_VERBOSITY.ordinal() > SUPER.LOG_VERBOSITY.LOW.ordinal()) SUPER.print("ABC2: Adding wait of [" + time + "] for " + activity);
		
		if (m_waitedActivities.size() == 0)
		{
			m_waitedActivities.add(new WaitedActivity(activity, time));
			return;
		}
		
		for (WaitedActivity wa : m_waitedActivities)
		{
			if (wa.ActivityName.compareToIgnoreCase(activity) == 0)
			{
				wa.update(time);
				return;
			}
		}
		
		m_waitedActivities.add(new WaitedActivity(activity, time));
		
		if (SUPER.ANTIBAN_VERBOSITY.ordinal() > SUPER.LOG_VERBOSITY.LOW.ordinal()) SUPER.print("ABC2: Added new wait time for activity [" + activity + "] - " + time);
	}
	
	private static long getAverageWait (String activity)
	{
		for (WaitedActivity wa : m_waitedActivities)
		{
			if (wa.ActivityName.compareToIgnoreCase(activity) == 0)
			{
				return wa.AverageWait;
			}
		}
		
		return 1000;
	}

	public static long getNextReactionTime(String activity) 
	{
		return getNextWait(activity);
	}
}
