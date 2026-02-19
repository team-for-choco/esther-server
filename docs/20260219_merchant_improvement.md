# 상인 시스템 개선: 카테고리별 전문 상인 + 판매 탭

## 작업 개요
- 범용 상인 → 카테고리별 전문 상인으로 분리 (씨앗/식재료/광물/특수)
- 상점 GUI에 판매 탭 추가 (인벤토리 아이템을 NPC를 통해 판매)
- `/sell` 명령어 삭제 (판매는 NPC 상점 GUI로 통합)
- JIRA: CHOCO-94

## 작업 전 요청사항
- 기존 CHOCO-93 NPC 상인 시스템 완료 상태에서 개선

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보
- 전문 상인은 자기 카테고리 아이템만 구매/판매 취급
- 판매 시 등급(고급 1.5x, 희귀 3.0x) 배율 자동 적용
- Shift+클릭으로 전체 판매

## 작업 체크리스트
- [x] Jira 이슈 생성 + 브랜치 + 문서
- [ ] ItemPriceRegistry — PriceEntry + 카테고리 매핑
- [ ] MerchantEntity — merchantType + NBT
- [ ] ShopPayloads — OpenShopPayload 수정 + SellItemPayload 추가
- [ ] ShopBuyRegistry — handleSell() + 카테고리 검증
- [ ] ShopClientHandler — merchantType 전달
- [ ] ShopScreen — 구매/판매 모드, 카테고리 필터링
- [ ] ShopCommand — 타입 인자
- [ ] EstherServerMod — SellCommand 제거 + SellItemPayload 등록
- [ ] SellCommand 삭제
- [ ] 번역 키 추가
- [ ] 빌드 확인

## 인게임 테스트
- [ ] `/shop summon seeds` → "씨앗 상인" 소환, 이름 표시
- [ ] `/shop summon food` → "식재료 상인" 소환
- [ ] `/shop summon minerals` → "광물 상인" 소환
- [ ] `/shop summon special` → "특수 상인" 소환
- [ ] 씨앗 상인 우클릭 → "씨앗 상점" GUI, 씨앗만 표시
- [ ] 구매 탭에서 아이템 구매 → 잔고 차감 + 아이템 획득
- [ ] 판매 탭 클릭 → 인벤토리의 해당 카테고리 판매 가능 아이템 표시
- [ ] 판매 탭에서 아이템 판매 → 아이템 감소 + 잔고 증가
- [ ] Shift+클릭 → 해당 슬롯 전체 판매
- [ ] 등급 아이템 판매 시 등급 배율 적용 (고급 1.5x, 희귀 3.0x)
- [ ] 다른 카테고리 아이템은 판매 목록에 미표시
- [ ] `/sell` 명령어 삭제 확인
- [ ] 서버 재시작 후 상인 타입 유지 (NBT)
- [ ] `/shop remove`, `/shop remove all` 정상 동작
