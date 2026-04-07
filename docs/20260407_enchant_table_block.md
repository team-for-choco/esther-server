# 인챈트 테이블/모루 차단

## 작업 개요

인챈트 테이블과 모루를 통한 인챈트를 전면 차단하여, 모든 인챈트 부여를 인챈트 상인 NPC를 통해서만 가능하게 변경한다.

CHOCO-144

## 작업 전 요청사항

- 인챈트 테이블 사용 불가 (모든 아이템)
- 모루에서 인챈트된 책 합치기 불가 (모든 아이템)
- 인챈트 상인 NPC만 인챈트 부여 가능

## 작업 진행 중 결정된 사항

- **인챈트 테이블**: `EnchantmentLevelSetEvent`에서 레벨을 0으로 설정 → 세 옵션 모두 비활성화 (조용히 차단)
- **모루**: `AnvilUpdateEvent`에서 오른쪽 슬롯에 인챈트 관련 아이템(인챈트된 책 or 인챈트된 아이템) 감지 시 취소 → 차단 메시지 출력
  - `STORED_ENCHANTMENTS` (인챈트된 책) 또는 `ENCHANTMENTS` (인챈트된 아이템) 보유 여부로 판단
  - `AnvilUpdateEvent.player`는 `Player` 타입이므로 `ServerPlayer`로 캐스팅하여 메시지 전송
- 이름 변경(오른쪽 슬롯 비어있을 때), 재료로 내구도 수리는 허용

## 작업 체크리스트

- [x] 인챈트 테이블 차단 (EnchantmentLevelSetEvent)
- [x] 모루 인챈트 차단 (AnvilUpdateEvent)
- [x] 차단 메시지 번역 추가 (message.estherserver.enchant_npc_only)

## 인게임 테스트

- [ ] 인챈트 테이블에서 아이템 올려도 인챈트 목록 표시 안 됨 확인
- [ ] 모루에서 인챈트된 책 + 아이템 조합 시 결과 없음 확인
- [ ] 차단 메시지 출력 확인
- [ ] 인챈트 상인 NPC는 정상 작동 확인
