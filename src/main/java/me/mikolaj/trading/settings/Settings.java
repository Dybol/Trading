package me.mikolaj.trading.settings;

import org.bukkit.Location;
import org.mineacademy.fo.settings.SimpleSettings;

public class Settings extends SimpleSettings {
	@Override
	protected int getConfigVersion() {
		return 1;
	}

	public static class Bazaar {
		public static Location PRIMARY;
		public static Location SECONDARY;
		public static Boolean ENABLED;
		public static Integer MAX_ITEMS_IN_BAZAAR;

		private static void init() {
			pathPrefix("Bazaars");

			ENABLED = getBoolean("Enabled");
			PRIMARY = get("Primary", Location.class);
			SECONDARY = get("Secondary", Location.class);
			MAX_ITEMS_IN_BAZAAR = getInteger("Max_Items_In_Bazaar");
			//getOrSetDefault() <-- moze się przydać
		}
	}

	public static class Trade {
		public static Boolean ENABLED;
		public static Boolean TRADING_EVERYWHERE;
		public static Location PRIMARY;
		public static Location SECONDARY;

		private static void init() {
			pathPrefix("Trade_Command");
			ENABLED = getBoolean("Enabled");
			TRADING_EVERYWHERE = getBoolean("Trading_Everywhere");
			PRIMARY = get("Primary", Location.class);
			SECONDARY = get("Secondary", Location.class);
		}
	}

	@Override //zmienic potem zeby wyswietlal sie ladny naglowek.
	protected String[] getHeader() {
		return new String[] {
				"siema",
				"kurwa"
		};
	}
}
