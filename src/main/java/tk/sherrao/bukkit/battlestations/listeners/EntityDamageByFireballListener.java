package tk.sherrao.bukkit.battlestations.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import tk.sherrao.bukkit.battlestations.BattleStations;
import tk.sherrao.bukkit.battlestations.station.Station;
import tk.sherrao.bukkit.battlestations.station.StationManager;
import tk.sherrao.bukkit.utils.plugin.SherEventListener;

public class EntityDamageByFireballListener extends SherEventListener {

	protected StationManager stationMgr;
	protected double damage;
	
	public EntityDamageByFireballListener( BattleStations pl ) {
		super(pl);
		
		this.stationMgr = pl.getStationManager();
		this.damage = pl.getStationsConfig().getInt( "turrets.damage" );
		
	}
	
	@EventHandler( priority = EventPriority.HIGH )
	public void onEntityDamageByFireball( EntityDamageByEntityEvent event ) {
		if( !( event.getDamager() instanceof Fireball ) ) 
			return;

		if( event.getEntityType() == EntityType.ZOMBIE || event.getEntityType() == EntityType.PIG_ZOMBIE ) 
			event.setCancelled( true );
		
		if( event.getEntityType() != EntityType.PLAYER )
			return;
		
		Station station = stationMgr.containedWithin( event.getDamager().getLocation() );
		if( station != null && station.getControllingFaction().getOnlinePlayers().contains( event.getEntity() ) )
			event.setCancelled( true );
		
		else if( station != null )
			event.setDamage( damage );
		
		else
			return;
		
	}
	
}