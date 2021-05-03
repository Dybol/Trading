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

/**
 * Klasa pomocnicza do bazarow
 */
@UtilityClass
public class BazaarUtil {

	/**
	 * Miejsce na bazary
	 */
	private static final Region bazaarReg = Settings.Bazaar.BAZAARS_EVERYWHERE ? null : new Region(Settings.Bazaar.PRIMARY, Settings.Bazaar.SECONDARY);

	/**
	 * Metoda sprawdzajaca czy gracz jest w miejscu na bazary
	 *
	 * @param player - gracz
	 * @return - czy gracz jest w miejscu na bazary
	 */
	public boolean isInBazaarRegion(final Player player) {
		return Settings.Bazaar.BAZAARS_EVERYWHERE || bazaarReg.isWithin(player.getLocation());
	}

	/**
	 * Metoda sprawdzajaca czy dana lokalizacja jest w miejscu na bazary
	 *
	 * @param location - lokalizacja
	 * @return - czy lokalizacja jest w miejscu na bazary
	 */
	public boolean isInBazaarRegion(final Location location) {
		return Settings.Bazaar.BAZAARS_EVERYWHERE || bazaarReg.isWithin(location);
	}

	/**
	 * Metoda odpowiadaja za formatowanie wiadomosci
	 *
	 * @param item   - item
	 * @param amount - cena
	 * @return sformatowana wiadomosc
	 */
	public String getItemAndAmountFormated(final ItemStack item, final int amount) {
		return "&a" + item.getType() + " x " + item.getAmount() + " za &6" + amount + " zlota.";
	}

	/**
	 * Metoda odpowiadaja za formatowanie wiadomosci
	 *
	 * @param item       - item
	 * @param sellAmount - cena kupna
	 * @param buyAmount  - cena sprzedazy
	 * @return sformatowana wiadomosc
	 */
	public String getItemAndAmountFormated(final ItemStack item, final int sellAmount, final int buyAmount) {
		return "&a" + item.getType() + " x " + item.getAmount() + " Sprzedaz: &6" + sellAmount + " zlota.&a Kupno: &6" + buyAmount + " zlota.";
	}

	/**
	 * Metoda odpowiadajaca za czyszczenie metadaty
	 *
	 * @param itemStack - item
	 * @return sformatowany itemstack
	 */
	public ItemStack clearingMeta(final ItemStack itemStack) {
		final ItemMeta meta = itemStack.getItemMeta();
		final List<String> lore = new ArrayList<>();
		meta.setLore(lore);
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	/**
	 * Metoda odpowiadajaca za wyliczanie ilosci calkowitego zlota z ekwipunku
	 *
	 * @param player - gracz
	 * @return ilosc zlota
	 */
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
	 * Metoda konwertujaca ilosc zlota na ilosc blokow zlota
	 * @param player - gracz
	 * @param gold_change - zmiana ilosci zlota
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

	/**
	 * Metoda odpowiadajaca za wymiane zlota na zlote bloki
	 *
	 * @param player      - gracz
	 * @param gold_change - zmiana zlota
	 */
	public void changeGoldToGoldBlocks(final Player player, final int gold_change) {
		final Inventory player_inventory = player.getInventory();

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
