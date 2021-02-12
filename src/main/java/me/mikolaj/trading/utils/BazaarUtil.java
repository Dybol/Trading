package me.mikolaj.trading.utils;

import lombok.experimental.UtilityClass;
import me.mikolaj.trading.settings.Settings;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mineacademy.fo.region.Region;

import java.util.ArrayList;
import java.util.List;

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

	public String getItemAndAmountFormated(final ItemStack item, final int sellAmount, final int buyAmount) {
		return "&a" + item.getType() + " x " + item.getAmount() + " Sprzedaz: &6" + sellAmount + " zlota.&a Kupno: &6" + buyAmount + " zlota.";
	}

	public ItemStack clearingMeta(final ItemStack itemStack) {
		final ItemMeta meta = itemStack.getItemMeta();
		final List<String> lore = new ArrayList<>();
		meta.setLore(lore);
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	//TODO do pomyslenia, bo bd trzeb sprawdzac przed kazdym tradem, czy grac ma odpowiednio duzo zlota
	// rozmieniajac bloki na sztabki, a potem zabierac tylko czesc zlota i sztabek, tak zeby nie rozmieniac wszystkiego
	public int changeGoldToGoldBlocks(final int amount) {
		//to bd dzielenie calkowite
		return amount / 9;
	}

}
