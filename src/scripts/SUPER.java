package scripts;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.types.*;
import org.tribot.api2007.Objects;
import org.tribot.api2007.WebWalking;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.*;

public final class SUPER
{
	// REFERENCE TO MAIN SCRIPT
	private static InfiniteBanana MAIN_SCRIPT;
	
	// "GLOBAL" variables
	public static boolean RUNNING;
	public static int TOTAL_BANANAS;
	public static long SCRIPT_START_TIME;
	public static long TOTAL_PROFIT;
	public static long BANANA_PRICE;
	public static ACTIVITY_STATE STATE;
	public static LOG_VERBOSITY ANTIBAN_VERBOSITY;
	public static int RUN_THRESHHOLD;
	public static double ANTIBAN_LEVEL;
	public static boolean SAVE_PROGS;
	public static String ACC_NAME;
	public static long TIME_LAST_PRICE_CHECK;
	
	// LOCATIONS
	public static final RSTile SHOP_COORDS = new RSTile(3018, 3207);
	public static final RSTile SHOP_COORDS_INSIDE = new RSTile(3013, 3206);
	public static final RSTile DEPOSIT_BOX_COORDS = new RSTile(3045, 3234);
	public static final RSTile BANANA_BOX_COORDS = new RSTile(3009, 3207);
	public static final RSTile SHOP_DOOR_OPEN = new RSTile(3017, 3206);
	public static final RSTile SHOP_DOOR_CLOSED = new RSTile(3016, 3206);
	public static final RSTile LUMB_GEN_STORE = new RSTile(3212, 3247);
	
	// STOREROOM BOUNDS
	private static final int STOREROOM_TOP = 3209, STOREROOM_BOTTOM = 3204, STOREROOM_LEFT = 3009, STOREROOM_RIGHT = 3011;
	
	// MAIN SHOP AREA TILES - USED FOR BOUND CHECK, AS THE ROOM IS NOT SUITABLE FOR AB check
	public static final RSTile SHOP_MAIN_AREA_TILES [] = new RSTile [] 
	{ 
		new RSTile(3012, 3203), new RSTile(3014, 3203), new RSTile(3012, 3204),
		new RSTile(3013, 3204), new RSTile(3014, 3204), new RSTile(3015, 3204),
		new RSTile(3016, 3204), new RSTile(3012, 3205), new RSTile(3015, 3205),
		new RSTile(3012, 3206), new RSTile(3013, 3206), new RSTile(3014, 3206),
		new RSTile(3015, 3206), new RSTile(3016, 3206), new RSTile(3013, 3207),
		new RSTile(3014, 3207), new RSTile(3015, 3207), new RSTile(3012, 3208),
		new RSTile(3013, 3208), new RSTile(3014, 3208), new RSTile(3013, 3209)
	};

	// HELPER VALUES
	public static final String SCRIPT_NAME = "Infinite Banana";
	public static final int OPERATING_RANGE = 45;
	public static final int NEAR_TRIGGER = 8;
	public static final int WORTH_TRIGGER_THRESSHOLD = 14;
	public static final int MILIS_IN_HOUR = 3600000;
	public static final String TAKE_BANANA_CONFIRMATION_MESSAGE = "Do you want to take a banana?";
	public static final int SLEEP_TIME = 40;
	public static final int CAM_ROT_MIN = 32, CAM_ROT_MAX = 149;
	public static final int CAM_ANG_MIN = 89, CAM_ANG_MAX = 100;
	public static final int NEAR_TRIGGER_DEPOSIT = 6;

	public static enum LOG_VERBOSITY
	{
		NONE,
		LOW,
		HIGH
	}
	
	public static enum DESTINATION
	{
		DEPOSIT_BOX,
		SHOP
	}
	
	public static enum ITEM_ID
	{
		BANANA(1963),
		WHITE_APRON(1005);
		
		private int m_id;
		
		ITEM_ID (int id) { this.m_id = id; }
		public int getID () { return this.m_id;}
	}
	
	public static enum OBJECT_ID
	{
		BANANA_CRATE(2071),
		SHOP_DOOR_CLOSED(1535),
		SHOP_DOOR_OPEN(1536),
		STORE_ROOM_DOOR_CLOSED(2069),
		STORE_ROOM_DOOR_OPEN(2070),
		DEPOSIT_BOX(26254);
		
		private int m_id;
		
		OBJECT_ID (int id) { this.m_id = id; }
		public int getID () { return this.m_id; }
	}
	
	public static enum INTERFACE_ID
	{
		SHOP(new ArrayList<Integer> (Arrays.asList(300))),								
		NO_APRON(new ArrayList<Integer> (Arrays.asList(231, 3))),							
		CONFIRM_TAKING_BANANA(new ArrayList<Integer> (Arrays.asList(219, 0, 0)));			
		
		private List<Integer> m_ids = new ArrayList<Integer>();
		private int m_depth;
		
		INTERFACE_ID (List<Integer> list) { this.m_ids = list; m_depth = list.size(); }
		public List<Integer> hierarchy () { return this.m_ids; }
		public int getDepth() { return this.m_depth; }
	}
	
	public static enum ACTIVITY_STATE
	{
		INITIALISING,
		COLLECTING_BANANAS,
		TRAVELLING_TO_DEPOSIT_BOX,
		INTERACTING_WITH_DEPOSIT_BOX,
		TRAVELLING_TO_SHOP,
	}
	
	// ERROR MESSAGES
	public static final String WYDIN_STANDARD = "Is it nice and tidy round the back now?";
	public static final String WYDIN_NO_APRON = "Can you put your white apron on before going in<br>there, please?";
	public static final String WYDIN_NO_ENTER = "Hey, you can't go in there. Only employees of the<br>grocery store can go in.";
	public static final String WYDIN_WELCOME=  "Welcome to my food store! Would you like to buy<br>anything?";
	
	// GUI LOCK
	public static boolean GUI_FINISHED;
	
	// RUN BEFORE EXECUTION
	public static void init(InfiniteBanana main)
	{
		MAIN_SCRIPT = main;
		
		runGUI();
		
		STATE = ACTIVITY_STATE.INITIALISING;
		RUNNING = true;
		TOTAL_BANANAS = 0;
		SCRIPT_START_TIME = System.currentTimeMillis();
		TOTAL_PROFIT = 0;
		Camera.setRotationMethod(Camera.ROTATION_METHOD.ONLY_MOUSE);
		WebWalking.setUseRun(false);
		priceCheckBanana();
	}

	private static void runGUI()
	{
		GUI_FINISHED = false;
		GUI gui = new GUI();
		
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenX = screensize.width / 2;
		int screenY = screensize.height / 2;
		
		Dimension dim = gui.getSize();
		
		gui.setVisible(true);
		
		gui.setLocation((int)(screenX - (dim.getWidth() / 2)), (int)(screenY - (dim.getHeight() / 2)));
		
		while (!GUI_FINISHED)
		{
			gui.autoCloseEventCheck();
			
			General.sleep(40);
		}
		
		gui.setVisible(false);
	}

	public static void priceCheckBanana()
	{
		try
		{
			long p = Long.parseLong(new PriceLookup().getPriceById(ITEM_ID.BANANA.getID()));
			
			BANANA_PRICE = p;
			
			print("Updated banana price using RS GE database. Current price is [ " + p + "gp ]");
			TIME_LAST_PRICE_CHECK = System.currentTimeMillis();
		}
		catch (Exception e)
		{
			print("Could not update banana price using RS GE database... Price may be out-dated");
		}
	}
	
	// HELPER FUNCTIONS
	public static void print(String message) { MAIN_SCRIPT.print(message); }
	public static void print(Long message) { MAIN_SCRIPT.print(message); }
	public static void print(boolean message) { MAIN_SCRIPT.print(message); }
	public static void print(double message) { MAIN_SCRIPT.print(message); }
	
	public static boolean isInStoreroom(RSTile pos)
	{
		if (pos.getX() >= STOREROOM_LEFT)
		{
			if (pos.getX() <= STOREROOM_RIGHT)
			{
				if (pos.getY() <= STOREROOM_TOP)
				{
					if (pos.getY() >= STOREROOM_BOTTOM)
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean isInShop (RSTile pos)
	{
		for (RSTile t : SHOP_MAIN_AREA_TILES)
		{
			if (isEqualPosition(t, pos))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isEqualPosition(RSTile p1, RSTile p2)
	{
		return (p1.getX() == p2.getX() && p1.getY() == p2.getY());
	}
	
	public static RSInterface isInterfaceOpen (List<Integer> hierarchy)
	{
		RSInterface confirmTakeBanana = Interfaces.get(hierarchy.get(0));
		
		if (confirmTakeBanana == null) return null;
		
		for (int i = 1; i < hierarchy.size(); i++)
		{
			confirmTakeBanana = confirmTakeBanana.getChild(hierarchy.get(i));
			
			if (confirmTakeBanana == null) return null;
		}
		
		return confirmTakeBanana;
	}
	
	public static boolean handleClosedShopDoor (RSTile playerPos)
	{
		if (isInShop(playerPos) && STATE == ACTIVITY_STATE.COLLECTING_BANANAS) return true;
		if (isInShop(playerPos) && STATE == ACTIVITY_STATE.TRAVELLING_TO_SHOP) return true;
		if (!isInShop(playerPos) && STATE == ACTIVITY_STATE.TRAVELLING_TO_DEPOSIT_BOX) return true;

		print("Shop door is closed. Attempting to deal with it");
		
		RSObject door [] = Objects.findNearest(15, OBJECT_ID.SHOP_DOOR_CLOSED.getID());
		
		if (door.length > 0 && isEqualPosition(door[0].getPosition(), SHOP_DOOR_CLOSED))
		{
			if (!door[0].isOnScreen())
			{
				Camera.turnToTile(door[0].getPosition());
			}
			
			if (DynamicClicking.clickRSObject(door[0], "Open door"))
			{
				if (Timing.waitCondition(new Condition ()
				{
					@Override
					public boolean active()
					{
						RSObject doornew [] = Objects.findNearest(15, OBJECT_ID.SHOP_DOOR_OPEN.getID());
						if (doornew.length > 0 && isEqualPosition(doornew[0].getPosition(), SHOP_DOOR_OPEN))
						{
							return true;
						}
						
						General.sleep(SUPER.SLEEP_TIME / 2);
						return false;
					}
				}, General.random(1800, 2400)))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;	
			}
		}
		
		return true;
	}
	
	public static boolean isShopDoorClosed ()
	{
		RSObject door [] = Objects.findNearest(15, OBJECT_ID.SHOP_DOOR_CLOSED.getID());
		if (door.length > 0 && isEqualPosition(door[0].getPosition(), SHOP_DOOR_CLOSED))
		{
			return true;
		}
		
		return false;
	}
	
	public static boolean isStoreroomDoorOpen ()
	{
		RSObject storeroomDoorOpen [] = Objects.findNearest(10, SUPER.OBJECT_ID.STORE_ROOM_DOOR_OPEN.getID());
		
		if (storeroomDoorOpen.length > 0) return true;
		
		return false;
	}
}
