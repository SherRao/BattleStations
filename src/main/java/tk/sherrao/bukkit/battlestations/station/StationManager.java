package tk.sherrao.bukkit.battlestations.station;

import static org.bukkit.Bukkit.dispatchCommand;
import static org.bukkit.Bukkit.getConsoleSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.P;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import tk.sherrao.bukkit.battlestations.BattleStations;
import tk.sherrao.bukkit.battlestations.GuiHolder;
import tk.sherrao.bukkit.utils.config.SherConfiguration;
import tk.sherrao.bukkit.utils.plugin.SherPluginFeature;
import tk.sherrao.utils.collections.Pair;

public class StationManager extends SherPluginFeature implements Runnable {

	private final class Lock {}

	private final Object lock = new Lock();

	protected SherConfiguration stationsConfig;
	protected WorldGuardPlugin wgPl;
	protected WorldEditPlugin wePl;
	protected P facPl;
	protected DataManager dataMgr;
	protected GuiHolder gui;
	
	protected World world;
	protected RegionManager regionMgr;
	protected Material core, turret;
	protected double coreHealth;
	protected int turretFireRange, turretHealth;
	
	protected List<Station> stations;
	protected int mapsLoaded;
	
	protected List<String> rewards;
	protected int rewardDelay;
	
	public enum CoreInteraction {
			
		NONE, FRIENDLY, CLAIM, INVALID_NO_FAC, INVALID_ALREADY_CLAIMED;

	}
	
	public StationManager( BattleStations pl ) {
		super(pl);

		this.stationsConfig = pl.getStationsConfig();
		this.wgPl = pl.getWorldGuardPlugin();
		this.wePl = pl.getWorldEditPlugin();
		this.facPl = pl.getFactionsPlugin();
		this.dataMgr = pl.getDataManager();
		
		this.world = Bukkit.getWorld( pl.getStationsConfig().getString( "world" ) );
		if( world == null )
			throw new IllegalArgumentException( "Invalid World: \"" + world + "\"" );
		
		this.regionMgr = wgPl.getRegionManager( world );
		this.core = stationsConfig.getMaterial( "core.block" );
		this.turret = stationsConfig.getMaterial( "turrets.block" );
		this.turretHealth = stationsConfig.getInt( "turrets.health" );
		this.coreHealth = stationsConfig.getDouble( "core.health" );
		this.turretFireRange = stationsConfig.getInt( "turrets.fire-range" );
		
		this.stations = Collections.synchronizedList( new ArrayList<>() );
		
		this.rewards = pl.getRewardsConfig().getStringList( "rewards" );
		this.rewardDelay = pl.getRewardsConfig().getInt( "time-between-rewards" );
		
		Bukkit.getScheduler().runTaskTimer( pl, this, 2 * 20, rewardDelay * 20 );
		
	}
	
	public void a() {
		this.gui = ((BattleStations) pl).getGuiHolder();
		
	}

	@Override
	public void run() {
		synchronized( lock ) {
			for( Station station : stations ) {
				if( station.getControllingFaction().isWilderness() )
					continue;
				
				for( Player player : station.getControllingFaction().getOnlinePlayers() ) {
					for( String cmd : rewards )
						dispatchCommand( getConsoleSender(), cmd.replace( "[player]", player.getName() ) );
					
				}
			}
		}
	}
	
	public int reloadMaps() {
		synchronized( lock ) {
			mapsLoaded = 0;
			stations.clear();
			for( Entry<String, ProtectedRegion> entry : regionMgr.getRegions().entrySet() ) {
				ProtectedRegion protRegion = entry.getValue();
				String name = entry.getKey();
				if( !name.startsWith( "station_" ) )
					continue;

				@SuppressWarnings( "deprecation" )
				CuboidRegion region = new CuboidRegion( BukkitUtil.getLocalWorld( world ), protRegion.getMinimumPoint(), protRegion.getMaximumPoint() );
				StationCore tempCore = null;
				List<StationTurret> tempTurrets = Collections.synchronizedList( new ArrayList<>() );
				
				for( BlockVector vec : region ) {
					Block block = world.getBlockAt( vec.getBlockX(), vec.getBlockY(), vec.getBlockZ() );
					if( block.getType() == core )
						tempCore = new StationCore( (BattleStations) pl, world, block.getLocation(), coreHealth );

					else if( block.getType() == turret )
						tempTurrets.add( new StationTurret( (BattleStations) pl, world, block.getLocation(), turretHealth, turretFireRange ) );

					else
						continue;

				}

				if( tempCore == null || tempTurrets.isEmpty() )
					log.warning( "Found WorldGuard region: " + name + " without valid blocks, ignoring...." );

				else {
					Station station = new Station( (BattleStations) pl, name, world, region, protRegion, tempCore, tempTurrets );
					dataMgr.load( station );
					stations.add( station );
				
				}
			}

			return mapsLoaded;
		}
	}
	
	public Faction coreDestroyed( Player player, Location location ) {
		synchronized( lock ) {
			for( Station station : stations ) {
				if( station.getCore().getLocation().equals( location ) ) {
					Faction fac = station.getControllingFaction();
					station.destroyed( FPlayers.getInstance().getByPlayer( player ).getFaction() );
					return fac;
					
				} else
					continue;
				
			}
			
			return null;
			
		}
	}
	
	
	public Pair<Station, CoreInteraction> coreClicked( Player player, Location location ) {
		synchronized( lock ) {
			FPlayer fplayer = FPlayers.getInstance().getByPlayer( player );
			Pair<Station, CoreInteraction> result = Pair.from( null, CoreInteraction.NONE );
			for( Station station : stations ) {
				if( !station.getCore().getLocation().equals( location ) )
					continue;
				
				else {
					if( station.getControllingFaction().isWilderness() ) {
						if( fplayer.getFaction().isWilderness() ) {
							result.setKey( station );
							result.setValue( CoreInteraction.INVALID_NO_FAC );
							break;
							
						} else {
							gui.claimGui( player, station );
							result.setKey( station );
							result.setValue( CoreInteraction.CLAIM );
							break;
							
						}
							
					} else {
						if( station.getControllingFaction().getFPlayers().contains( fplayer ) ) {
							gui.friendlyGui( player, station );
							result.setKey( station );
							result.setValue( CoreInteraction.FRIENDLY );
							break;
							
						} else {
							if( fplayer.getFaction().isWilderness() ) {
								result.setKey( station );
								result.setValue( CoreInteraction.INVALID_NO_FAC );
								break;
							
							} else {
								result.setKey( station );
								result.setValue( CoreInteraction.INVALID_ALREADY_CLAIMED );
								break;
								
							}
						}
					}
				}
			}		

			return result;
		
		}
	}
	
	@Deprecated
	public Station coreClicked1( Player player, Location location ) {
		synchronized( lock ) {
			Faction faction = FPlayers.getInstance().getByPlayer( player ).getFaction();
			for( Station station : stations ) {
				if( station.getCore().getLocation().equals( location ) ) {
					if( !faction.isWilderness() && station.getControllingFaction().getOnlinePlayers().contains( player ) ) {
						gui.friendlyGui( player, station );
						return station;
						
					} else if( !faction.isWilderness() && station.getControllingFaction().isWilderness() ) {
						gui.claimGui( player, station );
						return station;
						
					} else
						continue;
					
				} else
					continue;
				
			}
			
			return null;
			
		} 
	}

	public Station hasStation( Faction faction ) {
		synchronized( lock ) {
			for( Station station : stations ) {
				if( station.getControllingFaction().isWilderness() )
					continue;
					
				else if( station.getControllingFaction().getComparisonTag().equals( faction.getComparisonTag() ) )
					return station;
				
				else
					continue;
					
			}
			
			return null;
			
		}
	}
	
	public Station containedWithin( Location location ) {
		synchronized( lock ) {
			for( Station station : stations ) {
				if( station.getProtectedRegion().contains( location.getBlockX(), location.getBlockY(), location.getBlockZ() ) )
					return station;
				
				else
					continue;
				
			}
			
			return null;
			
		}
	}
	
	public Station toStation( String name ) {
		synchronized( lock ) {
			for( Station station : stations ) {
				if( station == null || station.getControllingFaction().isWilderness() )
					continue;
					
				else if( station.getName().equals( name ) )
					return station;
			
				else
					continue;
					
			}   
			
			return null;
			
		}
	}
	
	public Station toStation( ProtectedRegion region ) {
		synchronized( lock ) {
			for( Station station : stations )
				if( station.getProtectedRegion().equals( region ) )
					return station;
			
				else
					continue;
			
			return null;
			
		}
	}
	
	public World getWorld() {
		return world;
		
	}
	
	public List<Station> getStations() {
		synchronized( lock ) {
			return stations;
		
		}
	}

}