# 레벨/장비 효과 시스템 (Phase 4-3)

## 작업 개요
전문 레벨(Lv1~50)과 장비 강화(Lv0~5)에 실제 게임플레이 보너스를 추가한다.
기존에는 곡괭이 채굴 속도만 구현되어 있었으며, 이번 작업으로 모든 분야에 보너스를 부여한다.

## 작업 전 요청사항
- CHOCO-95 (전문 레벨), CHOCO-96 (장비 강화), CHOCO-98 (장비 기능화) 완료 상태

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보

### 장비 강화 효과 (Lv0~5)
| 분야 | 효과 | Lv3 | Lv5 |
|------|------|-----|-----|
| 채광 | 채굴 속도 | 1.5x | 2.0x | (이미 구현)
| 낚시 | Lure 대기시간 감소 | Lure I (20틱) | Lure II (40틱) |
| 농사 | 다수확 확률 | 25% | 50% |
| 요리 | 희귀 등급 확률 상승 | +5% | +10% |

### 전문 레벨 효과
| 레벨 | 효과 |
|------|------|
| Lv10 | XP 획득 +20% |
| Lv20 | 고급 등급 확률 +5% |
| Lv30 | XP 획득 +50% (Lv10과 합산) |
| Lv40 | 희귀 등급 확률 +3% |
| Lv50 | 분야별 고유 (채광: 2배 드롭 30%, 낚시: 2연낚 25%, 농사: 씨앗 보존 30%, 요리: 재료 절약 25%) |

### 수정 파일
| 파일 | 작업 |
|------|------|
| `profession/ProfessionBonusHelper.kt` | **신규** - 보너스 계산 유틸리티 |
| `quality/ItemQuality.kt` | `randomQualityWithBonus()` 추가 |
| `profession/ProfessionHandler.kt` | XP 배율 + 씨앗 매핑 |
| `loot/AssignQualityLootModifier.kt` | 등급/다수확/2배드롭/2연낚/씨앗보존 |
| `item/SpecialFishingRodItem.kt` | `use()` 오버라이드 (Lure) |
| `cooking/CookingQualityCalculator.kt` | 보너스 파라미터 추가 |
| `cooking/CookingStationBlock.kt` | 요리 등급 보너스 + 재료 절약 |
| `lang/ko_kr.json`, `lang/en_us.json` | 번역 키 4개 추가 |

## 작업 체크리스트
- [x] ProfessionBonusHelper 유틸리티 생성
- [x] ItemQuality에 randomQualityWithBonus() 추가
- [x] ProfessionHandler XP 배율 적용 + 씨앗 매핑
- [x] AssignQualityLootModifier 보너스 적용
- [x] SpecialFishingRodItem Lure 효과
- [x] CookingQualityCalculator 보너스 파라미터
- [x] CookingStationBlock 요리 보너스 + 재료 절약
- [x] 번역 키 추가
- [x] ROADMAP.md 업데이트
- [x] 빌드 확인

## 인게임 테스트
- [ ] Lv10 도달 후 XP +20% 보너스 적용 확인
- [ ] Lv20 도달 후 고급 등급 확률 상승 확인
- [ ] Lv50 채광: 2배 드롭 발동 + 메시지 확인
- [ ] Lv50 낚시: 2연낚 발동 + 메시지 확인
- [ ] Lv50 농사: 씨앗 보존 발동 + 메시지 확인
- [ ] Lv50 요리: 재료 절약 발동 + 메시지 확인
- [ ] 장비 Lv3 낚시: Lure 감소 효과 확인
- [ ] 장비 Lv3 농사: 다수확 25% 확인
- [ ] 장비 Lv3 요리: 희귀 등급 +5% 확인
- [ ] 장비 Lv5 각 분야 최대 보너스 확인

CHOCO-100
