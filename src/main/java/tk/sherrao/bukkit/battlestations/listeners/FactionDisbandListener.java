package tk.sherrao.bukkit.battlestations.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.event.FactionDisbandEvent;

import tk.sherrao.bukkit.battlestations.BattleStations;
import tk.sherrao.bukkit.battlestations.station.Station;
import tk.sherrao.bukkit.battlestations.station.StationManager;
import tk.sherrao.bukkit.utils.plugin.SherEventListener;

public class FactionDisbandListener extends SherEventListener {

	protected StationManager stationMgr;
	
	public FactionDisbandListener( BattleStations pl ) {
		super(pl);
		
		this.stationMgr = pl.getStationManager();
		
	}
	
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onFactionDisband( FactionDisbandEvent event ) {
		Faction faction = event.getFaction();
		for( Station station : stationMgr.getStations() ) {
			if( station.getControllingFaction().getComparisonTag().equals( faction.getComparisonTag() ) )
				station.destroyed( null );
		
			else 
				continue;
		
		}
	}
	
}
