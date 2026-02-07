# CHOCO-75: 커스텀 테스트 작물 구현

## 작업 개요
테스트용 커스텀 작물(crop)을 구현합니다.
- 바닐라 밀(wheat) 텍스처를 재활용하여 8단계(AGE_7) 성장 작물 추가
- 씨앗, 날것 수확물, 조리 수확물 아이템 등록
- 루트테이블 및 조리 레시피 추가

## 작업 전 요청사항
- 기존 CHOCO-74(커스텀 생선) 구현 패턴을 참고하여 일관성 있게 구현
- 바닐라 밀의 텍스처 및 구조를 그대로 재활용

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보

### 아이템/블록 ID
| 유형 | ID | 한국어 | 영어 |
|------|-----|--------|------|
| 작물 블록 | `test_crop` | 테스트 작물 | Test Crop |
| 씨앗 | `test_seeds` | 테스트 씨앗 | Test Seeds |
| 수확물 (날것) | `test_harvest` | 테스트 수확물 | Test Harvest |
| 수확물 (조리) | `cooked_test_harvest` | 구운 테스트 수확물 | Cooked Test Harvest |

### 주의사항
- 1.21.4에서는 `ItemNameBlockItem`이 제거됨 → `BlockItem` + `useItemDescriptionPrefix()`로 대체
- 블록 모델 텍스처 키는 `"crop"` (아이템의 `"layer0"`과 다름)
- 루트테이블 경로: `loot_table/blocks/` (1.21.4)
- 조리 수확물 텍스처: 바닐라 빵(bread) 재활용
- 밀 성장 모델: stage0~7 → 블록 모델은 stage0~3 (4개, 2단계씩 매핑)

### 생성된 파일
- `block/TestCropBlock.kt` - CropBlock 상속, `getBaseSeedId()` 오버라이드
- `EstherServerMod.kt` - 블록/아이템 등록, 크리에이티브 탭 추가
- `blockstates/test_crop.json` - age 0~7 블록스테이트
- `models/block/test_crop_stage0~3.json` (4개) - 밀 텍스처 참조
- `models/item/test_seeds.json`, `test_harvest.json`, `cooked_test_harvest.json`
- `items/test_seeds.json`, `test_harvest.json`, `cooked_test_harvest.json`
- `lang/en_us.json`, `lang/ko_kr.json` 수정
- `loot_table/blocks/test_crop.json`
- `recipe/cooked_test_harvest_from_{smelting,smoking,campfire}.json`
- `CustomCropTest.kt` - 22개 테스트 케이스

## 작업 체크리스트
- [x] 문서 생성
- [x] TestCropBlock.kt 작성
- [x] EstherServerMod.kt 수정 (블록/아이템 등록, 크리에이티브 탭)
- [x] blockstates/test_crop.json
- [x] models/block/test_crop_stage0~3.json (4개)
- [x] models/item/ (씨앗, 수확물, 조리 수확물 - 3개)
- [x] items/ (씨앗, 수확물, 조리 수확물 - Client Item Definition 3개)
- [x] lang/en_us.json, lang/ko_kr.json 수정
- [x] loot_table/blocks/test_crop.json
- [x] recipe/cooked_test_harvest_from_smelting.json
- [x] recipe/cooked_test_harvest_from_smoking.json
- [x] recipe/cooked_test_harvest_from_campfire.json
- [x] CustomCropTest.kt 작성
- [x] 빌드 확인

## 인게임 테스트 체크리스트

### 1. 아이템 확인
- [ ] 크리에이티브 탭 "Esther Server"에 테스트 씨앗, 테스트 수확물, 구운 테스트 수확물 표시
- [ ] 각 아이템 텍스처 정상 렌더링 (씨앗=밀씨앗, 수확물=밀, 조리=빵)
- [ ] 한국어/영어 이름 정상 표시

### 2. 작물 심기 & 성장
- [ ] 경작지(Farmland)에 테스트 씨앗 우클릭으로 심기
- [ ] 심은 후 작물 블록 정상 렌더링
- [ ] 뼛가루(Bone Meal)로 성장 촉진
- [ ] 8단계(age 0~7) 텍스처 변화 확인

### 3. 수확
- [ ] 완전 성장(age=7) 파괴 시 테스트 수확물 + 테스트 씨앗 드롭
- [ ] 미성장 파괴 시 테스트 씨앗만 드롭

### 4. 조리
- [ ] 화로: 테스트 수확물 → 구운 테스트 수확물 (200틱)
- [ ] 훈연기: 테스트 수확물 → 구운 테스트 수확물 (100틱)
- [ ] 캠프파이어: 테스트 수확물 → 구운 테스트 수확물 (600틱)

### 5. 음식
- [ ] 테스트 수확물 먹기 가능 (허기 2, 포만감 0.3)
- [ ] 구운 테스트 수확물 먹기 가능 (허기 6, 포만감 0.6)

### 유용한 명령어
```
/gamemode creative
/give @s estherserver:test_seeds
/give @s estherserver:test_harvest
/give @s estherserver:cooked_test_harvest
/give @s minecraft:bone_meal
```
