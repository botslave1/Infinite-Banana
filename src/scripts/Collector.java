package scripts;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Keyboard;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Camera;
import org.tribot.api2007.GroundItems;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSGroundItem;
import org.tribot.api2007.types.RSInterface;
import org.tribot.api2007.types.RSObject;

public class Collector 
{
	private int m_doorOpenAttempts = 0;
	private long m_freeBananaSpawnedAt = 0;
	private int m_clickStoreroomDoorAttempts = 0;
	
	private enum COLLECTOR_STATE
	{
		ENTERING_STOREROOM,
		COLLECTING_FROM_CRATE,
		PICKING_UP_FREE_BANANA,
	}
	
	private COLLECTOR_STATE m_state;
	
	public void run() 
	{
		if (m_doorOpenAttempts > 4)
		{
			SUPER.print("Tried to enter the storeroom 5 times but somehow kept failing. Maybe someone is griefing us?");
			SUPER.print("Shutting down script for safety and logging out");
			SUPER.RUNNING = false;
		}
		
		updateState();
		
		switch (m_state)
		{
			case COLLECTING_FROM_CRATE :
				adjustCamera();
				collectFromCrate();
				break;
			case ENTERING_STOREROOM :
				enterStoreRoom();
				break;
			case PICKING_UP_FREE_BANANA :
				adjustCamera();
				pickUpFreeBanana();
				break;
		}
	}

	private void adjustCamera() 
	{
		if (Camera.getCameraAngle() < SUPER.CAM_ANG_MIN || Camera.getCameraAngle() > SUPER.CAM_ANG_MAX)
		{
			Camera.setCameraAngle(General.random(SUPER.CAM_ANG_MIN, SUPER.CAM_ANG_MAX));
		}
	}
	
	private void updateState()
	{
		if (SUPER.isInStoreroom(Player.getPosition()))
		{
			if (confirmFreeBanana(null))
			{
				ANTIBAN.updateActivityStats("FreeBananaClick");
				
				if (m_freeBananaSpawnedAt == 0) 
				{
					m_freeBananaSpawnedAt = System.currentTimeMillis();
				}

				m_state = COLLECTOR_STATE.PICKING_UP_FREE_BANANA;
			}
			else
			{
				m_state = COLLECTOR_STATE.COLLECTING_FROM_CRATE;
			}
		}
		else
		{
			m_state = COLLECTOR_STATE.ENTERING_STOREROOM;
		}
	}
	
	private void collectFromCrate()
	{
		int bananasInInventory = Inventory.getCount(SUPER.ITEM_ID.BANANA.getID());
		
		RSObject crate [] = Objects.findNearest(10, SUPER.OBJECT_ID.BANANA_CRATE.getID());
		
		boolean interfaceWasOpened = true;
		
		if (SUPER.isInterfaceOpen(SUPER.INTERFACE_ID.CONFIRM_TAKING_BANANA.hierarchy()) == null)
		{
			if (crate.length > 0)
			{
				if (crate[0].click("Search Crate"))
				{
					ANTIBAN.startTimer("CrateClick");
					ANTIBAN.updateTracker("CrateClick");
					ANTIBAN.run();
				}
				else
				{
					interfaceWasOpened = false;
				}
			}
			else
			{
				SUPER.print("We are in the storeroom, but cannot find the crate! Something is wrong. Shutting down.");
				SUPER.RUNNING = false;
				return;
			}
		}
		else
		{
			interfaceWasOpened = false;
		}
		
		if(Timing.waitCondition(new Condition ()
		{
			@Override
			public boolean active()
			{
				RSInterface confirmTakeBanana = SUPER.isInterfaceOpen(SUPER.INTERFACE_ID.CONFIRM_TAKING_BANANA.hierarchy());
				
				if (confirmTakeBanana != null)
				{
					return true;
				}
				
				General.sleep(SUPER.SLEEP_TIME);
				return false;
			}
		}, General.random(6000, 7000)))
		{
			if (interfaceWasOpened)
			{
				ANTIBAN.updateActivityStats("CrateClick");
				ANTIBAN.waitForReactionTime("CrateClick");
			}

			Keyboard.sendType('1');
			
			ANTIBAN.startTimer("ConfirmTakeBanana");
			ANTIBAN.updateTracker("ConfirmTakeBanana");
			ANTIBAN.run();
			
			if (Timing.waitCondition(new Condition ()
			{
				@Override
				public boolean active()
				{
					if (Inventory.getCount(SUPER.ITEM_ID.BANANA.getID()) > bananasInInventory)
					{
						SUPER.TOTAL_BANANAS++;
						return true;
					}
					
					General.sleep(SUPER.SLEEP_TIME);
					return false;
				}
			}, General.random(1200, 1800)))
			{
				ANTIBAN.updateActivityStats("ConfirmTakeBanana");
				ANTIBAN.waitForReactionTime("ConfirmTakeBanana");
			}
		}
	}
	
	private void enterStoreRoom()
	{
		if (Player.getPosition().getPlane() != 0)
		{
			RSObject ladders [] = Objects.findNearest(20, "Ladder");
			
			if (ladders.length > 0)
			{
				DynamicClicking.clickRSObject(ladders[0], "Climb-down Ladder");
				
				Timing.waitCondition(new Condition ()
				{
					@Override
					public boolean active()
					{
						General.sleep(SUPER.SLEEP_TIME);
						return Player.getPosition().getPlane() == 0;
					}
				}, General.random(3000, 5000));
			}
			
			return;
		}
		
		if (SUPER.isShopDoorClosed()) SUPER.handleClosedShopDoor(Player.getPosition());
		
		RSObject storeroomDoor [] = Objects.findNearest(20, SUPER.OBJECT_ID.STORE_ROOM_DOOR_CLOSED.getID());
		
		if (storeroomDoor.length > 0)
		{
			if (DynamicClicking.clickRSObject(storeroomDoor[0], ("Open Door")))
			{
				m_clickStoreroomDoorAttempts = 0;
				ANTIBAN.startTimer("EnterStoreRoom");
				ANTIBAN.updateTracker("EnterStoreRoom");
				ANTIBAN.run();
				
				if (!Timing.waitCondition(new Condition ()
				{
					@Override
					public boolean active()
					{
						if (SUPER.isInStoreroom(Player.getPosition()) || !canEnterStoreroom() || SUPER.isStoreroomDoorOpen())
						{
							return true;
						}
						
						General.sleep(SUPER.SLEEP_TIME);
						return false;
					}
				}, General.random(4124, 5887)))
				{
					m_doorOpenAttempts++;
				}
				else
				{
					canEnterStoreroom();
					
					if (SUPER.isStoreroomDoorOpen())
					{
						Timing.waitCondition(new Condition ()
						{
							@Override
							public boolean active()
							{
								if (!SUPER.isStoreroomDoorOpen())
								{
									return true;
								}
								
								General.sleep(SUPER.SLEEP_TIME);
								return false;
							}
						}, General.random(2000, 3000));
						return;
					}
					
					ANTIBAN.updateActivityStats("EnterStoreRoom");
					ANTIBAN.waitForReactionTime("EnterStoreRoom");
					m_doorOpenAttempts = 0;	
				}
			}
			else
			{
				if (m_clickStoreroomDoorAttempts > 20)
				{
					Camera.turnToTile(storeroomDoor[0]);
				}
				m_clickStoreroomDoorAttempts++;	
				
				General.sleep(50, 200);
			}
		}
	}
	
	private void pickUpFreeBanana ()
	{		
		General.sleep(150, 250);
		
		int bananasInInventory = Inventory.getCount(SUPER.ITEM_ID.BANANA.getID());
		
		RSGroundItem localBananas [] = getBananas();
		
		if (confirmFreeBanana(localBananas))
		{
			if (localBananas[0].click("Take Banana"))
			{				
				if(Timing.waitCondition(new Condition ()
				{
					@Override
					public boolean active()
					{
						if (!confirmFreeBanana(null))
						{
							ANTIBAN.startTimer("CrateClick");
							ANTIBAN.updateTracker("CrateClick");
							return true;
						}
						
						General.sleep(SUPER.SLEEP_TIME);
						return false;
					}
				}, General.random(1800, 2400)))
				{
				}
			}
		}
			
		if (bananasInInventory < Inventory.getCount(SUPER.ITEM_ID.BANANA.getID())) SUPER.TOTAL_BANANAS++;
	}
	
	private boolean confirmFreeBanana (RSGroundItem [] bananas)
	{
		if (bananas == null) { bananas = getBananas(); }
			
		if (bananas.length > 0)
		{
			for (RSGroundItem gi : bananas)
			{
				if (SUPER.isInStoreroom(gi.getPosition()))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	private RSGroundItem [] getBananas ()
	{
		return GroundItems.find(SUPER.ITEM_ID.BANANA.getID());
	}
	
	// Checks if the no apron/no quest warning is present. Exits if found.
	private boolean canEnterStoreroom ()
	{
		RSInterface apronWarning = SUPER.isInterfaceOpen(SUPER.INTERFACE_ID.NO_APRON.hierarchy());
		
		if (apronWarning == null)
		{
			return true;
		}
		if (!apronWarning.getText().equals(SUPER.WYDIN_NO_APRON) && !apronWarning.getText().equals(SUPER.WYDIN_NO_ENTER))
		{
			return true;
		}
		
		if (SUPER.RUNNING)
		{
			SUPER.RUNNING = false;
			SUPER.print("Can't enter the store room! You either arent wearing a white apron, or haven't progressed far enough in 'Pirate's Treasure'");
			SUPER.print("You can start the quest by talking to Redbeard Frank at the north of Port Sarim. You can find a white apron in the fishing shop, hanging on the wall");
		}

		return false;
	}
}
