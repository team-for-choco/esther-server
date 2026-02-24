# 거점 제한 (Phase 6-2)

## 작업 개요
오버월드를 안전 거점으로 만든다. 자원 채집은 야생 차원(`estherserver:wild`)에서만 가능하도록 오버월드에 4가지 제한을 건다.

- JIRA: CHOCO-108
- 브랜치: `feature/CHOCO-108`

## 작업 전 요청사항
- Phase 6-1(야생 차원 분리)이 완료된 상태에서 진행

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보

### 구현 항목
1. **적대적 몹 스폰 차단** — `EntityJoinLevelEvent`로 오버월드 MONSTER 카테고리 차단
2. **광물 생성 제거** — `OreFeature.place()` Mixin으로 오버월드 광물 생성 차단 (블록 태그 기반)
3. **PvP 비활성화** — `AttackEntityEvent` + `LivingIncomingDamageEvent`로 플레이어 간 피해 차단
4. **폭발 피해 차단** — `ExplosionEvent.Detonate`로 블록 파괴 + 엔티티 피해 방지

### 기술 결정
- 광물 차단은 BiomeModifier가 아닌 Mixin 사용 (야생과 오버월드가 같은 바이옴 공유)
- NeoForge 1.21.4에서 `LivingAttackEvent` → `LivingIncomingDamageEvent`로 변경됨
- 몹/폭발 차단은 무음 처리, PvP만 공격자에게 메시지 표시

## 작업 체크리스트
- [x] Jira 이슈 생성 (CHOCO-108)
- [x] 브랜치 생성
- [x] 문서 생성
- [x] `OverworldProtectionHandler.kt` 구현
- [x] `OreFeatureMixin.java` 구현
- [x] `overworld_blocked_ores.json` 블록 태그 생성
- [x] `estherserver.mixins.json` 업데이트
- [x] `EstherServerMod.kt` 이벤트 등록
- [x] 번역 키 추가 (ko_kr, en_us)
- [ ] 빌드 확인

## 인게임 테스트
- [ ] 오버월드에서 적대적 몹(좀비, 크리퍼 등) 자연 스폰 안 됨
- [ ] 오버월드에서 동물(소, 양, 닭 등) 정상 스폰
- [ ] 야생 차원에서 적대적 몹 정상 스폰
- [ ] 오버월드 새 청크에 광물(바닐라+커스텀) 없음
- [ ] 야생 차원에서 광물 정상 생성
- [ ] 오버월드에서 플레이어 공격 시 차단 + 메시지
- [ ] 오버월드에서 화살/투사체 PvP 차단
- [ ] 야생에서 PvP 정상 작동
- [ ] 오버월드에서 TNT 폭발 시 블록 파괴 없음
- [ ] 오버월드에서 폭발 엔티티 피해 없음
- [ ] 야생에서 폭발 정상 작동
