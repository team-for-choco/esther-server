# 등급별 XP 차등 + 전문 보관함 등급 테두리 + 툴팁

## 작업 개요
- JIRA: CHOCO-116
- ProfessionLootModifier에서 등급별 XP 차등 부여 (일반1/고급3/희귀5)
- 전문 보관함 GUI에서 등급별 아이템 테두리 렌더링
- 전문 보관함 슬롯 호버 시 아이템 툴팁 표시

## 작업 전 요청사항
- 없음 (기획 확정)

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보
- `getContentGradeForItem()`: fish/crop/recipe 그레이드 맵 통합 조회
- `getGradeXp()`: COMMON→1, ADVANCED→3, RARE→5
- 등급 테두리는 ADVANCED=초록(GRADE_FINE), RARE=시안(GRADE_RARE), COMMON=없음

## 작업 체크리스트
- [x] ProfessionBonusHelper에 통합 등급 조회 + XP 헬퍼 추가
- [x] ProfessionLootModifier에 등급별 XP 적용
- [x] ProfessionInventoryContainerScreen에 등급 테두리 + 툴팁 추가
- [x] 빌드 통과

## 인게임 테스트
- [ ] 일반 어종 낚시 → XP 1 획득
- [ ] 고급 어종 낚시 → XP 3 획득
- [ ] 희귀 어종 낚시 → XP 5 획득
- [ ] 전문 보관함 고급 아이템 슬롯에 초록 테두리
- [ ] 전문 보관함 희귀 아이템 슬롯에 시안 테두리
- [ ] 전문 보관함 아이템 호버 시 툴팁 표시
- [ ] 상점 구매/판매 탭에서 고급 아이템 초록 테두리 표시
- [ ] 상점 구매/판매 탭에서 희귀 아이템 시안 테두리 표시
- [ ] 상점에서 씨앗에 작물 등급 테두리 표시
- [ ] 호미 Lv0 상태에서 커스텀 씨앗 심기 차단 확인
- [ ] 호미 Lv1에서 고급/희귀 씨앗 심기 차단, 일반 씨앗 심기 가능 확인
- [ ] 호미 등급 부족 시 상점 씨앗 구매 차단 확인
- [ ] 조리대 재료 투입 시점부터 동시 사용 제한 적용 확인
