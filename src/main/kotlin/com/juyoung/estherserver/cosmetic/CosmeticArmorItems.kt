package com.juyoung.estherserver.cosmetic

import net.minecraft.Util
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.equipment.ArmorMaterial
import net.minecraft.world.item.equipment.ArmorType
import net.minecraft.world.item.equipment.EquipmentAsset
import net.minecraft.world.item.equipment.EquipmentAssets
import java.util.EnumMap

/**
 * 치장 렌더링 전용 가상 ArmorItem들.
 * 방어력 0, 크리에이티브 탭 미등록, 렌더링 파이프라인 활용 목적.
 */
object CosmeticArmorItems {

    // ── Equipment Asset Keys ──
    val COSMETIC_CAT_KEY: ResourceKey<EquipmentAsset> = ResourceKey.create(
        EquipmentAssets.ROOT_ID,
        ResourceLocation.fromNamespaceAndPath("estherserver", "cosmetic_cat")
    )

    // ── ArmorMaterial (방어력 0) ──
    val COSMETIC_CAT_MATERIAL = ArmorMaterial(
        1, // durability (minimal)
        Util.make(EnumMap<ArmorType, Int>(ArmorType::class.java)) { map ->
            map[ArmorType.HELMET] = 0
            map[ArmorType.CHESTPLATE] = 0
            map[ArmorType.LEGGINGS] = 0
            map[ArmorType.BOOTS] = 0
        },
        1, // enchantmentValue (must be positive in 1.21.4)
        SoundEvents.ARMOR_EQUIP_LEATHER,
        0f, // toughness
        0f, // knockbackResistance
        ItemTags.REPAIRS_LEATHER_ARMOR,
        COSMETIC_CAT_KEY
    )

    // ── 치장 ID → ItemStack 캐시 (렌더링용) ──
    private val cachedStacks = mutableMapOf<String, ItemStack>()

    /**
     * 치장 ID에 대응하는 렌더링용 가상 ArmorItem ItemStack을 반환.
     * Mixin에서 render state의 equipment 필드를 이 스택으로 교체.
     */
    fun getVirtualStack(cosmeticId: String): ItemStack? {
        val def = CosmeticRegistry.get(cosmeticId) ?: return null
        return cachedStacks.getOrPut(cosmeticId) {
            val item = getArmorItemForSlot(def.setId, def.slot) ?: return null
            ItemStack(item)
        }
    }

    /**
     * setId + slot으로 등록된 ArmorItem을 찾는다.
     */
    private fun getArmorItemForSlot(setId: String, slot: EquipmentSlot): ArmorItem? {
        return when (setId) {
            "cosmetic_cat" -> when (slot) {
                EquipmentSlot.HEAD -> com.juyoung.estherserver.EstherServerMod.COSMETIC_CAT_HEAD.get() as? ArmorItem
                EquipmentSlot.CHEST -> com.juyoung.estherserver.EstherServerMod.COSMETIC_CAT_CHEST.get() as? ArmorItem
                EquipmentSlot.LEGS -> com.juyoung.estherserver.EstherServerMod.COSMETIC_CAT_LEGS.get() as? ArmorItem
                EquipmentSlot.FEET -> com.juyoung.estherserver.EstherServerMod.COSMETIC_CAT_FEET.get() as? ArmorItem
                else -> null
            }
            else -> null
        }
    }
}
