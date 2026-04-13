# 장비 강화 장인의 기운(천장) 시스템

## 작업 개요
강화 실패 시 '장인의 기운'이 누적되어, 100% 도달 시 다음 강화가 무조건 성공하는 천장 시스템 도입.
운이 나빠도 무한 실패가 없도록 최대 비용 상한선을 보장한다.

CHOCO-155

## 작업 전 요청사항
- 돈 쓸 곳이 너무 많아 경제적 부담이 크다는 유저 피드백
- 로스트아크의 '장인의 기운' 시스템 참고

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보

### 적립 비율
| 구간 | 성공률 | 실패 시 기운 적립 | 최대 시도 횟수 |
|------|--------|-----------------|--------------|
| 0→1 | 100% | - | 1회 (확정) |
| 1→2 | 80% | +20% | 6회 |
| 2→3 | 60% | +15% | 8회 |
| 3→4 | 40% | +10% | 11회 |
| 4→5 | 15% | +5% | 21회 |

### 동작 규칙
- 강화 실패 시: 해당 전문 분야의 장인의 기운이 적립 비율만큼 증가
- 장인의 기운이 100% 이상일 때: 다음 강화 시도 시 무조건 성공 (guaranteed일 때 랜덤 롤 생략)
- 강화 성공 시: 해당 전문 분야의 장인의 기운 0%로 초기화
- 기운은 전문 분야(profession)별로 독립 관리
- 데이터는 플레이어 어태치먼트로 저장 (사망 시 보존)
- 기운 값은 Int(0~100)로 저장하여 부동소수점 오차 방지
- 변경 시 `player.setData()`를 호출하여 저장 보장
- 로그인/리스폰/차원이동 시 자동으로 클라이언트에 동기화

### 구현 범위
1. `EnhancementPityData` — 플레이어 어태치먼트 (분야별 기운 값 Int 저장, readEnum/writeEnum 직렬화)
2. `ModEnhancement` — 어태치먼트 등록 (copyOnDeath)
3. `EnhancementHandler.handleEnhance()` — 기운 적립/소비 로직 + setData() 호출
4. `EnhancementHandler` 이벤트 리스너 — 로그인/리스폰/차원이동 시 기운 동기화 (EVENT_BUS 등록)
5. `EnhancementPitySyncPayload` — 서버→클라이언트 기운 동기화 패킷
6. `EnhancementClientHandler` — 클라이언트 캐시 (getPity, getPityPercent)
7. `EnhancementScreen` — UI에 기운 게이지 바 표시

## 작업 체크리스트
- [x] EnhancementPityData 어태치먼트 생성 (Int 기반)
- [x] ModEnhancement 어태치먼트 등록
- [x] EnhancementHandler에 기운 로직 추가 + setData() 호출
- [x] EnhancementHandler EVENT_BUS 등록 + 로그인/리스폰/차원이동 동기화
- [x] EnhancementPitySyncPayload + EnhancementClientHandler 동기화
- [x] EnhancementScreen에 기운 게이지 UI 추가
- [x] 번역 텍스트 추가
- [x] 빌드 확인

## 인게임 테스트
- [ ] 강화 실패 시 기운 게이지가 올라가는지 확인
- [ ] 기운 100% 도달 시 다음 강화 무조건 성공하는지 확인
- [ ] 성공 후 기운 0%로 초기화되는지 확인
- [ ] 전문 분야별 기운이 독립적인지 확인
- [ ] 서버 재시작 후 기운 데이터 유지 확인
- [ ] 사망 후 기운 데이터 유지 확인
