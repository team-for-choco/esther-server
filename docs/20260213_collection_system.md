# 도감 시스템 (Collection/Encyclopedia System)

## 작업 개요
생선/작물/광물/요리 수집 도감 GUI 구현.
플레이어가 도감대 블록에 아이템을 등록하면 도감에 기록되고, GUI에서 수집 현황을 열람할 수 있다.
서버 핵심 동력 — 수집 욕구로 플레이어 지속 참여 유도.

- **JIRA**: CHOCO-85 (Phase A), Phase B 별도 이슈 예정
- **Phase**: 2-2
- **참고**: 띵타이쿤 (도감이 서버 핵심 동력), 악어놀이터 (도감→스탯 상승)

## 작업 전 요청사항
- 조리대와 유사한 도감대 블록 방식 (자동 수집 아님)
- 등록 시 아이템 소멸 (공유 방지)
- 등급별 별도 수집
- 바닐라 아이템까지 확장 가능한 구조
- GUI 열기 키는 설정 변경 가능하게

## Phase A (CHOCO-85) — 이번 작업
### 도감대 블록
- 관리자가 공용 공간에 설치 (파괴 불가, 조리대와 동일)
- 아이템 들고 우클릭 → collectible 태그 확인 → 등록 + 아이템 소멸
- 이미 등록된 아이템 → 소멸하지 않음 + "이미 등록되어 있습니다" 메시지
- 아이템별 등록 필요 수량: 구조만 마련, 이번엔 전부 1개

### 수집 대상 (46항목)
| 카테고리 | 아이템 | 품질 | 항목 수 |
|---------|--------|------|--------|
| 생선 | test_fish, cooked_test_fish | 각 3등급 | 6 |
| 농산물 | test_seeds, rice_seeds, red_pepper_seeds, spinach_seeds | 품질 없음 | 4 |
| 농산물 | test_harvest, cooked_test_harvest, rice, cooked_rice, red_pepper, spinach | 각 3등급 | 18 |
| 광물 | test_ore_raw, test_ore_ingot | 각 3등급 | 6 |
| 요리 | spinach_bibimbap, fish_stew, gimbap, harvest_bibimbap | 각 3등급 | 12 |
| **합계** | | | **46** |

### 데이터 저장
- NeoForge `AttachmentType`으로 플레이어별 저장
- `copyOnDeath = true` → 사망 시 유지
- Codec 기반 JSON 직렬화
- 서버 측 저장, 네트워크로 클라이언트 동기화

### CollectionData 구조
```
CollectionData:
  - entries: Map<CollectionKey, CollectionEntry>
  - CollectionKey = (ResourceLocation, ItemQuality?)  // 아이템 + 등급 조합

CollectionEntry:
  - firstDiscoveredAt: Long (게임 틱)
  - count: Int (등록한 횟수, 추후 수량 조건용)

CollectibleDefinition:
  - key: CollectionKey
  - category: CollectionCategory
  - requiredCount: Int (등록에 필요한 수량, 기본 1)
```

### GUI (CollectionScreen)
- **G키**로 어디서든 열기 (키 설정 변경 가능)
- 카테고리 탭: [전체] [생선] [농산물] [광물] [요리]
- 발견된 아이템: 아이콘 + 이름 + 등급 색상
- 미발견 아이템: ? 아이콘 (회색)
- 진행률 표시
- 바닐라 텍스처 재활용 (추후 커스텀 교체)

### 네트워크
| 패킷 | 방향 | 용도 |
|------|------|------|
| `CollectionSyncPayload` | S→C | 로그인/차원이동 시 전체 데이터 동기화 |
| `CollectionUpdatePayload` | S→C | 새 아이템 등록 시 단건 업데이트 |

### 수집 알림
- 도감대에서 등록 성공 시 채팅 메시지: `[도감] {아이템 이름}을(를) 등록했습니다! (12/46)`
- 소리 효과

## Phase B (별도 이슈 예정) — 마일스톤 + 칭호
### 마일스톤 & 칭호
| 마일스톤 | 조건 | 보상 칭호 |
|---------|------|----------|
| 첫 발견 | 1종 수집 | 초보 수집가 |
| 생선 완성 | 생선 전부 | 어부 |
| 농산물 완성 | 농산물 전부 | 농부 |
| 광물 완성 | 광물 전부 | 광부 |
| 요리 완성 | 요리 전부 | 요리사 |
| 절반 달성 | 23종 이상 | 수집가 |
| 전체 완성 | 46종 전부 | 만물박사 |

### 칭호 시스템
- 달성한 칭호 중 하나를 활성 칭호로 설정
- 채팅 메시지 앞에 표시: `[요리사] 플레이어: 안녕!`
- `ServerChatEvent`로 채팅 포맷 수정
- 설정 방법: 도감 GUI 내 또는 명령어

## 작업 진행 중 결정된 사항
- 수집 트리거: 자동 줍기 → 도감대 블록 상호작용으로 변경
- 아이템 소멸: 등록 시 소멸, 이미 등록된 경우 소멸하지 않음
- 등급별 별도 수집: 같은 아이템이라도 등급이 다르면 별도 항목
- 등록 수량: 아이템별 차등 구조, 이번엔 전부 1개
- 확장성: collectible 태그 기반, 추후 바닐라 아이템도 추가 가능
- GUI 열기: G키 (설정 변경 가능), 등록은 도감대 블록에서만
- 스코프 분리: Phase A (도감대+수집+GUI) / Phase B (마일스톤+칭호)

## 작업 체크리스트
### Phase A (CHOCO-85)
- [x] CollectionData + CollectionEntry (데이터 모델, Codec)
- [x] ModCollection (AttachmentType 등록, 수집 대상 정의)
- [x] CollectibleRegistry (수집 대상 46항목 정의)
- [x] CollectionPedestalBlock (도감대 블록)
- [x] CollectionPayloads (네트워크 패킷 2종: Sync, Update)
- [x] CollectionClientHandler (클라이언트 캐시)
- [x] CollectionHandler (서버 이벤트 핸들러)
- [x] EstherServerMod 연동 (등록, 네트워크, 이벤트)
- [x] collectible 태그 JSON
- [x] CollectionScreen (도감 GUI)
- [x] ModKeyBindings — G키 바인딩 추가
- [x] 번역 파일 (ko_kr.json, en_us.json)
- [x] 블록 리소스 (blockstate, model, item definition)
- [x] 빌드 확인

### Phase B (별도 이슈)
- [ ] MilestoneDefinitions (마일스톤/칭호 정의)
- [ ] TitleHandler (채팅 칭호 표시)
- [ ] 도감 GUI에 마일스톤 탭 추가
- [ ] 칭호 선택 기능

## 인게임 테스트

### 도감대 블록
- [x] 도감대 블록이 크리에이티브 탭에 표시됨
- [x] 도감대 설치 가능
- [x] 도감대 파괴 불가 (서바이벌)

### 아이템 등록
- [x] collectible 아이템 들고 도감대 우클릭 → 등록 메시지 + 아이템 소멸
- [x] 등록 불가 아이템 (예: 돌) 들고 우클릭 → "등록할 수 없습니다" 메시지, 소멸 안 함
- [x] 이미 등록된 아이템 재등록 시도 → "이미 등록" 메시지, 소멸 안 함
- [x] 품질 있는 아이템 (일반/고급/희귀) 각각 별도 등록 확인
- [x] 품질 없는 아이템 (씨앗류) 등록 확인
- [x] 품질 태그 대상인데 품질 없는 아이템 → "등급이 없는 아이템" 메시지
- [x] 등록 시 사운드 재생

### 도감 GUI
- [x] G키 → 도감 GUI 열림
- [x] 등록한 아이템이 아이콘으로 표시됨
- [x] 미등록 아이템이 "?"로 표시됨
- [x] 카테고리 탭 전환 (전체/생선/농산물/광물/요리)
- [x] 등록한 아이템에 마우스 호버 → 이름 + 등급 툴팁
- [x] 미등록 아이템에 마우스 호버 → "???" 툴팁
- [x] 진행률 바 + 텍스트 정상 표시
- [x] ESC로 GUI 닫기
- [x] GUI 열린 상태에서 게임 일시정지 안 됨

### 데이터 유지
- [x] 사망 후 리스폰 → 도감 데이터 유지 (G키로 확인)
- [x] 로그아웃 후 재접속 → 도감 데이터 유지
- [x] 차원 이동 (네더/엔드) → 도감 데이터 유지
