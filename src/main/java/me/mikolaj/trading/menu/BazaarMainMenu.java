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

/**
 * Bazarowe menu
 */
public class BazaarMainMenu extends Menu {

	/**
	 * Gracz, ktory ma bazar
	 */
	private final Player bazaarPlayer;

	/**
	 * Zawartosc menu - kazdy wystawiony item jest przyciskiem
	 */
	private final Button[] itemButton = new Button[36];

	/**
	 * Konstruktor, w ktorym ustawiamy wiekszosc rzeczy
	 *
	 * @param bazaarPlayer - gracz, ktory ma bazar
	 */
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

			final ItemStack finalItem = item;
			final int finalI = i;

			this.itemButton[i] = new Button() {
				/**
				 * Metoda wywyolujaca sie podczas klikniecia na przycisk z przedmiotem
				 *
				 * @param player    - gracz
				 * @param menu      - dane menu
				 * @param clickType - rodzaj klikniecia mysza
				 */
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

				/**
				 * Metoda zwracajaca dany item
				 *
				 * @return - item
				 */
				@Override
				public ItemStack getItem() {
					return finalItem;
				}
			};
		}
	}

	/**
	 * Metoda zwracajaca itemy na bazarze
	 *
	 * @param slot - slot (miejsce w menu)
	 * @return - item, ktory ma zostac wyswietlony
	 */
	@Override
	public ItemStack getItemAt(final int slot) {

		final PlayerCache cache = PlayerCache.getCache(bazaarPlayer);
		if (slot < cache.getCounter()) {
			return itemButton[slot].getItem();
		}

		return null;
	}

	/**
	 * Menu kupna itemu od gracza
	 */
	private final class BuyMenu extends Menu {

		/**
		 * Przycisk kupna
		 */
		private final Button buyButton;

		/**
		 * Przycisk powrotu
		 */
		private final Button backButton;

		/**
		 * Item, ktory chcemy kupic
		 */
		private final ItemStack itemStack;

		/**
		 * Konstruktor, w ktorym ustawiamy wiekszosc rzecyz
		 * @param bazaarPlayer - gracz, ktory ma bazar
		 * @param itemStack - item, ktory bedziemy kupowac
		 * @param sellAmount - cena danego itemu
		 */
		private BuyMenu(final Player bazaarPlayer, final ItemStack itemStack, final int sellAmount) {

			this.itemStack = itemStack;
			setTitle("&2Kupujesz za: &6" + sellAmount + " zlota.");

			this.buyButton = new Button() {

				/**
				 * Metoda wywolywana podczas klikniecia na przycisk kupna
				 * @param player - gracz
				 * @param menu - menu
				 * @param clickType - rodzaj klikniecia mysza
				 */
				@Override
				public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
					final Inventory bazaarInventory = bazaarPlayer.getInventory();

					//Sprawdzanie czy gracze maja odpowiednie itemy
					if (player.getInventory().containsAtLeast(CompMaterial.GOLD_INGOT.toItem(), sellAmount) || BazaarUtil.calculateGoldWithGoldBlocks(player) >= sellAmount) {
						if (bazaarInventory.containsAtLeast(itemStack, itemStack.getAmount())) {
							bazaarInventory.removeItem(itemStack);
							BazaarUtil.changeGoldToGoldBlocks(bazaarPlayer, sellAmount);

							Common.tell(bazaarPlayer, "&aUdalo Ci sie sprzedac " + BazaarUtil.getItemAndAmountFormated(itemStack, sellAmount));
						} else {
							animateTitle("&cOferta wyprzedana!");
							Common.runLater(20, () -> new BazaarMainMenu(bazaarPlayer).displayTo(player));
							return;
						}

						BazaarUtil.changeGoldToGoldBlocks(player, -sellAmount);

						player.getInventory().addItem(itemStack);
						animateTitle("&2Transakcja udana!");

						onTransactionSuccess(bazaarPlayer, player);
						Common.runLater(20, () -> new BazaarMainMenu(bazaarPlayer).displayTo(player));

					} else {
						animateTitle("&cNie masz wystarczajacej ilosc zlota!");
						Common.runLater(20, () -> new BazaarMainMenu(bazaarPlayer).displayTo(player));
						return;
					}
				}

				/**
				 * Metoda zwracajaca dany item do wyswietlenia
				 *
				 * @return - item
				 */
				@Override
				public ItemStack getItem() {
					return ItemCreator.of(CompMaterial.EMERALD_BLOCK,
							"&2Potwierdz zakup").build().make();
				}
			};

			this.backButton = new Button() {

				/**
				 * Metoda wywolywana podczas klikniecia na przycisk powrotu
				 *
				 * @param player    - gracz
				 * @param menu      - menu
				 * @param clickType - rodzaj klikniecia mysza
				 */
				@Override
				public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
					new BazaarMainMenu(bazaarPlayer).displayTo(player);
				}


				/**
				 * Metoda zwracajaca dany item do wyswietlenia
				 *
				 * @return - item
				 */
				@Override
				public ItemStack getItem() {
					return ItemCreator.of(CompMaterial.REDSTONE_BLOCK,
							"&4Anuluj zakup").build().make();
				}
			};
		}

		/**
		 * Metoda zwracajaca mozliwe opcje - potwierdzenie kupna lub powrot
		 *
		 * @param slot - slot
		 * @return item do wyswietlenia
		 */
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

	/**
	 * Menu sprzedazy itemu graczowi
	 */
	private final class SellMenu extends Menu {

		/**
		 * Przycisk sprzedazy
		 */
		private final Button sellButton;

		/**
		 * Przycisk powrotu
		 */
		private final Button backButton;

		/**
		 * Dany item
		 */
		private final ItemStack itemStack;

		/**
		 * Konstruktor w ktorym ustawiamy wiekszosc rzeczy
		 * @param bazaarPlayer - gracz, ktory ma bazar
		 * @param itemStack - dany item
		 * @param buyAmount - cena
		 */
		private SellMenu(final Player bazaarPlayer, final ItemStack itemStack, final int buyAmount) {

			this.itemStack = itemStack;
			setTitle("&2Sprzedajesz za: &6" + buyAmount + " zlota.");

			this.sellButton = new Button() {

				/**
				 * Metoda wywolywana podczas klikniecia na przycisk sprzedazy
				 * @param player - gracz
				 * @param menu - menu
				 * @param clickType - rodzaj klikniecia
				 */
				@Override
				public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
					final Inventory playerInventory = player.getInventory();

					//Sprawdzanie, czy gracz i bazaarplayer maja odpowiednie itemy
					if (bazaarPlayer.getInventory().containsAtLeast(CompMaterial.GOLD_INGOT.toItem(), buyAmount) || BazaarUtil.calculateGoldWithGoldBlocks(bazaarPlayer) >= buyAmount) {
						if (playerInventory.containsAtLeast(itemStack, itemStack.getAmount())) {
							BazaarUtil.changeGoldToGoldBlocks(player, buyAmount);
							playerInventory.removeItem(itemStack);

							Common.tell(bazaarPlayer, "&aUdalo Ci sie kupic " + BazaarUtil.getItemAndAmountFormated(itemStack, buyAmount));
						} else {
							animateTitle("&cNie masz juz wiecej itemow!");
							Common.runLater(20, () -> new BazaarMainMenu(bazaarPlayer).displayTo(player));
							return;
						}

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

				/**
				 * Metoda zwracajaca dany item do wyswietlenia
				 *
				 * @return - item
				 */
				@Override
				public ItemStack getItem() {
					return ItemCreator.of(CompMaterial.EMERALD_BLOCK,
							"&2Potwierdz sprzedaz").build().make();
				}
			};

			this.backButton = new Button() {

				/**
				 * Metoda wywolywana podczas klikniecia na przycisk powrotu
				 *
				 * @param player    - gracz
				 * @param menu      - menu
				 * @param clickType - rodzaj klikniecia
				 */
				@Override
				public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
					new BazaarMainMenu(bazaarPlayer).displayTo(player);
				}

				/**
				 * Metoda zwracajaca dany item do wyswietlenia
				 *
				 * @return - item
				 */
				@Override
				public ItemStack getItem() {
					return ItemCreator.of(CompMaterial.REDSTONE_BLOCK,
							"&4Anuluj sprzedaz").build().make();
				}
			};
		}

		/**
		 * Metoda zwracajaca mozliwe opcje - potwierdzenie sprzedazy lub powrot
		 *
		 * @param slot - slot
		 * @return item do wyswietlenia
		 */
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

	/**
	 * Metoda wywolywana po udanej transakcji
	 * @param seller - sprzedajacy
	 * @param buyer - kupujacy
	 */
	public void onTransactionSuccess(final Player seller, final Player buyer) {

	}
}