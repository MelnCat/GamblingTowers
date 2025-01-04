package dev.melncat.gamblingskyblock.gambling

import com.destroystokyo.paper.MaterialSetTag
import com.destroystokyo.paper.MaterialTags
import com.sun.source.doctree.AttributeTree.ValueKind
import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Consumable
import io.papermc.paper.datacomponent.item.FoodProperties
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import io.papermc.paper.datacomponent.item.ItemEnchantments
import io.papermc.paper.datacomponent.item.PotionContents
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Registry
import org.bukkit.Tag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.components.FoodComponent
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import java.math.RoundingMode
import kotlin.math.min
import kotlin.random.Random

object GamblingManager {
	var enabled = true
	
	private val potions = setOf(Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.TIPPED_ARROW)
	private val blacklist = setOf(Material.ENDER_DRAGON_SPAWN_EGG, Material.WITHER_SPAWN_EGG)
	private val possible = Material.entries.subtract(potions).subtract(setOf(Material.ENCHANTED_BOOK)).subtract(blacklist)
		.filter { it.isItem }
	private val randomWords = listOf(
		"Piss",
		"Gabe",
		"Catgirl",
		"Among Us",
		"Silksong",
		"Random",
		"Skibidi",
		"Brainrot",
		"CGRB",
		"Mewing",
		"Cancer",
		"Sigma"
	)
	private var nextTime = System.currentTimeMillis() + 1000 * 60
	private var nextBiomeTime = System.currentTimeMillis() + 10000 * 60
	
	fun setupGambling(plugin: Plugin) {
		Bukkit.getScheduler().runTaskTimer(plugin, ::tickGambling, 0L, 2L)
		Bukkit.getScheduler().runTaskTimer(plugin, ::tickBiomeGambling, 0L, 2L)
	}
	
	private fun tickBiomeGambling() {
		if (!enabled) return
		val players = Bukkit.getOnlinePlayers()
		if (nextBiomeTime > System.currentTimeMillis()) return
		nextBiomeTime = System.currentTimeMillis() + 10000 * 60
		val chunks = players.map { it.location.chunk }.distinct()
		for (chunk in chunks) {
			val biome = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).toList().random()
			for (y in -64..319)
				for (x in 0..<16)
					for (z in 0..<16) {
						val block = chunk.getBlock(x, y, z)
						chunk.world.setBiome(block.location, biome)
						
			}
		}
		for (player in players) player.sendMessage(Component.text("Biomes randomized."))
	}
	
	
	private fun tickGambling() {
		if (!enabled) return
		val players = Bukkit.getOnlinePlayers()
		if (nextTime > System.currentTimeMillis()) {
			val remaining = nextTime - System.currentTimeMillis()
			if (remaining < 10000) {
				for (player in players) {
					player.sendActionBar(Component.text("${
						(remaining.toDouble() / 1000.0).toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
					}s").color(NamedTextColor.YELLOW).append(Component.text(" left")))
				}
			}
			return
		}
		nextTime = System.currentTimeMillis() + 1000 * 60
		for (player in players) {
			player.sendActionBar(Component.empty())
			val item = generateRandomItem()
			player.sendMessage(Component.text("You got ").append(item.displayName()).append(Component.text("!")))
			val leftovers = player.inventory.addItem(item)
			if (leftovers.isNotEmpty()) player.world.dropItemNaturally(player.location, leftovers.values.first())
		}
	}
	
	private fun generateRandomItem(): ItemStack {
		if (Random.nextInt(100) < 90) {
			val material = possible.random()
			val item = ItemStack(material)
			return item
		}
		if (Random.nextInt(100) < 20) {
			val potionMaterial = potions.random()
			val potion = ItemStack(potionMaterial)
			val color = Color.fromRGB(Random.nextInt(0x1000000));
			potion.setData(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents()
				.potion(PotionType.entries.random())
				.addCustomEffects(
					Registry.POTION_EFFECT_TYPE.toList().shuffled().take((1..4).random()).map {
						PotionEffect(it, (20..200).random(), (0..3).random())
					}
				)
				.customColor(color)
			)
			potion.setData(DataComponentTypes.ITEM_NAME, Component.text(
				when (potionMaterial) {
					Material.POTION -> "Potion of ${randomWords.random()}"
					Material.SPLASH_POTION -> "Splash Potion of ${randomWords.random()}"
					Material.LINGERING_POTION -> "Lingering Potion of ${randomWords.random()}"
					Material.TIPPED_ARROW -> "Arrow of ${randomWords.random()}"
					else -> "?"
				}
			).color(TextColor.color(color.asRGB())))
			return potion
		}
		if (Random.nextInt(100) < 30) {
			val book = ItemStack(Material.ENCHANTED_BOOK)
			val pairs = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).toList().shuffled().take(
				(1..4).random()
			).map { it to (1..min(it.maxLevel, 3)).random() }
			book.setData(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantments.itemEnchantments(
				pairs.toMap(), true
			))
			return book
		}
		val material = possible.random()
		val item = ItemStack(material)
		var modelData: Key? = null
		while (modelData == null) modelData = possible.random().getDefaultData(DataComponentTypes.ITEM_MODEL)
		item.setData(DataComponentTypes.ITEM_MODEL, modelData)
		var attrs: ItemAttributeModifiers? = null
		while (attrs == null) attrs = possible.random().getDefaultData(DataComponentTypes.ATTRIBUTE_MODIFIERS)
		item.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrs)
		item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable())
		item.setData(DataComponentTypes.FOOD, FoodProperties.food()
			.nutrition((1..10).random())
			.saturation(Random.nextFloat() * 3)
			.canAlwaysEat(true))
		return item
	}
}