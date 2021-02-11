package me.mikolaj.trading.utils;

import lombok.experimental.UtilityClass;
import me.mikolaj.trading.settings.Settings;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.region.Region;

@UtilityClass
public class BazaarUtil {

	final static private Region bazaarReg = new Region(Settings.Bazaar.PRIMARY, Settings.Bazaar.SECONDARY);

	public boolean isInBazaarRegion(final Player player) {
		return bazaarReg.isWithin(player.getLocation());
	}

	public boolean isInBazaarRegion(final Location location) {
		return bazaarReg.isWithin(location);
	}
	public String getItemAndAmountFormated(final ItemStack item, final int amount) {
		return "&a" + item.getType() + " x " + item.getAmount() + " za &6" + amount + " zlota.";
	}

}
