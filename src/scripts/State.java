package scripts;

import org.tribot.api2007.Banking;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Player;

public class State 
{
	private SUPER.ACTIVITY_STATE m_previousState;
	
	public SUPER.ACTIVITY_STATE getState()
	{
		// If we are more than halfway to the deposit box
		if (Player.getPosition().distanceTo(SUPER.DEPOSIT_BOX_COORDS) < (SUPER.OPERATING_RANGE / 2))
		{
			// If we have more than the minimum number of banana required to deposit
			if (Inventory.getCount(SUPER.ITEM_ID.BANANA.getID()) > SUPER.WORTH_TRIGGER_THRESSHOLD)
			{
				// If we are near enough to the deposit box
				if (Banking.isDepositBoxOpen() || Player.getPosition().distanceTo(SUPER.DEPOSIT_BOX_COORDS) < (SUPER.NEAR_TRIGGER_DEPOSIT))
				{
					return returnAndUpdateState(SUPER.ACTIVITY_STATE.INTERACTING_WITH_DEPOSIT_BOX);
				}
				
				// Otherwise we should move to the deposit box
				return returnAndUpdateState(SUPER.ACTIVITY_STATE.TRAVELLING_TO_DEPOSIT_BOX);
			}
			else
			{
				// Otherwise we should go to the shop
				return returnAndUpdateState(SUPER.ACTIVITY_STATE.TRAVELLING_TO_SHOP);
			}
		}
		else
		{
			// If our inventory is full
			if (Inventory.isFull())
			{
				return returnAndUpdateState(SUPER.ACTIVITY_STATE.TRAVELLING_TO_DEPOSIT_BOX);
			}
			
			// If we are near enough to the shop to start collecting
			if (SUPER.isInStoreroom(Player.getPosition()) || SUPER.isInShop(Player.getPosition()))
			{
				// Otherwise, keep collecting
				return returnAndUpdateState(SUPER.ACTIVITY_STATE.COLLECTING_BANANAS);
			}
			else
			{
				// Otherwise we should go to the shop
				return returnAndUpdateState(SUPER.ACTIVITY_STATE.TRAVELLING_TO_SHOP);
			}
		}
	}
	
	private SUPER.ACTIVITY_STATE returnAndUpdateState (SUPER.ACTIVITY_STATE state)
	{
		if (m_previousState != state)
		{
			ANTIBAN.resetStarts();
		}
		
		SUPER.STATE = state;
		m_previousState = state;
		return state;
	}
}
