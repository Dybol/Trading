package me.mikolaj.trading.menu;

import me.mikolaj.trading.PlayerCache;
import me.mikolaj.trading.utils.BazaarUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;

/**
 * Menu odpowiadajace za usuwanie itemow z bazaru
 */
public class BazaarDeleteMenu extends Menu {

	/**
	 * Gracz, ktory ma bazar
	 */
	private final Player bazaarPlayer;

	/**
	 * Wystawione itemy
	 */
	private final Button[] itemButton = new Button[36];

	/**
	 * Konstuktor, w ktorym ustawiamy wiekszosc rzeczy
	 *
	 * @param bazaarPlayer - gracz, ktory ma bazar
	 */
	public BazaarDeleteMenu(final Player bazaarPlayer) {

		this.bazaarPlayer = bazaarPlayer;

		final PlayerCache cache = PlayerCache.getCache(bazaarPlayer);
		setTitle("&1Wybierz item do usuniecia");

		for (int i = 0; i < cache.getCounter(); i++) {
			ItemStack item = cache.getContent()[i];
			item.setItemMeta(cache.getMeta()[i]);

			item = ItemCreator.of(item)
					.lore("&2Kupujesz za: &6" + cache.getSellAmount()[i] + " zlota.")
					.lore("&2Sprzedajesz za &6" + cache.getBuyAmount()[i] + " zlota.")
					.build().make();

			this.itemButton[i] = new ButtonMenu(new ConfirmRemoveItemMenu(bazaarPlayer, item), item);
		}

	}

	/**
	 * Wyswietlanie itemow, ktore gracz aktualnie wystawil
	 *
	 * @param slot - slot
	 * @return - item
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
	 * Menu potwierdzajace usuniecie itemu
	 */
	private static final class ConfirmRemoveItemMenu extends Menu {

		/**
		 * Item do usuniecia
		 */
		private final ItemStack item;

		/**
		 * Przycisk potwierdzajacy usuniecie danego itemu
		 */
		private final Button removeButton;

		/**
		 * Przycisk powrotu
		 */
		private final Button backButton;

		/**
		 * Konstruktor, w ktorym ustawiamy wiekszosc rzeczy
		 * @param bazaarPlayer - gracz, ktory ma bazar
		 * @param item - item, ktory chcemy usunac
		 */
		private ConfirmRemoveItemMenu(final Player bazaarPlayer, final ItemStack item) {

			setTitle("&bPotwierdz usuniecie itemu z bazaru");

			this.item = item;

			this.removeButton = new Button() {

				/**
				 * Metoda wywolywana podczas klikniecia na przycisk potwierdzenia usuwania
				 * @param player - gracz
				 * @param menu - menu
				 * @param clickType - rodzaj klikniecia
				 */
				@Override
				public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
					final PlayerCache bazaarCache = PlayerCache.getCache(bazaarPlayer);

					if (bazaarCache.deleteItem(item)) {
						animateTitle("&2Usunieto!");
						Common.runLater(20, () -> new BazaarDeleteMenu(bazaarPlayer).displayTo(player));
					} else {
						//sytuacja raczej niemozliwa
						animateTitle("&cBlad!");
					}
				}

				/**
				 * Metoda zwracajaca dany item do wyswietlenia
				 * @return item
				 */
				@Override
				public ItemStack getItem() {
					return ItemCreator.of(CompMaterial.EMERALD_BLOCK,
							"&aPotwierdz usuniecie").build().make();
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
					new BazaarDeleteMenu(bazaarPlayer).displayTo(player);
				}

				/**
				 * Metoda zwracajaca dany item do wyswietlenia
				 *
				 * @return - item
				 */
				@Override
				public ItemStack getItem() {
					return ItemCreator.of(CompMaterial.REDSTONE_BLOCK,
							"&cAnuluj").build().make();
				}
			};
		}

		/**
		 * Metoda wyswietlajaca mozliwe akcje - potwierdzenie
		 * usuniecia itemu, lub powrot, oraz przedmiot ktory
		 * aktualnie chcemy usunac
		 *
		 * @param slot - slot
		 * @return item
		 */
		@Override
		public ItemStack getItemAt(final int slot) {
			if (slot == getCenterSlot()) {
				return BazaarUtil.clearingMeta(item);
			}

			if (slot == getCenterSlot() - 2)
				return this.removeButton.getItem();

			if (slot == getCenterSlot() + 2)
				return this.backButton.getItem();

			return CompMaterial.GLASS_PANE.toItem();

		}
	}
}
