# 청크 클레임 행동 제한 설정 (Claim Permission Settings)

## 작업 개요
- **JIRA**: CHOCO-90
- **Phase**: 2.5-2
- 현재 청크 클레임 시스템(CHOCO-87)은 소유자 외 모든 행동을 일괄 차단
- 소유자가 허용/금지 항목을 세부 설정할 수 있도록 확장
- 기본값: 블록 파괴/설치 금지, 상호작용 허용

## 작업 전 요청사항
- 기존 청크 클레임 시스템(CHOCO-87) 완료 상태
- ClaimProtectionHandler에서 break/place/interact 이벤트 처리 중

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보
- ClaimPermissions 데이터 클래스로 3가지 권한 관리 (break, place, interact)
- 기존 데이터 하위 호환: permissions 태그 없으면 기본값 사용
- CookingStation/CollectionPedestal 예외는 permissions.allowInteract와 무관하게 유지

## 작업 체크리스트
- [x] Jira 이슈 생성 (CHOCO-90)
- [x] 브랜치 생성 (feature/CHOCO-90)
- [x] 문서 생성
- [x] ClaimPermissions 데이터 클래스 구현
- [x] ChunkClaimEntry에 permissions 필드 추가
- [x] ChunkClaimManager에 updatePermissions 메서드 추가
- [x] ClaimProtectionHandler permissions 기반 보호 로직
- [x] /claim settings 명령어 추가
- [x] /claim info 출력 보강
- [x] 번역 키 추가
- [x] 빌드 성공 확인

## 인게임 테스트
- [ ] 기본값: 파괴/설치 차단, 상호작용 허용
- [ ] `/claim settings break allow` → 비소유자 파괴 가능
- [ ] `/claim settings interact deny` → 상호작용 차단 (조리대/도감대는 예외 유지)
- [ ] 기존 데이터 하위 호환성
- [ ] `/claim info` → permissions 상태 표시
