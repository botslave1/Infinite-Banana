package scripts;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Objects;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.types.RSObject;

public class Banker
{
	private int m_attempts = 0;
	
	public void run()
	{
		if (m_attempts == 5)
		{
			SUPER.print("Failed to deposit items 5 times in a row... Something is wrong. Shutting down");
			SUPER.RUNNING = false;
			return;
		}
		
		if (!Banking.isDepositBoxOpen())
		{
			RSObject depositBox [] = Objects.findNearest(20, SUPER.OBJECT_ID.DEPOSIT_BOX.getID());
			
			if (depositBox.length > 0)
			{
				if (DynamicClicking.clickRSObject(depositBox[0], "Deposit Bank deposit box"))
				{
					ANTIBAN.startTimer("OpenDepositBox");
					ANTIBAN.updateTracker("OpenDepositBox");
					ANTIBAN.run();
					
					if (Timing.waitCondition(new Condition ()
					{
						@Override
						public boolean active()
						{
							if (Banking.isDepositBoxOpen())
							{
								return true;
							}
							
							General.sleep(SUPER.SLEEP_TIME);
							return false;
						}
					}, General.random(4000, 6000)))
					{
						m_attempts = 0;
						ANTIBAN.updateActivityStats("OpenDepositBox");
						ANTIBAN.waitForReactionTime("OpenDepositBox");
					}
				}
				else
				{
					m_attempts++;
					if (m_attempts > 2)
					{
						pointCameraAtDepositBox(depositBox);
					}
					else if (m_attempts > 3)
					{
						WebWalking.walkTo(depositBox[0]);
					}
					
					General.sleep(50);
				}
			}
		}
		else
		{
			Banking.depositAll();
			
			ANTIBAN.startTimer("Deposit");
			ANTIBAN.updateTracker("Deposit");
			ANTIBAN.run();
				
			if (Timing.waitCondition(new Condition ()
			{
				@Override
				public boolean active()
				{
					if (Inventory.getCount(SUPER.ITEM_ID.BANANA.getID()) == 0)
					{
						return true;
					}
					
					General.sleep(SUPER.SLEEP_TIME);
					return false;
				}
			}, General.random(3000, 4000)))
			{
				m_attempts = 0;
				ANTIBAN.updateActivityStats("Deposit");
				ANTIBAN.waitForReactionTime("Deposit");
			}
			else
			{
				m_attempts++;
			}
		}
	}

	private void pointCameraAtDepositBox(RSObject[] depositBox)
	{
		Camera.turnToTile(depositBox[0].getPosition());
	}
}
