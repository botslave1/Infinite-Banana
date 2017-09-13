package scripts;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.tribot.api.Timing;
import org.tribot.api2007.Login;

public final class PAINT
{
	private final static Color WHITE = new Color(255, 255, 255);
	
	private static final RenderingHints RENDERING_HINT = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	
	private static Image m_paint = getImage("http://i.imgur.com/sD6rAm2.png");
	
	private static Image m_cursor = getImage("http://i.imgur.com/p2JacYc.png");
	
	private static boolean m_ready = false;

	public static void init()
	{
		m_ready = true;
	}
	
	private static Image getImage(String url)
	{
		try
		{
			URL u = new URL(url);
			
			Image image = ImageIO.read(u.openStream());
			
			return image;
		} 
		catch(IOException e) 
		{
			SUPER.print("Couldnt read the image!");
			SUPER.print(e.getMessage());
			return null;
		}
	}


	public static void paint(Graphics g)
	{
		if (!m_ready) return;
		
		if (Login.getLoginState() != Login.STATE.INGAME) return;
		
		Graphics2D gg = (Graphics2D)g;
		gg.setRenderingHints(RENDERING_HINT);
		gg.drawImage(m_paint, 0, 324, null);
		
		FontMetrics fontMetrics = gg.getFontMetrics();
		
		long runtimeMs = System.currentTimeMillis() - SUPER.SCRIPT_START_TIME;
		String runtime = Timing.msToString(runtimeMs) + " runtime";
		String state = "Currently " + SUPER.STATE.toString().replace("_", " ").toLowerCase();
		String totalTanned = Integer.toString(SUPER.TOTAL_BANANAS) + " bananas collected";
		String bananasPerHour;
		String totalProfit;
		String profitPerHour;
		
		if (SUPER.TOTAL_BANANAS == 0)
		{
			bananasPerHour = "0 bananas per hour (waiting...)";
			totalProfit = "0gp profit made (waiting...)";
			profitPerHour = "0gp profit per hour (waiting...)"; 
		}
		else
		{
			bananasPerHour = Long.toString((SUPER.MILIS_IN_HOUR) / (runtimeMs / SUPER.TOTAL_BANANAS)) + " bananas per hour";
			SUPER.TOTAL_PROFIT = SUPER.BANANA_PRICE * SUPER.TOTAL_BANANAS;
			totalProfit = Long.toString(SUPER.TOTAL_PROFIT) + "gp profit made (approx)";
			profitPerHour = Long.toString((long)((double)SUPER.TOTAL_PROFIT / ((double)runtimeMs / (double)SUPER.MILIS_IN_HOUR))) + "gp profit per hour (approx)";
		}
				
	    g.setColor(WHITE);

	    g.drawString(runtime, 502 - fontMetrics.stringWidth(runtime), 364);
	    g.drawString(state, 502 - fontMetrics.stringWidth(state), 384);
	    g.drawString(totalTanned, 502 - fontMetrics.stringWidth(totalTanned), 404);
	    g.drawString(bananasPerHour, 502 - fontMetrics.stringWidth(bananasPerHour), 424);
	    g.drawString(profitPerHour, 502 - fontMetrics.stringWidth(profitPerHour), 444);
	    g.drawString(totalProfit, 502 - fontMetrics.stringWidth(totalProfit), 464);
		
	}

	public static void paintMouse(Graphics g, Point p)
	{
		Graphics2D gg = (Graphics2D)g;
		gg.drawImage(m_cursor, p.x, p.y, null);
	}
}
