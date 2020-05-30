package tk.sherrao.bukkit.battlestations.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import tk.sherrao.bukkit.battlestations.BattleStations;
import tk.sherrao.bukkit.battlestations.station.Station;
import tk.sherrao.bukkit.battlestations.station.StationManager;
import tk.sherrao.bukkit.battlestations.station.StationTurret;
import tk.sherrao.bukkit.utils.plugin.SherEventListener;

public class EntityDamageByPlayerListener extends SherEventListener {

	protected StationManager stationMgr;
	
	public EntityDamageByPlayerListener( BattleStations pl ) {
		super(pl);
		
		this.stationMgr = pl.getStationManager();
		
	}
	
	@EventHandler( priority = EventPriority.HIGH )
	public void onEntityDamageByPlayer( EntityDamageByEntityEvent event ) {
		if( !( event.getDamager() instanceof Player ) ) 
			return;

		if( event.getEntityType() == EntityType.ZOMBIE || event.getEntityType() == EntityType.PIG_ZOMBIE ) {
			Player player = (Player) event.getDamager();
			Entity entity = event.getEntity();
			for( Station station : stationMgr.getStations() ) {
				if( station.getCore().getLocation().equals( entity.getLocation() ) ) {
					if( station.getControllingFaction().getOnlinePlayers().contains( player ) || station.getControllingFaction().isWilderness() )
						event.setCancelled( true );

					else
						continue;

				} else {
					for( StationTurret turret : station.getTurrets() ) {
						if( turret.getLocation().equals( entity.getLocation() ) )
							if( station.getControllingFaction().getOnlinePlayers().contains( player ) || station.getControllingFaction().isWilderness() )
								event.setCancelled( true );

							else
								continue;

						else
							continue;

					}
				}
			}
		}
	}
	
}