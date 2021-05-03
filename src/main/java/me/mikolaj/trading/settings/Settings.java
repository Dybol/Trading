package me.mikolaj.trading.settings;

import org.bukkit.Location;
import org.mineacademy.fo.settings.SimpleSettings;

/**
 * Ustawienia
 */
public class Settings extends SimpleSettings {
	/**
	 * @return - wersja aktualnych ustawien
	 */
	@Override
	protected int getConfigVersion() {
		return 1;
	}

	/**
	 * Ustawienia zwiazane z bazarem
	 */
	public static class Bazaar {

		/**
		 * Pierwsza lokalizacja do stworzenia miejsca na bazary
		 * Mozna traktowac jako wspolrzedne wierzcholka prostokata
		 */
		public static Location PRIMARY;

		/**
		 * Druga lokalizacja do stworzenia miejsca na bazary
		 * Mozna traktowac jako przeciwlegla wspolrzedna wierzcholka prostokata
		 */
		public static Location SECONDARY;

		/**
		 * Czy bazary sa aktywne
		 */
		public static Boolean ENABLED;

		/**
		 * Maksymalna ilosc itemow na bazarze
		 */
		public static Integer MAX_ITEMS_IN_BAZAAR;

		/**
		 * Czy mozna tworzyc wszedzie bazary
		 */
		public static Boolean BAZAARS_EVERYWHERE;

		/**
		 * Metoda ustawiajaca zmienne z pliku settings.yml
		 */
		private static void init() {
			pathPrefix("Bazaars");

			ENABLED = getBoolean("Enabled");
			PRIMARY = get("Primary", Location.class);
			SECONDARY = get("Secondary", Location.class);
			MAX_ITEMS_IN_BAZAAR = getInteger("Max_Items_In_Bazaar");
			BAZAARS_EVERYWHERE = getBoolean("Bazaars_Everywhere");
		}
	}

	/**
	 * Metoda odpowiadajaca za wymiany
	 */
	public static class Trade {
		/**
		 * Czy wymiana miedy graczami jest wlaczona
		 */
		public static Boolean ENABLED;

		/**
		 * Czy mozna sie wymieniac wszedzie
		 */
		public static Boolean TRADING_EVERYWHERE;

		/**
		 * Pierwsza lokalizacja do stworzenia miejsca na wymiane
		 * Mozna traktowac jako wspolrzedne wierzcholka prostokata
		 */
		public static Location PRIMARY;

		/**
		 * Druga lokalizacja do stworzenia miejsca na wymiane
		 * Mozna traktowac jako przeciwlegla wspolrzedna wierzcholka prostokata
		 */
		public static Location SECONDARY;

		/**
		 * Metoda ustawiajaca dane zmienne z pliku settings.yml
		 */
		private static void init() {
			pathPrefix("Trade_Command");
			ENABLED = getBoolean("Enabled");
			TRADING_EVERYWHERE = getBoolean("Trading_Everywhere");
			PRIMARY = get("Primary", Location.class);
			SECONDARY = get("Secondary", Location.class);
		}
	}
}
