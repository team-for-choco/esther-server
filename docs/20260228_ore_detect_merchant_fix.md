# 커스텀 광물 감지 + 상인 목록 보완

## 작업 개요
- **JIRA**: CHOCO-127
- **브랜치**: `feature/CHOCO-127`
- 특수 곡괭이 Lv5의 `OreVeinDetector`가 커스텀 광물 10종(20블록)과 네더 광물(석영, 고대 잔해)을 감지하지 못하는 문제 수정
- `ItemPriceRegistry`에 `gold_nugget`과 `ancient_debris` 판매가 추가

## 작업 전 요청사항
- 없음

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보

### OreVeinDetector 변경
- `BlockState.isCustomOre(vararg ores: DeferredBlock<Block>)` 헬퍼 확장 함수 추가
- 커스텀 광물 10종(일반/고급/딥슬레이트 포함 20블록) + 네더 광물 2종 감지
- 등급별 색상 배정:
  - 일반: 주석(은백), 아연(청회), 옥(연녹)
  - 고급: 은(밝은 은), 루비(빨강), 사파이어(파랑), 티타늄(금속 회)
  - 희귀: 백금(밝은 백금), 오팔(분홍), 탄자나이트(보라)
  - 네더: 석영(크림), 고대 잔해(갈색)

### ItemPriceRegistry 변경
- `gold_nugget`: 2원 (gold_ingot 15 / 9 ≈ 2)
- `ancient_debris`: 100원 (netherite_scrap과 동일, 1:1 제련)

## 작업 체크리스트
- [x] OreVeinDetector 커스텀 광물 감지 추가
- [x] OreVeinDetector 네더 광물 감지 추가
- [x] ItemPriceRegistry gold_nugget 판매가 추가
- [x] ItemPriceRegistry ancient_debris 판매가 추가
- [x] 빌드 성공 확인

## 인게임 테스트
- [ ] 커스텀 광물 근처에서 Lv5 곡괭이 들고 색상별 파티클 표시 확인
- [ ] 네더 석영/고대 잔해 근처에서 파티클 표시 확인
- [ ] 광물 상인에서 gold_nugget 판매 가능 확인
- [ ] 광물 상인에서 ancient_debris 판매 가능 확인
