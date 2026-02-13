# 청크 클레임 기본 구현 (Phase 2.5-1)

## 작업 개요
토지 증서 아이템으로 청크를 클레임하고, 블록 파괴/설치를 보호하는 기본 시스템.
Phase 2.5-2(행동 제한 설정), 2.5-3(플레이어 초대)은 이후 확장 — 데이터 모델에 여지만 남겨둔다.

**JIRA**: CHOCO-87

## 작업 전 요청사항
- 도감 시스템(Phase A+B)까지 완료된 상태
- SavedData(월드 레벨) 패턴 사용 (기존 AttachmentType과 다른 패턴)

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보

### 핵심 설계
- 청크 소유권은 월드 레벨 데이터 → SavedData로 Overworld에 저장
- 모든 차원에서 Overworld의 SavedData를 공유
- `level.dataStorage.computeIfAbsent(factory, name)`으로 접근

### 데이터 구조
```
ChunkClaimEntry:
  ownerUUID: UUID
  ownerName: String       // 표시용 (오프라인 조회)
  claimedAt: Long         // 게임 틱
  yMin: Int = 0           // 미래 Y축 세분화용 (지금은 미사용)
  yMax: Int = 319

ChunkClaimData (SavedData):
  claims: MutableMap<Long, ChunkClaimEntry>  // key = ChunkPos.toLong()
```

### 토지 증서 아이템
- `LandDeedItem` — `Item` 확장, `use()` 오버라이드
- 우클릭 → 현재 청크 클레임 + 증서 1개 소모
- 스택 크기 16, `Rarity.UNCOMMON` (노란 이름)
- 텍스처: `minecraft:item/map` (임시)

### 블록 보호
- `BlockEvent.BreakEvent` — 비소유자 파괴 차단
- `BlockEvent.EntityPlaceEvent` — 비소유자 설치 차단
- OP(권한 레벨 2+)는 보호 우회
- 미클레임 청크는 누구나 자유

### /claim 명령어
- `/claim info` — 현재 청크 소유 정보
- `/claim remove` — 클레임 해제 + 증서 반환
- `/claim list` — 내 소유 청크 목록

## 작업 체크리스트
- [x] ChunkClaimData.kt — 데이터 모델 + SavedData 직렬화
- [x] ChunkClaimManager.kt — 비즈니스 로직 파사드
- [x] LandDeedItem.kt — 토지 증서 아이템
- [x] EstherServerMod.kt — 아이템 등록 + 크리에이티브 탭
- [x] 리소스 파일 — items/land_deed.json, models/item/land_deed.json
- [x] ClaimProtectionHandler.kt — 블록 보호 이벤트
- [x] ClaimCommand.kt — /claim 명령어
- [x] 번역 파일 업데이트
- [x] 빌드 확인
- [x] 문서/ROADMAP/CLAUDE.md 업데이트

## 인게임 테스트
- [x] 크리에이티브 탭에 토지 증서 표시
- [x] 토지 증서 우클릭 → 청크 클레임 성공 + 증서 소모
- [x] 같은 청크 재클레임 시도 → 에러 메시지 + 증서 미소모
- [x] 비소유자 블록 파괴 → 차단 + 액션바 메시지
- [x] 비소유자 블록 설치 → 차단 + 액션바 메시지
- [x] 소유자 블록 파괴/설치 → 정상 동작
- [x] OP 블록 파괴/설치 → 보호 우회
- [x] 미클레임 청크 → 누구나 자유
- [x] `/claim info` — 클레임/미클레임 청크 정보
- [x] `/claim list` — 소유 청크 목록
- [x] `/claim remove` — 클레임 해제 + 증서 반환
- [x] 서버 재시작 후 클레임 유지 확인
