# 청크 클레임 신뢰 시스템 (플레이어 초대)

## 작업 개요
소유자가 특정 플레이어를 초대하여 자신의 모든 클레임 청크에서 소유자와 동일한 권한으로 활동할 수 있는 신뢰(Trust) 시스템 구현.

## 작업 전 요청사항
- CHOCO-90 (청크 클레임 행동 제한 설정) 완료 필요 → 완료됨

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보
- **소유자 단위 신뢰**: 청크별이 아닌, 소유자의 모든 클레임 청크에 일괄 적용
- 신뢰된 플레이어는 소유자와 동일하게 permissions 무시하고 바로 허용
- 이름 캐시로 오프라인 플레이어도 목록에서 이름 표시 가능
- 기존 데이터 하위 호환: trust 태그 없으면 빈 맵으로 초기화

## 작업 체크리스트
- [x] ChunkClaimData에 신뢰 목록 저장소 추가 (NBT 저장/로드)
- [x] ChunkClaimManager에 신뢰 관련 메서드 추가
- [x] ClaimProtectionHandler 수정 (신뢰 플레이어 보호 우회)
- [x] /claim trust add/remove/list 명령어 추가
- [x] /claim info 출력 보강
- [x] 번역 키 추가 (ko_kr, en_us)
- [x] ./gradlew build 성공

## 인게임 테스트
- [ ] `/claim trust add <player>` → 해당 플레이어가 소유자의 모든 청크에서 파괴/설치/상호작용 가능
- [ ] `/claim trust remove <player>` → 접근 해제
- [ ] `/claim trust list` → 신뢰 목록 표시
- [ ] 자기 자신 초대 방지
- [ ] 기존 데이터 하위 호환성
