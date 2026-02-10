# 커스텀 광물 + 월드젠 구현

## 작업 개요
테스트 광물 1종(석재/딥슬레이트 변형)을 추가하고, 오버월드에 자동 생성되도록 월드젠을 설정한다.
바닐라 철광석과 유사한 구조 (원석 드롭, 제련→주괴).

**JIRA**: CHOCO-77

## 작업 전 요청사항
- 바닐라 철광석과 유사한 동작 (silk touch/fortune 지원)
- 오버월드 Y -24~56 사이에 자동 생성
- 곡괭이 + 철 도구 이상 필요

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보

### 등록 목록
| ID | 타입 | 설명 |
|----|------|------|
| `test_ore` | Block | 테스트 광석 (석재) |
| `deepslate_test_ore` | Block | 딥슬레이트 테스트 광석 |
| `test_ore_raw` | Item | 테스트 원석 |
| `test_ore_ingot` | Item | 테스트 주괴 |

### 월드젠 설정
- ConfiguredFeature: `minecraft:ore` 타입, stone+deepslate 타겟, vein size 10
- PlacedFeature: Y -24~56 (trapezoid), 청크당 12회
- BiomeModifier: `#c:is_overworld`, `underground_ores` 스텝

### 사용 텍스처 (바닐라)
- test_ore → iron_ore
- deepslate_test_ore → deepslate_iron_ore
- test_ore_raw → raw_iron
- test_ore_ingot → iron_ingot

## 작업 체크리스트
- [x] Jira 이슈 생성 (CHOCO-77)
- [x] 브랜치 생성 (feature/CHOCO-77)
- [x] 문서 생성
- [x] Kotlin 블록/아이템 등록
- [x] Asset 파일 생성 (blockstates, models, items, lang)
- [x] Data 파일 생성 (loot tables, recipes, tags, worldgen)
- [x] 테스트 작성
- [x] 빌드 확인
- [ ] PR 생성

## 인게임 테스트
- [X] 새 월드에서 Y -24~56 사이에 광석 생성 확인
- [X] 곡괭이로 채굴 시 원석 드롭 확인
- [X] silk touch로 채굴 시 광석 블록 드롭 확인
- [X] fortune 적용 확인
- [X] 원석 제련 → 주괴 확인
- [X] 원석 용광로 → 주괴 확인
- [X] 크리에이티브 탭에 아이템 표시 확인
