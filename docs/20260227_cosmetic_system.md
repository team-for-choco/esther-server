# 치장(코디) 시스템 구현 (Phase 11-1)

## 작업 개요
플레이어가 실제 방어구와 별개로 외형 전용 아이템을 장착하여, 방어구 성능은 유지하면서 외형만 바꿀 수 있는 치장 시스템.

- **JIRA**: CHOCO-122
- **브랜치**: `feature/CHOCO-122`

## 작업 전 요청사항
- Phase 10까지 모든 기능 완료 상태
- 뽑기 시스템(CHOCO-121) 이미 구현됨 → 연동 필요

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보

### 기획 요약
| 항목 | 결정 |
|------|------|
| 장착 단위 | 부위별 개별 (HEAD / CHEST / LEGS / FEET) |
| 아이템 외형 | 가상 ArmorItem + equipment JSON + 커스텀 텍스처 |
| 획득 경로 | 뽑기 전용 (기존 가챠 시스템 연동) |
| GUI | 별도 키바인딩 (C키) |
| 빈 슬롯 | 실제 방어구 없어도 치장만 렌더링됨 |
| 초기 테마 | 고양이 세트 (4부위) |

### 아키텍처
- **CosmeticData**: AttachmentType, copyOnDeath — 해금 목록 + 슬롯별 장착 상태
- **가상 ArmorItem**: 방어력 0, 실제 장착 불가, 렌더링 전용
- **Mixin**: HumanoidArmorLayer.render()에서 치장 장착 시 렌더링 가로채기
- **네트워크**: C→S 요청/장착, S→C 동기화, S→All 브로드캐스트
- **GUI**: Screen 기반, 4개 슬롯 탭 + 해금된 치장 그리드

### 1.21.4 렌더링 방식
- ArmorMaterial(record) → assetId(ResourceKey<EquipmentAsset>) → equipment JSON → 텍스처
- 텍스처 경로: `textures/entity/equipment/humanoid/<name>.png`, `humanoid_leggings/<name>.png`
- Mixin으로 render state의 equipment 필드를 가상 ArmorItem으로 교체

## 작업 체크리스트
- [ ] Step 1: CosmeticData + ModCosmetics + CosmeticRegistry + CosmeticTokenItem + ArmorItem 등록
- [ ] Step 2: CosmeticPayloads + CosmeticHandler + CosmeticClientHandler + CosmeticScreen + 키바인딩
- [ ] Step 3: Mixin (HumanoidArmorLayer) + equipment JSON + 텍스처
- [ ] Step 4: 뽑기 연동 (GachaRegistry에 치장 보상 추가)
- [ ] Step 5: 리소스 (아이템 모델, 언어 파일, 텍스처)
- [ ] 빌드 성공 확인

## 인게임 테스트
- [ ] C키로 치장 GUI 오픈
- [ ] 치장 토큰 사용 → 해금 확인
- [ ] GUI에서 부위별 장착/해제
- [ ] 다이아 투구 착용 + 치장 모자 → 외형은 치장, 방어력은 다이아
- [ ] 방어구 미착용 + 치장 → 치장만 보임
- [ ] 다른 플레이어에게도 치장 보이는지 확인 (멀티플레이어)
- [ ] 사망 후 치장 데이터 유지 확인
