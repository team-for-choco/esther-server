# MC 서버 자동 배포 (Jenkins + K8s)

## 작업 개요
PR을 main에 머지하면 자동으로 모드를 빌드하고, MC 서버를 mods에 넣은 채로 K8s Pod로 띄우는 CI/CD 파이프라인 구축.

## 작업 전 요청사항
- 서버 컴퓨터의 Docker Registry 종류/주소 (로컬 Registry, Docker Hub, Harbor 등)
- K8s 클러스터 구성 (단일 노드? 멀티 노드?)
- 기존 MC 서버 실행 방식 (현재 어떻게 서버를 켜고 있는지)
- NeoForge 서버 파일 위치/버전
- 서버 할당 메모리 (Xmx)
- 월드 데이터 저장 경로
- Jenkins에서 K8s 접근 가능 여부 (kubectl 설정)

## 아키텍처

```
PR 머지 → GitHub Webhook → Jenkins Pipeline
                              ├─ 1. git checkout main
                              ├─ 2. ./gradlew build
                              ├─ 3. Docker build (NeoForge + mod JAR)
                              ├─ 4. Docker push (Registry)
                              └─ 5. kubectl set image (K8s Deployment 업데이트)
                                      ↓
                              K8s: 기존 Pod 종료 (graceful) → 새 Pod 생성
                                      ↓
                              MC 서버 자동 시작 (mods에 최신 JAR 포함)
```

## 구성 요소

### 1. Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre

WORKDIR /minecraft
# NeoForge 서버 파일 (base 이미지 또는 COPY)
COPY server/ .
# 빌드된 모드 JAR 주입
COPY build/libs/estherserver-*.jar mods/

EXPOSE 25565
RUN echo "eula=true" > eula.txt

# graceful shutdown을 위해 RCON 활성화 권장
CMD ["java", "-Xmx4G", "-jar", "forge-server.jar", "nogui"]
```

### 2. Jenkinsfile

```groovy
pipeline {
    agent any

    triggers {
        githubPush()  // GitHub Webhook으로 main 푸시 감지
    }

    environment {
        IMAGE = "your-registry/mc-server"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/team-for-choco/esther-server.git'
            }
        }

        stage('Build Mod') {
            steps {
                sh './gradlew build'
            }
        }

        stage('Docker Build & Push') {
            steps {
                sh """
                    docker build -t ${IMAGE}:${BUILD_NUMBER} .
                    docker tag ${IMAGE}:${BUILD_NUMBER} ${IMAGE}:latest
                    docker push ${IMAGE}:${BUILD_NUMBER}
                    docker push ${IMAGE}:latest
                """
            }
        }

        stage('Deploy to K8s') {
            steps {
                sh """
                    kubectl set image deployment/mc-server \
                        mc-server=${IMAGE}:${BUILD_NUMBER} \
                        --namespace minecraft
                """
            }
        }
    }
}
```

### 3. K8s 매니페스트

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mc-server
  namespace: minecraft
spec:
  replicas: 1
  strategy:
    type: Recreate  # MC 서버는 동시 실행 불가
  selector:
    matchLabels:
      app: mc-server
  template:
    metadata:
      labels:
        app: mc-server
    spec:
      terminationGracePeriodSeconds: 60
      containers:
      - name: mc-server
        image: your-registry/mc-server:latest
        ports:
        - containerPort: 25565
        volumeMounts:
        - name: world-data
          mountPath: /minecraft/world
        - name: world-data
          mountPath: /minecraft/world_the_wild
          subPath: world_the_wild
        lifecycle:
          preStop:
            exec:
              command: ["/bin/sh", "-c", "rcon-cli stop || true"]
      volumes:
      - name: world-data
        persistentVolumeClaim:
          claimName: mc-world-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: mc-server
  namespace: minecraft
spec:
  type: NodePort
  ports:
  - port: 25565
    targetPort: 25565
    nodePort: 25565
  selector:
    app: mc-server
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mc-world-pvc
  namespace: minecraft
spec:
  accessModes: [ReadWriteOnce]
  resources:
    requests:
      storage: 10Gi
```

## 핵심 주의사항

| 항목 | 설명 |
|------|------|
| **월드 데이터** | 반드시 PVC로 분리. 컨테이너 재생성 시에도 월드 보존 |
| **strategy: Recreate** | MC 서버는 동시 실행 불가. RollingUpdate 사용 금지 |
| **graceful shutdown** | Pod 종료 시 `/stop` 명령으로 월드 저장 필요 (preStop + RCON) |
| **클라이언트 배포** | 기존 GitHub Actions + packwiz 유지 (서버 배포와 독립) |
| **롤백** | `kubectl rollout undo deployment/mc-server` 로 즉시 이전 버전 복구 가능 |

## 작업 체크리스트

- [ ] 서버 환경 정보 확인 (Registry, K8s 구성, 메모리 등)
- [ ] NeoForge 서버 base 이미지 준비
- [ ] Dockerfile 작성 + 로컬 테스트
- [ ] K8s namespace + PVC + Deployment + Service 생성
- [ ] Jenkins Pipeline 구성 + GitHub Webhook 연결
- [ ] 머지 → 자동 배포 E2E 테스트
- [ ] graceful shutdown (RCON) 검증
- [ ] 롤백 테스트

## 인게임 테스트
해당 없음 (인프라 작업)
