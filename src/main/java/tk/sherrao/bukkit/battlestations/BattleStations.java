package tk.sherrao.bukkit.battlestations;

import java.io.File;
import java.io.IOException;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.massivecraft.factions.P;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import net.milkbowl.vault.economy.Economy;
import tk.sherrao.bukkit.battlestations.commands.BattleStationsCommand;
import tk.sherrao.bukkit.battlestations.listeners.EntityDamageByFireballListener;
import tk.sherrao.bukkit.battlestations.listeners.EntityDamageByPlayerListener;
import tk.sherrao.bukkit.battlestations.listeners.EntityDeathListener;
import tk.sherrao.bukkit.battlestations.listeners.FactionDisbandListener;
import tk.sherrao.bukkit.battlestations.listeners.PlayerInteractWithCoreListener;
import tk.sherrao.bukkit.battlestations.listeners.PlayerRegionEnterListener;
import tk.sherrao.bukkit.battlestations.listeners.WorldChunkUnloadListener;
import tk.sherrao.bukkit.battlestations.station.DataManager;
import tk.sherrao.bukkit.battlestations.station.Station;
import tk.sherrao.bukkit.battlestations.station.StationManager;
import tk.sherrao.bukkit.utils.ItemBuilder;
import tk.sherrao.bukkit.utils.config.SherConfiguration;
import tk.sherrao.bukkit.utils.plugin.SherPlugin;

public class BattleStations extends SherPlugin {

	protected WorldGuardPlugin wgPlugin;
	protected WorldEditPlugin wePlugin;
	protected P facPlugin;
	protected Economy economy;

	protected ItemStack redemtionItem;
	protected GuiHolder guiHolder;
	protected DataManager dataMgr;
	protected StationManager stationMgr;

	protected SherConfiguration messagesConfig, soundsConfig, stationsConfig, rewardsConfig, guiConfig;

	@Override
	public void onLoad() {
		super.onLoad();

	}

	@Override
	public void onEnable() {
		super.onEnable();
		try {
			wgPlugin = WGBukkit.getPlugin();
			wePlugin = WGBukkit.getPlugin().getWorldEdit();
			facPlugin = P.p;

			RegisteredServiceProvider<Economy> provider = super.getServer().getServicesManager().getRegistration( net.milkbowl.vault.economy.Economy.class );
	        if ( provider != null) {
	            this.economy = provider.getProvider();
	            log.info( "Hooked onto Vault for Economy..." );
	            
	        } else
				log.severe( "Failed to load Vault! is it installed?", new NullPointerException() );

	   } catch ( Exception e ) { log.severe( "Failed to load WorldEdit or FactionsUUID! Are they installed?", e ); }

		super.saveResource( "messages.yml", false );
		super.saveResource( "sounds.yml", false );
		super.saveResource( "stations.yml", false );
		super.saveResource( "rewards.yml", false );
		super.saveResource( "gui.yml", false );

		messagesConfig = new SherConfiguration( this, new File( super.getDataFolder(), "messages.yml" ) );
		soundsConfig = new SherConfiguration( this, new File( super.getDataFolder(), "sounds.yml" ) );
		stationsConfig = new SherConfiguration( this, new File( super.getDataFolder(), "stations.yml" ) );
		rewardsConfig = new SherConfiguration( this, new File( super.getDataFolder(), "rewards.yml" ) );
		guiConfig = new SherConfiguration( this, new File( super.getDataFolder(), "gui.yml" ) );
		
		redemtionItem = new ItemBuilder( stationsConfig.getMaterial( "claim-item.item" ) )
				.setName( stationsConfig.getString( "claim-item.title" ) )
				.setLore( stationsConfig.getStringList( "claim-item.lore" ) )
				.toItemStack();
		
		guiHolder = new GuiHolder( this );
		dataMgr = new DataManager( this );
		stationMgr = new StationManager( this );
		stationMgr.reloadMaps();
		
		stationMgr.a();
		guiHolder.a();
		
		super.registerCommand( "battlestation", new BattleStationsCommand( this ) );
		super.registerEventListener( new EntityDeathListener( this ) );
		super.registerEventListener( new PlayerInteractWithCoreListener( this ) );
		super.registerEventListener( new EntityDamageByFireballListener( this ) );
		super.registerEventListener( new EntityDamageByPlayerListener( this ) );
		super.registerEventListener( new FactionDisbandListener( this ) );
		super.registerEventListener( new PlayerRegionEnterListener( this ) );
		super.registerEventListener( new WorldChunkUnloadListener( this ));
		super.complete();

	}

	@Override
	public void onDisable() {
		for( Station station : stationMgr.getStations() ) {
			try {
				if( station.getControllingFaction().isWilderness() )
					continue;
				
				else 
					dataMgr.save( station );
			
			} catch ( IOException e ) { 
				log.severe( "Failed to save data for station at the coordinates of: " + station.location() + ", printing stacktrace...", e ); 
				
			}
		}
		
		super.onDisable();

	}

	public WorldGuardPlugin getWorldGuardPlugin() {
		return wgPlugin;

	}

	public WorldEditPlugin getWorldEditPlugin() {
		return wePlugin;

	}
	
	public P getFactionsPlugin() {
		return facPlugin;
		
	}
	
	public ItemStack getRedemtionItem() {
		return redemtionItem;
		
	}
	
	public Economy getVaultEconomyPlugin() {
		return economy;
		
	}
	
	public GuiHolder getGuiHolder() {
		return guiHolder;
		
	}
	
	public DataManager getDataManager() {
		return dataMgr;
		
	}
	
	public StationManager getStationManager() {
		return stationMgr;
		
	}

	public SherConfiguration getMessagesConfig() {
		return messagesConfig;

	}

	public SherConfiguration getSoundsConfig() {
		return soundsConfig;

	}

	public SherConfiguration getStationsConfig() {
		return stationsConfig;

	}

	public SherConfiguration getRewardsConfig() {
		return rewardsConfig;

	}
	
	public SherConfiguration getGuiConfiguration() {
		return guiConfig;
		
	}

}