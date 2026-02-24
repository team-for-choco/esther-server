package com.juyoung.estherserver

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * 전문 루트 모디파이어 및 품질 시스템 제거 검증 테스트
 */
class ItemQualityTest {

    @Nested
    @DisplayName("루트 모디파이어 테스트")
    inner class LootModifierTests {

        @Test
        @DisplayName("profession_loot 루트 모디파이어가 올바른 형식이어야 함")
        fun professionLootModifierIsValid() {
            val modifier = loadJsonResource("data/estherserver/loot_modifiers/profession_loot.json")

            assertEquals("estherserver:profession_loot", modifier.get("type").asString, "type이 올바라야 함")
            assertTrue(modifier.has("conditions"), "conditions 필드가 있어야 함")

            val conditions = modifier.getAsJsonArray("conditions")
            assertEquals(0, conditions.size(), "conditions가 비어있어야 함")
        }

        @Test
        @DisplayName("global_loot_modifiers에 profession_loot가 포함되어야 함")
        fun globalLootModifiersContainsProfessionLoot() {
            val global = loadJsonResource("data/neoforge/loot_modifiers/global_loot_modifiers.json")

            assertFalse(global.get("replace").asBoolean, "replace가 false여야 함")

            val entries = global.getAsJsonArray("entries")
            val entryList = entries.map { it.asString }

            assertTrue(entryList.contains("estherserver:profession_loot"), "profession_loot가 포함되어야 함")
            assertFalse(entryList.contains("estherserver:assign_quality"), "assign_quality가 없어야 함")
        }

        @Test
        @DisplayName("profession_loot가 add_test_fish 뒤에 위치해야 함")
        fun professionLootIsAfterAddTestFish() {
            val global = loadJsonResource("data/neoforge/loot_modifiers/global_loot_modifiers.json")
            val entries = global.getAsJsonArray("entries")
            val entryList = entries.map { it.asString }

            val addFishIndex = entryList.indexOf("estherserver:add_test_fish")
            val professionLootIndex = entryList.indexOf("estherserver:profession_loot")

            assertTrue(addFishIndex >= 0, "add_test_fish가 있어야 함")
            assertTrue(professionLootIndex >= 0, "profession_loot가 있어야 함")
            assertTrue(professionLootIndex > addFishIndex, "profession_loot가 add_test_fish 뒤에 있어야 함")
        }
    }

    @Nested
    @DisplayName("품질 시스템 제거 확인")
    inner class QualityRemovedTests {

        @Test
        @DisplayName("has_quality 태그 파일이 삭제되어야 함")
        fun hasQualityTagRemoved() {
            val resource = javaClass.classLoader.getResource("data/estherserver/tags/item/has_quality.json")
            assertNull(resource, "has_quality.json이 삭제되어야 함")
        }

        @Test
        @DisplayName("assign_quality 모디파이어 파일이 삭제되어야 함")
        fun assignQualityModifierRemoved() {
            val resource = javaClass.classLoader.getResource("data/estherserver/loot_modifiers/assign_quality.json")
            assertNull(resource, "assign_quality.json이 삭제되어야 함")
        }

        @Test
        @DisplayName("언어 파일에서 품질 번역이 제거되어야 함")
        fun qualityTranslationsRemoved() {
            val enLang = loadJsonResource("assets/estherserver/lang/en_us.json")
            assertFalse(enLang.has("quality.estherserver.common"), "common 품질 번역이 없어야 함")
            assertFalse(enLang.has("quality.estherserver.fine"), "fine 품질 번역이 없어야 함")
            assertFalse(enLang.has("quality.estherserver.rare"), "rare 품질 번역이 없어야 함")

            val koLang = loadJsonResource("assets/estherserver/lang/ko_kr.json")
            assertFalse(koLang.has("quality.estherserver.common"), "common 품질 한글 번역이 없어야 함")
            assertFalse(koLang.has("quality.estherserver.fine"), "fine 품질 한글 번역이 없어야 함")
            assertFalse(koLang.has("quality.estherserver.rare"), "rare 품질 한글 번역이 없어야 함")
        }
    }
}
