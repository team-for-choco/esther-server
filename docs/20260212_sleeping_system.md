# 슬리핑 시스템 구현

## 작업 개요
1명만 자도 밤을 넘기는 슬리핑 시스템 구현. 수면 시 채팅 알림 + 아침 알림.

- JIRA: CHOCO-81
- 브랜치: `feature/CHOCO-81`

## 작업 전 요청사항
- NeoForge 1.21.4의 수면 이벤트(`CanPlayerSleepEvent`, `SleepFinishedTimeEvent`) 활용
- `playersSleepingPercentage` gamerule을 0으로 설정하여 1명만 자도 밤 넘기기 가능

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보
- `CanPlayerSleepEvent`에서 `problem == null`이면 수면 진행 직전 시점. 이때 본인은 아직 isSleeping이 아니므로 +1 처리
- `SleepFinishedTimeEvent`는 충분한 플레이어가 자서 밤이 넘어갈 때 발생
- gamerule 설정은 `ServerStartingEvent`에서 수행
- 번역 키를 통해 한글/영어 메시지 지원

## 작업 체크리스트
- [x] 작업 문서 생성
- [x] `SleepHandler.kt` 구현
- [x] `EstherServerMod.kt` 수정 (gamerule + 이벤트 등록)
- [x] 번역 키 추가 (`ko_kr.json`, `en_us.json`)
- [x] `./gradlew build` 확인
- [ ] 커밋 & Push & PR

## 인게임 테스트
- [ ] `/gamerule playersSleepingPercentage` → 0 확인
- [ ] 침대 눕기 → 채팅에 "X님이 잠자리에 들었습니다 (1/1)" 표시
- [ ] 1명 수면으로 밤 넘어감 확인
- [ ] 밤 넘긴 후 채팅에 "좋은 아침입니다!" 표시
