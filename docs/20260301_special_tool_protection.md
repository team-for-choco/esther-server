# 특수 도구 사망 보존 + 드롭/이동 제한 + 전용 도구 슬롯

## 작업 개요
특수 도구 4종(낚싯대/호미/곡괭이/조리도구)은 강화 레벨이 누적되는 핵심 장비.
사망 시 일반 아이템처럼 드롭되어 분실 위험이 있고, Q키/드래그로 드롭하거나 상자 등에 넣을 수 있어 양도/분실 가능.

**목표**: 사망 보존 + 드롭 불가 + 상자 이동 불가 + 전문 보관함 도구 슬롯에만 보관 가능

## 작업 전 요청사항
- CHOCO-128

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보
- `EnhancementHandler.EQUIPMENT_MAP` 기반으로 특수 도구 판별
- 사망 시 리스폰 복원: `PlayerEvent.Clone` 이벤트 활용
- Mixin으로 외부 컨테이너 이동 차단 (Slot.mayPlace + AbstractContainerMenu.doClick)

## 작업 체크리스트
- [ ] ProfessionInventoryHandler: isSpecialTool + 이벤트 핸들러 3종
- [ ] ProfessionInventoryData: 도구 슬롯 데이터 + 직렬화
- [ ] ProfessionToolSlot: 새 슬롯 클래스
- [ ] ProfessionInventoryMenu: 도구 슬롯 통합
- [ ] ProfessionInventoryContainerScreen: GUI 업데이트
- [ ] SpecialToolSlotMixin: Slot.mayPlace 인젝트
- [ ] SpecialToolContainerMixin: doClick 인젝트
- [ ] 번역 키 추가
- [ ] 빌드 성공

## 인게임 테스트
- [ ] keepInventory=false 사망 → 특수 도구 유지, 일반 아이템 드롭
- [ ] Q키로 특수 도구 드롭 → 차단 메시지
- [ ] 인벤토리 밖 드래그 → 차단
- [ ] 상자에 특수 도구 넣기(클릭/Shift) → 차단
- [ ] 번호키로 상자 슬롯과 교환 → 차단
- [ ] 전문 보관함 도구 슬롯에 넣기 → 허용
- [ ] 도구 슬롯에서 다시 인벤토리로 꺼내기 → 허용
