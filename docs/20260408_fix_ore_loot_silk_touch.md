# 특수 광물 루트 테이블 실크터치 조건 포맷 수정

## 작업 개요
특수 광물을 특수 곡괭이로 채굴 시 원광(raw ore) 대신 광물 블록(ore block)이 드롭되는 버그 수정.

CHOCO-145

## 작업 전 요청사항
특수광물을 캐면 광물이 아니라 광물 블록이 나온다는 버그 제보.

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보

### 원인 분석
커스텀 광물 루트 테이블의 실크터치 조건이 Minecraft 1.21.4 이전의 구버전 포맷을 사용하고 있었음.
1.21.4에서 `ItemPredicate`의 인챈트 체크 형식이 변경되어, 구버전 포맷이 항상 참(true)으로 평가됨.
→ 실크터치가 없어도 항상 실크터치 분기(광물 블록 드롭)가 선택되는 문제.

### 포맷 비교
**구버전 (오류):**
```json
"predicate": {
  "enchantments": [{"enchantment": "minecraft:silk_touch", "levels": {"min": 1}}]
}
```

**1.21.4 신버전 (수정):**
```json
"predicate": {
  "predicates": {
    "minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]
  }
}
```

변경 포인트 2가지:
1. 최상위 `enchantments` → `predicates.minecraft:enchantments` 로 이동
2. 배열 내 `enchantment` (단수) → `enchantments` (복수) 로 변경

### 수정 파일
커스텀 광물 루트 테이블 20개:
- `jade_ore.json`, `deepslate_jade_ore.json`
- `silver_ore.json`, `deepslate_silver_ore.json`
- `ruby_ore.json`, `deepslate_ruby_ore.json`
- `sapphire_ore.json`, `deepslate_sapphire_ore.json`
- `tin_ore.json`, `deepslate_tin_ore.json`
- `zinc_ore.json`, `deepslate_zinc_ore.json`
- `titanium_ore.json`, `deepslate_titanium_ore.json`
- `platinum_ore.json`, `deepslate_platinum_ore.json`
- `opal_ore.json`, `deepslate_opal_ore.json`
- `tanzanite_ore.json`, `deepslate_tanzanite_ore.json`

### 참고
바닐라 1.21.4 `deepslate_iron_ore.json` 루트 테이블을 직접 추출하여 포맷 확인.

## 작업 체크리스트
- [x] 원인 분석 (바닐라 1.21.4 루트 테이블 포맷 대조)
- [x] 루트 테이블 20개 일괄 수정
- [x] 문서 작성
- [x] 브랜치 생성 및 커밋
- [x] PR 생성

## 인게임 테스트
- [ ] 특수 곡괭이로 커스텀 광물 채굴 시 원광 드롭 확인
- [ ] 실크터치 특수 곡괭이로 채굴 시 광물 블록 드롭 확인
