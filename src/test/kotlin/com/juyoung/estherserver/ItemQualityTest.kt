package com.juyoung.estherserver

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

/**
 * 아이템 등급 시스템 관련 리소스 파일 테스트
 *
 * 마인크래프트 환경 없이 JSON 파일 형식과 필수 필드를 검증합니다.
 */
class ItemQualityTest {

    private val gson = Gson()

    private fun loadJsonResource(path: String): JsonObject {
        val stream = javaClass.classLoader.getResourceAsStream(path)
            ?: throw AssertionError("Resource not found: $path")
        return InputStreamReader(stream, StandardCharsets.UTF_8).use { reader ->
            gson.fromJson(reader, JsonObject::class.java)
        }
    }

    @Nested
    @DisplayName("아이템 태그 테스트")
    inner class TagTests {

        @Test
        @DisplayName("has_quality 태그가 올바른 형식이어야 함")
        fun hasQualityTagIsValid() {
            val tag = loadJsonResource("data/estherserver/tags/item/has_quality.json")

            assertFalse(tag.get("replace").asBoolean, "replace가 false여야 함")
            assertTrue(tag.has("values"), "values 필드가 있어야 함")

            val values = tag.getAsJsonArray("values")
            val valueList = values.map { it.asString }

            assertTrue(valueList.contains("estherserver:test_fish"), "test_fish가 포함되어야 함")
            assertTrue(valueList.contains("estherserver:cooked_test_fish"), "cooked_test_fish가 포함되어야 함")
            assertTrue(valueList.contains("estherserver:test_harvest"), "test_harvest가 포함되어야 함")
            assertTrue(valueList.contains("estherserver:cooked_test_harvest"), "cooked_test_harvest가 포함되어야 함")
            assertTrue(valueList.contains("estherserver:test_ore_raw"), "test_ore_raw가 포함되어야 함")
            assertTrue(valueList.contains("estherserver:test_ore_ingot"), "test_ore_ingot가 포함되어야 함")
        }

        @Test
        @DisplayName("has_quality 태그에 제외 아이템이 없어야 함")
        fun hasQualityTagExcludesCorrectItems() {
            val tag = loadJsonResource("data/estherserver/tags/item/has_quality.json")
            val values = tag.getAsJsonArray("values")
            val valueList = values.map { it.asString }

            assertFalse(valueList.contains("estherserver:test_seeds"), "test_seeds가 포함되지 않아야 함")
            assertFalse(valueList.contains("estherserver:example_item"), "example_item이 포함되지 않아야 함")
            assertFalse(valueList.contains("estherserver:example_block"), "example_block이 포함되지 않아야 함")
        }

        @Test
        @DisplayName("has_quality 태그에 정확히 6개 아이템이 있어야 함")
        fun hasQualityTagHasCorrectCount() {
            val tag = loadJsonResource("data/estherserver/tags/item/has_quality.json")
            val values = tag.getAsJsonArray("values")
            assertEquals(6, values.size(), "등급 대상 아이템이 6개여야 함")
        }
    }

    @Nested
    @DisplayName("루트 모디파이어 테스트")
    inner class LootModifierTests {

        @Test
        @DisplayName("assign_quality 루트 모디파이어가 올바른 형식이어야 함")
        fun assignQualityModifierIsValid() {
            val modifier = loadJsonResource("data/estherserver/loot_modifiers/assign_quality.json")

            assertEquals("estherserver:assign_quality", modifier.get("type").asString, "type이 올바라야 함")
            assertTrue(modifier.has("conditions"), "conditions 필드가 있어야 함")

            val conditions = modifier.getAsJsonArray("conditions")
            assertEquals(0, conditions.size(), "conditions가 비어있어야 함 (태그로 필터링)")
        }

        @Test
        @DisplayName("global_loot_modifiers에 assign_quality가 포함되어야 함")
        fun globalLootModifiersContainsAssignQuality() {
            val global = loadJsonResource("data/neoforge/loot_modifiers/global_loot_modifiers.json")

            assertFalse(global.get("replace").asBoolean, "replace가 false여야 함")

            val entries = global.getAsJsonArray("entries")
            val entryList = entries.map { it.asString }

            assertTrue(entryList.contains("estherserver:assign_quality"), "assign_quality가 포함되어야 함")
        }

        @Test
        @DisplayName("assign_quality가 add_test_fish 뒤에 위치해야 함")
        fun assignQualityIsAfterAddTestFish() {
            val global = loadJsonResource("data/neoforge/loot_modifiers/global_loot_modifiers.json")
            val entries = global.getAsJsonArray("entries")
            val entryList = entries.map { it.asString }

            val addFishIndex = entryList.indexOf("estherserver:add_test_fish")
            val assignQualityIndex = entryList.indexOf("estherserver:assign_quality")

            assertTrue(addFishIndex >= 0, "add_test_fish가 있어야 함")
            assertTrue(assignQualityIndex >= 0, "assign_quality가 있어야 함")
            assertTrue(assignQualityIndex > addFishIndex, "assign_quality가 add_test_fish 뒤에 있어야 함")
        }
    }

    @Nested
    @DisplayName("언어 파일 테스트")
    inner class LanguageTests {

        @Test
        @DisplayName("영어 등급 번역이 있어야 함")
        fun englishQualityTranslationsExist() {
            val lang = loadJsonResource("assets/estherserver/lang/en_us.json")

            assertTrue(lang.has("quality.estherserver.common"), "common 번역이 있어야 함")
            assertTrue(lang.has("quality.estherserver.fine"), "fine 번역이 있어야 함")
            assertTrue(lang.has("quality.estherserver.rare"), "rare 번역이 있어야 함")

            assertEquals("Common", lang.get("quality.estherserver.common").asString)
            assertEquals("Fine", lang.get("quality.estherserver.fine").asString)
            assertEquals("Rare", lang.get("quality.estherserver.rare").asString)
        }

        @Test
        @DisplayName("한국어 등급 번역이 있어야 함")
        fun koreanQualityTranslationsExist() {
            val lang = loadJsonResource("assets/estherserver/lang/ko_kr.json")

            assertTrue(lang.has("quality.estherserver.common"), "common 번역이 있어야 함")
            assertTrue(lang.has("quality.estherserver.fine"), "fine 번역이 있어야 함")
            assertTrue(lang.has("quality.estherserver.rare"), "rare 번역이 있어야 함")

            assertEquals("일반", lang.get("quality.estherserver.common").asString)
            assertEquals("고급", lang.get("quality.estherserver.fine").asString)
            assertEquals("희귀", lang.get("quality.estherserver.rare").asString)
        }
    }
}
