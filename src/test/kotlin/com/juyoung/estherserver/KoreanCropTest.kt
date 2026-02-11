package com.juyoung.estherserver

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * 한국식 작물 리소스 파일 테스트
 *
 * 마인크래프트 환경 없이 JSON 파일 형식과 필수 필드를 검증합니다.
 */
class KoreanCropTest {

    companion object {
        val CROPS = listOf("rice", "red_pepper", "spinach")
        val SEEDS = listOf("rice_seeds", "red_pepper_seeds", "spinach_seeds")
        val HARVESTS = listOf("rice", "red_pepper", "spinach")
        val ALL_ITEMS = listOf("rice_seeds", "rice", "cooked_rice", "red_pepper_seeds", "red_pepper", "spinach_seeds", "spinach")
    }

    @Nested
    @DisplayName("블록스테이트 테스트")
    inner class BlockStateTests {

        @ParameterizedTest(name = "{0}_crop 블록스테이트가 8단계(age 0~7)를 모두 포함해야 함")
        @ValueSource(strings = ["rice", "red_pepper", "spinach"])
        fun blockstateHasAllAges(cropName: String) {
            val blockstate = loadJsonResource("assets/estherserver/blockstates/${cropName}_crop.json")

            assertTrue(blockstate.has("variants"), "$cropName: variants 필드가 있어야 함")
            val variants = blockstate.getAsJsonObject("variants")

            for (age in 0..7) {
                assertTrue(variants.has("age=$age"), "$cropName: age=$age 가 있어야 함")
                val variant = variants.getAsJsonObject("age=$age")
                assertTrue(variant.has("model"), "$cropName: age=$age 에 model 필드가 있어야 함")
                assertTrue(
                    variant.get("model").asString.startsWith("estherserver:block/${cropName}_crop_stage"),
                    "$cropName: age=$age 모델이 estherserver:block/${cropName}_crop_stage 로 시작해야 함"
                )
            }
        }

        @ParameterizedTest(name = "{0}_crop 블록스테이트가 4개의 모델 단계를 사용해야 함")
        @ValueSource(strings = ["rice", "red_pepper", "spinach"])
        fun blockstateUsesFourStages(cropName: String) {
            val blockstate = loadJsonResource("assets/estherserver/blockstates/${cropName}_crop.json")
            val variants = blockstate.getAsJsonObject("variants")

            val models = (0..7).map { age ->
                variants.getAsJsonObject("age=$age").get("model").asString
            }.toSet()

            assertEquals(4, models.size, "$cropName: 4개의 서로 다른 모델 단계가 있어야 함")
        }
    }

    @Nested
    @DisplayName("블록 모델 테스트")
    inner class BlockModelTests {

        @ParameterizedTest(name = "{0}_crop 모든 블록 모델의 parent가 minecraft:block/crop이어야 함")
        @ValueSource(strings = ["rice", "red_pepper", "spinach"])
        fun allStagesHaveCropParent(cropName: String) {
            for (stage in 0..3) {
                val model = loadJsonResource("assets/estherserver/models/block/${cropName}_crop_stage$stage.json")
                assertEquals(
                    "minecraft:block/crop", model.get("parent").asString,
                    "$cropName stage$stage: parent가 minecraft:block/crop이어야 함"
                )
            }
        }

        @ParameterizedTest(name = "{0}_crop 모든 블록 모델이 커스텀 텍스처를 참조해야 함")
        @ValueSource(strings = ["rice", "red_pepper", "spinach"])
        fun allStagesUseCustomTexture(cropName: String) {
            for (stage in 0..3) {
                val model = loadJsonResource("assets/estherserver/models/block/${cropName}_crop_stage$stage.json")
                assertTrue(model.has("textures"), "$cropName stage$stage: textures 필드가 있어야 함")

                val textures = model.getAsJsonObject("textures")
                assertTrue(textures.has("crop"), "$cropName stage$stage: crop 텍스처 키가 있어야 함")

                val crop = textures.get("crop").asString
                assertEquals(
                    "estherserver:block/${cropName}_stage$stage", crop,
                    "$cropName stage$stage: 텍스처 경로가 올바라야 함"
                )
            }
        }

        @ParameterizedTest(name = "{0}_crop 모든 블록 모델이 cutout 렌더 타입이어야 함")
        @ValueSource(strings = ["rice", "red_pepper", "spinach"])
        fun allStagesHaveCutoutRenderType(cropName: String) {
            for (stage in 0..3) {
                val model = loadJsonResource("assets/estherserver/models/block/${cropName}_crop_stage$stage.json")
                assertEquals(
                    "minecraft:cutout", model.get("render_type").asString,
                    "$cropName stage$stage: render_type이 minecraft:cutout이어야 함"
                )
            }
        }
    }

    @Nested
    @DisplayName("아이템 모델 테스트")
    inner class ItemModelTests {

        @ParameterizedTest(name = "{0} 아이템 모델이 올바른 형식이어야 함")
        @ValueSource(strings = ["rice_seeds", "rice", "cooked_rice", "red_pepper_seeds", "red_pepper", "spinach_seeds", "spinach"])
        fun itemModelIsValid(itemName: String) {
            val model = loadJsonResource("assets/estherserver/models/item/$itemName.json")

            assertTrue(model.has("parent"), "$itemName: parent 필드가 있어야 함")
            assertEquals("minecraft:item/generated", model.get("parent").asString, "$itemName: parent가 minecraft:item/generated이어야 함")

            assertTrue(model.has("textures"), "$itemName: textures 필드가 있어야 함")
            val textures = model.getAsJsonObject("textures")
            assertTrue(textures.has("layer0"), "$itemName: layer0 텍스처가 있어야 함")
            assertEquals(
                "estherserver:item/$itemName", textures.get("layer0").asString,
                "$itemName: layer0 텍스처 경로가 올바라야 함"
            )
        }
    }

    @Nested
    @DisplayName("Client Item Definition 테스트")
    inner class ClientItemDefinitionTests {

        @ParameterizedTest(name = "{0} Client Item Definition이 올바른 형식이어야 함")
        @ValueSource(strings = ["rice_seeds", "rice", "cooked_rice", "red_pepper_seeds", "red_pepper", "spinach_seeds", "spinach"])
        fun clientItemDefIsValid(itemName: String) {
            val itemDef = loadJsonResource("assets/estherserver/items/$itemName.json")

            assertTrue(itemDef.has("model"), "$itemName: model 필드가 있어야 함")
            val model = itemDef.getAsJsonObject("model")
            assertEquals("minecraft:model", model.get("type").asString, "$itemName: type이 minecraft:model이어야 함")
            assertEquals(
                "estherserver:item/$itemName", model.get("model").asString,
                "$itemName: model 경로가 올바라야 함"
            )
        }
    }

    @Nested
    @DisplayName("루트테이블 테스트")
    inner class LootTableTests {

        @ParameterizedTest(name = "{0}_crop 루트테이블이 올바른 형식이어야 함")
        @ValueSource(strings = ["rice", "red_pepper", "spinach"])
        fun cropLootTableIsValid(cropName: String) {
            val lootTable = loadJsonResource("data/estherserver/loot_table/blocks/${cropName}_crop.json")

            assertEquals("minecraft:block", lootTable.get("type").asString, "$cropName: type이 minecraft:block이어야 함")
            assertTrue(lootTable.has("pools"), "$cropName: pools 필드가 있어야 함")

            val pools = lootTable.getAsJsonArray("pools")
            assertTrue(pools.size() >= 2, "$cropName: 최소 2개의 풀이 있어야 함 (수확물 + 씨앗 보너스)")
        }

        @ParameterizedTest(name = "{0}_crop 완전 성장 시 수확물을 드롭해야 함")
        @ValueSource(strings = ["rice", "red_pepper", "spinach"])
        fun dropsHarvestWhenFullyGrown(cropName: String) {
            val harvestName = cropName // harvest item name matches crop name
            val lootTable = loadJsonResource("data/estherserver/loot_table/blocks/${cropName}_crop.json")
            val pools = lootTable.getAsJsonArray("pools")
            val firstPool = pools[0].asJsonObject

            val entries = firstPool.getAsJsonArray("entries")
            val alternatives = entries[0].asJsonObject
            assertEquals("minecraft:alternatives", alternatives.get("type").asString)

            val children = alternatives.getAsJsonArray("children")
            val harvestEntry = children[0].asJsonObject
            assertEquals("estherserver:$harvestName", harvestEntry.get("name").asString)

            // Verify age=7 condition
            val conditions = harvestEntry.getAsJsonArray("conditions")
            val condition = conditions[0].asJsonObject
            assertEquals("minecraft:block_state_property", condition.get("condition").asString)
            assertEquals("estherserver:${cropName}_crop", condition.get("block").asString)
        }

        @ParameterizedTest(name = "{0}_crop 미성장 시 씨앗만 드롭해야 함")
        @ValueSource(strings = ["rice", "red_pepper", "spinach"])
        fun dropsSeedsWhenNotFullyGrown(cropName: String) {
            val lootTable = loadJsonResource("data/estherserver/loot_table/blocks/${cropName}_crop.json")
            val pools = lootTable.getAsJsonArray("pools")
            val firstPool = pools[0].asJsonObject

            val entries = firstPool.getAsJsonArray("entries")
            val alternatives = entries[0].asJsonObject
            val children = alternatives.getAsJsonArray("children")

            val seedEntry = children[1].asJsonObject
            assertEquals("estherserver:${cropName}_seeds", seedEntry.get("name").asString)
        }

        @ParameterizedTest(name = "{0}_crop 행운 인챈트 보너스 씨앗 풀이 있어야 함")
        @ValueSource(strings = ["rice", "red_pepper", "spinach"])
        fun hasFortuneBonusSeedPool(cropName: String) {
            val lootTable = loadJsonResource("data/estherserver/loot_table/blocks/${cropName}_crop.json")
            val pools = lootTable.getAsJsonArray("pools")
            val bonusPool = pools[1].asJsonObject

            val entries = bonusPool.getAsJsonArray("entries")
            val seedEntry = entries[0].asJsonObject
            assertEquals("estherserver:${cropName}_seeds", seedEntry.get("name").asString)

            val functions = seedEntry.getAsJsonArray("functions")
            val bonusFunction = functions[0].asJsonObject
            assertEquals("minecraft:apply_bonus", bonusFunction.get("function").asString)
            assertEquals("minecraft:fortune", bonusFunction.get("enchantment").asString)
        }
    }

    @Nested
    @DisplayName("레시피 테스트 (쌀 조리)")
    inner class RecipeTests {

        @Test
        @DisplayName("제련 레시피가 올바른 형식이어야 함")
        fun smeltingRecipeIsValid() {
            val recipe = loadJsonResource("data/estherserver/recipe/cooked_rice_from_smelting.json")

            assertEquals("minecraft:smelting", recipe.get("type").asString)
            assertEquals("estherserver:rice", recipe.get("ingredient").asString)

            val result = recipe.getAsJsonObject("result")
            assertEquals("estherserver:cooked_rice", result.get("id").asString)

            assertEquals(200, recipe.get("cookingtime").asInt, "제련 시간이 200이어야 함")
            assertTrue(recipe.get("experience").asFloat > 0, "경험치가 0보다 커야 함")
        }

        @Test
        @DisplayName("훈연 레시피가 올바른 형식이어야 함")
        fun smokingRecipeIsValid() {
            val recipe = loadJsonResource("data/estherserver/recipe/cooked_rice_from_smoking.json")

            assertEquals("minecraft:smoking", recipe.get("type").asString)
            assertEquals("estherserver:rice", recipe.get("ingredient").asString)

            val result = recipe.getAsJsonObject("result")
            assertEquals("estherserver:cooked_rice", result.get("id").asString)

            assertEquals(100, recipe.get("cookingtime").asInt, "훈연 시간이 100이어야 함")
        }

        @Test
        @DisplayName("캠프파이어 레시피가 올바른 형식이어야 함")
        fun campfireRecipeIsValid() {
            val recipe = loadJsonResource("data/estherserver/recipe/cooked_rice_from_campfire.json")

            assertEquals("minecraft:campfire_cooking", recipe.get("type").asString)
            assertEquals("estherserver:rice", recipe.get("ingredient").asString)

            val result = recipe.getAsJsonObject("result")
            assertEquals("estherserver:cooked_rice", result.get("id").asString)

            assertEquals(600, recipe.get("cookingtime").asInt, "캠프파이어 시간이 600이어야 함")
        }

        @Test
        @DisplayName("훈연 레시피가 제련보다 빨라야 함")
        fun smokingIsFasterThanSmelting() {
            val smelting = loadJsonResource("data/estherserver/recipe/cooked_rice_from_smelting.json")
            val smoking = loadJsonResource("data/estherserver/recipe/cooked_rice_from_smoking.json")

            assertTrue(
                smoking.get("cookingtime").asInt < smelting.get("cookingtime").asInt,
                "훈연이 제련보다 빨라야 함"
            )
        }

        @Test
        @DisplayName("캠프파이어 레시피가 제련보다 느려야 함")
        fun campfireIsSlowerThanSmelting() {
            val smelting = loadJsonResource("data/estherserver/recipe/cooked_rice_from_smelting.json")
            val campfire = loadJsonResource("data/estherserver/recipe/cooked_rice_from_campfire.json")

            assertTrue(
                campfire.get("cookingtime").asInt > smelting.get("cookingtime").asInt,
                "캠프파이어가 제련보다 느려야 함"
            )
        }
    }

    @Nested
    @DisplayName("등급 태그 테스트")
    inner class QualityTagTests {

        @Test
        @DisplayName("has_quality 태그에 한국식 작물 수확물이 포함되어야 함")
        fun hasQualityTagIncludesKoreanCrops() {
            val tag = loadJsonResource("data/estherserver/tags/item/has_quality.json")

            assertFalse(tag.get("replace").asBoolean, "replace가 false여야 함")

            val values = tag.getAsJsonArray("values").map { it.asString }

            assertTrue(values.contains("estherserver:rice"), "rice가 포함되어야 함")
            assertTrue(values.contains("estherserver:cooked_rice"), "cooked_rice가 포함되어야 함")
            assertTrue(values.contains("estherserver:red_pepper"), "red_pepper가 포함되어야 함")
            assertTrue(values.contains("estherserver:spinach"), "spinach가 포함되어야 함")
        }

        @Test
        @DisplayName("씨앗은 등급 태그에 포함되지 않아야 함")
        fun seedsShouldNotHaveQuality() {
            val tag = loadJsonResource("data/estherserver/tags/item/has_quality.json")
            val values = tag.getAsJsonArray("values").map { it.asString }

            assertFalse(values.contains("estherserver:rice_seeds"), "rice_seeds는 포함되지 않아야 함")
            assertFalse(values.contains("estherserver:red_pepper_seeds"), "red_pepper_seeds는 포함되지 않아야 함")
            assertFalse(values.contains("estherserver:spinach_seeds"), "spinach_seeds는 포함되지 않아야 함")
        }
    }

    @Nested
    @DisplayName("언어 파일 테스트")
    inner class LanguageTests {

        @Test
        @DisplayName("영어 번역이 있어야 함")
        fun englishTranslationsExist() {
            val lang = loadJsonResource("assets/estherserver/lang/en_us.json")

            // Blocks
            assertTrue(lang.has("block.estherserver.rice_crop"), "rice_crop 블록 번역이 있어야 함")
            assertTrue(lang.has("block.estherserver.red_pepper_crop"), "red_pepper_crop 블록 번역이 있어야 함")
            assertTrue(lang.has("block.estherserver.spinach_crop"), "spinach_crop 블록 번역이 있어야 함")

            // Seeds
            assertTrue(lang.has("item.estherserver.rice_seeds"), "rice_seeds 번역이 있어야 함")
            assertTrue(lang.has("item.estherserver.red_pepper_seeds"), "red_pepper_seeds 번역이 있어야 함")
            assertTrue(lang.has("item.estherserver.spinach_seeds"), "spinach_seeds 번역이 있어야 함")

            // Harvests
            assertTrue(lang.has("item.estherserver.rice"), "rice 번역이 있어야 함")
            assertTrue(lang.has("item.estherserver.red_pepper"), "red_pepper 번역이 있어야 함")
            assertTrue(lang.has("item.estherserver.spinach"), "spinach 번역이 있어야 함")

            // Cooked
            assertTrue(lang.has("item.estherserver.cooked_rice"), "cooked_rice 번역이 있어야 함")
        }

        @Test
        @DisplayName("한국어 번역이 있어야 함")
        fun koreanTranslationsExist() {
            val lang = loadJsonResource("assets/estherserver/lang/ko_kr.json")

            assertTrue(lang.has("block.estherserver.rice_crop"), "rice_crop 블록 번역이 있어야 함")
            assertTrue(lang.has("block.estherserver.red_pepper_crop"), "red_pepper_crop 블록 번역이 있어야 함")
            assertTrue(lang.has("block.estherserver.spinach_crop"), "spinach_crop 블록 번역이 있어야 함")

            assertTrue(lang.has("item.estherserver.rice_seeds"), "rice_seeds 번역이 있어야 함")
            assertTrue(lang.has("item.estherserver.red_pepper_seeds"), "red_pepper_seeds 번역이 있어야 함")
            assertTrue(lang.has("item.estherserver.spinach_seeds"), "spinach_seeds 번역이 있어야 함")

            assertTrue(lang.has("item.estherserver.rice"), "rice 번역이 있어야 함")
            assertTrue(lang.has("item.estherserver.red_pepper"), "red_pepper 번역이 있어야 함")
            assertTrue(lang.has("item.estherserver.spinach"), "spinach 번역이 있어야 함")

            assertTrue(lang.has("item.estherserver.cooked_rice"), "cooked_rice 번역이 있어야 함")
        }

        @Test
        @DisplayName("한국어 번역 내용이 올바라야 함")
        fun koreanTranslationValues() {
            val lang = loadJsonResource("assets/estherserver/lang/ko_kr.json")

            assertEquals("볍씨", lang.get("item.estherserver.rice_seeds").asString)
            assertEquals("쌀", lang.get("item.estherserver.rice").asString)
            assertEquals("밥", lang.get("item.estherserver.cooked_rice").asString)
            assertEquals("고추 씨앗", lang.get("item.estherserver.red_pepper_seeds").asString)
            assertEquals("고추", lang.get("item.estherserver.red_pepper").asString)
            assertEquals("시금치 씨앗", lang.get("item.estherserver.spinach_seeds").asString)
            assertEquals("시금치", lang.get("item.estherserver.spinach").asString)
        }
    }
}
