package tk.sherrao.bukkit.battlestations.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.mewin.WGRegionEvents.events.RegionEnterEvent;

import tk.sherrao.bukkit.battlestations.BattleStations;
import tk.sherrao.bukkit.battlestations.station.Station;
import tk.sherrao.bukkit.battlestations.station.StationManager;
import tk.sherrao.bukkit.utils.plugin.SherEventListener;

public class PlayerRegionEnterListener extends SherEventListener {

	protected StationManager stationMgr;
	protected String message;
	protected Sound sound;
	
	public PlayerRegionEnterListener( BattleStations pl ) {
		super(pl);
		
		this.stationMgr = pl.getStationManager();
		this.message = pl.getMessagesConfig().getString( "shield.trying-to-enter" );
		this.sound = pl.getSoundsConfig().getSound( "shield.trying-to-enter" );

	}
	
	@EventHandler( priority = EventPriority.HIGH )
	public void onPlayerRegionEnter( RegionEnterEvent event ) {
		Station station = stationMgr.toStation( event.getRegion() );
		Player player = event.getPlayer();
		if( station == null )
			return;
		
		else if( station.getControllingFaction().isWilderness() )
			return;
		
		else if( station.getControllingFaction().getOnlinePlayers().contains( player ) )
			return;
		
		else if( station.getChargeTime() != 0 )
			return;
		
		else {
			player.setVelocity( player.getLocation().subtract( (double) station.getRegion().getWidth() + 10, 0, (double) station.getRegion().getLength() + 10 ).toVector() );
			player.sendMessage( message.replace( "[faction]", station.getControllingFaction().getTag() ) );
			pl.playSound( player, sound );
		
		}
		
	}

}