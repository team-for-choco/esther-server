# 전문 레벨 시스템 (Phase 4-1a)

## 작업 개요
4분야(낚시/농사/채광/요리) 전문 레벨 시스템 구현.
활동 시 경험치 획득 → 레벨업 → 알림.

## 작업 전 요청사항
- 기획 문서: docs/20260219_plan_profession_system.md
- JIRA: CHOCO-95

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보
- Profession enum: FISHING, FARMING, MINING, COOKING (확장 가능)
- AttachmentType 기반 데이터 저장 (copyOnDeath)
- 경험치: 일반 1xp, 고급 3xp, 희귀 5xp
- 경험치 테이블: 구간별 증가 (총 39,500 XP)
- 활동 감지:
  - 낚시: Global Loot Modifier에서 감지
  - 농사: BlockEvent.BreakEvent (작물 블록)
  - 채광: BlockEvent.BreakEvent (광석 블록)
  - 요리: CookingStationBlock에서 직접 호출

## 작업 체크리스트
- [ ] Profession enum 생성
- [ ] ProfessionData 데이터 클래스 (NBT/StreamCodec)
- [ ] ModProfession AttachmentType 등록
- [ ] ProfessionHandler (경험치 추가/레벨업 로직)
- [ ] 활동 감지 이벤트 연결
  - [ ] 낚시 (Loot Modifier)
  - [ ] 농사 (BlockEvent)
  - [ ] 채광 (BlockEvent)
  - [ ] 요리 (CookingStationBlock)
- [ ] 레벨업 알림 메시지
- [ ] 번역 키 추가
- [ ] EstherServerMod 등록
- [ ] 빌드 확인

## 인게임 테스트
- [ ] 생선 낚기 → 낚시 경험치 획득 확인
- [ ] 작물 수확 → 농사 경험치 획득 확인
- [ ] 광물 채굴 → 채광 경험치 획득 확인
- [ ] 요리 완성 → 요리 경험치 획득 확인
- [ ] 등급별 경험치 차등 (일반 1 / 고급 3 / 희귀 5)
- [ ] 레벨업 시 채팅 알림
- [ ] 서버 재접속 후 데이터 유지
- [ ] 사망 후 데이터 유지
