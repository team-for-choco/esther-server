package com.juyoung.estherserver

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * 커스텀 생선 관련 리소스 파일 테스트
 *
 * 마인크래프트 환경 없이 JSON 파일 형식과 필수 필드를 검증합니다.
 */
class CustomFishTest {

    @Nested
    @DisplayName("아이템 모델 테스트")
    inner class ItemModelTests {

        @Test
        @DisplayName("test_fish 모델 파일이 올바른 형식이어야 함")
        fun testFishModelIsValid() {
            val model = loadJsonResource("assets/estherserver/models/item/test_fish.json")

            assertTrue(model.has("parent"), "parent 필드가 있어야 함")
            assertTrue(model.has("textures"), "textures 필드가 있어야 함")

            val textures = model.getAsJsonObject("textures")
            assertTrue(textures.has("layer0"), "layer0 텍스처가 있어야 함")
        }

        @Test
        @DisplayName("cooked_test_fish 모델 파일이 올바른 형식이어야 함")
        fun cookedTestFishModelIsValid() {
            val model = loadJsonResource("assets/estherserver/models/item/cooked_test_fish.json")

            assertTrue(model.has("parent"), "parent 필드가 있어야 함")
            assertTrue(model.has("textures"), "textures 필드가 있어야 함")

            val textures = model.getAsJsonObject("textures")
            assertTrue(textures.has("layer0"), "layer0 텍스처가 있어야 함")
        }
    }

    @Nested
    @DisplayName("레시피 테스트")
    inner class RecipeTests {

        @Test
        @DisplayName("제련 레시피가 올바른 형식이어야 함")
        fun smeltingRecipeIsValid() {
            val recipe = loadJsonResource("data/estherserver/recipe/cooked_test_fish_from_smelting.json")

            assertEquals("minecraft:smelting", recipe.get("type").asString)
            assertEquals("estherserver:test_fish", recipe.get("ingredient").asString)

            val result = recipe.getAsJsonObject("result")
            assertEquals("estherserver:cooked_test_fish", result.get("id").asString)

            assertTrue(recipe.get("experience").asFloat > 0, "경험치가 0보다 커야 함")
            assertTrue(recipe.get("cookingtime").asInt > 0, "조리 시간이 0보다 커야 함")
        }

        @Test
        @DisplayName("훈연 레시피가 올바른 형식이어야 함")
        fun smokingRecipeIsValid() {
            val recipe = loadJsonResource("data/estherserver/recipe/cooked_test_fish_from_smoking.json")

            assertEquals("minecraft:smoking", recipe.get("type").asString)
            assertEquals("estherserver:test_fish", recipe.get("ingredient").asString)
        }

        @Test
        @DisplayName("캠프파이어 레시피가 올바른 형식이어야 함")
        fun campfireRecipeIsValid() {
            val recipe = loadJsonResource("data/estherserver/recipe/cooked_test_fish_from_campfire.json")

            assertEquals("minecraft:campfire_cooking", recipe.get("type").asString)
            assertEquals("estherserver:test_fish", recipe.get("ingredient").asString)
        }

        @Test
        @DisplayName("훈연 레시피가 제련보다 빨라야 함")
        fun smokingIsFasterThanSmelting() {
            val smelting = loadJsonResource("data/estherserver/recipe/cooked_test_fish_from_smelting.json")
            val smoking = loadJsonResource("data/estherserver/recipe/cooked_test_fish_from_smoking.json")

            val smeltingTime = smelting.get("cookingtime").asInt
            val smokingTime = smoking.get("cookingtime").asInt

            assertTrue(smokingTime < smeltingTime, "훈연($smokingTime)이 제련($smeltingTime)보다 빨라야 함")
        }
    }

    @Nested
    @DisplayName("루트 모디파이어 테스트")
    inner class LootModifierTests {

        @Test
        @DisplayName("글로벌 루트 모디파이어 등록 파일이 올바른 형식이어야 함")
        fun globalLootModifiersIsValid() {
            val global = loadJsonResource("data/neoforge/loot_modifiers/global_loot_modifiers.json")

            assertTrue(global.has("entries"), "entries 필드가 있어야 함")

            val entries = global.getAsJsonArray("entries")
            assertTrue(entries.size() > 0, "최소 하나의 엔트리가 있어야 함")
            assertTrue(
                entries.any { it.asString == "estherserver:add_test_fish" },
                "add_test_fish 모디파이어가 등록되어 있어야 함"
            )
        }

        @Test
        @DisplayName("테스트 생선 루트 모디파이어가 올바른 형식이어야 함")
        fun addTestFishModifierIsValid() {
            val modifier = loadJsonResource("data/estherserver/loot_modifiers/add_test_fish.json")

            assertEquals("estherserver:replace_item", modifier.get("type").asString)
            assertEquals("estherserver:test_fish", modifier.get("item").asString)
            assertTrue(modifier.has("conditions"), "conditions 필드가 있어야 함")
        }

        @Test
        @DisplayName("낚시 루트테이블을 타겟으로 해야 함")
        fun modifierTargetsFishingLootTable() {
            val modifier = loadJsonResource("data/estherserver/loot_modifiers/add_test_fish.json")
            val conditions = modifier.getAsJsonArray("conditions")

            val hasLootTableCondition = conditions.any { condition ->
                val obj = condition.asJsonObject
                obj.get("condition")?.asString == "neoforge:loot_table_id" &&
                    obj.get("loot_table_id")?.asString == "minecraft:gameplay/fishing"
            }

            assertTrue(hasLootTableCondition, "낚시 루트테이블 조건이 있어야 함")
        }
    }

    @Nested
    @DisplayName("언어 파일 테스트")
    inner class LanguageTests {

        @Test
        @DisplayName("영어 번역이 있어야 함")
        fun englishTranslationsExist() {
            val lang = loadJsonResource("assets/estherserver/lang/en_us.json")

            assertTrue(lang.has("item.estherserver.test_fish"), "test_fish 번역이 있어야 함")
            assertTrue(lang.has("item.estherserver.cooked_test_fish"), "cooked_test_fish 번역이 있어야 함")
        }

        @Test
        @DisplayName("한국어 번역이 있어야 함")
        fun koreanTranslationsExist() {
            val lang = loadJsonResource("assets/estherserver/lang/ko_kr.json")

            assertTrue(lang.has("item.estherserver.test_fish"), "test_fish 번역이 있어야 함")
            assertTrue(lang.has("item.estherserver.cooked_test_fish"), "cooked_test_fish 번역이 있어야 함")
        }
    }
}
