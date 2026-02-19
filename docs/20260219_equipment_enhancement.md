# 장비 강화 시스템 (Phase 4-1b)

## 작업 개요
전용 장비 4종 + 강화석 + 대장장이 NPC + 강화 GUI 구현.

## 작업 전 요청사항
- 기획 문서: docs/20260219_plan_profession_system.md
- JIRA: CHOCO-96
- 선행 작업: CHOCO-95 (전문 레벨 시스템)

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보
- 전용 장비: 특수 낚시대/호미/곡괭이/조리도구 (스택 1, 인벤토리 보유로 효과)
- 강화 레벨: DataComponent로 저장 (Lv0~5)
- 장비 등급: 일반(Lv0~2), 고급(Lv3~4), 희귀(Lv5)
- 대장장이: MerchantEntity 재활용 (ShopCategory.BLACKSMITH)
- 강화석: 희귀 등급 아이템, 채광/낚시 Lv4+ 장비 보유 시 드롭 (2%)
- 장비 미보유 시 대장장이에서 구매 가능 (5,000 기운)
- 강화 비용: 1,500 / 4,500 / 10,500 / 22,500 / 45,000
- 강화 확률: 100% / 80% / 60% / 40% / 15%
- 강화 실패: 레벨 유지, 비용+재료 소모
- 장비 배율: x1.0 / x1.2 / x1.5 / x2.0 / x2.5 / x3.5 (추후 적용)

## 작업 체크리스트
- [x] ENHANCEMENT_LEVEL DataComponent 등록
- [x] 전용 장비 4종 아이템 등록
- [x] 강화석 아이템 등록
- [x] ShopCategory에 BLACKSMITH 추가
- [x] MerchantEntity BLACKSMITH 분기 처리 (ShopClientHandler에서 라우팅)
- [x] EnhancementHandler (강화 로직)
- [x] EnhancementPayloads (네트워크)
- [x] EnhancementScreen (강화 GUI)
- [x] ShopCommand 대장장이 소환 추가 (ShopCategory enum에 포함되어 자동 서제스션)
- [x] 강화석 드롭 로직 (루트 모디파이어)
- [x] 아이템 툴팁 (강화 레벨 표시)
- [x] 번역 키 추가
- [x] 클라이언트 아이템 정의 JSON
- [x] EstherServerMod 등록
- [x] 빌드 확인

## 인게임 테스트
- [x] `/shop summon blacksmith` → "대장장이" NPC 소환
- [x] 대장장이 우클릭 → 강화 GUI 열림
- [x] 장비 미보유 시 "구매" 버튼으로 장비 구매
- [x] Lv0→Lv1 강화 (100% 성공, 1,500 기운)
- [x] Lv1→Lv2 강화 (80%, 4,500 기운)
- [x] 강화 실패 시 레벨 유지 + 비용 차감
- [x] Lv4→Lv5 강화 시 강화석 필요 + 소멸
- [x] 강화석 미보유 시 강화 불가
- [x] 장비 등급 색상 (일반=흰색, 고급=초록, 희귀=파랑)
- [x] 장비 툴팁에 강화 레벨 표시
- [x] 서버 재시작 후 대장장이 타입 유지
- [x] 잔고 부족 시 강화 불가
