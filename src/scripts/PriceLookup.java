/**
 * @author Jacob Smallridge - Polycoding/Polymorphism
 * Polycoding 2017 http://polycoding.com/
 * 
 * Please leave comments & documentation in place. Modify and distribute as you please.
 */

package scripts;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;

public class PriceLookup {

	private static final String PRICES_BASE = "http://services.runescape.com/m=itemdb_oldschool/api/catalogue/detail.json?item=";

	private static final String ITEMS_BASE = "http://polycoding.com/osb/itemIdNames.php?getId=";

	private static final String[][] VALUE_MATRIX = { { "K", "1000" }, { "M", "1000000" },
			{ "B", "1000000000" }, { "T", "1000000000000" } };

	public PriceLookup() {

	}

	/**
	 * Retrieves an item's price from Jagex using it's name.
	 * 
	 * @param name
	 * @return String representation of price OR empty String on failure
	 */
	public String getPriceByName(String name) {
		try {
			final String id = getId(name.replace(" ", "%20"));
			if (id == null || id.trim().isEmpty())
				return "";
			return parseRS(Integer.valueOf(id));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Retrieves an item's price from Jagex using it's in-game ID
	 * 
	 * @param id
	 * @return String representation of price OR empty String on failure
	 */
	public String getPriceById(int id) {
		try {
			final String price = parseRS(id);
			if (price == null || price.trim().isEmpty())
				return "";
			return price;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Parses the JSON from RuneScape website
	 * 
	 * @param itemID
	 * @return itemPrice
	 * @throws IOException
	 */
	private String parseRS(final int itemID) throws IOException {
		final URL url = new URL(PRICES_BASE + itemID);
		BufferedReader file = new BufferedReader(new InputStreamReader(url.openStream()));
		String line;
		String price = null;
		while ((line = file.readLine()) != null) {
			if (line.contains("price")) {
				price = (line).trim();
			}
		}
		price = price.substring(price.indexOf("current"), price.indexOf("today")).replace("\"", "");
		price = price.substring(price.indexOf("price:") + 6, price.lastIndexOf("}"));
		price = addZeros(price.toUpperCase());
		file.close();
		return price.replace(",", "");
	}

	/**
	 * Queries Polycoding server to exchange an item's name for it's ID
	 * 
	 * @param itemName
	 *            Items name
	 * @return Empty string on failure or the raw ID as a String
	 * @throws IOException
	 */
	public String getId(String itemName) throws IOException {
		final URL url = new URL(ITEMS_BASE + itemName);
		BufferedReader file = new BufferedReader(new InputStreamReader(url.openStream()));
		String line;
		String content = null;
		while ((line = file.readLine()) != null) {
			content = line.trim();
		}
		file.close();
		return content;
	}

	/**
	 * Should convert jagex's 15k to 15000 and 15m to 15000000, etc etc
	 * 
	 * @param str
	 * @return Unformatted String representation of any number input
	 */
	private String addZeros(String str) {
		for (int i = 0; i < VALUE_MATRIX.length; i++) {
			if (str.endsWith(VALUE_MATRIX[i][0])) {
				BigDecimal temp = new BigDecimal(str.substring(0, str.indexOf(VALUE_MATRIX[i][0])));
				temp = temp.multiply(new BigDecimal(VALUE_MATRIX[i][1]));
				str = temp.toBigInteger().toString();
				break;
			}
		}
		return str;

	}
}