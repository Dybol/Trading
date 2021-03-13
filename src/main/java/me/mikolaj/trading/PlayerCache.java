package me.mikolaj.trading;

import lombok.Getter;
import org.bukkit.Location;
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

	private static final HashMap<UUID, PlayerCache> cacheMap = new HashMap<>();

	private final UUID uuid;
	private boolean hasBazaar = false;

	private final ExpiringMap<UUID, Boolean> tradeOffersMap = ExpiringMap.builder().expiration(1, TimeUnit.MINUTES).build();

	private final ItemStack[] content = new ItemStack[36];
	private final ItemMeta[] meta = new ItemMeta[36];
	//ilosc zlota, za jaka zwykly gracz kupuje item od bazaarplayer - cena sprzedaży
	//sell i buy z punktu widzenia gracza ktory ma bazar
	private final Integer[] sellAmount = new Integer[36];
	private final Integer[] buyAmount = new Integer[36];

	@Getter
	private boolean isInTradeEnd;

	ItemStack[] inventoryBeforeTrade = new ItemStack[36];

	//todo

	private int counter = 0;
	private Location bazaarLoc;

	protected PlayerCache(final UUID uuid) {
		super(uuid.toString());
		this.uuid = uuid;

		loadConfiguration(NO_DEFAULT, "data.db");
	}


	@Override
	protected void onLoadFinish() {
		//Czy oplaca sie zapisywac dane do pliku na dysku? nw chyba n
		//this.hasBazaar = getBoolean("Has_Bazaar");
	}

	public void addEverything(final ItemStack item, final ItemMeta meta, final int sellAmount, final int buyAmount) {
		this.content[counter] = item;
		this.meta[counter] = meta;
		this.sellAmount[counter] = sellAmount;
		this.buyAmount[counter] = buyAmount;
		this.counter++;
	}

	//trzeba wczesniej zrobic safety checki
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

	public void createInventorySnapshot(final Player player) {
		int counter = 0;
		for (final ItemStack item : player.getInventory().getStorageContents()) {
			System.out.println("wywolano " + item);
			this.inventoryBeforeTrade[counter] = item == null ? null : item.clone();
			counter++;
		}
		System.out.println("Wywolalo sie " + counter + " razy");
	}

	public void printEverything() {
		for (final ItemStack item : this.inventoryBeforeTrade)
			System.out.println("Item: " + item);
	}

	//todo
	public void restorePlayerSnapshot(final Player player) {
		Common.runLater(() -> player.getInventory().setContents(this.inventoryBeforeTrade));
		//Arrays.fill(this.inventoryBeforeTrade, null); //todo dodalem to zeby nie zajmowac pamieci ale hgw czy warto
	}

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

	public static PlayerCache getCache(final Player player) {
		return getCache(player.getUniqueId());
	}

	public static PlayerCache getCache(final UUID uuid) {
		PlayerCache cache = cacheMap.get(uuid);

		if (cache == null) {
			cache = new PlayerCache(uuid);

			cacheMap.put(uuid, cache);
		}
		return cache;
	}
}