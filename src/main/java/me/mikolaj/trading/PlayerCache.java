package me.mikolaj.trading;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.collection.expiringmap.ExpiringMap;
import org.mineacademy.fo.settings.YamlSectionConfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
public class PlayerCache extends YamlSectionConfig {

	/**
	 * Mapa wszystkich cachow graczy
	 */
	private static final HashMap<UUID, PlayerCache> cacheMap = new HashMap<>();

	/**
	 * UUID
	 */
	private final UUID uuid;

	/**
	 * Czy gracz ma bazar
	 */
	private boolean hasBazaar = false;

	/**
	 * Nazwa bazaru
	 */
	@Setter
	private String bazaarName = null;

	/**
	 * ArmorStand jako nazwa
	 */
	@Setter
	private ArmorStand bazaarStand = null;

	/**
	 * Mapa ofert wymiany danego gracza
	 */
	private final ExpiringMap<UUID, Boolean> tradeOffersMap = ExpiringMap.builder().expiration(1, TimeUnit.MINUTES).build();

	/**
	 * Itemy z ekwipunku gracza
	 */
	private final ItemStack[] content = new ItemStack[36];
	/**
	 * Metadata itemow
	 */
	private final ItemMeta[] meta = new ItemMeta[36];

	/**
	 * Ilosc zlota za jaka gracz kupuje itemy od danego gracza
	 */
	private final Integer[] sellAmount = new Integer[36];
	/**
	 * Ilosc zlota za jaka gracz sprzedaje itemy od danego gracza
	 */
	private final Integer[] buyAmount = new Integer[36];

	/**
	 * Zmienna pomocnicza sprawdzajaca czy gracz jest w ostatnim etapie wymiany
	 */
	@Getter
	private boolean isInTradeEnd;

	/**
	 * Kopia itemow przed rozpoczeciem wymiany
	 */
	ItemStack[] inventoryBeforeTrade = new ItemStack[36];

	/**
	 * Licznik pomocniczy do wypelniania tablic
	 */
	private int counter = 0;

	/**
	 * Lokalizacja bazaru
	 */
	private Location bazaarLoc;

	/**
	 * Konstruktor
	 *
	 * @param uuid - uuid gracza
	 */
	protected PlayerCache(final UUID uuid) {
		super(uuid.toString());
		this.uuid = uuid;

		loadConfiguration(NO_DEFAULT, "data.db");
	}

	/**
	 * Metoda odpowiadajaca za dodawanie itemow do tablic
	 * Wywolywania, gdy gracz dodaje dany item do swojego bazaru
	 *
	 * @param item       - item
	 * @param meta       - metadata
	 * @param sellAmount - cena sprzedazy
	 * @param buyAmount  - cena kupna
	 */
	public void addEverything(final ItemStack item, final ItemMeta meta, final int sellAmount, final int buyAmount) {
		this.content[counter] = item;
		this.meta[counter] = meta;
		this.sellAmount[counter] = sellAmount;
		this.buyAmount[counter] = buyAmount;
		this.counter++;
	}

	//trzeba wczesniej zrobic safety checki

	/**
	 * Metoda odpowiadajaca za usuwanie itemu z bazaru
	 *
	 * @param itemStack - item
	 * @return - sukces operacji
	 */
	public boolean deleteItem(final ItemStack itemStack) {
		for (int i = 0; i < counter; i++) {
			if (itemStack.equals(content[i])) {

				for (int j = i; j < counter - 1; j++) {
					content[j] = content[j + 1];
					meta[j] = meta[j + 1];
					sellAmount[j] = sellAmount[j + 1];
					buyAmount[j] = buyAmount[j + 1];
				}
				content[counter - 1] = null;
				meta[counter - 1] = null;
				sellAmount[counter - 1] = null;
				buyAmount[counter - 1] = null;
				counter--;

				return true;
			}
		}
		return false;
	}

	/**
	 * Metoda odpowiadajaca za stworzenie kopii ekwipunku
	 * Wywolywana przed rozpoczeciem wymiany
	 *
	 * @param player - gracz
	 */
	public void createInventorySnapshot(final Player player) {
		int counter = 0;
		for (final ItemStack item : player.getInventory().getStorageContents()) {
			this.inventoryBeforeTrade[counter] = item == null ? null : item.clone();
			counter++;
		}
	}

	/**
	 * Metoda odpowiadajaca za przywrocenie "zrzutu" ekwipunku gracza
	 *
	 * @param player - gracz
	 */
	public void restorePlayerSnapshot(final Player player) {
		Common.runLater(() -> player.getInventory().setContents(this.inventoryBeforeTrade));
	}

	/**
	 * Czyszczenie tablic z itemami z bazaru
	 */
	public void clearContent() {
		Arrays.fill(this.content, null);
		Arrays.fill(this.meta, null);
		Arrays.fill(this.sellAmount, null);
		Arrays.fill(this.buyAmount, null);

		this.counter = 0;
	}

	public void setHasBazaar(final boolean hasBazaar) {
		this.hasBazaar = hasBazaar;
		save("Has_Bazaar", hasBazaar);
	}

	public void setBazaarLoc(final Location location) {
		this.bazaarLoc = location;
	}

	public void setIsInTradeEnd(final boolean isInTradeEnd) {
		this.isInTradeEnd = isInTradeEnd;
	}

	public boolean hasBazaar() {
		return this.hasBazaar;
	}

	public void addOfferToTradeMap(final UUID uuid) {
		this.tradeOffersMap.put(uuid, true);
	}

	/**
	 * Metoda pobierajaca z pliku lub z mapy cache gracza
	 *
	 * @param player - gracz
	 * @return - cache danego gracza
	 */
	public static PlayerCache getCache(final Player player) {
		return getCache(player.getUniqueId());
	}

	/**
	 * Metoda pobierajaca z pliku lub z mapy cache gracza
	 *
	 * @param uuid - uuid gracza
	 * @return - cache danego gracza
	 */
	public static PlayerCache getCache(final UUID uuid) {
		PlayerCache cache = cacheMap.get(uuid);

		if (cache == null) {
			cache = new PlayerCache(uuid);

			cacheMap.put(uuid, cache);
		}
		return cache;
	}
}