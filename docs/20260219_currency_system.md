# 화폐 시스템 (Phase 3-1)

## 작업 개요
잔고 기반 단일 화폐 "에스더의 기운"을 도입하여 아이템 판매로 수익을 얻고, 플레이어 간 송금이 가능한 경제 기반을 구축한다.

CHOCO-92

## 작업 전 요청사항
- Phase 2.5 (청크 클레임) 완료 상태
- 기존 커스텀 아이템 + 바닐라 아이템 가격표 확정

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보

### 설계 결정
- **화폐 형태**: 잔고(계좌) 시스템 (물리 아이템 아님)
- **화폐 이름**: 에스더의 기운
- **화폐 단위**: 단일 (Long 타입)
- **초기 잔액**: 0
- **획득 경로**: 아이템 판매 (`/sell`)
- **판매 대상**: 커스텀 아이템 + 바닐라 아이템

### 등급별 배율
| 등급 | 배율 |
|------|------|
| Common | x1.0 |
| Fine | x1.5 |
| Rare | x3.0 |

### 신규 파일 (economy 패키지)
| 파일 | 역할 |
|------|------|
| `BalanceData.kt` | 잔고 데이터 (toNBT/fromNBT, STREAM_CODEC) |
| `ModEconomy.kt` | AttachmentType 등록 |
| `EconomyHandler.kt` | 서버 핸들러: 잔고 CRUD + 동기화 |
| `EconomyPayloads.kt` | BalanceSyncPayload (서버→클라이언트) |
| `EconomyClientHandler.kt` | 클라이언트 잔고 캐시 |
| `BalanceHudOverlay.kt` | HUD 오버레이 렌더링 |
| `ItemPriceRegistry.kt` | 아이템 가격표 |
| `MoneyCommand.kt` | `/money`, `/money pay`, `/money admin` |
| `SellCommand.kt` | `/sell` |

### 수정 파일
| 파일 | 변경 내용 |
|------|----------|
| `EstherServerMod.kt` | ModEconomy 등록, 페이로드/커맨드/핸들러 등록 |
| `Config.kt` | HUD 위치 설정값 추가 |
| `lang/ko_kr.json` | 번역 키 추가 |
| `lang/en_us.json` | 번역 키 추가 |

### 명령어
```
/money                              — 내 잔고 확인
/money pay <player> <amount>        — 송금
/money admin add <player> <amount>  — (OP) 지급
/money admin remove <player> <amount> — (OP) 회수
/money admin set <player> <amount>  — (OP) 설정
/sell                               — 손에 든 아이템 스택 전체 판매
```

## 작업 체크리스트
- [x] Jira 이슈 생성 (CHOCO-92)
- [x] 브랜치 생성 (feature/CHOCO-92)
- [x] 문서 생성
- [x] BalanceData.kt + ModEconomy.kt
- [x] EconomyPayloads.kt + EconomyClientHandler.kt
- [x] EconomyHandler.kt
- [x] ItemPriceRegistry.kt
- [x] MoneyCommand.kt + SellCommand.kt
- [x] BalanceHudOverlay.kt + Config.kt 수정
- [x] EstherServerMod.kt 통합
- [x] 번역 키 추가
- [x] 빌드 확인

## 인게임 테스트
- [x] 첫 접속 시 잔고 0
- [x] HUD에 잔고 표시
- [x] `/sell`로 아이템 판매 → 잔고 증가 + HUD 갱신
- [x] 등급별 판매 가격 차이 확인
- [ ] `/money pay` 송금 → 양쪽 잔고 변경 (멀티플레이어 필요)
- [x] `/money admin add/remove/set` 동작
- [x] 사망 후 잔고 유지 (copyOnDeath)
- [x] 서버 재시작 후 잔고 유지
- [x] F3 화면에서 HUD 숨김
