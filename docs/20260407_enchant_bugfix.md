# 인챈트 상인 / 강화 UI 버그 수정

## 작업 개요

CHOCO-143 머지 후 발견된 인게임 버그 2건 수정.

## 버그 1: 강화 성공 후 UI 레벨 미반영

### 증상
대장장이 NPC에서 장비 강화 성공 후 강화 GUI에 레벨이 이전 값으로 유지됨.

### 원인
`EnhancementHandler.handleEnhance()`가 장비를 전문 보관함에서 찾았을 때, 서버에서 데이터 컴포넌트를 업데이트한 뒤 `ProfessionInventoryHandler.syncToClient()`를 호출하지 않아 클라이언트 `cachedData`가 갱신되지 않았음.

일반 인벤토리의 아이템은 Minecraft가 자동으로 슬롯 동기화를 처리하지만, 전문 보관함은 커스텀 AttachmentType이라 수동 sync 필요.

### 수정
`EnhancementHandler.kt` — 강화 성공 시 `ProfessionInventoryHandler.syncToClient(player)` 호출 추가.

---

## 버그 2: 즉시 부여 후 UI "처리 중..." 멈춤

### 증상
인챈트 상인 NPC에서 즉시 부여(500기운) 클릭 후 UI가 "처리 중..." 상태에서 멈추고 버튼이 비활성화된 채로 유지됨.

### 원인
클라이언트는 즉시 부여 버튼 클릭 시 `ScreenState.WAITING`으로 전환하고 서버에 `EnchantRequestPayload(OVERWRITE)`를 전송하는데, 서버가 처리 완료 후 클라이언트에 아무 응답도 보내지 않았음. 선택 부여(CHOOSE)는 `EnchantPreviewPayload`로 상태가 전환되지만, OVERWRITE는 응답 패킷이 없어 WAITING에서 빠져나오지 못했음.

### 수정
- `EnchantMerchantPayloads.kt` — `EnchantDonePayload` 추가 (서버 → 클라이언트)
- `EnchantMerchantHandler.kt` — OVERWRITE 처리 완료(성공/실패 모든 경우) 후 `EnchantDonePayload` 전송
- `EnchantMerchantClientHandler.kt` — `handleDone()` 추가: 화면이 열려 있으면 `onDone()` 호출
- `EnchantMerchantScreen.kt` — `onDone()`: `ScreenState.IDLE`로 복귀
- `EstherServerMod.kt` — `EnchantDonePayload` 등록

## 인게임 테스트

- [ ] 즉시 부여 클릭 후 처리 완료 시 버튼 정상 복귀 확인
- [ ] 즉시 부여 실패(잔액 부족) 시에도 UI 정상 복귀 확인
- [ ] 강화 성공 후 강화 GUI에 레벨 즉시 반영 확인 (전문 보관함에 도구가 있을 때)
