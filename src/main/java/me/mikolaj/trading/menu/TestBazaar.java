package me.mikolaj.trading.menu;

import me.mikolaj.trading.PlayerCache;
import me.mikolaj.trading.utils.BazaarUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;

import java.util.ArrayList;
import java.util.List;

public class TestBazaar extends Menu {

	private final Player bazaarPlayer;

	private final Button[] itemButton = new ButtonMenu[36];

	public TestBazaar(final Player bazaarPlayer) {

		this.bazaarPlayer = bazaarPlayer;

		final PlayerCache cache = PlayerCache.getCache(bazaarPlayer);
		setTitle("&1Oferty gracza " + bazaarPlayer.getName());

		for (int i = 0; i < cache.getCounter(); i++) {
			ItemStack item = cache.getContent()[i];
			item.setItemMeta(cache.getMeta()[i]);

			item = ItemCreator.of(item)
					.lore("&2Cena: &6" + cache.getAmount()[i] + " zlota.")
					.build().make();

			this.itemButton[i] = new ButtonMenu(new BuyMenu(bazaarPlayer, item, cache.getAmount()[i]), item);
		}
	}

	@Override
	public ItemStack getItemAt(final int slot) {

		final PlayerCache cache = PlayerCache.getCache(bazaarPlayer);
		if(slot < cache.getCounter()) {
			return itemButton[slot].getItem();
		}
/*
		if(slot < cache.getCounter()) {
			final ItemStack item = cache.getContent()[slot];
			item.setItemMeta(cache.getMeta()[slot]);

			return ItemCreator.of(item)
					.lore("&2Cena: &6" + cache.getAmount()[slot] + " zlota.")
					.build().make();
		}*/
		return null;
	}

	private final class BuyMenu extends Menu {

		//TODO zrobic przycisk sellButton, dla kogos kto odwiedza bazar(nie bazaarplayer)
		private final Button buyButton;
		private final Button backButton;
		private final int amount;

		private final ItemStack itemStack;


		private BuyMenu(final Player bazaarPlayer, final ItemStack itemStack, final int amount) {
			//super(TestBazaar.this);

			this.itemStack = itemStack;
			this.amount = amount;
			setTitle("&2Cena: &6" + amount + " zlota.");

			this.buyButton = new Button() {
				@Override
				public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
					final Inventory bazaarInventory = bazaarPlayer.getInventory();

					if(player.getInventory().containsAtLeast(CompMaterial.GOLD_INGOT.toItem(), amount)) {
						if(bazaarInventory.containsAtLeast(itemStack, itemStack.getAmount())) {
							bazaarInventory.removeItem(itemStack);
							bazaarInventory.addItem(new ItemStack(Material.GOLD_INGOT, amount));
							Common.tell(bazaarPlayer, "&aUdalo Ci sie sprzedac " + BazaarUtil.getItemAndAmountFormated(itemStack, amount));
						} else {
							animateTitle("&cOferta wyprzedana!");
							Common.runLater(20, () -> 	new TestBazaar(bazaarPlayer).displayTo(player));
							return;
						}

						PlayerUtil.take(player, CompMaterial.GOLD_INGOT, amount);
						player.getInventory().addItem(itemStack);
						animateTitle("&2Transakcja udana!");
						Common.runLater(20, () -> 	new TestBazaar(bazaarPlayer).displayTo(player));

					} else {
						animateTitle("&cNie masz wystarczajacej ilosc zlota!");
						Common.runLater(20, () -> 	new TestBazaar(bazaarPlayer).displayTo(player));
						return;
					}
				}

				@Override
				public ItemStack getItem() {
					return ItemCreator.of(CompMaterial.EMERALD_BLOCK,
							"&2Potwierdz zakup").build().make();
				}
			};

			this.backButton = new Button() {
				@Override
				public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
					new TestBazaar(bazaarPlayer).displayTo(player);
				}

				@Override
				public ItemStack getItem() {
					return ItemCreator.of(CompMaterial.REDSTONE_BLOCK,
							"&4Anuluj zakup").build().make();
				}
			};
		}

		@Override
		public ItemStack getItemAt(final int slot) {
			if(slot == getCenterSlot()) {
				//Czyszczenie ceny z itemu, zeby wygladal odpowiednio.
				final ItemMeta meta = itemStack.getItemMeta();
				final List<String> lore = new ArrayList<>();
				meta.setLore(lore);
				itemStack.setItemMeta(meta);
				return itemStack;
			}

			if(slot == getCenterSlot() - 2)
				return this.buyButton.getItem();

			if(slot == getCenterSlot() + 2)
				return this.backButton.getItem();

			return CompMaterial.GLASS_PANE.toItem();

		}
	}

}