package me.mikolaj.trading.utils;

import lombok.experimental.UtilityClass;
import me.mikolaj.trading.settings.Settings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mineacademy.fo.region.Region;
import org.mineacademy.fo.remain.CompMaterial;

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

	//todo do pomyslenia, bo bd trzeb sprawdzac przed kazdym tradem, czy grac ma odpowiednio duzo zlota
	// rozmieniajac bloki na sztabki, a potem zabierac tylko czesc zlota i sztabek, tak zeby nie rozmieniac wszystkiego

	public int calculateGoldWithoutGoldBlocks(final Player player) {
		int counter = 0;
		for (final ItemStack item : player.getInventory().getContents()) {
			if (item != null && item.getType() == Material.GOLD_INGOT) {
				counter += item.getAmount();
			}
		}
		return counter;
	}

	public int calculateGoldWithGoldBlocks(final Player player) {
		int counter = 0;
		for (final ItemStack item : player.getInventory().getContents()) {
			if (item != null) {
				if (CompMaterial.fromItem(item) == CompMaterial.GOLD_INGOT)
					counter += item.getAmount();
				else if (CompMaterial.fromItem(item) == CompMaterial.GOLD_BLOCK)
					counter += (9 * item.getAmount());
			}
		}
		return counter;
	}

	/**
	 * @param player - player
	 * @return - na zerowym elemencie ilosc blokow, a na pierwszym ilosc zlota
	 */
	public Integer[] calculateGoldToGoldBlocks(final Player player, final int gold_change) {
		final Integer[] gold = new Integer[2];
		final int total_gold = calculateGoldWithGoldBlocks(player) + gold_change;

		final int gold_blocks = total_gold / 9;
		final int gold_ingots = total_gold % 9;
		gold[0] = gold_blocks;
		gold[1] = gold_ingots;

		return gold;
	}

	public void changeGoldToGoldBlocks(final Player player, final int gold_change) {
		final Inventory player_inventory = player.getInventory();
		//to bd dzielenie calkowite
		final int gold_blocks = calculateGoldToGoldBlocks(player, gold_change)[0];
		final int gold_ingots = calculateGoldToGoldBlocks(player, gold_change)[1];
		for (final ItemStack item : player_inventory.getContents()) {
			if (item != null && (CompMaterial.fromItem(item) == CompMaterial.GOLD_INGOT || CompMaterial.fromItem(item) == CompMaterial.GOLD_BLOCK))
				player.getInventory().remove(item);
		}

		for (int i = 64; i < gold_blocks; i++) {
			player_inventory.addItem(new ItemStack(Material.GOLD_BLOCK, 64));
		}
		player_inventory.addItem(new ItemStack(Material.GOLD_BLOCK, gold_blocks % 64));

		player_inventory.addItem(new ItemStack(Material.GOLD_INGOT, gold_ingots));
	}
}
