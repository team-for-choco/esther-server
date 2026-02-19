# Phase 1 코드 정리 및 리팩토링

## 작업 개요
Phase 1 완료 후 레거시 코드 제거, 경고 해소, 테스트 유틸 정리를 수행한다.

## 작업 전 요청사항
- 플랜 모드에서 작업 항목 3개로 정리 후 승인받아 진행

## 작업관련 주요 정보
- Jira 인증 실패로 이슈 수동 생성 필요
- 브랜치: `chore/cleanup-phase1`
- 커밋 3개로 분리하여 진행

### 변경 사항
1. **TestCropBlock → CustomCropBlock 전환**: TEST_CROP 등록을 `CustomCropBlock(properties, Supplier { TEST_SEEDS.get() })` + `cropProperties()`로 변경하고 `TestCropBlock.kt` 삭제
2. **example_block/example_item 제거**: 선언, 크리에이티브 탭 등록, 번역, 테스트 assertion 모두 제거. 크리에이티브 탭 아이콘을 TEST_FISH로 변경
3. **테스트 유틸리티 추출**: `TestUtils.kt`에 `loadJsonResource()` + `gson` 공유 함수 생성, 5개 테스트 파일에서 중복 코드 제거

## 작업 체크리스트
- [x] TestCropBlock → CustomCropBlock 전환
- [x] TestCropBlock.kt 삭제
- [x] example_block, example_item 선언 제거
- [x] 크리에이티브 탭 아이콘 변경 (EXAMPLE_ITEM → TEST_FISH)
- [x] addCreative() 메서드 + 리스너 등록 제거
- [x] en_us.json, ko_kr.json example 번역 제거
- [x] ItemQualityTest example 관련 assertion 제거
- [x] TestUtils.kt 생성
- [x] 5개 테스트 파일 중복 코드 제거
- [x] `./gradlew build` 성공 확인

## 인게임 테스트
- [x] 로그에서 `No model loaded for default item ID estherserver:example_block` 경고 사라짐 확인
- [x] 크리에이티브 탭 아이콘이 TEST_FISH로 변경 확인
- [x] 모든 기존 아이템/블록 정상 동작 확인
