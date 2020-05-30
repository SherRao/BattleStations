package tk.sherrao.bukkit.battlestations;

import static com.massivecraft.factions.FPlayers.getInstance;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.massivecraft.factions.Faction;

import net.milkbowl.vault.economy.Economy;
import tk.sherrao.bukkit.battlestations.station.Station;
import tk.sherrao.bukkit.battlestations.station.StationManager;
import tk.sherrao.bukkit.utils.ItemBuilder;
import tk.sherrao.bukkit.utils.SherGUI;
import tk.sherrao.bukkit.utils.config.SherConfiguration;
import tk.sherrao.bukkit.utils.plugin.SherPluginFeature;
import tk.sherrao.utils.TimeUtils;

public class GuiHolder extends SherPluginFeature {

	protected Economy economy;
	protected SherConfiguration guiConfig, stationConfig, messagesConfig, soundsConfig;
	protected StationManager stationMgr;
	protected SherGUI claimGui, friendlyGui, shieldGui;
	protected String claimGuiTitle, friendlyGuiTitle, shieldGuiTitle;

	
	protected int cGuiButtonSlot, fGuiCoreButtonSlot, fGuiTurretButtonSlot, fGuiShieldButtonSlot;
	protected ItemStack cGuiBackground, fGuiBackground, sGuiBackground,
		cGuiButton, 
		fGuiCoreFullHealthButton, fGuiCoreHealButton, 
		fGuiTurretFullHealthButton, fGuiTurretHealButton,
		fGuiShieldUpButton, fGuiShieldDownButton;

	
	protected int sGuiHalfSlot, sGuiOneSlot, sGuiTwoSlot, sGuiThreeSlot, sGuiSixSlot;
	protected ItemStack sGuiHalfUp, sGuiOneUp, sGuiTwoUp, sGuiThreeUp, sGuiSixUp,
		sGuiHalfDown, sGuiOneDown, sGuiTwoDown, sGuiThreeDown, sGuiSixDown;
	
	
	protected int redemtionItemsNeeded;
	protected ItemStack redemtionItem;
	
	protected long turretHealCooldown, coreHealCooldown;
	protected double healTurretCost, healCoreCost;
	protected String healTurretNoMoneyMsg, healTurretSucceessMsg, healTurretCooldownMsg, healCoreNoMoneyMsg, healCoreSucceessMsg, 
		healCoreCooldownMsg;
	
	protected Sound healTurretNoMoneySound, healTurretSucceessSound, healTurretCooldownSound, healCoreNoMoneySound, healCoreSucceessSound,
		healCoreCooldownSound;

	protected String redeemSuccessMsg, redeemFailureMsg, redeemBroadcastMsg, needFactionMsg, alreadyHaveStationMsg;
	protected Sound redeemSuccessSound, redeemFailureSound, redeemBroadcastSound, needFactionSound, alreadyHaveStationSound;
	
	public GuiHolder( BattleStations pl ) {
		super(pl);

		this.economy = pl.getVaultEconomyPlugin();
		this.guiConfig = pl.getGuiConfiguration();
		this.stationConfig = pl.getStationsConfig();
		this.messagesConfig = pl.getMessagesConfig();
		this.soundsConfig = pl.getSoundsConfig();
		
		this.claimGuiTitle = guiConfig.getString( "claiming-gui.title" );
		this.friendlyGuiTitle = guiConfig.getString( "friendly-gui.title" );
		this.shieldGuiTitle = guiConfig.getString( "shield-gui.title" );

		this.cGuiButtonSlot = guiConfig.getInt( "claiming-gui.items.button.slot" );
		this.fGuiCoreButtonSlot = guiConfig.getInt( "friendly-gui.items.core.slot" );
		this.fGuiTurretButtonSlot = guiConfig.getInt( "friendly-gui.items.turrets.slot" );
		this.fGuiShieldButtonSlot = guiConfig.getInt( "friendly-gui.items.shield.slot" );
		this.cGuiBackground = new ItemBuilder( guiConfig.getMaterial( "claiming-gui.items.background.item" ) )
				.setName( guiConfig.getString( "claiming-gui.items.background.title" ) )
				.setLore( guiConfig.getStringList( "claiming-gui.items.background.lore" ) )
				.toItemStack();
		this.fGuiBackground = new ItemBuilder( guiConfig.getMaterial( "friendly-gui.items.background.item" ) )
				.setName( guiConfig.getString( "friendly-gui.items.background.title" ) )
				.setLore( guiConfig.getStringList( "friendly-gui.items.background.lore" ) )
				.toItemStack();
		this.sGuiBackground = new ItemBuilder( guiConfig.getMaterial( "shield-gui.items.background.item" ) )
				.setName( guiConfig.getString( "shield-gui.items.background.title" ) )
				.setLore( guiConfig.getStringList( "shield-gui.items.background.lore" ) )
				.toItemStack();
		
		
		this.cGuiButton = new ItemBuilder( guiConfig.getMaterial( "claiming-gui.items.button.item" ) )
				.setName( guiConfig.getString( "claiming-gui.items.button.title" ) )
				.setLore( guiConfig.getStringList( "claiming-gui.items.button.lore" ) )
				.toItemStack();
		
		
		this.fGuiCoreFullHealthButton = new ItemBuilder( guiConfig.getMaterial( "friendly-gui.items.core.full-health.item" ) )
				.setName( guiConfig.getString( "friendly-gui.items.core.full-health.title" ) )
				.setLore( guiConfig.getStringList( "friendly-gui.items.core.full-health.lore" ) )
				.toItemStack();
		this.fGuiCoreHealButton = new ItemBuilder( guiConfig.getMaterial( "friendly-gui.items.core.heal.item" ) )
				.setName( guiConfig.getString( "friendly-gui.items.core.heal.title" ) )
				.setLore( guiConfig.getStringList( "friendly-gui.items.core.heal.lore" ) )
				.toItemStack();
		
		
		this.fGuiTurretFullHealthButton = new ItemBuilder( guiConfig.getMaterial( "friendly-gui.items.turrets.full-health.item" ) )
				.setName( guiConfig.getString( "friendly-gui.items.turrets.full-health.title" ) )
				.setLore( guiConfig.getStringList( "friendly-gui.items.turrets.full-health.lore" ) )
				.toItemStack();
		this.fGuiTurretHealButton = new ItemBuilder( guiConfig.getMaterial( "friendly-gui.items.turrets.heal.item" ) )
				.setName( guiConfig.getString( "friendly-gui.items.turrets.heal.title" ) )
				.setLore( guiConfig.getStringList( "friendly-gui.items.turrets.heal.lore" ) )
				.toItemStack();
		
		
		this.fGuiShieldUpButton = new ItemBuilder( guiConfig.getMaterial( "friendly-gui.items.shield.up.item" ) )
				.setName( guiConfig.getString( "friendly-gui.items.shield.up.title" ) )
				.setLore( guiConfig.getStringList( "friendly-gui.items.shield.up.lore" ) )
				.toItemStack();
		this.fGuiShieldDownButton = new ItemBuilder( guiConfig.getMaterial( "friendly-gui.items.shield.down.item" ) )
				.setName( guiConfig.getString( "friendly-gui.items.shield.down.title" ) )
				.setLore( guiConfig.getStringList( "friendly-gui.items.shield.down.lore" ) )
				.toItemStack();
		
		
		
		this.sGuiHalfSlot = guiConfig.getInt( "shield-gui.items.half.slot" );
		this.sGuiOneSlot = guiConfig.getInt( "shield-gui.items.one.slot" );
		this.sGuiTwoSlot = guiConfig.getInt( "shield-gui.items.two.slot" );
		this.sGuiThreeSlot = guiConfig.getInt( "shield-gui.items.three.slot" );
		this.sGuiSixSlot = guiConfig.getInt( "shield-gui.items.six.slot" );
		this.sGuiHalfUp = new ItemBuilder( guiConfig.getMaterial( "shield-gui.items.half.up.item" ) )
				.setName( guiConfig.getString( "shield-gui.items.half.up.title" ) )
				.setLore( guiConfig.getStringList( "shield-gui.items.half.up.lore" ) )
				.toItemStack();
		this.sGuiHalfDown = new ItemBuilder( guiConfig.getMaterial( "shield-gui.items.half.down.item" ) )
				.setName( guiConfig.getString( "shield-gui.items.half.down.title" ) )
				.setLore( guiConfig.getStringList( "shield-gui.items.half.down.lore" ) )
				.toItemStack();
		
		
		this.sGuiOneUp = new ItemBuilder( guiConfig.getMaterial( "shield-gui.items.one.up.item" ) )
				.setName( guiConfig.getString( "shield-gui.items.one.up.title" ) )
				.setLore( guiConfig.getStringList( "shield-gui.items.one.up.lore" ) )
				.toItemStack();
		this.sGuiOneDown = new ItemBuilder( guiConfig.getMaterial( "shield-gui.items.one.down.item" ) )
				.setName( guiConfig.getString( "shield-gui.items.one.down.title" ) )
				.setLore( guiConfig.getStringList( "shield-gui.items.one.down.lore" ) )
				.toItemStack();
		
		
		this.sGuiTwoUp = new ItemBuilder( guiConfig.getMaterial( "shield-gui.items.two.up.item" ) )
				.setName( guiConfig.getString( "shield-gui.items.two.up.title" ) )
				.setLore( guiConfig.getStringList( "shield-gui.items.two.up.lore" ) )
				.toItemStack();
		this.sGuiTwoDown = new ItemBuilder( guiConfig.getMaterial( "shield-gui.items.two.down.item" ) )
				.setName( guiConfig.getString( "shield-gui.items.two.down.title" ) )
				.setLore( guiConfig.getStringList( "shield-gui.items.two.down.lore" ) )
				.toItemStack();
		
		
		this.sGuiThreeUp = new ItemBuilder( guiConfig.getMaterial( "shield-gui.items.three.up.item" ) )
				.setName( guiConfig.getString( "shield-gui.items.three.up.title" ) )
				.setLore( guiConfig.getStringList( "shield-gui.items.three.up.lore" ) )
				.toItemStack();
		this.sGuiThreeDown = new ItemBuilder( guiConfig.getMaterial( "shield-gui.items.three.down.item" ) )
				.setName( guiConfig.getString( "shield-gui.items.three.down.title" ) )
				.setLore( guiConfig.getStringList( "shield-gui.items.three.down.lore" ) )
				.toItemStack();
		
		
		this.sGuiSixUp = new ItemBuilder( guiConfig.getMaterial( "shield-gui.items.six.up.item" ) )
				.setName( guiConfig.getString( "shield-gui.items.six.up.title" ) )
				.setLore( guiConfig.getStringList( "shield-gui.items.six.up.lore" ) )
				.toItemStack();
		this.sGuiSixDown = new ItemBuilder( guiConfig.getMaterial( "shield-gui.items.six.down.item" ) )
				.setName( guiConfig.getString( "shield-gui.items.six.down.title" ) )
				.setLore( guiConfig.getStringList( "shield-gui.items.six.down.lore" ) )
				.toItemStack();
		
		
		
		this.claimGui = new SherGUI( pl, claimGuiTitle, guiConfig.getInt( "claiming-gui.rows" ) * 9 );
		claimGui.setEmptySlotType( cGuiBackground );
		claimGui.setClickableItems( false );
		
		this.friendlyGui = new SherGUI( pl, friendlyGuiTitle, guiConfig.getInt( "friendly-gui.rows" ) * 9 );
		friendlyGui.setEmptySlotType( fGuiBackground );
		friendlyGui.setClickableItems( false );
		
		this.shieldGui = new SherGUI( pl, shieldGuiTitle, guiConfig.getInt( "shield-gui.rows" ) * 9 );
		shieldGui.setEmptySlotType( sGuiBackground );
		shieldGui.setClickableItems( false );
		
		
		this.redemtionItemsNeeded = stationConfig.getInt( "claim-item.amount-needed" );
		this.redemtionItem = pl.getRedemtionItem();
		this.turretHealCooldown = pl.getStationsConfig().getInt( "healing.turret-cooldown" ) * 1000;
		this.coreHealCooldown = pl.getStationsConfig().getInt( "healing.core-cooldown" ) * 1000;
		this.healTurretCost = stationConfig.getInt( "healing.price-per-turret-hp-point" );
		this.healCoreCost = stationConfig.getInt( "healing.price-per-core-hp-point" );
		
		this.healTurretNoMoneyMsg = messagesConfig.getString( "healing.turret-failure" );
		this.healTurretSucceessMsg = messagesConfig.getString( "healing.turret-success" );
		this.healTurretCooldownMsg = messagesConfig.getString( "healing.turret-cooldown" );
		this.healCoreNoMoneyMsg = messagesConfig.getString( "healing.core-failure" );
		this.healCoreSucceessMsg = messagesConfig.getString( "healing.core-success" );
		this.healCoreCooldownMsg = messagesConfig.getString( "healing.core-cooldown" );
		
		this.healTurretNoMoneySound = soundsConfig.getSound( "healing.turret-failure" );
		this.healTurretSucceessSound = soundsConfig.getSound( "healing.turret-success" );
		this.healTurretCooldownSound = soundsConfig.getSound( "healing.turret-cooldown" );
		this.healCoreNoMoneySound = soundsConfig.getSound( "healing.core-failure" );
		this.healCoreSucceessSound = soundsConfig.getSound( "healing.core-success" );
		this.healCoreCooldownSound = soundsConfig.getSound( "healing.core-cooldown" );

		this.redeemSuccessMsg = messagesConfig.getString( "claiming.success" );
		this.redeemFailureMsg = messagesConfig.getString( "claiming.failure" );
		this.redeemBroadcastMsg = messagesConfig.getString( "claiming.broadcast" );
		this.needFactionMsg = messagesConfig.getString( "claiming.need-faction" );
		this.alreadyHaveStationMsg = messagesConfig.getString( "claiming.already-have-station" ); 
		
		this.redeemSuccessSound = soundsConfig.getSound( "claiming.success" );
		this.redeemFailureSound = soundsConfig.getSound( "claiming.failure" );
		this.redeemBroadcastSound = soundsConfig.getSound( "claiming.broadcast" );
		this.needFactionSound = soundsConfig.getSound( "claiming.need-faction" );
		this.alreadyHaveStationSound = soundsConfig.getSound( "claiming.already-have-station" ); 

	}

	public void a() {
		this.stationMgr = ((BattleStations) pl).getStationManager();
		
	}
	
	public void claimGui( Player player, Station station ) {
		claimGui.clearItems();
		claimGui.setTitle( claimGuiTitle );
		claimGui.addItem( cGuiButton, cGuiButtonSlot );
		claimGui.doOnClickOf( cGuiButtonSlot, ( event ) -> {
			Faction faction = getInstance().getByPlayer( player ).getFaction();
			if( stationMgr.hasStation( faction ) == null ) {
				int amount = 0;
				List<Integer> slots = new ArrayList<>(9);
				PlayerInventory inv = player.getInventory();
				for( ItemStack item : inv ) {
					if( item == null || item.getType() == Material.AIR )
						continue;

					else if( itemEquals( item ) ) {
						slots.add( inv.first( item ) );
						amount += item.getAmount();

					} else
						continue;

				}

				if( amount >= redemtionItemsNeeded ) {
					if( !faction.isWilderness() ) {
						station.claimed( faction );
						station.charge( player, 1 ); //30 );
						for( Integer slot : slots )
							inv.clear( slot.intValue() );

						if( amount > 9 ) {
							ItemStack is = redemtionItem.clone();
							is.setAmount( amount - 9 );
							inv.addItem( is );

						}

						player.closeInventory();
						friendlyGui( player, station );

						player.sendMessage( redeemSuccessMsg );
						pl.playSound( player, redeemSuccessSound );

						Bukkit.broadcastMessage( redeemBroadcastMsg.replace( "[player]", player.getName() )
								.replace( "[faction]", faction.getTag() ) );
						for( Player p : Bukkit.getOnlinePlayers() )
							pl.playSound( p, redeemBroadcastSound );

					} else {
						player.sendMessage( needFactionMsg );
						pl.playSound( player, needFactionSound );

					}

				} else {
					player.sendMessage( redeemFailureMsg );
					pl.playSound( player, redeemFailureSound );

				}
				
			} else {
				player.sendMessage( alreadyHaveStationMsg );
				pl.playSound( player, alreadyHaveStationSound );

			}

		} );
		
		claimGui.open( player );
		
	}
	
	public void friendlyGui( Player player, Station station ) {
		initReplace( station );
		friendlyGui.setTitle( friendlyGuiTitle.replace( "[faction]", station.getControllingFaction().getTag() ) );
		friendlyGui.clearItems();
		if( station.getCoreHealth() == station.getCoreMaxHealth() ) 
			friendlyGui.addItem( processGuiItem( fGuiCoreFullHealthButton, station ), fGuiCoreButtonSlot );
			
		else {
			friendlyGui.addItem( processGuiItem( fGuiCoreHealButton, station ), fGuiCoreButtonSlot );
			friendlyGui.doOnClickOf( fGuiCoreButtonSlot, ( event ) -> {
				double health = station.getCoreMaxHealth() - station.getCoreHealth();
				double cost = health * healCoreCost;
				if( economy.withdrawPlayer( player, cost ).transactionSuccess() ) {
					if( TimeUtils.isTimedOut( station.getLastCoreHeal(), coreHealCooldown ) ) {
						this.coreHealCooldown = System.currentTimeMillis();
						player.sendMessage( healCoreSucceessMsg.replace( "[health-gained]", String.valueOf( health ) )
								.replace( "[cost]", String.valueOf( cost ) ) );

						pl.playSound( player, healCoreSucceessSound );
						station.healCore();
						player.closeInventory();
						friendlyGui( player, station );

					} else {
						player.sendMessage( healCoreCooldownMsg );
						pl.playSound( player, healCoreCooldownSound );

					}

				} else {
					player.sendMessage( healCoreNoMoneyMsg.replace( "[cost]", String.valueOf( economy.getBalance( player ) - cost ) ) );
					pl.playSound( player, healCoreNoMoneySound );

				}

			} );
		}
		
		
		if( station.getTurretsCollectiveHealth() == station.getTurretsCollectiveMaxHealth() ) 
			friendlyGui.addItem( processGuiItem( fGuiTurretFullHealthButton, station ), fGuiTurretButtonSlot );
			
		else {
			friendlyGui.addItem( processGuiItem( fGuiTurretHealButton, station ),  fGuiTurretButtonSlot );
			friendlyGui.doOnClickOf( fGuiTurretButtonSlot, ( event ) -> {
				double health = station.getTurretsCollectiveMaxHealth() - station.getTurretsCollectiveHealth();
				double cost = health * healTurretCost;
				if( economy.withdrawPlayer( player, cost ).transactionSuccess() ) {
					if( TimeUtils.isTimedOut( station.getLastTurretHeal(), turretHealCooldown ) ) {
						this.turretHealCooldown = System.currentTimeMillis();
						player.sendMessage( healTurretSucceessMsg.replace( "[health-gained]", String.valueOf( health ) )
								.replace( "[cost]", String.valueOf( cost ) ) );

						pl.playSound( player, healTurretSucceessSound );
						station.healTurrets();
						player.closeInventory();
						friendlyGui( player, station );
					} else {
						player.sendMessage( healTurretCooldownMsg );
						pl.playSound( player, healTurretCooldownSound );
					
					}

				} else {
					player.sendMessage( healTurretNoMoneyMsg.replace( "[cost]",
							String.valueOf( economy.getBalance( player ) - cost ) ) );
					pl.playSound( player, healTurretNoMoneySound );

				}
			} );
		}
		
		
		if( station.getChargeTime() == 0 ) {
			friendlyGui.addItem( processGuiItem( fGuiShieldUpButton, station ), fGuiShieldButtonSlot );
			friendlyGui.doOnClickOf( fGuiShieldButtonSlot, (event) -> {
				shieldGui( player, station );
				
			} );
			
		} else { 
			friendlyGui.addItem( processGuiItem( fGuiShieldDownButton, station ), fGuiShieldButtonSlot );
			friendlyGui.doOnClickOf( fGuiShieldButtonSlot, (event) -> {} );
			
		}
		
		friendlyGui.open( player );
	
	}
	
	public void shieldGui( Player player, Station station ) {
		initReplace( station );
		shieldGui.clearItems();
		shieldGui.setTitle( shieldGuiTitle.replace( "[faction]", station.getControllingFaction().getTag() ) );
		shieldGui.addItem( processGuiItem( sGuiHalfDown, station ), sGuiHalfSlot );
		shieldGui.doOnClickOf( sGuiHalfSlot, ( event ) -> {
			station.charge( player, 30 );
			player.closeInventory();
			friendlyGui( player, station );

		} );

		shieldGui.addItem( processGuiItem( sGuiOneDown, station ), sGuiOneSlot );
		shieldGui.doOnClickOf( sGuiOneSlot, ( event ) -> {
			station.charge( player, 60 );
			player.closeInventory();
			friendlyGui( player, station );

		} );

		shieldGui.addItem( processGuiItem( sGuiTwoDown, station ), sGuiTwoSlot );
		shieldGui.doOnClickOf( sGuiTwoSlot, ( event ) -> {
			station.charge( player, 120 );
			player.closeInventory();
			friendlyGui( player, station );

		} );

		shieldGui.addItem( processGuiItem( sGuiThreeDown, station ), sGuiThreeSlot );
		shieldGui.doOnClickOf( sGuiThreeSlot, ( event ) -> {
			station.charge( player, 180 );
			player.closeInventory();
			friendlyGui( player, station );

		} );

		shieldGui.addItem( processGuiItem( sGuiSixDown, station ), sGuiSixSlot );
		shieldGui.doOnClickOf( sGuiSixSlot, ( event ) -> {
			station.charge( player, 360 );
			player.closeInventory();
			friendlyGui( player, station );
			
		} );

		shieldGui.open( player );

	}
	
	private boolean itemEquals( ItemStack first ) {
		return first.getType() == redemtionItem.getType() && 
				first.getDurability() == redemtionItem.getDurability() && 
				first.getItemMeta().getDisplayName().equals( redemtionItem.getItemMeta().getDisplayName() ) &&
				first.getItemMeta().getLore().equals( redemtionItem.getItemMeta().getLore() );
		
	}
	
	private ItemStack processGuiItem( ItemStack item, Station station ) {
		ItemStack it = item.clone();
		ItemMeta im = it.getItemMeta();
		List<String> lore = new ArrayList<String>( item.getItemMeta().getLore().size() );
		for( String line : item.getItemMeta().getLore() )
			lore.add( replace( line ) );
			
		im.setDisplayName( replace( item.getItemMeta().getDisplayName() ) );
		im.setLore( lore );
		it.setItemMeta( im );
		return it;
				
	}

	private String coreMaxHealth, coreHealth, turretsMaxHealth, turretsHealth, shieldTime, chargeTime;
	private void initReplace( Station station ) {
		coreMaxHealth = String.valueOf( station.getCoreMaxHealth() );
		coreMaxHealth = coreMaxHealth.substring( 0, coreMaxHealth.indexOf( "." ) + 2 );
		
		coreHealth = String.valueOf( station.getCoreHealth() );
		coreHealth = coreHealth.substring( 0, coreHealth.indexOf( "." ) + 2 );
		
		turretsMaxHealth = String.valueOf( station.getTurretsCollectiveMaxHealth() );
		turretsMaxHealth = turretsMaxHealth.substring( 0, turretsMaxHealth.indexOf( "." ) + 2 );
		
		turretsHealth = String.valueOf( station.getTurretsCollectiveHealth() );
		turretsHealth = turretsHealth.substring( 0, turretsHealth.indexOf( "." ) + 2 );
		
		shieldTime = String.valueOf( (double) station.getShieldTime() / 60 );
		shieldTime = shieldTime.substring( 0, shieldTime.indexOf( "." ) + 2 );
		
		chargeTime = String.valueOf( (double) station.getChargeTime() / 60 );		
		chargeTime = chargeTime.substring( 0, chargeTime.indexOf( "." ) + 2 );

	}
	
	private String replace( String str ) {
		return str.replace( "[core-max-health]", coreMaxHealth ) 
				.replace( "[core-health]", coreHealth )
				.replace( "[turrets-max-health]", turretsMaxHealth )
				.replace( "[turrets-health]", turretsHealth )
				.replace( "[shield-time]", shieldTime )
				.replace( "[charge-time]", chargeTime );

	}
	
}