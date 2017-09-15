package scripts;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Game;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Options;
import org.tribot.api2007.Player;
import org.tribot.api2007.Walking;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;

public class Walker
{
	private int m_runThreshhold = -1;
	private boolean m_webWalkStuck;
	
	private enum WALKER_STATE
	{
		IN_STOREROOM,
		WALKING
	}
	
	private WALKER_STATE m_state;
	
	public void run(SUPER.DESTINATION destination)
	{
		updateState();
		
		switch (destination)
		{
			case DEPOSIT_BOX :
				walkToDepositBox();
				break;
			case SHOP :
				walkToShop();
				break;
		}
	}
	
	private void walkToShop ()
	{
		switch (m_state)
		{
			case IN_STOREROOM :
				// This should never happen
				break;
			case WALKING :
				webWalk(SUPER.SHOP_COORDS);
				break;
		}
	}
	
	private void walkToDepositBox ()
	{
		switch (m_state)
		{
			case IN_STOREROOM :
				leaveStoreRoom();
				break;
			case WALKING :
				webWalk(SUPER.DEPOSIT_BOX_COORDS);
				break;
		}
	}
	
	private void updateState ()
	{
		if (SUPER.isInStoreroom(Player.getPosition()))
		{
			m_state = WALKER_STATE.IN_STOREROOM;
		}
		else
		{
			m_state = WALKER_STATE.WALKING;	
		}
	}
	
	private void webWalk (RSTile target)
	{
		long walkingStart = System.currentTimeMillis();
		long walkingTimeout = General.randomLong(80000, 100000);
		m_webWalkStuck = false;
		
		if (!WebWalking.walkTo(target, new Condition () 
		{
			long ticker = 1;
			RSTile previousPos = Player.getPosition();
			
			@Override
			public boolean active() 
			{
				if (shouldRun()) Options.setRunOn(true);
				
				checkTimeOut(walkingStart, walkingTimeout);
				
				if (ticker % 30 == 0)
				{
					if (SUPER.isEqualPosition(previousPos, Player.getPosition()))
					{
						m_webWalkStuck = true;
						return true;
					}
					
					previousPos = Player.getPosition();
				}
				
				if (Player.getPosition().distanceTo(target) < (int)((double)SUPER.NEAR_TRIGGER * 1.5) || (System.currentTimeMillis() - walkingStart > walkingTimeout))
				{
					return true;
				}
				
				ticker++;
				General.sleep(SUPER.SLEEP_TIME);
				return false;
			}
			
		}, General.randomLong(60000, 80000)))
		{
			if (m_webWalkStuck)
			{
				return;
			}
			
			checkTimeOut(walkingStart, walkingTimeout);
			
			if (Player.getPosition().distanceTo(SUPER.SHOP_DOOR_CLOSED) < 4 && SUPER.isShopDoorClosed())
			{
				SUPER.handleClosedShopDoor(Player.getPosition());
			}
			else
			{
				if (SUPER.STATE == SUPER.ACTIVITY_STATE.TRAVELLING_TO_SHOP && (Player.getPosition().distanceTo(SUPER.SHOP_COORDS_INSIDE) > SUPER.NEAR_TRIGGER || !SUPER.isInShop(Player.getPosition())))
				{
					RSTile innerTarget = SUPER.SHOP_MAIN_AREA_TILES[General.random(7, SUPER.SHOP_MAIN_AREA_TILES.length - 4)];
					
					Walking.clickTileMM(innerTarget, 1);
					
					Timing.waitCondition(new Condition ()
					{
						@Override
						public boolean active()
						{
							if (Player.getPosition().distanceTo(SUPER.SHOP_COORDS_INSIDE) < 5)
							{
								return true;
							}
							
							General.sleep(SUPER.SLEEP_TIME);
							return false;
						}
					}, General.random(3000, 4000));
				}
			}
		}
	}

	private void checkTimeOut(long walkingStart, long walkingTimeout) 
	{
		if ((System.currentTimeMillis() - walkingStart > walkingTimeout))
		{
			SUPER.print("We seem to have gotten lost... Shutting down!");
			SUPER.RUNNING = false;
		}
	}
	
	private void leaveStoreRoom ()
	{
		RSObject storeroomDoor [] = Objects.findNearest(15, SUPER.OBJECT_ID.STORE_ROOM_DOOR_CLOSED.getID());
		
		if (storeroomDoor.length > 0)
		{
			if (DynamicClicking.clickRSObject(storeroomDoor[0], "Open Door"))
			{
				ANTIBAN.startTimer("ExitStoreRoom");
				ANTIBAN.updateTracker("ExitStoreRoom");
				ANTIBAN.run();
				
				if (Timing.waitCondition(new Condition ()
				{
					@Override
					public boolean active()
					{
						if (SUPER.isStoreroomDoorOpen())
						{
							return true;
						}
						
						General.sleep(SUPER.SLEEP_TIME);
						return false;
					}
				}, General.random(5000, 6000)))
				{
					
					ANTIBAN.updateActivityStats("ExitStoreRoom");
					ANTIBAN.waitForReactionTime("ExitStoreRoom");
				}
			}
		}
	}
	
	private boolean shouldRun()
	{
		if (m_runThreshhold == -1) 
		{
			generateNewRunThreshhold();
		}
		
		if (m_runThreshhold < Game.getRunEnergy())
		{
			m_runThreshhold = SUPER.RUN_THRESHHOLD + General.random(-5, 5);
			return true;
		}
		
		return false;
	}
	
	private void generateNewRunThreshhold()
	{
		m_runThreshhold = SUPER.RUN_THRESHHOLD + General.random(-5, 5);
	}
}
