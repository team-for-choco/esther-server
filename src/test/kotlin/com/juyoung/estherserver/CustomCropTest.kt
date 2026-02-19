package com.juyoung.estherserver

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * 커스텀 작물 관련 리소스 파일 테스트
 *
 * 마인크래프트 환경 없이 JSON 파일 형식과 필수 필드를 검증합니다.
 */
class CustomCropTest {

    @Nested
    @DisplayName("블록스테이트 테스트")
    inner class BlockStateTests {

        @Test
        @DisplayName("test_crop 블록스테이트가 8단계(age 0~7)를 모두 포함해야 함")
        fun blockstateHasAllAges() {
            val blockstate = loadJsonResource("assets/estherserver/blockstates/test_crop.json")

            assertTrue(blockstate.has("variants"), "variants 필드가 있어야 함")
            val variants = blockstate.getAsJsonObject("variants")

            for (age in 0..7) {
                assertTrue(variants.has("age=$age"), "age=$age 가 있어야 함")
                val variant = variants.getAsJsonObject("age=$age")
                assertTrue(variant.has("model"), "age=$age 에 model 필드가 있어야 함")
                assertTrue(
                    variant.get("model").asString.startsWith("estherserver:block/test_crop_stage"),
                    "age=$age 모델이 estherserver:block/test_crop_stage 로 시작해야 함"
                )
            }
        }

        @Test
        @DisplayName("블록스테이트가 4개의 모델 단계를 사용해야 함")
        fun blockstateUsesFourStages() {
            val blockstate = loadJsonResource("assets/estherserver/blockstates/test_crop.json")
            val variants = blockstate.getAsJsonObject("variants")

            val models = (0..7).map { age ->
                variants.getAsJsonObject("age=$age").get("model").asString
            }.toSet()

            assertEquals(4, models.size, "4개의 서로 다른 모델 단계가 있어야 함")
        }
    }

    @Nested
    @DisplayName("블록 모델 테스트")
    inner class BlockModelTests {

        @Test
        @DisplayName("test_crop_stage0 블록 모델이 올바른 형식이어야 함")
        fun stage0ModelIsValid() {
            val model = loadJsonResource("assets/estherserver/models/block/test_crop_stage0.json")

            assertEquals("minecraft:block/crop", model.get("parent").asString, "parent가 minecraft:block/crop이어야 함")
            assertTrue(model.has("textures"), "textures 필드가 있어야 함")

            val textures = model.getAsJsonObject("textures")
            assertTrue(textures.has("crop"), "crop 텍스처 키가 있어야 함")
        }

        @Test
        @DisplayName("모든 블록 모델이 밀 텍스처를 참조해야 함")
        fun allStagesUseWheatTexture() {
            for (stage in 0..3) {
                val model = loadJsonResource("assets/estherserver/models/block/test_crop_stage$stage.json")
                val textures = model.getAsJsonObject("textures")
                val crop = textures.get("crop").asString

                assertTrue(
                    crop.startsWith("minecraft:block/wheat_stage"),
                    "stage$stage 가 밀 텍스처를 참조해야 함, 실제: $crop"
                )
            }
        }

        @Test
        @DisplayName("모든 블록 모델의 parent가 minecraft:block/crop이어야 함")
        fun allStagesHaveCropParent() {
            for (stage in 0..3) {
                val model = loadJsonResource("assets/estherserver/models/block/test_crop_stage$stage.json")
                assertEquals(
                    "minecraft:block/crop", model.get("parent").asString,
                    "stage$stage parent가 minecraft:block/crop이어야 함"
                )
            }
        }
    }

    @Nested
    @DisplayName("아이템 모델 테스트")
    inner class ItemModelTests {

        @Test
        @DisplayName("test_seeds 모델 파일이 올바른 형식이어야 함")
        fun testSeedsModelIsValid() {
            val model = loadJsonResource("assets/estherserver/models/item/test_seeds.json")

            assertTrue(model.has("parent"), "parent 필드가 있어야 함")
            assertTrue(model.has("textures"), "textures 필드가 있어야 함")

            val textures = model.getAsJsonObject("textures")
            assertTrue(textures.has("layer0"), "layer0 텍스처가 있어야 함")
            assertEquals("minecraft:item/wheat_seeds", textures.get("layer0").asString)
        }

        @Test
        @DisplayName("test_harvest 모델 파일이 올바른 형식이어야 함")
        fun testHarvestModelIsValid() {
            val model = loadJsonResource("assets/estherserver/models/item/test_harvest.json")

            assertTrue(model.has("parent"), "parent 필드가 있어야 함")
            assertTrue(model.has("textures"), "textures 필드가 있어야 함")

            val textures = model.getAsJsonObject("textures")
            assertTrue(textures.has("layer0"), "layer0 텍스처가 있어야 함")
            assertEquals("minecraft:item/wheat", textures.get("layer0").asString)
        }

        @Test
        @DisplayName("cooked_test_harvest 모델 파일이 올바른 형식이어야 함")
        fun cookedTestHarvestModelIsValid() {
            val model = loadJsonResource("assets/estherserver/models/item/cooked_test_harvest.json")

            assertTrue(model.has("parent"), "parent 필드가 있어야 함")
            assertTrue(model.has("textures"), "textures 필드가 있어야 함")

            val textures = model.getAsJsonObject("textures")
            assertTrue(textures.has("layer0"), "layer0 텍스처가 있어야 함")
            assertEquals("minecraft:item/bread", textures.get("layer0").asString)
        }
    }

    @Nested
    @DisplayName("Client Item Definition 테스트")
    inner class ClientItemDefinitionTests {

        @Test
        @DisplayName("test_seeds Client Item Definition이 올바른 형식이어야 함")
        fun testSeedsItemDefIsValid() {
            val itemDef = loadJsonResource("assets/estherserver/items/test_seeds.json")

            assertTrue(itemDef.has("model"), "model 필드가 있어야 함")
            val model = itemDef.getAsJsonObject("model")
            assertEquals("minecraft:model", model.get("type").asString)
            assertEquals("estherserver:item/test_seeds", model.get("model").asString)
        }

        @Test
        @DisplayName("test_harvest Client Item Definition이 올바른 형식이어야 함")
        fun testHarvestItemDefIsValid() {
            val itemDef = loadJsonResource("assets/estherserver/items/test_harvest.json")

            assertTrue(itemDef.has("model"), "model 필드가 있어야 함")
            val model = itemDef.getAsJsonObject("model")
            assertEquals("minecraft:model", model.get("type").asString)
            assertEquals("estherserver:item/test_harvest", model.get("model").asString)
        }

        @Test
        @DisplayName("cooked_test_harvest Client Item Definition이 올바른 형식이어야 함")
        fun cookedTestHarvestItemDefIsValid() {
            val itemDef = loadJsonResource("assets/estherserver/items/cooked_test_harvest.json")

            assertTrue(itemDef.has("model"), "model 필드가 있어야 함")
            val model = itemDef.getAsJsonObject("model")
            assertEquals("minecraft:model", model.get("type").asString)
            assertEquals("estherserver:item/cooked_test_harvest", model.get("model").asString)
        }
    }

    @Nested
    @DisplayName("루트테이블 테스트")
    inner class LootTableTests {

        @Test
        @DisplayName("작물 루트테이블이 올바른 형식이어야 함")
        fun cropLootTableIsValid() {
            val lootTable = loadJsonResource("data/estherserver/loot_table/blocks/test_crop.json")

            assertEquals("minecraft:block", lootTable.get("type").asString, "type이 minecraft:block이어야 함")
            assertTrue(lootTable.has("pools"), "pools 필드가 있어야 함")

            val pools = lootTable.getAsJsonArray("pools")
            assertTrue(pools.size() >= 1, "최소 1개의 풀이 있어야 함")

            // Verify match_tool condition (special hoe required)
            val firstPool = pools[0].asJsonObject
            assertTrue(firstPool.has("conditions"), "풀에 조건이 있어야 함")
            val poolConditions = firstPool.getAsJsonArray("conditions")
            val matchTool = poolConditions[0].asJsonObject
            assertEquals("minecraft:match_tool", matchTool.get("condition").asString, "match_tool 조건이어야 함")
        }

        @Test
        @DisplayName("완전 성장 시 수확물을 드롭해야 함")
        fun dropsHarvestWhenFullyGrown() {
            val lootTable = loadJsonResource("data/estherserver/loot_table/blocks/test_crop.json")
            val pools = lootTable.getAsJsonArray("pools")
            val firstPool = pools[0].asJsonObject

            val entries = firstPool.getAsJsonArray("entries")
            val alternatives = entries[0].asJsonObject

            assertEquals("minecraft:alternatives", alternatives.get("type").asString)

            val children = alternatives.getAsJsonArray("children")
            val harvestEntry = children[0].asJsonObject
            assertEquals("estherserver:test_harvest", harvestEntry.get("name").asString)
        }

        @Test
        @DisplayName("미성장 시 씨앗만 드롭해야 함")
        fun dropsSeedsWhenNotFullyGrown() {
            val lootTable = loadJsonResource("data/estherserver/loot_table/blocks/test_crop.json")
            val pools = lootTable.getAsJsonArray("pools")
            val firstPool = pools[0].asJsonObject

            val entries = firstPool.getAsJsonArray("entries")
            val alternatives = entries[0].asJsonObject
            val children = alternatives.getAsJsonArray("children")

            val seedEntry = children[1].asJsonObject
            assertEquals("estherserver:test_seeds", seedEntry.get("name").asString)
        }
    }

    @Nested
    @DisplayName("레시피 테스트")
    inner class RecipeTests {

        @Test
        @DisplayName("제련 레시피가 올바른 형식이어야 함")
        fun smeltingRecipeIsValid() {
            val recipe = loadJsonResource("data/estherserver/recipe/cooked_test_harvest_from_smelting.json")

            assertEquals("minecraft:smelting", recipe.get("type").asString)
            assertEquals("estherserver:test_harvest", recipe.get("ingredient").asString)

            val result = recipe.getAsJsonObject("result")
            assertEquals("estherserver:cooked_test_harvest", result.get("id").asString)

            assertEquals(200, recipe.get("cookingtime").asInt, "제련 시간이 200이어야 함")
            assertTrue(recipe.get("experience").asFloat > 0, "경험치가 0보다 커야 함")
        }

        @Test
        @DisplayName("훈연 레시피가 올바른 형식이어야 함")
        fun smokingRecipeIsValid() {
            val recipe = loadJsonResource("data/estherserver/recipe/cooked_test_harvest_from_smoking.json")

            assertEquals("minecraft:smoking", recipe.get("type").asString)
            assertEquals("estherserver:test_harvest", recipe.get("ingredient").asString)

            val result = recipe.getAsJsonObject("result")
            assertEquals("estherserver:cooked_test_harvest", result.get("id").asString)

            assertEquals(100, recipe.get("cookingtime").asInt, "훈연 시간이 100이어야 함")
        }

        @Test
        @DisplayName("캠프파이어 레시피가 올바른 형식이어야 함")
        fun campfireRecipeIsValid() {
            val recipe = loadJsonResource("data/estherserver/recipe/cooked_test_harvest_from_campfire.json")

            assertEquals("minecraft:campfire_cooking", recipe.get("type").asString)
            assertEquals("estherserver:test_harvest", recipe.get("ingredient").asString)

            val result = recipe.getAsJsonObject("result")
            assertEquals("estherserver:cooked_test_harvest", result.get("id").asString)

            assertEquals(600, recipe.get("cookingtime").asInt, "캠프파이어 시간이 600이어야 함")
        }

        @Test
        @DisplayName("훈연 레시피가 제련보다 빨라야 함")
        fun smokingIsFasterThanSmelting() {
            val smelting = loadJsonResource("data/estherserver/recipe/cooked_test_harvest_from_smelting.json")
            val smoking = loadJsonResource("data/estherserver/recipe/cooked_test_harvest_from_smoking.json")

            val smeltingTime = smelting.get("cookingtime").asInt
            val smokingTime = smoking.get("cookingtime").asInt

            assertTrue(smokingTime < smeltingTime, "훈연($smokingTime)이 제련($smeltingTime)보다 빨라야 함")
        }

        @Test
        @DisplayName("캠프파이어 레시피가 제련보다 느려야 함")
        fun campfireIsSlowerThanSmelting() {
            val smelting = loadJsonResource("data/estherserver/recipe/cooked_test_harvest_from_smelting.json")
            val campfire = loadJsonResource("data/estherserver/recipe/cooked_test_harvest_from_campfire.json")

            val smeltingTime = smelting.get("cookingtime").asInt
            val campfireTime = campfire.get("cookingtime").asInt

            assertTrue(campfireTime > smeltingTime, "캠프파이어($campfireTime)가 제련($smeltingTime)보다 느려야 함")
        }
    }

    @Nested
    @DisplayName("언어 파일 테스트")
    inner class LanguageTests {

        @Test
        @DisplayName("영어 번역이 있어야 함")
        fun englishTranslationsExist() {
            val lang = loadJsonResource("assets/estherserver/lang/en_us.json")

            assertTrue(lang.has("block.estherserver.test_crop"), "test_crop 블록 번역이 있어야 함")
            assertTrue(lang.has("item.estherserver.test_seeds"), "test_seeds 번역이 있어야 함")
            assertTrue(lang.has("item.estherserver.test_harvest"), "test_harvest 번역이 있어야 함")
            assertTrue(lang.has("item.estherserver.cooked_test_harvest"), "cooked_test_harvest 번역이 있어야 함")
        }

        @Test
        @DisplayName("한국어 번역이 있어야 함")
        fun koreanTranslationsExist() {
            val lang = loadJsonResource("assets/estherserver/lang/ko_kr.json")

            assertTrue(lang.has("block.estherserver.test_crop"), "test_crop 블록 번역이 있어야 함")
            assertTrue(lang.has("item.estherserver.test_seeds"), "test_seeds 번역이 있어야 함")
            assertTrue(lang.has("item.estherserver.test_harvest"), "test_harvest 번역이 있어야 함")
            assertTrue(lang.has("item.estherserver.cooked_test_harvest"), "cooked_test_harvest 번역이 있어야 함")
        }
    }
}
