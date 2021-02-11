package me.mikolaj.trading;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mineacademy.fo.collection.expiringmap.ExpiringMap;
import org.mineacademy.fo.settings.YamlSectionConfig;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
public class PlayerCache extends YamlSectionConfig {

	private static final ExpiringMap<UUID, PlayerCache> cacheMap = ExpiringMap.builder().expiration(30, TimeUnit.MINUTES).build();

	private final UUID uuid;
	private boolean hasBazaar = false;


	//TODO raczej stad bedzie trzeba zrobic usuwanie i przesuwanie tablicy
	private final ItemStack[] content = new ItemStack[36];
	private final ItemMeta[] meta = new ItemMeta[36];
	private final Integer[] amount = new Integer[36];
	private int counter = 0;
	private Location bazaarLoc;


	protected PlayerCache(final UUID uuid) {
		super(uuid.toString());
		this.uuid = uuid;

		loadConfiguration(NO_DEFAULT, "data.db");
	}


	@Override
	protected void onLoadFinish() {
		//this.hasBazaar = getBoolean("Has_Bazaar");
	}

	public void addEverything(final ItemStack item, final ItemMeta meta, final int amount) {
		this.content[counter] = item;
		this.meta[counter] = meta;
		this.amount[counter] = amount;
		this.counter++;

	}


	public void clearContent() {
		Arrays.fill(this.content, null);
		Arrays.fill(this.meta, null);
		Arrays.fill(this.amount, null);
		this.counter = 0;
	}

	public void setHasBazaar(final boolean hasBazaar) {
		this.hasBazaar = hasBazaar;
		save("Has_Bazaar", hasBazaar);
	}

	public void setBazaarLoc(final Location location) {
		this.bazaarLoc = location;
	}


	public boolean hasBazaar() {
		return this.hasBazaar;
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
