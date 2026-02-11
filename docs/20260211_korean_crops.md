# 한국식 작물 추가 (쌀, 고추, 시금치)

## 작업 개요
3종의 한국식 작물(쌀, 고추, 시금치)을 기존 테스트 작물 패턴 기반으로 추가한다.
재사용 가능한 `CustomCropBlock` 클래스를 생성하여 모든 작물에 적용한다.

CHOCO-79

## 작업 전 요청사항
- 텍스처 19개 준비 필요 (사용자)
  - 블록 텍스처 12개: rice/red_pepper/spinach 각 stage0~3
  - 아이템 텍스처 7개: 씨앗3 + 수확물3 + 조리1(cooked_rice)

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보

### 작물 구성
| 작물 | Block ID | Seeds | Harvest | Cooked |
|------|----------|-------|---------|--------|
| 쌀 | `rice_crop` | `rice_seeds` | `rice` | `cooked_rice` |
| 고추 | `red_pepper_crop` | `red_pepper_seeds` | `red_pepper` | - |
| 시금치 | `spinach_crop` | `spinach_seeds` | `spinach` | - |

### 음식 수치
| 아이템 | nutrition | saturation | 비고 |
|--------|-----------|------------|------|
| `rice` | 1 | 0.3 | 날것 |
| `cooked_rice` | 6 | 0.7 | 밥 |
| `red_pepper` | 2 | 0.3 | 원재료 |
| `spinach` | 2 | 0.4 | 채소류 |

### CustomCropBlock
- seed Supplier를 받는 범용 클래스로, TestCropBlock을 대체하는 패턴
- 기존 TestCropBlock은 그대로 유지 (하위 호환)

## 작업 체크리스트
- [x] Jira 이슈 생성 (CHOCO-79)
- [x] 브랜치 생성 (feature/CHOCO-79)
- [x] Jira 상태 → 진행 중
- [x] 문서 생성
- [x] CustomCropBlock.kt 생성
- [x] EstherServerMod.kt 수정 (블록/아이템 등록)
- [x] Blockstate JSON 3개
- [x] Block model JSON 12개
- [x] Item model JSON 7개
- [x] Client item definition JSON 7개
- [x] Loot table JSON 3개
- [x] Recipe JSON 3개 (쌀 조리)
- [x] has_quality.json 수정
- [x] 언어 파일 수정 (en_us, ko_kr)
- [x] KoreanCropTest.kt 작성
- [x] 빌드 성공 확인
- [x] 커밋 & PR 생성 (PR #10)
- [x] Jira 상태 → 완료

## 인게임 테스트
- [ ] 크리에이티브 탭에서 10개 아이템 확인
- [ ] 씨앗 심기 → 성장 → 수확 (3종 모두)
- [ ] 쌀 조리 (화로/훈연기/캠프파이어)
- [ ] 아이템 등급 부여 확인 (rice, cooked_rice, red_pepper, spinach)
- [ ] 툴팁 표시 확인
