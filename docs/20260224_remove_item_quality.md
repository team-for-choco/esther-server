# Phase 5-1b: ItemQuality 품질 시스템 제거

## 작업 개요
Phase 5에서 "아이템에 랜덤 품질 부여" 방식을 완전히 제거한다.
기존의 `ItemQuality`(COMMON/FINE/RARE 랜덤 부여) 시스템을 삭제하고,
도감(`CollectibleRegistry`)을 품질 없는 단일 등록 구조로 재작성한다.

## 작업 전 요청사항
- JIRA: CHOCO-101
- 브랜치: `feature/CHOCO-101`

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보

### 삭제 대상 파일
- `ItemQuality.kt` — enum 전체
- `CookingQualityCalculator.kt` — 요리 품질 계산기
- `data/estherserver/tags/item/has_quality.json` — 품질 태그
- `data/estherserver/loot_modifiers/assign_quality.json` — 루트 모디파이어 JSON

### 리팩토링
- `AssignQualityLootModifier` → `ProfessionLootModifier`로 이름 변경
  - 품질 부여 로직 제거, grade filtering + XP(고정 1) + 장비 효과 유지
- `CollectibleRegistry`: 품질별 3항목 → 아이템당 1항목 (48→18)
- `CollectionKey`: quality 필드 제거
- XP: `getXpForQuality(quality)` → 고정 1 XP
- 가격: quality multiplier 제거, base price만 사용

## 작업 체크리스트
- [x] JIRA 이슈 생성 (CHOCO-101)
- [x] 브랜치 생성 (feature/CHOCO-101)
- [x] 문서 생성
- [ ] 핵심 품질 코드 삭제
- [ ] ModDataComponents에서 ITEM_QUALITY 제거
- [ ] AssignQualityLootModifier → ProfessionLootModifier 리팩토링
- [ ] 리소스 파일 정리
- [ ] 도감 시스템 재작성
- [ ] 요리/경제/UI 시스템에서 품질 제거
- [ ] 언어 파일 정리
- [ ] 테스트 파일 정리
- [ ] `./gradlew build` 성공 확인
- [ ] grep으로 잔여 참조 확인

## 인게임 테스트
- [x] 낚시 시 물고기 드롭 확인 (품질 없이)
- [x] 작물 수확 시 드롭 확인 (품질 없이)
- [x] 요리 결과물에 품질 없음 확인
- [x] 도감 등록 정상 작동 확인
- [x] 도감 GUI에서 품질 색상 없이 표시 확인
- [x] 상점 판매 시 base price만 적용 확인
- [x] 툴팁에 품질 표시 안 됨 확인
