package tk.sherrao.bukkit.battlestations.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;

import com.massivecraft.factions.Faction;

import tk.sherrao.bukkit.battlestations.BattleStations;
import tk.sherrao.bukkit.battlestations.station.StationManager;
import tk.sherrao.bukkit.utils.plugin.SherEventListener;

public class EntityDeathListener extends SherEventListener {

	protected StationManager stationMgr;
	protected String message, broadcastMsg;
	protected Sound sound, broadcastSound;
	
	public EntityDeathListener( BattleStations pl ) {
		super(pl);
	
		this.stationMgr = pl.getStationManager();
		this.message = pl.getMessagesConfig().getString( "destroy.to-player" );
		this.broadcastMsg = pl.getMessagesConfig().getString( "destroy.broadcast" );
		this.sound = pl.getSoundsConfig().getSound( "destroy.to-player" );
		this.broadcastSound = pl.getSoundsConfig().getSound( "destroy.broadcast" );
		
	}
	
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onEntityDeath( EntityDeathEvent event ) {
		if( event.getEntityType() == EntityType.ZOMBIE ) {
			Faction faction = null;
			if( (faction = stationMgr.coreDestroyed( event.getEntity().getKiller(), event.getEntity().getLocation() ) ) != null ) {
				Player player = event.getEntity().getKiller();
				player.sendMessage( message.replace( "[faction]", faction.getTag() ) );
				pl.playSound( player, sound );
				
				Bukkit.broadcastMessage( broadcastMsg.replace( "[player]", player.getName() ).replace( "[faction]", faction.getTag() ) );
				for( Player p : Bukkit.getOnlinePlayers() ) 
					pl.playSound( p, broadcastSound );
				
			} else
				return;
		
		} else 
			return;
			
		
	}
	
}
