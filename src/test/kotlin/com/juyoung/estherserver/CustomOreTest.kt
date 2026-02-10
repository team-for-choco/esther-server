package com.juyoung.estherserver

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.InputStreamReader

/**
 * 커스텀 광물 관련 리소스 파일 테스트
 *
 * 마인크래프트 환경 없이 JSON 파일 형식과 필수 필드를 검증합니다.
 */
class CustomOreTest {

    private val gson = Gson()

    private fun loadJsonResource(path: String): JsonObject {
        val stream = javaClass.classLoader.getResourceAsStream(path)
            ?: throw AssertionError("Resource not found: $path")
        return InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8).use { reader ->
            gson.fromJson(reader, JsonObject::class.java)
        }
    }

    @Nested
    @DisplayName("블록스테이트 테스트")
    inner class BlockstateTests {

        @Test
        @DisplayName("test_ore 블록스테이트가 올바른 형식이어야 함")
        fun testOreBlockstateIsValid() {
            val blockstate = loadJsonResource("assets/estherserver/blockstates/test_ore.json")

            assertTrue(blockstate.has("variants"), "variants 필드가 있어야 함")
            val variants = blockstate.getAsJsonObject("variants")
            assertTrue(variants.has(""), "기본 variant가 있어야 함")

            val defaultVariant = variants.getAsJsonObject("")
            assertEquals("estherserver:block/test_ore", defaultVariant.get("model").asString)
        }

        @Test
        @DisplayName("deepslate_test_ore 블록스테이트가 올바른 형식이어야 함")
        fun deepslateTestOreBlockstateIsValid() {
            val blockstate = loadJsonResource("assets/estherserver/blockstates/deepslate_test_ore.json")

            assertTrue(blockstate.has("variants"), "variants 필드가 있어야 함")
            val variants = blockstate.getAsJsonObject("variants")
            assertTrue(variants.has(""), "기본 variant가 있어야 함")

            val defaultVariant = variants.getAsJsonObject("")
            assertEquals("estherserver:block/deepslate_test_ore", defaultVariant.get("model").asString)
        }
    }

    @Nested
    @DisplayName("블록 모델 테스트")
    inner class BlockModelTests {

        @Test
        @DisplayName("test_ore 블록 모델이 cube_all 형식이어야 함")
        fun testOreBlockModelIsValid() {
            val model = loadJsonResource("assets/estherserver/models/block/test_ore.json")

            assertEquals("minecraft:block/cube_all", model.get("parent").asString)
            assertTrue(model.has("textures"), "textures 필드가 있어야 함")

            val textures = model.getAsJsonObject("textures")
            assertTrue(textures.has("all"), "all 텍스처가 있어야 함")
        }

        @Test
        @DisplayName("deepslate_test_ore 블록 모델이 cube_all 형식이어야 함")
        fun deepslateTestOreBlockModelIsValid() {
            val model = loadJsonResource("assets/estherserver/models/block/deepslate_test_ore.json")

            assertEquals("minecraft:block/cube_all", model.get("parent").asString)
            assertTrue(model.has("textures"), "textures 필드가 있어야 함")

            val textures = model.getAsJsonObject("textures")
            assertTrue(textures.has("all"), "all 텍스처가 있어야 함")
        }
    }

    @Nested
    @DisplayName("아이템 모델 테스트")
    inner class ItemModelTests {

        @Test
        @DisplayName("test_ore_raw 모델 파일이 올바른 형식이어야 함")
        fun testOreRawModelIsValid() {
            val model = loadJsonResource("assets/estherserver/models/item/test_ore_raw.json")

            assertTrue(model.has("parent"), "parent 필드가 있어야 함")
            assertTrue(model.has("textures"), "textures 필드가 있어야 함")

            val textures = model.getAsJsonObject("textures")
            assertTrue(textures.has("layer0"), "layer0 텍스처가 있어야 함")
        }

        @Test
        @DisplayName("test_ore_ingot 모델 파일이 올바른 형식이어야 함")
        fun testOreIngotModelIsValid() {
            val model = loadJsonResource("assets/estherserver/models/item/test_ore_ingot.json")

            assertTrue(model.has("parent"), "parent 필드가 있어야 함")
            assertTrue(model.has("textures"), "textures 필드가 있어야 함")

            val textures = model.getAsJsonObject("textures")
            assertTrue(textures.has("layer0"), "layer0 텍스처가 있어야 함")
        }
    }

    @Nested
    @DisplayName("클라이언트 아이템 정의 테스트")
    inner class ClientItemDefinitionTests {

        @Test
        @DisplayName("test_ore 아이템 정의가 올바른 형식이어야 함")
        fun testOreItemDefIsValid() {
            val itemDef = loadJsonResource("assets/estherserver/items/test_ore.json")

            assertTrue(itemDef.has("model"), "model 필드가 있어야 함")
            val model = itemDef.getAsJsonObject("model")
            assertEquals("minecraft:model", model.get("type").asString)
            assertEquals("estherserver:block/test_ore", model.get("model").asString)
        }

        @Test
        @DisplayName("deepslate_test_ore 아이템 정의가 올바른 형식이어야 함")
        fun deepslateTestOreItemDefIsValid() {
            val itemDef = loadJsonResource("assets/estherserver/items/deepslate_test_ore.json")

            assertTrue(itemDef.has("model"), "model 필드가 있어야 함")
            val model = itemDef.getAsJsonObject("model")
            assertEquals("minecraft:model", model.get("type").asString)
            assertEquals("estherserver:block/deepslate_test_ore", model.get("model").asString)
        }

        @Test
        @DisplayName("test_ore_raw 아이템 정의가 올바른 형식이어야 함")
        fun testOreRawItemDefIsValid() {
            val itemDef = loadJsonResource("assets/estherserver/items/test_ore_raw.json")

            assertTrue(itemDef.has("model"), "model 필드가 있어야 함")
            val model = itemDef.getAsJsonObject("model")
            assertEquals("minecraft:model", model.get("type").asString)
            assertEquals("estherserver:item/test_ore_raw", model.get("model").asString)
        }

        @Test
        @DisplayName("test_ore_ingot 아이템 정의가 올바른 형식이어야 함")
        fun testOreIngotItemDefIsValid() {
            val itemDef = loadJsonResource("assets/estherserver/items/test_ore_ingot.json")

            assertTrue(itemDef.has("model"), "model 필드가 있어야 함")
            val model = itemDef.getAsJsonObject("model")
            assertEquals("minecraft:model", model.get("type").asString)
            assertEquals("estherserver:item/test_ore_ingot", model.get("model").asString)
        }
    }

    @Nested
    @DisplayName("루트테이블 테스트")
    inner class LootTableTests {

        @Test
        @DisplayName("test_ore 루트테이블이 silk touch/fortune을 지원해야 함")
        fun testOreLootTableIsValid() {
            val lootTable = loadJsonResource("data/estherserver/loot_table/blocks/test_ore.json")

            assertEquals("minecraft:block", lootTable.get("type").asString)
            assertTrue(lootTable.has("pools"), "pools 필드가 있어야 함")

            val pools = lootTable.getAsJsonArray("pools")
            assertTrue(pools.size() > 0, "최소 하나의 풀이 있어야 함")

            val firstPool = pools[0].asJsonObject
            val entries = firstPool.getAsJsonArray("entries")
            val alternatives = entries[0].asJsonObject
            assertEquals("minecraft:alternatives", alternatives.get("type").asString)

            val children = alternatives.getAsJsonArray("children")
            assertTrue(children.size() >= 2, "silk touch와 일반 드롭이 있어야 함")

            // silk touch entry
            val silkTouchEntry = children[0].asJsonObject
            assertEquals("estherserver:test_ore", silkTouchEntry.get("name").asString)

            // raw ore entry
            val rawOreEntry = children[1].asJsonObject
            assertEquals("estherserver:test_ore_raw", rawOreEntry.get("name").asString)
        }

        @Test
        @DisplayName("deepslate_test_ore 루트테이블이 silk touch/fortune을 지원해야 함")
        fun deepslateTestOreLootTableIsValid() {
            val lootTable = loadJsonResource("data/estherserver/loot_table/blocks/deepslate_test_ore.json")

            assertEquals("minecraft:block", lootTable.get("type").asString)

            val pools = lootTable.getAsJsonArray("pools")
            val entries = pools[0].asJsonObject.getAsJsonArray("entries")
            val alternatives = entries[0].asJsonObject
            val children = alternatives.getAsJsonArray("children")

            // silk touch should drop the deepslate block
            val silkTouchEntry = children[0].asJsonObject
            assertEquals("estherserver:deepslate_test_ore", silkTouchEntry.get("name").asString)

            // normal drop should give raw ore
            val rawOreEntry = children[1].asJsonObject
            assertEquals("estherserver:test_ore_raw", rawOreEntry.get("name").asString)
        }
    }

    @Nested
    @DisplayName("레시피 테스트")
    inner class RecipeTests {

        @Test
        @DisplayName("제련 레시피가 올바른 형식이어야 함")
        fun smeltingRecipeIsValid() {
            val recipe = loadJsonResource("data/estherserver/recipe/test_ore_ingot_from_smelting_raw.json")

            assertEquals("minecraft:smelting", recipe.get("type").asString)
            assertEquals("estherserver:test_ore_raw", recipe.get("ingredient").asString)

            val result = recipe.getAsJsonObject("result")
            assertEquals("estherserver:test_ore_ingot", result.get("id").asString)

            assertEquals(0.7f, recipe.get("experience").asFloat, "경험치가 0.7이어야 함")
            assertEquals(200, recipe.get("cookingtime").asInt, "조리 시간이 200이어야 함")
        }

        @Test
        @DisplayName("용광로 레시피가 올바른 형식이어야 함")
        fun blastingRecipeIsValid() {
            val recipe = loadJsonResource("data/estherserver/recipe/test_ore_ingot_from_blasting_raw.json")

            assertEquals("minecraft:blasting", recipe.get("type").asString)
            assertEquals("estherserver:test_ore_raw", recipe.get("ingredient").asString)

            val result = recipe.getAsJsonObject("result")
            assertEquals("estherserver:test_ore_ingot", result.get("id").asString)

            assertEquals(0.7f, recipe.get("experience").asFloat, "경험치가 0.7이어야 함")
            assertEquals(100, recipe.get("cookingtime").asInt, "조리 시간이 100이어야 함")
        }

        @Test
        @DisplayName("용광로 레시피가 제련보다 빨라야 함")
        fun blastingIsFasterThanSmelting() {
            val smelting = loadJsonResource("data/estherserver/recipe/test_ore_ingot_from_smelting_raw.json")
            val blasting = loadJsonResource("data/estherserver/recipe/test_ore_ingot_from_blasting_raw.json")

            val smeltingTime = smelting.get("cookingtime").asInt
            val blastingTime = blasting.get("cookingtime").asInt

            assertTrue(blastingTime < smeltingTime, "용광로($blastingTime)가 제련($smeltingTime)보다 빨라야 함")
        }
    }

    @Nested
    @DisplayName("태그 테스트")
    inner class TagTests {

        @Test
        @DisplayName("곡괭이 채굴 태그에 광석이 포함되어야 함")
        fun pickaxeTagIsValid() {
            val tag = loadJsonResource("data/minecraft/tags/block/mineable/pickaxe.json")

            assertFalse(tag.get("replace").asBoolean, "replace가 false여야 함")

            val values = tag.getAsJsonArray("values")
            val valueList = values.map { it.asString }
            assertTrue(valueList.contains("estherserver:test_ore"), "test_ore가 포함되어야 함")
            assertTrue(valueList.contains("estherserver:deepslate_test_ore"), "deepslate_test_ore가 포함되어야 함")
        }

        @Test
        @DisplayName("철 도구 태그에 광석이 포함되어야 함")
        fun ironToolTagIsValid() {
            val tag = loadJsonResource("data/minecraft/tags/block/needs_iron_tool.json")

            assertFalse(tag.get("replace").asBoolean, "replace가 false여야 함")

            val values = tag.getAsJsonArray("values")
            val valueList = values.map { it.asString }
            assertTrue(valueList.contains("estherserver:test_ore"), "test_ore가 포함되어야 함")
            assertTrue(valueList.contains("estherserver:deepslate_test_ore"), "deepslate_test_ore가 포함되어야 함")
        }
    }

    @Nested
    @DisplayName("월드젠 테스트")
    inner class WorldgenTests {

        @Test
        @DisplayName("ConfiguredFeature가 올바른 형식이어야 함")
        fun configuredFeatureIsValid() {
            val feature = loadJsonResource("data/estherserver/worldgen/configured_feature/ore_test_ore.json")

            assertEquals("minecraft:ore", feature.get("type").asString)
            assertTrue(feature.has("config"), "config 필드가 있어야 함")

            val config = feature.getAsJsonObject("config")
            assertEquals(10, config.get("size").asInt, "vein size가 10이어야 함")

            val targets = config.getAsJsonArray("targets")
            assertEquals(2, targets.size(), "stone과 deepslate 타겟이 있어야 함")

            // stone target
            val stoneTarget = targets[0].asJsonObject
            val stoneRule = stoneTarget.getAsJsonObject("target")
            assertEquals("minecraft:tag_match", stoneRule.get("predicate_type").asString)
            val stoneState = stoneTarget.getAsJsonObject("state")
            assertEquals("estherserver:test_ore", stoneState.get("Name").asString)

            // deepslate target
            val deepslateTarget = targets[1].asJsonObject
            val deepslateState = deepslateTarget.getAsJsonObject("state")
            assertEquals("estherserver:deepslate_test_ore", deepslateState.get("Name").asString)
        }

        @Test
        @DisplayName("PlacedFeature가 올바른 형식이어야 함")
        fun placedFeatureIsValid() {
            val placed = loadJsonResource("data/estherserver/worldgen/placed_feature/ore_test_ore.json")

            assertEquals("estherserver:ore_test_ore", placed.get("feature").asString)
            assertTrue(placed.has("placement"), "placement 필드가 있어야 함")

            val placement = placed.getAsJsonArray("placement")
            assertTrue(placement.size() >= 4, "최소 4개의 placement 설정이 있어야 함")

            // count
            val count = placement[0].asJsonObject
            assertEquals("minecraft:count", count.get("type").asString)
            assertEquals(12, count.get("count").asInt, "청크당 12회여야 함")

            // height_range
            val heightRange = placement[2].asJsonObject
            assertEquals("minecraft:height_range", heightRange.get("type").asString)
            val height = heightRange.getAsJsonObject("height")
            assertEquals("minecraft:trapezoid", height.get("type").asString)
        }

        @Test
        @DisplayName("BiomeModifier가 올바른 형식이어야 함")
        fun biomeModifierIsValid() {
            val modifier = loadJsonResource("data/estherserver/neoforge/biome_modifier/add_test_ore.json")

            assertEquals("neoforge:add_features", modifier.get("type").asString)
            assertEquals("#c:is_overworld", modifier.get("biomes").asString)
            assertEquals("estherserver:ore_test_ore", modifier.get("features").asString)
            assertEquals("underground_ores", modifier.get("step").asString)
        }
    }

    @Nested
    @DisplayName("언어 파일 테스트")
    inner class LanguageTests {

        @Test
        @DisplayName("영어 번역이 있어야 함")
        fun englishTranslationsExist() {
            val lang = loadJsonResource("assets/estherserver/lang/en_us.json")

            assertTrue(lang.has("block.estherserver.test_ore"), "test_ore 번역이 있어야 함")
            assertTrue(lang.has("block.estherserver.deepslate_test_ore"), "deepslate_test_ore 번역이 있어야 함")
            assertTrue(lang.has("item.estherserver.test_ore_raw"), "test_ore_raw 번역이 있어야 함")
            assertTrue(lang.has("item.estherserver.test_ore_ingot"), "test_ore_ingot 번역이 있어야 함")
        }

        @Test
        @DisplayName("한국어 번역이 있어야 함")
        fun koreanTranslationsExist() {
            val lang = loadJsonResource("assets/estherserver/lang/ko_kr.json")

            assertTrue(lang.has("block.estherserver.test_ore"), "test_ore 번역이 있어야 함")
            assertTrue(lang.has("block.estherserver.deepslate_test_ore"), "deepslate_test_ore 번역이 있어야 함")
            assertTrue(lang.has("item.estherserver.test_ore_raw"), "test_ore_raw 번역이 있어야 함")
            assertTrue(lang.has("item.estherserver.test_ore_ingot"), "test_ore_ingot 번역이 있어야 함")
        }
    }
}
