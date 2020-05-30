package tk.sherrao.bukkit.battlestations.station;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import tk.sherrao.bukkit.battlestations.BattleStations;
import tk.sherrao.bukkit.utils.plugin.SherPluginFeature;
import tk.sherrao.utils.collections.Pair;
import tk.sherrao.utils.strings.StringMultiJoiner;

public class Station extends SherPluginFeature implements Runnable {

	private final class Lock {};
	private final Object lock = new Lock();

	protected BukkitTask task;
	
	protected String name;
	protected CuboidRegion region;
	protected ProtectedRegion protRegion;
	protected World world;
	protected String location;
	
	protected Faction faction;
	protected StationCore core;
	protected List<StationTurret> turrets;
	
	protected boolean charging;
	protected int shieldTime, chargeTime, fireRate;
	protected String turretNameTag, coreNameTag;
	
	protected List< Pair<Integer, Integer> > boostTimes;
	protected double regularMultiplier, boostMultiplier;
	protected boolean boost;
	protected long lastTurretHeal, lastCoreHeal;
	
	protected String shieldUpMsg, shieldDownMsg, bcShieldUpMsg, bcShieldDownMsg;
	protected Sound shieldUpSound, shieldDownSound, bcShieldUpSound, bcShieldDownSound;
	
	public Station( BattleStations pl, String name, World world, CuboidRegion region, ProtectedRegion protRegion, StationCore core, List<StationTurret> turrets ) {
		super(pl);
		
		this.name = name;
		this.region = region;
		this.protRegion = protRegion;
		this.world = world;
		this.location = new StringMultiJoiner( ", ", "( ", " )" ).add( String.valueOf( core.getLocation().getBlockX() ) )
				.add( String.valueOf( core.getLocation().getBlockY() ) )
				.add( String.valueOf( core.getLocation().getBlockZ() ) )
				.toString();
				
		this.faction = Factions.getInstance().getWilderness();
		this.core = core;
		this.turrets = turrets;
	
		for( Entity en : world.getNearbyEntities( BukkitUtil.toLocation( world, region.getCenter() ), 
				region.getWidth(), region.getHeight(), region.getLength() ) ) {
			if( en instanceof LivingEntity && !(en instanceof Player) )
				((LivingEntity) en).remove();
			
			else
				continue;
			
		}
		
		this.fireRate = pl.getStationsConfig().getInt( "turrets.fire-rate" );
		this.turretNameTag = pl.getStationsConfig().getString( "turrets.name-tag" );
		this.coreNameTag = pl.getStationsConfig().getString( "core.name-tag" );

		this.boostTimes = new ArrayList<>();
		this.regularMultiplier = pl.getRewardsConfig().getDouble( "boosts.regular-multiplier" );
		this.boostMultiplier = pl.getRewardsConfig().getDouble( "boosts.boost-multiplier" );
		this.boost = false;

		this.shieldUpMsg = pl.getMessagesConfig().getString( "shield.up" );
		this.shieldDownMsg = pl.getMessagesConfig().getString( "shield.down" );
		this.shieldUpSound = pl.getSoundsConfig().getSound( "shield.up" );
		this.shieldDownSound = pl.getSoundsConfig().getSound( "shield.down" );
		
		this.bcShieldUpMsg = pl.getMessagesConfig().getString( "shield.bc-up" );
		this.bcShieldDownMsg = pl.getMessagesConfig().getString( "shield.bc-down" );
		this.bcShieldUpSound = pl.getSoundsConfig().getSound( "shield.bc-up" );
		this.bcShieldDownSound = pl.getSoundsConfig().getSound( "shield.bc-down" );
		
		Bukkit.dispatchCommand( Bukkit.getConsoleSender(), "region flag -w " + world.getName() + " " + name + " entry allow" );
		protRegion.getMembers().removeAll();
		core.initEntity( this, 100 );
		
		for( String str : pl.getRewardsConfig().getStringList( "boosts.times" ) ) {
			try {
				String[] ints = str.split( "-" );
				boostTimes.add( Pair.from( Integer.valueOf( ints[0] ), Integer.valueOf( ints[0] ) ) );
				continue;
				
			} catch( NumberFormatException | ArrayIndexOutOfBoundsException e ) { continue; }
		}
	}
	
	@Override
	public void run() {
		synchronized( lock ) {
			int hour = Calendar.getInstance().get( Calendar.HOUR_OF_DAY );
			for( Pair<Integer, Integer> pair : boostTimes ) {
				if( pair.getKey() == hour ) {
					boost = true;
					
				} else if( pair.getValue() == hour ) {
					boost = false;
					
				} else 
					continue;
				
			}
			
			for( StationTurret turret : turrets ) {
				turret.fire();
				turret.entity.setCustomNameVisible( true );
				turret.entity.setCustomName( turretNameTag.replace( "[health]", String.valueOf( turret.getHealth() ) )
					.replace( "[total-health]", String.valueOf( turret.getMaxHealth() ) ));
			
			}
			
			core.entity.setCustomNameVisible( true );
			core.entity.setCustomName( coreNameTag.replace( "[health]", String.valueOf( core.getHealth() ) )
					.replace( "[total-health]", String.valueOf( core.getMaxHealth() ) ));
			
			if( shieldTime >= 172800 )
				return;
				
			else if( charging ) {
				if( chargeTime != 0 ) {
					chargeTime -= fireRate;
					if( boost )
						shieldTime += fireRate * boostMultiplier;
						
					else
						shieldTime += fireRate * regularMultiplier;
						
				} else {
					for( Player p : Bukkit.getOnlinePlayers() ) {
						if( faction.getOnlinePlayers().contains(p) ) {
							p.sendMessage( shieldUpMsg.replace( "[shield-time]", String.valueOf( (double) getShieldTime() / 60 ) ) 
									.replace( "[faction]", faction.getTag() ) );
							pl.playSound( p, shieldUpSound );
							
						} else {
							p.sendMessage( bcShieldUpMsg.replace( "[shield-time]", String.valueOf( (double) getShieldTime() / 60 ) )
									.replace( "[faction]", faction.getTag() ) );

							pl.playSound( p, bcShieldUpSound );
						}
					}
					
					charging = false;
				
				}
			
			} else if( shieldTime == 0 && chargeTime == 0 )
				charge( null, 30 );
				
			else {
				shieldTime -= fireRate;
				for( Entity en : world.getNearbyEntities( core.getLocation(), region.getWidth() / 2, region.getHeight() / 2, region.getLength() / 2 ) ) {
					if( !( en instanceof Player) )
						continue;
					
					if( this.faction.getOnlinePlayers().contains( Bukkit.getPlayer( en.getUniqueId() ) ) )
						continue;
					
					else
						en.setVelocity( en.getLocation().subtract( (double) region.getWidth() + 10, 0, (double) region.getLength() + 10 ).toVector() );
					
				}
			}	
		}	
	}
	
	public void charge( Player player, int minutes ) {
		synchronized( lock ) {
			chargeTime = minutes * 60;
			charging = true;
			for( Player p : Bukkit.getOnlinePlayers() ) {
				if( faction.getOnlinePlayers().contains(p) ) {
					p.sendMessage( shieldDownMsg.replace( "[charge-time]", String.valueOf( (double) getChargeTime() / 60 ) )
							.replace( "[faction]", faction.getTag() ) );
					pl.playSound( p, shieldDownSound );
					
				} else {
					p.sendMessage( bcShieldDownMsg.replace( "[charge-time]", String.valueOf( (double) getChargeTime() / 60 ) )
							.replace( "[faction]", faction.getTag() ) );
					pl.playSound( p, bcShieldDownSound );
				
				}
			}
		}
	}
	
	public void claimed( Faction faction ) {
		synchronized( lock ) {
			this.faction = faction;
			
			shieldTime = 0;
			chargeTime = 0;
			core.initEntity( this, core.getMaxHealth() );
			for( StationTurret turret : turrets ) 
				turret.initEntity( this, turret.getMaxHealth() );
			
			if( task != null && Bukkit.getScheduler().isCurrentlyRunning( task.getTaskId() ) ) 
				this.task.cancel();
			
			this.task = Bukkit.getScheduler().runTaskTimer( pl, this, 0, 20 * fireRate );

			Vector tempEntityVector = new Vector();
			for( Entity en : world.getEntities() ) {
				if( en instanceof Player && !FPlayers.getInstance().getByPlayer( (Player) en ).getFaction().getComparisonTag().equals( getControllingFaction().getComparisonTag() ) ) {
					tempEntityVector = tempEntityVector.setX( en.getLocation().getX() );
					tempEntityVector = tempEntityVector.setY( en.getLocation().getY() );
					tempEntityVector = tempEntityVector.setZ( en.getLocation().getZ() );
					if( region.contains( tempEntityVector ) && !(en instanceof Player) )
						en.setVelocity( en.getLocation().subtract( (double) region.getWidth() + 10, 0, (double) region.getLength() + 10 ).toVector() );
				
					else
						continue;
				
				} else
					continue;
				
			}
		}
	}
	
	public void destroyed( Faction faction ) {
		synchronized( lock ) {
			protRegion.getMembers().removeAll();
			
			this.faction = Factions.getInstance().getWilderness();
			shieldTime = 0;
			core.initEntity( null, core.getMaxHealth() );
			for( StationTurret turret : turrets ) 
				turret.initEntity( null, 0 );
			
			this.task.cancel();
			this.task = null;
			
		}
	}
	
	public void addTime( long time ) {
		synchronized( lock ) {
			this.shieldTime += time;
				
		}
	}
	
	public void healCore() {
		synchronized( lock ) {
			core.addHealth( core.getMaxHealth() - core.getHealth() );
				
		}
	}
	
	public void healTurrets() {
		synchronized( lock ) {
			for( StationTurret turret : turrets ) 
				turret.addHealth( turret.getMaxHealth() - turret.getHealth() );
				
		}
	}
	
	public boolean isShieldUp() {
		synchronized( lock ) {
			return shieldTime != 0;
			
		}
	}
	
	public int getShieldTime() {
		synchronized( lock ) {
			return shieldTime;
		
		}
	}
	
	public int getChargeTime() {
		synchronized( lock ) {
			return chargeTime;
			
		}
	}

	public double getCoreHealth() {
		return core.getHealth();
		
	}
	
	public double getCoreMaxHealth() {
		return core.getMaxHealth();
		
	}
	
	public double getTurretsCollectiveHealth() {
		synchronized( lock ) {
			int h = 0;
			for( StationTurret turret : turrets )
				h += turret.getHealth();
			
			return h;
			
		}
	}
	
	public double getTurretsCollectiveMaxHealth() {
		synchronized( lock ) {
			int h = 0;
			for( StationTurret turret : turrets )
				h += turret.getMaxHealth();
			
			return h;
			
		}
	}

	public String getName() {
		return name;
		
	}
	
	public CuboidRegion getRegion() {
		return region;
		
	}
	
	public ProtectedRegion getProtectedRegion() {
		return protRegion;
		
	}
	
	public String location() {
		return location;
		
	}
	
	public Faction getControllingFaction() {
		return faction;
		
	}
	
	public StationCore getCore() {
		return core;
		
	}
	
	public List<StationTurret> getTurrets() {
		return turrets;
		
	}
	
	public long getLastTurretHeal() {
		return lastTurretHeal;
		
	}
	
	public long getLastCoreHeal() {
		return lastCoreHeal;
		
	}

}