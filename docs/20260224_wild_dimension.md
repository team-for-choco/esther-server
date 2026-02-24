# 거점/야생 차원 분리 (Phase 6)

## 작업 개요
오버월드를 안전 거점으로, 커스텀 차원(야생)을 자원 지역으로 분리.
야생은 리셋 가능하여 콘텐츠 갱신에 용이.

- JIRA: CHOCO-107
- 브랜치: feature/CHOCO-107

## 작업 전 요청사항
- 6-2(거점 제한)는 Phase 5 완료 후 별도 진행

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보
- 야생 차원: `estherserver:wild` (오버월드와 동일한 지형)
- 이동: 전용 포탈 블록 우클릭 방식
  - 야생 포탈 (관리자 설치) → 야생 랜덤 위치
  - 귀환 포탈 (자동 생성) → 오버월드 원래 위치
- 복귀 좌표: AttachmentType (copyOnDeath)
- 안전 위치 탐색: Heightmap.MOTION_BLOCKING_NO_LEAVES, Y>=63, 10회 시도
- 리셋: `/wild reset` (OP 레벨 2), 차원 파일 삭제 방식

## 작업 체크리스트
- [x] Jira 이슈 생성 (CHOCO-107)
- [x] 브랜치 생성 (feature/CHOCO-107)
- [x] 문서 생성
- [x] 야생 차원 JSON (dimension_type, dimension)
- [x] WildDimensionKeys.kt
- [x] WildReturnData.kt + ModWild.kt (AttachmentType)
- [x] WildPortalBlock.kt
- [x] ReturnPortalBlock.kt
- [x] WildTeleportHelper.kt
- [x] WildCommand.kt
- [x] 블록스테이트 + 블록 모델 JSON
- [x] EstherServerMod.kt 수정 (블록/아이템 등록, 크리에이티브 탭, 명령어, AttachmentType)
- [x] ClaimProtectionHandler.kt 수정 (포탈 블록 화이트리스트)
- [x] 번역 키 추가 (ko_kr.json, en_us.json)
- [x] 빌드 성공 확인

## 인게임 테스트
- [ ] 야생 차원이 오버월드와 동일한 지형으로 생성되는지
- [ ] 야생 포탈 우클릭 → 야생 랜덤 위치 텔레포트
- [ ] 착지 위치에 귀환 포탈 블록 자동 생성
- [ ] 귀환 포탈 우클릭 → 오버월드 원래 위치 복귀
- [ ] 오버월드 외 차원에서 야생 포탈 사용 시 차단 메시지
- [ ] 사망 후에도 복귀 좌표 유지 (copyOnDeath)
- [ ] 클레임 청크 내 포탈 블록 상호작용 가능
- [ ] `/wild reset` → 야생 플레이어 대피 + 차원 파일 삭제 + 브로드캐스트
- [ ] 리셋 후 야생 진입 시 새 지형 생성
