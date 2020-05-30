package tk.sherrao.bukkit.battlestations.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.ChunkUnloadEvent;

import tk.sherrao.bukkit.battlestations.BattleStations;
import tk.sherrao.bukkit.battlestations.station.StationManager;
import tk.sherrao.bukkit.utils.plugin.SherEventListener;

public class WorldChunkUnloadListener extends SherEventListener {

	protected StationManager stationMgr;
	
	public WorldChunkUnloadListener( BattleStations pl ) {
		super(pl);
		
		this.stationMgr = pl.getStationManager();
		
	}
	
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onWorldChunkUnload( ChunkUnloadEvent event ) {
		if( event.getWorld().getUID().equals( stationMgr.getWorld().getUID() ) ) 
			event.setCancelled( true );
		
		else
			return;
		
	}
	
}
