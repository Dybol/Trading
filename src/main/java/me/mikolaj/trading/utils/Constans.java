package me.mikolaj.trading.utils;

import lombok.experimental.UtilityClass;
import me.mikolaj.trading.settings.Settings;

/**
 * Klasa ze stalymi
 */
@UtilityClass
public class Constans {

	/**
	 * Maksymalna ilosc itemow w bazarze
	 */
	public final Integer MAX_ITEMS_IN_BAZAAR = Settings.Bazaar.MAX_ITEMS_IN_BAZAAR;

}
