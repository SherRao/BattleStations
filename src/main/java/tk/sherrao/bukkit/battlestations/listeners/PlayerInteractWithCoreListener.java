package tk.sherrao.bukkit.battlestations.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import tk.sherrao.bukkit.battlestations.BattleStations;
import tk.sherrao.bukkit.battlestations.station.Station;
import tk.sherrao.bukkit.battlestations.station.StationManager;
import tk.sherrao.bukkit.battlestations.station.StationManager.CoreInteraction;
import tk.sherrao.bukkit.utils.plugin.SherEventListener;
import tk.sherrao.utils.collections.Pair;

public class PlayerInteractWithCoreListener extends SherEventListener {

	protected StationManager stationMgr;
	protected String noFacMsg, alreadyClaimedMsg, triedClaimMsg;
	protected Sound noFacSound, alreadyClaimedSound, triedClaimSound;
	
	public PlayerInteractWithCoreListener( BattleStations pl ) {
		super(pl);
		
		this.stationMgr = pl.getStationManager();
		this.noFacMsg = pl.getMessagesConfig().getString( "claiming.need-faction" );
		this.triedClaimMsg = pl.getMessagesConfig().getString( "claiming.tried-claiming" );

		this.noFacSound = pl.getSoundsConfig().getSound( "claiming.need-faction" );
		this.triedClaimSound = pl.getSoundsConfig().getSound( "claiming.tried-claiming" );

	}
	
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerInteractWithEntity( PlayerInteractEntityEvent event ) {
		Entity clicked = event.getRightClicked();
		if( clicked.getType() != EntityType.ZOMBIE )
			return;
		
		Player player = event.getPlayer();
		Pair<Station, CoreInteraction> result = stationMgr.coreClicked( event.getPlayer(), clicked.getLocation() );
		if( result.getValue() == CoreInteraction.INVALID_ALREADY_CLAIMED ) {
			player.sendMessage( alreadyClaimedMsg );
			pl.playSound( player, alreadyClaimedSound );
			for( Player p : result.getKey().getControllingFaction().getOnlinePlayers() ) {
				p.sendMessage( triedClaimMsg.replace( "[player]", player.getName() ) );
				pl.playSound( p, triedClaimSound );
				
			}
			
		} else if( result.getValue() == CoreInteraction.INVALID_NO_FAC ) {
			player.sendMessage( noFacMsg );
			pl.playSound( player, noFacSound );
			
		} else
			return;
		
	}
	
}