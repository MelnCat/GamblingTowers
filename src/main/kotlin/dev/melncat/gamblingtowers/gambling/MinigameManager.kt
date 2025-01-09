package dev.melncat.gamblingtowers.gambling

import dev.melncat.gamblingtowers.GamblingTowers
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MinigameManager(val plugin: GamblingTowers) {
	var players: MutableList<UUID> = mutableListOf()
	var positions: MutableList<Triple<Int, Int, Int>> = mutableListOf()
	var ongoing = false
	var startTime = 0L
	fun clearWorld() {
		val world = plugin.minigameWorld
		world.cbWorld.players.map { it.teleport(Bukkit.getWorlds()[0].spawnLocation) }
		plugin.multiverse.mvWorldManager.regenWorld("minigame", false, false, "")
		plugin.minigameWorld = plugin.multiverse.mvWorldManager.getMVWorld("minigame")
	}
	
	fun start() {
		if (ongoing) return
		startTime = System.currentTimeMillis()
		val world = plugin.minigameWorld
		for (i in players.indices) {
			val x = (13.0 * sin(2.0 * PI / players.size * i)).toInt()
			val y = 64
			val z = (13.0 * cos(2.0 * PI / players.size * i)).toInt()
			positions.add(Triple(x, y, z))
			world.cbWorld.setBlockData(
				x,
				y,
				z,
				Material.BEDROCK.createBlockData()
			)
		}
		ongoing = true
		for (i in players.indices) {
			val p = Bukkit.getPlayer(players[i]) ?: continue
			p.heal(100.0)
			p.foodLevel = 20
			p.saturation = 10.0F
			p.clearActivePotionEffects()
			
			p.inventory?.clear()
			p.teleport(Location(world.cbWorld, positions[i].first + 0.5, positions[i].second + 1.5, positions[i].third + 0.5))
		}
		
	}
	
	fun end(player: Player) {
		val survivor = plugin.minigameWorld.cbWorld.players.find { x -> x.gameMode == GameMode.SURVIVAL && !x.isDead && x != player }
		Bukkit.getOnlinePlayers().map {
			if (survivor != null) {
				it.sendRichMessage("<yellow>${survivor.name} <white>is the winner!")
			}
		}
		Bukkit.getScheduler().runTaskLater(plugin, Runnable {
			clearWorld()
			ongoing = false
			players.clear()
			positions.clear()
			Bukkit.getOnlinePlayers().forEach { p->
				p.inventory.clear()
				p.heal(100.0)
				p.foodLevel = 20
				p.saturation = 10.0F
				p.clearActivePotionEffects()
			}
			
		}, 20L * 5)
	}
}