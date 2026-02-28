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
    val COSMETIC_CAT_KEY: ResourceKey<EquipmentAsset> = createKey("cosmetic_cat")
    val COSMETIC_DOG_KEY: ResourceKey<EquipmentAsset> = createKey("cosmetic_dog")
    val COSMETIC_RABBIT_KEY: ResourceKey<EquipmentAsset> = createKey("cosmetic_rabbit")
    val COSMETIC_FOX_KEY: ResourceKey<EquipmentAsset> = createKey("cosmetic_fox")

    private fun createKey(name: String): ResourceKey<EquipmentAsset> = ResourceKey.create(
        EquipmentAssets.ROOT_ID,
        ResourceLocation.fromNamespaceAndPath("estherserver", name)
    )

    private fun createMaterial(key: ResourceKey<EquipmentAsset>): ArmorMaterial = ArmorMaterial(
        1,
        Util.make(EnumMap<ArmorType, Int>(ArmorType::class.java)) { map ->
            map[ArmorType.HELMET] = 0
            map[ArmorType.CHESTPLATE] = 0
            map[ArmorType.LEGGINGS] = 0
            map[ArmorType.BOOTS] = 0
        },
        1,
        SoundEvents.ARMOR_EQUIP_LEATHER,
        0f,
        0f,
        ItemTags.REPAIRS_LEATHER_ARMOR,
        key
    )

    // ── ArmorMaterials (방어력 0) ──
    val COSMETIC_CAT_MATERIAL = createMaterial(COSMETIC_CAT_KEY)
    val COSMETIC_DOG_MATERIAL = createMaterial(COSMETIC_DOG_KEY)
    val COSMETIC_RABBIT_MATERIAL = createMaterial(COSMETIC_RABBIT_KEY)
    val COSMETIC_FOX_MATERIAL = createMaterial(COSMETIC_FOX_KEY)

    // ── setId → (slot → ArmorItem supplier) 테이블 ──
    private val armorTable = mutableMapOf<String, Map<EquipmentSlot, () -> ArmorItem?>>()

    fun registerArmorSet(setId: String, slotMap: Map<EquipmentSlot, () -> ArmorItem?>) {
        armorTable[setId] = slotMap
    }

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
        return armorTable[setId]?.get(slot)?.invoke()
    }
}
