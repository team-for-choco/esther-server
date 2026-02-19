# NPC 상인 시스템 (Phase 3-3)

## 작업 개요
화폐 시스템(CHOCO-92) 완료 후, 화폐 소비처(골드 싱크)를 만들어 경제 순환을 구축한다.
NPC 엔티티가 서버 기본 상점 역할을 하며, 기본 재료를 별도 가격표(판매가보다 높은 구매가)로 판매한다.

- **JIRA**: CHOCO-93
- **브랜치**: `feature/CHOCO-93`

## 작업 전 요청사항
- Phase 3-1 화폐 시스템 완료 상태에서 진행

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보

### 설계
- NPC 엔티티: PathfinderMob 확장, 빌리저 외형, 무적/부동/영구
- 가격: 별도 구매 가격표 (판매가의 ~2-3배, 골드 싱크)
- 판매 품목: 기본 재료 위주 (씨앗, 식재료, 광물, 토지 증서)
- 재고: 무제한 (서버 기본 상점)

### 구매 가격표

| 카테고리 | 아이템 | 구매가 |
|----------|--------|--------|
| 씨앗 | 밀/호박/수박/비트 씨앗 | 5 |
| 씨앗 | 테스트 씨앗 | 6 |
| 씨앗 | 볍씨, 고추/시금치 씨앗 | 8 |
| 식재료 | 당근, 감자 | 6 |
| 식재료 | 사과, 달콤한 열매 | 8 |
| 식재료 | 빵, 사탕수수 | 10 |
| 식재료 | 구운 대구 | 15 |
| 광물 | 석탄 | 5 |
| 광물 | 구리 원석 | 8 |
| 광물 | 레드스톤 | 8 |
| 광물 | 라피스 라줄리 | 10 |
| 광물 | 철 원석 | 12 |
| 특수 | 토지 증서 | 500 |

### 신규 파일 (merchant 패키지)

| 파일 | 역할 |
|------|------|
| `MerchantEntity.kt` | NPC 엔티티 |
| `MerchantEntityRenderer.kt` | 빌리저 모델 기반 렌더러 |
| `ShopBuyRegistry.kt` | 구매 가격표 + 구매 처리 로직 |
| `ShopPayloads.kt` | OpenShopPayload (S→C), BuyItemPayload (C→S) |
| `ShopClientHandler.kt` | 클라이언트: 상점 화면 열기 |
| `ShopScreen.kt` | 상점 GUI |
| `ShopCommand.kt` | `/shop summon`, `/shop remove` |

### 수정 파일

| 파일 | 변경 내용 |
|------|----------|
| `EstherServerMod.kt` | MERCHANT_ENTITY 등록, 페이로드/커맨드/렌더러 등록, ShopBuyRegistry.init() |
| `lang/ko_kr.json` | 번역 키 추가 |
| `lang/en_us.json` | 번역 키 추가 |

## 작업 체크리스트
- [x] Jira 이슈 생성 (CHOCO-93)
- [x] 브랜치 생성 (feature/CHOCO-93)
- [x] 문서 생성
- [x] ShopBuyRegistry.kt 구현
- [x] ShopPayloads.kt + ShopClientHandler.kt 구현
- [x] MerchantEntity.kt 구현
- [x] MerchantEntityRenderer.kt 구현
- [x] ShopScreen.kt 구현
- [x] ShopCommand.kt 구현
- [x] EstherServerMod.kt 통합
- [x] 번역 키 추가
- [x] 빌드 확인

## 인게임 테스트
- [x] `/shop summon`으로 NPC 소환 → 빌리저 외형 표시
- [x] NPC 우클릭 → 상점 GUI 열림
- [x] 아이템 클릭 구매 → 잔고 차감 + 아이템 획득
- [x] Shift+클릭 다량 구매
- [x] 잔고 부족 시 구매 실패 메시지
- [x] 인벤토리 풀 시 구매 차단 (드롭 대신 차단으로 변경)
- [x] `/shop remove`로 NPC 제거
- [x] 서버 재시작 후 NPC 유지
- [x] NPC 무적/부동 확인 (물 밀림 방지 포함)
