package tk.sherrao.bukkit.battlestations.station;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPigZombie;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Relation;

import tk.sherrao.bukkit.battlestations.BattleStations;
import tk.sherrao.bukkit.utils.config.SherConfiguration;
import tk.sherrao.bukkit.utils.plugin.SherTask;

public class StationTurret extends SherTask {

	private final class Lock {}
	private final Object lock = new Lock();
	
	protected SherConfiguration stationsConfig;

	protected Station station;
	protected World world;
	protected Location location, fireLocation;

	protected int fireRange;
	protected boolean flammable, bounceable;
	protected double health;
	protected PigZombie entity;
	protected Vector shootAt;
	
	public StationTurret( BattleStations pl, World world, Location location, int fireRange, double health ) {
		super(pl);

		this.stationsConfig = pl.getStationsConfig();

		this.world = world;
		this.location = location.add( .5, .5, .5 );
		this.fireLocation = location.clone().add( 0, 4, 0 );

		this.fireRange = fireRange;
		this.flammable = pl.getStationsConfig().getBoolean( "turrets.flammable" );
		this.bounceable = pl.getStationsConfig().getBoolean( "turrets.bounceable" );
		this.health = health;
		this.shootAt = new Vector();

	}

	public void initEntity( Station station, double health ) {
		this.station = station;
		if( health == 0 ) {
			if( entity != null && !entity.isDead() )
				entity.remove();
				
		} else {
			if( entity == null || entity.isDead() )
				entity = world.spawn( location, PigZombie.class );
			
			entity.addPotionEffect( new PotionEffect( PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true, false ) );
			entity.getEquipment().setHelmet( new ItemStack( Material.BARRIER ) );
			entity.setBaby( false );
			entity.setMaxHealth( this.health );
			entity.setHealth( health );
			entity.setRemoveWhenFarAway( false );
			net.minecraft.server.v1_8_R3.Entity nmsEntity = ( (CraftPigZombie) entity ).getHandle();
			net.minecraft.server.v1_8_R3.NBTTagCompound tag = nmsEntity.getNBTTag();
			if( tag == null )
				tag = new net.minecraft.server.v1_8_R3.NBTTagCompound();

			nmsEntity.c( tag );
			tag.setInt( "NoAI", 1 );
			tag.setInt( "NoGravity", 1 );
			tag.setInt( "Silent", 1 );
			nmsEntity.f( tag );
		
		}
	}

	@Override
	public void fire() {
		synchronized( lock ) {
			if( entity == null || entity.isDead() )
				return;

			Faction faction = station.getControllingFaction();
			for( Entity en : entity.getNearbyEntities( fireRange, fireRange, fireRange ) ) {
				if( en instanceof Player ) {
					Relation rel = faction.getRelationTo( FPlayers.getInstance().getByPlayer( (Player) en ).getFaction() );
					if( rel == Relation.NEUTRAL || rel == Relation.ENEMY ) {
						Vector direction = new Vector( fireLocation.getX() - en.getLocation().getX(),
								fireLocation.getY() - en.getLocation().getY(),
								fireLocation.getZ() - en.getLocation().getZ() ).normalize().multiply( -1 );

						Fireball ball = world.spawn( fireLocation, Fireball.class );
						ball.setDirection( direction );
						ball.setVelocity( direction );
						ball.setIsIncendiary( flammable );
						ball.setBounce( bounceable );
						break;

					} else
						continue;

				} else
					continue;

			}
		}
	}
	
	public void addHealth( double health ) {
		if( entity == null || entity.isDead() )
			initEntity( this.station, health );

		else
			entity.setHealth( ( entity.getHealth() + health > entity.getMaxHealth() ) ? entity.getMaxHealth()
					: entity.getHealth() + health );

	}
	
	public void setHealth( double health ) {
		if( entity == null  || entity.isDead() )
			initEntity( this.station, health );
		
		else
			entity.setHealth( health > entity.getMaxHealth() ? entity.getMaxHealth() : health );
		
	}

	public double getHealth() {
		return entity.getHealth();

	}
	
	public double getMaxHealth() {
		return health;
		
	}

	public boolean isActive() {
		return !entity.isDead();

	}

	public int getFireRange() {
		return fireRange;

	}

	public Location getLocation() {
		return location;

	}

}
