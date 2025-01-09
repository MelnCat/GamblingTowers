package dev.melncat.gamblingtowers.gambling

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import dev.melncat.gamblingtowers.GamblingTowers
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent
import java.util.*

class GamblingListener(val plugin: GamblingTowers) : Listener {
	private val deathPosCache = mutableMapOf<UUID, Location>()
	
	@EventHandler
	fun on(event: EntityExplodeEvent) {
		//event.blockList().clear()
	}
	
	@EventHandler
	fun on(event: EntityChangeBlockEvent) {
		//if (event.to == Material.AIR && (event.entityType == EntityType.WITHER || event.entityType == EntityType.ENDER_DRAGON) ) event.isCancelled = true
	}
	
	@EventHandler
	fun on(event: PlayerDeathEvent) {
		if (event.player.world != plugin.minigameWorld.cbWorld) return
		deathPosCache[event.player.uniqueId] = event.player.location
		val count = plugin.minigameWorld.cbWorld.players.filter { it.gameMode == GameMode.SURVIVAL && !it.isDead }.size
		if (count <= 1) {
			plugin.manager.end(event.player)
		} else {
			Bukkit.getOnlinePlayers().map { p -> 
				p.sendRichMessage("<yellow>${event.player.name}</yellow> has died. <red>${
					count
				}</red> players remain.") }
		}
		
	}
	@EventHandler
	fun on(event: PlayerJoinEvent) {
		if (event.player.world == plugin.minigameWorld.cbWorld && !plugin.manager.ongoing)
			event.player.teleport(Bukkit.getWorlds()[0].spawnLocation)
	}
	
	@EventHandler
	fun on(event: PlayerRespawnEvent) {
		if (event.player.world != plugin.minigameWorld.cbWorld) return
		val a = deathPosCache[event.player.uniqueId] ?: return
		event.respawnLocation = plugin.minigameWorld.cbWorld.spawnLocation
	}
	
	@EventHandler
	fun on(event: PlayerPostRespawnEvent) {
		if (event.player.world != plugin.minigameWorld.cbWorld) return
		event.player.gameMode = GameMode.SPECTATOR
		event.player.isFlying = true
	}
}