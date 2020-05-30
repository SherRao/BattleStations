package tk.sherrao.bukkit.battlestations.station;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftZombie;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import tk.sherrao.bukkit.battlestations.BattleStations;
import tk.sherrao.bukkit.utils.config.SherConfiguration;
import tk.sherrao.bukkit.utils.plugin.SherPluginFeature;

public class StationCore extends SherPluginFeature {

	protected SherConfiguration stationsConfig;
	
	protected Station station;
	protected World world;
	protected Location location;
	protected Zombie entity;

	protected double health;
	
	public StationCore( BattleStations pl, World world, Location location, double health ) {
		super(pl);
		
		this.stationsConfig = pl.getStationsConfig();
		
		this.world = world;
		this.location = location.add( .5, .5, .5 );
		this.entity = world.spawn( location, Zombie.class );
	
		this.health = health;
		
	}
	
	public void initEntity( Station station, double health ) {
		this.station = station;
		if( entity == null || entity.isDead() )
			entity = world.spawn( location, Zombie.class );
		
		entity.addPotionEffect( new PotionEffect( PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true, false ) );
		entity.getEquipment().setHelmet( new ItemStack( Material.BARRIER ) );
		entity.setBaby( false );
		entity.setMaxHealth( this.health );
		entity.setHealth( health );
		entity.setRemoveWhenFarAway( false );
		net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftZombie) entity).getHandle();
	    net.minecraft.server.v1_8_R3.NBTTagCompound tag = nmsEntity.getNBTTag();
	    if( tag == null )
	    	tag = new net.minecraft.server.v1_8_R3.NBTTagCompound();
	    
	    nmsEntity.c( tag );
	    tag.setInt( "NoAI", 1 );
	    tag.setInt( "NoGravity", 1 );
	    tag.setInt( "Silent", 1 );
	    nmsEntity.f( tag );
		
	} 

	public void addHealth( double health ) {
		if( entity.isDead() )
			initEntity( this.station, health );
		
		else
			entity.setHealth( (entity.getHealth() + health > entity.getMaxHealth()) ?
					entity.getMaxHealth() : entity.getHealth() + health );
		
	}
	
	public double getHealth() {
		return entity.getHealth();
		
	}
	
	public double getMaxHealth() {
		return health;
		
	}
	
	public Location getLocation() {
		return location;
		
	}
	
}