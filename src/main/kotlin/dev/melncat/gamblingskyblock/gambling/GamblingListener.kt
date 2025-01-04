package dev.melncat.gamblingskyblock.gambling

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.plugin.Plugin

class GamblingListener(val plugin: Plugin): Listener {
	@EventHandler
	fun on(event: EntityExplodeEvent) {
		event.blockList().clear()
	}
	@EventHandler
	fun on(event: EntityChangeBlockEvent) {
		if (event.to == Material.AIR && event.entity.type != EntityType.VILLAGER) event.isCancelled = true
	}
}