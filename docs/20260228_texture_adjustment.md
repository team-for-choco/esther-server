# 텍스처 조정 (Phase 11-5)

## 작업 개요
- JIRA: CHOCO-126
- 야생/귀환 포탈을 1칸 → 3칸 높이 멀티블록으로 변경
- 조리대: 솥단지 느낌의 커스텀 3D 모델
- 도감대: 떠있는 책 느낌의 커스텀 3D 모델
- 포탈 파티클 효과 추가

## 작업 전 요청사항
- 포탈 멀티블록 구조: Base(master) + Middle(part 0) + Top(part 1)
- 바닐라 텍스처 재사용으로 커스텀 텍스처 최소화

## 작업 진행 중 결정된 사항
- AbstractPortalBlock 상위 클래스로 공통 로직 추출
- PortalDummyBlock/BE로 더미 블록 구현
- 소파 BE의 masterPos 저장/로드 패턴 재사용
- 조리대/도감대: noOcclusion() 추가로 비-풀큐브 렌더링

## 작업 체크리스트
- [x] Jira 이슈 생성 (CHOCO-126)
- [x] 브랜치 생성 (feature/CHOCO-126)
- [ ] AbstractPortalBlock.kt 신규 생성
- [ ] PortalDummyBlock.kt 신규 생성
- [ ] PortalDummyBlockEntity.kt 신규 생성
- [ ] WildPortalBlock.kt 수정 (AbstractPortalBlock 확장)
- [ ] ReturnPortalBlock.kt 수정 (AbstractPortalBlock 확장)
- [ ] ModWild.kt 수정 (BE 레지스트리 추가)
- [ ] WildTeleportHelper.kt 수정 (3블록 귀환 포탈 배치)
- [ ] EstherServerMod.kt 수정 (더미 블록 등록 + noOcclusion)
- [ ] 포탈 모델/블록스테이트 리소스 생성/수정
- [ ] 조리대 모델 커스텀 elements
- [ ] 도감대 모델 커스텀 elements
- [ ] 텍스처 PNG 생성
- [ ] 언어 파일 업데이트
- [ ] 빌드 성공 확인
- [ ] PR 생성

## 인게임 테스트
- [ ] 야생 포탈 설치 시 3블록 생성 확인
- [ ] 포탈 3블록 모두 우클릭 시 텔레포트 동작 확인
- [ ] 크리에이티브 파괴 시 연쇄 제거 확인
- [ ] 야생 텔레포트 시 귀환 포탈 3블록 생성 확인
- [ ] 조리대 비-풀큐브 모델 렌더링 확인
- [ ] 도감대 비-풀큐브 모델 렌더링 확인
- [ ] 포탈 파티클 효과 확인
