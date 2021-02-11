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

public class BazaarDeleteMenu extends Menu {

	private final Player bazaarPlayer;

	private final Button[] itemButton = new Button[36];

	public BazaarDeleteMenu(final Player bazaarPlayer) {

		this.bazaarPlayer = bazaarPlayer;

		final PlayerCache cache = PlayerCache.getCache(bazaarPlayer);
		setTitle("&1Wybierz item ktory chcesz usunac ");

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

	@Override
	public ItemStack getItemAt(final int slot) {
		final PlayerCache cache = PlayerCache.getCache(bazaarPlayer);

		if (slot < cache.getCounter()) {
			return itemButton[slot].getItem();
		}

		return null;
	}

	private static final class ConfirmRemoveItemMenu extends Menu {

		private final ItemStack item;
		private final Button removeButton;
		private final Button backButton;


		private ConfirmRemoveItemMenu(final Player bazaarPlayer, final ItemStack item) {

			setTitle("&bPotwierdz usuniecie itemu z bazaru");

			this.item = item;

			this.removeButton = new Button() {
				@Override
				public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
					final PlayerCache bazaarCache = PlayerCache.getCache(bazaarPlayer);

					if (bazaarCache.deleteItem(item)) {
						animateTitle("&2Usunieto!");
						//menu.restartMenu();
						Common.runLater(20, () -> new BazaarDeleteMenu(bazaarPlayer).displayTo(player));
					} else { //nw czy mozliwe to
						animateTitle("&cBlad!");
					}
				}

				@Override
				public ItemStack getItem() {
					return ItemCreator.of(CompMaterial.EMERALD_BLOCK,
							"&aPotwierdz usuniecie").build().make();
				}
			};

			this.backButton = new Button() {
				@Override
				public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
					new BazaarDeleteMenu(bazaarPlayer).displayTo(player);
				}

				@Override
				public ItemStack getItem() {
					return ItemCreator.of(CompMaterial.REDSTONE_BLOCK,
							"&cAnuluj").build().make();
				}
			};
		}

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
