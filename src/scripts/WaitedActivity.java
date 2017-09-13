package scripts;

public class WaitedActivity 
{
	public String ActivityName;
	public long AverageWait;
	
	private long m_totalWaitedTime;
	private long m_occurences;
	
	public WaitedActivity (String activityName, long time)
	{
		ActivityName = activityName;
		AverageWait = time;
		m_totalWaitedTime = time;
		m_occurences = 1;
	}
	
	public void update(Long t)
	{
		// No point preventing overflow of any of these long values... According to a quick calculation it will take over 1.75 billion years!
		m_occurences++;
		
		m_totalWaitedTime += t;
		
		AverageWait = m_totalWaitedTime / m_occurences;
	}
}
