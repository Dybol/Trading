package me.mikolaj.trading.menu;

import me.mikolaj.trading.PlayerCache;
import me.mikolaj.trading.utils.BazaarUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;

public class BazaarMainMenu extends Menu {

	private final Player bazaarPlayer;

	private final Button[] itemButton = new Button[36];

	public BazaarMainMenu(final Player bazaarPlayer) {

		this.bazaarPlayer = bazaarPlayer;

		final PlayerCache cache = PlayerCache.getCache(bazaarPlayer);
		setTitle("&1Oferty gracza " + bazaarPlayer.getName());

		for (int i = 0; i < cache.getCounter(); i++) {
			ItemStack item = cache.getContent()[i];
			item.setItemMeta(cache.getMeta()[i]);

			item = ItemCreator.of(item)
					.lore("&2Kupujesz za: &6" + cache.getSellAmount()[i] + " zlota.")
					.lore("&2Sprzedajesz za &6" + cache.getBuyAmount()[i] + " zlota.")
					.build().make();

			//this.itemButton[i] = new ButtonMenu(new BuyMenu(bazaarPlayer, item, cache.getAmount()[i]), item);
			final ItemStack finalItem = item;
			final int finalI = i;
			this.itemButton[i] = new Button() {
				@Override
				public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {

					if (clickType == ClickType.LEFT) {
						final int sellAmount = cache.getSellAmount()[finalI];

						if (sellAmount == 0) {
							animateTitle("&cGracz nie sprzedaje tego itemu!");
							return;
						}
						new BuyMenu(bazaarPlayer, finalItem, sellAmount).displayTo(player);
					}

					if (clickType == ClickType.RIGHT) {
						final int buyAmount = cache.getBuyAmount()[finalI];

						if (buyAmount == 0) {
							animateTitle("&cGracz nie kupuje tego itemu!");
							return;
						}
						new SellMenu(bazaarPlayer, finalItem, buyAmount).displayTo(player);
					}
				}

				@Override
				public ItemStack getItem() {
					return finalItem;
				}
			};
		}
	}

	@Override
	public ItemStack getItemAt(final int slot) {

		final PlayerCache cache = PlayerCache.getCache(bazaarPlayer);
		if (slot < cache.getCounter()) {
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

		private final Button buyButton;
		private final Button backButton;

		private final ItemStack itemStack;

		private BuyMenu(final Player bazaarPlayer, final ItemStack itemStack, final int sellAmount) {

			this.itemStack = itemStack;
			setTitle("&2Kupujesz za: &6" + sellAmount + " zlota.");

			this.buyButton = new Button() {
				@Override
				public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
					final Inventory bazaarInventory = bazaarPlayer.getInventory();

					//Sprawdzanie czy gracze maja odpowiednie itemy
					if (player.getInventory().containsAtLeast(CompMaterial.GOLD_INGOT.toItem(), sellAmount) || BazaarUtil.calculateGoldWithGoldBlocks(player) > sellAmount) {
						if (bazaarInventory.containsAtLeast(itemStack, itemStack.getAmount())) {
							bazaarInventory.removeItem(itemStack);
							//bazaarInventory.addItem(new ItemStack(Material.GOLD_INGOT, sellAmount));
							BazaarUtil.changeGoldToGoldBlocks(bazaarPlayer, sellAmount);

							Common.tell(bazaarPlayer, "&aUdalo Ci sie sprzedac " + BazaarUtil.getItemAndAmountFormated(itemStack, sellAmount));
						} else {
							animateTitle("&cOferta wyprzedana!");
							Common.runLater(20, () -> new BazaarMainMenu(bazaarPlayer).displayTo(player));
							return;
						}

						//PlayerUtil.take(player, CompMaterial.GOLD_INGOT, sellAmount);
						BazaarUtil.changeGoldToGoldBlocks(player, -sellAmount);

						player.getInventory().addItem(itemStack);
						animateTitle("&2Transakcja udana!");
						onTransactionSuccess(bazaarPlayer, player);
						//todo wywolywac event onBazaarSuccess() ? moze sie przydac podczas np zamiany zlota na bloki itp
						Common.runLater(20, () -> new BazaarMainMenu(bazaarPlayer).displayTo(player));

					} else {
						animateTitle("&cNie masz wystarczajacej ilosc zlota!");
						Common.runLater(20, () -> new BazaarMainMenu(bazaarPlayer).displayTo(player));
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
					new BazaarMainMenu(bazaarPlayer).displayTo(player);
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
			if (slot == getCenterSlot()) {
				//Czyszczenie ceny z itemu, zeby wygladal odpowiednio.
				return BazaarUtil.clearingMeta(itemStack);
			}

			if (slot == getCenterSlot() - 2)
				return this.buyButton.getItem();

			if (slot == getCenterSlot() + 2)
				return this.backButton.getItem();

			return CompMaterial.GLASS_PANE.toItem();

		}
	}


	private final class SellMenu extends Menu {

		private final Button sellButton;
		private final Button backButton;

		private final ItemStack itemStack;

		private SellMenu(final Player bazaarPlayer, final ItemStack itemStack, final int buyAmount) {

			this.itemStack = itemStack;
			setTitle("&2Sprzedajesz za: &6" + buyAmount + " zlota.");

			this.sellButton = new Button() {
				@Override
				public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
					final Inventory playerInventory = player.getInventory();

					//Sprawdzanie, czy gracz i bazaarplayer maja odpowiednie itemy
					if (bazaarPlayer.getInventory().containsAtLeast(CompMaterial.GOLD_INGOT.toItem(), buyAmount) || BazaarUtil.calculateGoldWithGoldBlocks(bazaarPlayer) > buyAmount) {
						if (playerInventory.containsAtLeast(itemStack, itemStack.getAmount())) {
							BazaarUtil.changeGoldToGoldBlocks(player, buyAmount);
							playerInventory.removeItem(itemStack);
							//playerInventory.addItem(new ItemStack(Material.GOLD_INGOT, buyAmount));
							Common.tell(bazaarPlayer, "&aUdalo Ci sie kupic " + BazaarUtil.getItemAndAmountFormated(itemStack, buyAmount));
						} else {
							animateTitle("&cNie masz juz wiecej itemow!");
							Common.runLater(20, () -> new BazaarMainMenu(bazaarPlayer).displayTo(player));
							return;
						}

						//PlayerUtil.take(bazaarPlayer, CompMaterial.GOLD_INGOT, buyAmount);
						BazaarUtil.changeGoldToGoldBlocks(bazaarPlayer, -buyAmount);
						bazaarPlayer.getInventory().addItem(itemStack);
						animateTitle("&2Transakcja udana!");

						onTransactionSuccess(player, bazaarPlayer);

						Common.runLater(20, () -> new BazaarMainMenu(bazaarPlayer).displayTo(player));

					} else {
						animateTitle("&cOferta wyprzedana - gracz nie ma srodkow!");
						Common.runLater(20, () -> new BazaarMainMenu(bazaarPlayer).displayTo(player));
						return;
					}
				}

				@Override
				public ItemStack getItem() {
					return ItemCreator.of(CompMaterial.EMERALD_BLOCK,
							"&2Potwierdz sprzedaz").build().make();
				}
			};

			this.backButton = new Button() {
				@Override
				public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
					new BazaarMainMenu(bazaarPlayer).displayTo(player);
				}

				@Override
				public ItemStack getItem() {
					return ItemCreator.of(CompMaterial.REDSTONE_BLOCK,
							"&4Anuluj sprzedaz").build().make();
				}
			};
		}

		@Override
		public ItemStack getItemAt(final int slot) {
			if (slot == getCenterSlot()) {
				//Czyszczenie ceny z itemu, zeby wygladal odpowiednio.
				return BazaarUtil.clearingMeta(itemStack);
			}

			if (slot == getCenterSlot() - 2)
				return this.sellButton.getItem();

			if (slot == getCenterSlot() + 2)
				return this.backButton.getItem();

			return CompMaterial.GLASS_PANE.toItem();

		}
	}

	public void onTransactionSuccess(final Player seller, final Player buyer) {
//		final Inventory sellerInv = seller.getInventory();
		System.out.println("Z sukcesywnej transakcji");
		//BazaarUtil.changeGoldToGoldBlocks(seller, 0);
		//BazaarUtil.changeGoldToGoldBlocks(buyer, 0);
	}
}