# 전문 분야 현황 GUI (Phase 4-1c)

## 작업 개요
전문 분야 레벨/경험치 + 장비 강화 현황을 한눈에 볼 수 있는 전용 GUI 구현.

## 작업 전 요청사항
- 기획 문서: docs/20260219_plan_profession_system.md
- JIRA: CHOCO-97
- 선행 작업: CHOCO-95 (전문 레벨), CHOCO-96 (장비 강화)

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보
- 열기 방식: 키바인드 (도감 키바인드 패턴 재활용)
- ProfessionData 클라이언트 동기화 필요 (AttachmentType은 서버 전용)
- 표시 내용: 분야별 레벨, XP 진행도 바, 장비 강화 레벨/등급, 배율
- 보너스 수치는 4-1d 미정이므로 배율만 표시

## 작업 체크리스트
- [x] ProfessionSyncPayload (서버→클라이언트)
- [x] ProfessionClientHandler (클라이언트 캐시)
- [x] ProfessionHandler에 동기화 트리거 추가 (로그인/리스폰/차원변경/XP획득)
- [x] ProfessionScreen (현황 GUI)
- [x] 키바인드 등록 (J키, ModKeyBindings)
- [x] 번역 키 추가
- [x] EstherServerMod 등록
- [x] 빌드 확인

## 인게임 테스트
- [ ] 키바인드로 전문 분야 GUI 열기
- [ ] 분야별 레벨 + XP 진행도 바 표시
- [ ] 장비 보유 시 강화 레벨 + 등급 표시
- [ ] 장비 미보유 시 "미보유" 표시
- [ ] XP 획득 후 GUI 재열기 시 갱신 반영
- [ ] 서버 재접속 후 데이터 유지
