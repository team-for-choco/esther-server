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

## CHOCO-95 — 전문 레벨 시스템 (Phase 4-1a)
> 원본: [20260219_profession_level.md](20260219_profession_level.md)

- [ ] 생선 낚기 → 낚시 경험치 획득 확인
- [ ] 작물 수확 → 농사 경험치 획득 확인
- [ ] 광물 채굴 → 채광 경험치 획득 확인
- [ ] 요리 완성 → 요리 경험치 획득 확인
- [ ] 등급별 경험치 차등 (일반 1 / 고급 3 / 희귀 5)
- [ ] 레벨업 시 채팅 알림
- [ ] 서버 재접속 후 데이터 유지
- [ ] 사망 후 데이터 유지

