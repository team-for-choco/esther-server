# 일일/주간 퀘스트 시스템

## 작업 개요
플레이어 일일/주간 활동을 유도하고 꾸준한 화폐+음식 수급 경로를 제공하기 위한 퀘스트 시스템 추가.

CHOCO-117

## 작업 전 요청사항
- Phase 7까지 완료된 상태
- 기존 4개 전문 분야(낚시/농사/채광/요리) 연동 필요

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보
- 일일 5개 제공 / 최대 3개 완료 / 매일 자정 KST 리셋
- 주간 5개 제공 / 최대 3개 완료 / 매주 월요일 자정 KST 리셋
- 서버 시드 기반 동일 퀘스트 할당 (수락 불필요)
- 보상: 화폐(에스더의 기운) + 만년 수프
- 새 아이템: 만년 수프 (Hunter's Pot) - nutrition 8, saturation 0.8f
- H키로 퀘스트 GUI 열기

## 작업 체크리스트
- [x] Jira 이슈 생성 (CHOCO-117)
- [x] 브랜치 생성 (feature/CHOCO-117)
- [x] QuestType.kt + QuestPool.kt 구현
- [x] QuestData.kt + ModQuest.kt 구현
- [x] QuestPayloads.kt + QuestClientHandler.kt 구현
- [x] QuestHandler.kt 구현
- [x] 만년 수프 아이템 등록
- [x] EconomyHandler.kt skipQuestTracking 파라미터 추가
- [x] 기존 코드 훅 추가
- [x] QuestCommand.kt 구현
- [x] QuestScreen.kt + H키 바인딩
- [x] EstherServerMod 와이어링
- [x] 언어 파일 + 아이템 모델/텍스처
- [ ] 빌드 성공 확인

## 인게임 테스트
- [ ] H키로 퀘스트 GUI 열기
- [ ] 일일/주간 탭 전환
- [ ] 낚시 → 진행도 증가 확인
- [ ] 농사 → 진행도 증가 확인
- [ ] 채광 → 진행도 증가 확인
- [ ] 요리 → 진행도 증가 확인
- [ ] 판매 → 진행도 증가 확인
- [ ] 도감 등록 → 진행도 증가 확인
- [ ] 화폐 획득 → 진행도 증가 확인
- [ ] 퀘스트 완료 시 보상 수령
- [ ] 3개 수령 후 추가 수령 차단
- [ ] 3/3 보너스 수령
- [ ] 서버 재접속 시 데이터 유지
- [ ] 자정 경과 후 리셋
- [ ] `/quest admin reset` 동작
- [ ] 만년 수프 음식 효과 확인
