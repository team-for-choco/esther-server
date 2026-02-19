# 요리 시스템 멀티플레이어 개선 + 칭호 버그/기능

## 작업 개요
멀티서버 테스트 중 발견된 문제 수정 및 칭호 기능 개선:
1. 요리 시스템: 플레이어별 재료 분리 (A가 넣은 재료에 B가 간섭 불가)
2. 요리 시스템: 요리 재료가 아닌 아이템 투입 방지 (`cooking_ingredient` 태그 검증)
3. 칭호 버그: TAB에서 칭호가 두 번 표시되는 문제 수정
4. 칭호 기능: 캐릭터 머리 위 이름에도 칭호 표시 (Scoreboard Team 활용)

## 작업 전 요청사항
- CHOCO-88

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보

### 요리 재료 분리
- `CookingStationBlockEntity`의 `ingredients`를 `MutableMap<UUID, MutableList<ItemStack>>`로 변경
- NBT 직렬화도 플레이어별로 저장/로드

### 요리 재료 태그
- `cooking_ingredient` 아이템 태그 생성
- 대상: cooked_test_fish, cooked_test_harvest, cooked_rice, red_pepper, spinach

### 칭호 TAB 중복 원인
- `onTabListNameFormat`에서 `player.displayName`을 fallback으로 사용
- `player.displayName`은 이미 `NameFormat` 이벤트에서 칭호가 붙은 상태
- 결과: 칭호가 두 번 적용됨

### 칭호 머리 위 표시 — Scoreboard Team 방식
- `NameFormat` 이벤트는 서버 사이드에서만 동작 (`ServerPlayer` 체크)
- 클라이언트 사이드 네임태그 렌더링에는 적용 안 됨
- Scoreboard Team의 prefix를 사용하면 TAB, 채팅, 머리 위 네임태그 모두 자동 적용
- 기존 `NameFormat`/`TabListNameFormat` 이벤트 핸들러 → Team 기반으로 교체

## 작업 체크리스트
- [x] `CookingStationBlockEntity` 플레이어별 Map 구조 변경 + NBT
- [x] `CookingStationBlock` player.uuid 전달 + cooking_ingredient 태그 검증
- [x] `cooking_ingredient.json` 태그 파일 생성
- [x] `ChatTitleHandler` → Scoreboard Team 기반으로 교체
- [x] `CollectionHandler.handleTitleSelect`에서 Team 업데이트 호출
- [x] 번역키 추가
- [x] `./gradlew build` 성공

## 인게임 테스트
- [x] 멀티: A 플레이어 재료 투입 → B 플레이어 빈 손 우클릭 → A 재료에 접근 불가
- [x] 비재료 아이템(네더라이트 갑옷 등) 투입 시도 → 거부 메시지
- [x] TAB에서 칭호가 한 번만 표시
- [x] 캐릭터 머리 위 이름에 칭호 표시
- [x] 채팅에서 칭호 표시
