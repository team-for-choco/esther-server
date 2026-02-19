# 인게임 테스트 대기 목록

> 코드 구현은 완료되었으나 인게임 테스트가 아직 진행되지 않은 항목들.
> 테스트 완료 시 이 문서에서 해당 항목을 **삭제**한다. (원본 문서에는 체크 표시)

---

## CHOCO-90 — 청크 클레임 행동 제한 설정
> 원본: [20260214_claim_permissions.md](20260214_claim_permissions.md)

- [ ] 기본값: 파괴/설치 차단, 상호작용 허용
- [ ] `/claim settings break allow` → 비소유자 파괴 가능
- [ ] `/claim settings interact deny` → 상호작용 차단 (조리대/도감대는 예외 유지)
- [ ] 기존 데이터 하위 호환성
- [ ] `/claim info` → permissions 상태 표시

## CHOCO-91 — 청크 클레임 신뢰 시스템
> 원본: [20260214_claim_trust.md](20260214_claim_trust.md)

- [ ] `/claim trust add <player>` → 해당 플레이어가 소유자의 모든 청크에서 파괴/설치/상호작용 가능
- [ ] `/claim trust remove <player>` → 접근 해제
- [ ] `/claim trust list` → 신뢰 목록 표시
- [ ] 자기 자신 초대 방지
- [ ] 기존 데이터 하위 호환성

## CHOCO-92 — 화폐 시스템 (Phase 3-1)
> 원본: [20260219_currency_system.md](20260219_currency_system.md)

- [ ] `/money pay` 송금 → 양쪽 잔고 변경 (멀티플레이어 필요)

## CHOCO-99 — PR #29 코드 리뷰 반영
> 원본: [20260220_pr29_review.md](20260220_pr29_review.md)

- [ ] 특수 곡괭이로 광석 채굴 시 속도 보너스 적용 확인
- [ ] 특수 곡괭이로 흙/나무 채굴 시 속도 보너스 미적용 확인
- [ ] 분무기 툴팁에 충전량 정상 표시 확인

## CHOCO-100 — 레벨/장비 효과 시스템 (Phase 4-3)
> 원본: [20260220_level_equipment_effects.md](20260220_level_equipment_effects.md)

- [ ] Lv10 도달 후 XP +20% 보너스 적용 확인
- [ ] Lv20 도달 후 고급 등급 확률 상승 확인
- [ ] Lv50 채광: 2배 드롭 발동 + 메시지 확인
- [ ] Lv50 낚시: 2연낚 발동 + 메시지 확인
- [ ] Lv50 농사: 씨앗 보존 발동 + 메시지 확인
- [ ] Lv50 요리: 재료 절약 발동 + 메시지 확인
- [ ] 장비 Lv3 낚시: Lure 감소 효과 확인
- [ ] 장비 Lv3 농사: 다수확 25% 확인
- [ ] 장비 Lv3 요리: 희귀 등급 +5% 확인
- [ ] 장비 Lv5 각 분야 최대 보너스 확인

