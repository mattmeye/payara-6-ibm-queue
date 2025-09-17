# Payara 6 IBM MQ Integration mit JBatch und PostgreSQL

Dieses Projekt zeigt, wie IBM MQ in Payara 6 integriert werden kann, nachdem die Managed Beans in Payara 6 entfernt wurden. Die Lösung umfasst die automatische Verarbeitung von MQ-Nachrichten und deren Speicherung in PostgreSQL mittels JBatch.

## Überblick

### Migration von Payara 5 zu Payara 6

In Payara 5 waren IBM MQ Managed Beans verfügbar, die eine einfache Integration ermöglichten. In Payara 6 wurden diese entfernt, daher nutzt diese Lösung:

- **IBM MQ Client Libraries** direkt
- **CDI Producer** für ConnectionFactory
- **Advanced Connection Pooling** für optimale Performance
- **JBatch** für asynchrone MQ-zu-PostgreSQL Verarbeitung
- **Transactional Messaging** für Datenkonsistenz
- **Dead Letter Queue** Handling für Fehlerbehandlung
- **JPA/Hibernate** für PostgreSQL Integration
- **Monitoring & Metriken** mit Micrometer/Prometheus
- **SSL/TLS Verschlüsselung** für sichere Verbindungen
- **MicroProfile Config** für Konfiguration
- **JAX-RS** für REST API
- **Docker** für lokales Testing

## Architektur

```
├── config/
│   ├── IBMMQConfig.java           # MicroProfile Config für IBM MQ
│   └── ConnectionPoolConfig.java  # Connection Pool Konfiguration
├── producer/
│   └── IBMMQConnectionFactoryProducer.java  # CDI Producer für ConnectionFactory
├── pool/
│   ├── IBMMQConnectionPool.java   # Advanced Connection Pool
│   └── PooledConnection.java      # Pooled Connection Wrapper
├── entity/
│   └── MQMessage.java             # JPA Entity für MQ Nachrichten
├── repository/
│   └── MQMessageRepository.java   # Repository für Datenbankoperationen
├── batch/
│   ├── MQMessageReader.java       # JBatch Reader für MQ
│   ├── MQMessageProcessor.java    # JBatch Processor für Nachrichten
│   ├── MQMessageWriter.java       # JBatch Writer für PostgreSQL
│   └── MQBatchJobListener.java    # JBatch Job Listener
├── transaction/
│   └── TransactionalMQService.java # Transactional Messaging Service
├── deadletter/
│   └── DeadLetterQueueService.java # DLQ Handling Service
├── monitoring/
│   ├── MQMetricsService.java      # Metrics Collection
│   └── MicrometerConfig.java      # Micrometer Configuration
├── security/
│   ├── SSLConfig.java             # SSL/TLS Configuration
│   └── SSLConnectionFactoryProducer.java # SSL Connection Factory
├── service/
│   ├── IBMMQService.java          # MQ Service Layer
│   └── BatchJobService.java       # JBatch Management Service
└── rest/
    ├── IBMMQResource.java         # REST API für MQ
    ├── BatchJobResource.java      # REST API für JBatch
    ├── MQMessageResource.java     # REST API für gespeicherte Nachrichten
    ├── MetricsResource.java       # REST API für Metriken
    └── RestApplication.java       # JAX-RS Application
```

### JBatch Workflow

1. **MQMessageReader** liest Nachrichten aus IBM MQ
2. **MQMessageProcessor** validiert und verarbeitet die Nachrichten
3. **MQMessageWriter** speichert sie in PostgreSQL
4. **BatchJobService** verwaltet Job-Ausführungen

## Konfiguration

Die Konfiguration erfolgt über `microprofile-config.properties`:

```properties
ibm.mq.queueManager=QM1
ibm.mq.hostname=localhost
ibm.mq.port=1414
ibm.mq.channel=DEV.APP.SVRCONN
ibm.mq.username=app
ibm.mq.password=passw0rd
ibm.mq.queue.request=DEV.QUEUE.1
ibm.mq.queue.response=DEV.QUEUE.2
```

## Anwendung starten

### Voraussetzungen

- Java 11 oder höher
- Maven 3.6+
- Docker und Docker Compose

### Schritt-für-Schritt Anleitung

#### 1. Repository klonen und ins Verzeichnis wechseln

```bash
git clone <repository-url>
cd payara-6-ibm-queue
```

#### 2. Anwendung bauen

```bash
mvn clean package
```

#### 3. IBM MQ Container starten

```bash
# IBM MQ Container im Hintergrund starten
docker-compose up -d ibmmq

# Warten bis IBM MQ bereit ist (ca. 30-60 Sekunden)
docker-compose logs -f ibmmq
```

**Warten Sie bis Sie diese Meldung sehen:**
```
Started web server at port '9443' for the Web Console
```

#### 4. Anwendung starten

**Option A: Mit Docker Compose (empfohlen)**
```bash
# Payara 6 Container starten
docker-compose up -d payara

# Logs verfolgen
docker-compose logs -f payara
```

**Option B: Lokal mit Payara Micro**
```bash
mvn payara-micro:start
```

#### 5. Überprüfung

Die Anwendung ist bereit wenn:

```bash
# Health Check erfolgreich
curl http://localhost:8080/api/mq/health

# Erwartete Antwort: {"status":"UP","mqConnection":"OK"}
```

#### 6. Test der Funktionalität

```bash
# Nachricht senden
curl -X POST http://localhost:8080/api/mq/send \
  -H "Content-Type: text/plain" \
  -d "Hello IBM MQ!"

# Nachricht empfangen
curl http://localhost:8080/api/mq/receive
```

### Kompletter Start-Befehl (alles zusammen)

```bash
# 1. Bauen
mvn clean package

# 2. Alles starten (IBM MQ + Payara)
docker-compose up -d

# 3. Logs verfolgen
docker-compose logs -f

# 4. Health Check (in neuem Terminal)
curl http://localhost:8080/api/mq/health
```

### Anwendung stoppen

```bash
# Alle Container stoppen
docker-compose down

# Nur Payara stoppen (IBM MQ weiterlaufen lassen)
docker-compose stop payara
```

## API Endpoints

Die Anwendung stellt folgende REST Endpoints bereit:

### Nachrichten senden

```bash
# Standard Queue
curl -X POST http://localhost:8080/api/mq/send \
  -H "Content-Type: text/plain" \
  -d "Hello IBM MQ!"

# Spezifische Queue
curl -X POST http://localhost:8080/api/mq/send/MY.QUEUE \
  -H "Content-Type: text/plain" \
  -d "Hello specific queue!"
```

### Nachrichten empfangen

```bash
# Standard Queue
curl http://localhost:8080/api/mq/receive

# Spezifische Queue
curl http://localhost:8080/api/mq/receive/MY.QUEUE
```

### Request-Response Pattern

```bash
curl -X POST http://localhost:8080/api/mq/sendreceive \
  -H "Content-Type: text/plain" \
  -d "Request message"
```

### Health Check

```bash
curl http://localhost:8080/api/mq/health
```

## IBM MQ Setup

### Docker Container

Das Docker Compose Setup startet automatisch einen IBM MQ Container mit:

- Queue Manager: `QM1`
- Port: `1414`
- Web Console: `https://localhost:9443` (admin/passw0rd)
- Vorkonfigurierte Queues: `DEV.QUEUE.1`, `DEV.QUEUE.2`

### Manuelles Setup

Falls Sie einen eigenen IBM MQ Server nutzen:

1. Queue Manager erstellen
2. Channels konfigurieren (`DEV.APP.SVRCONN`)
3. Queues erstellen (`DEV.QUEUE.1`, `DEV.QUEUE.2`)
4. Benutzer und Berechtigungen einrichten

## Verwendung im Code

### Service Injection

```java
@Inject
private IBMMQService mqService;

public void sendMessage() {
    mqService.sendMessage("Hello from Payara 6!");
}

public String receiveMessage() {
    return mqService.receiveMessage();
}
```

### Konfiguration überschreiben

```java
@Inject
@ConfigProperty(name = "ibm.mq.hostname")
private String mqHost;
```

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
# IBM MQ Container starten
docker-compose up -d ibmmq

# Tests ausführen
mvn verify

# Container stoppen
docker-compose down
```

### Code Coverage mit JaCoCo

```bash
# Coverage Report generieren (ignoriert Test-Fehler)
mvn test -Dmaven.test.failure.ignore=true jacoco:report

# Nur Coverage Report (wenn Tests bereits gelaufen sind)
mvn jacoco:report

# Integration Tests mit Coverage
mvn verify -Dmaven.test.failure.ignore=true

# Kombinierter Coverage Report
mvn verify jacoco:merged-report
```

**Coverage Reports:**
- Unit Tests: `target/site/jacoco/index.html`
- Integration Tests: `target/site/jacoco-it/index.html`
- Combined: `target/site/jacoco-merged/index.html`

**Coverage Thresholds:**
- Gesamt Line Coverage: ≥75%
- Branch Coverage: ≥70%
- Kritische Packages (adapter, service): ≥85%

**Aktuelle Coverage:**
- **Repository**: 100% ✅
- **Security**: 100% ✅
- **Services**: 92% ✅
- **Dead Letter Queue**: 93% ✅
- **Transactions**: 93% ✅
- **Monitoring**: 79% ✅
- **REST APIs**: 61% ⚡ (JAX-RS Provider Issues)
- **Jakarta JMS Adapter**: 2% ⚠️ (Simplified Tests)

**Coverage Quality Gates:**
```bash
# Coverage check only (manuell)
mvn jacoco:check@check

# Coverage Report öffnen (macOS)
open target/site/jacoco/index.html

# Coverage Report öffnen (Linux)
xdg-open target/site/jacoco/index.html
```

**Praktisches Beispiel:**
```bash
# 1. Tests laufen lassen und Coverage sammeln
mvn test -Dmaven.test.failure.ignore=true jacoco:report

# 2. Report im Browser öffnen
open target/site/jacoco/index.html

# 3. Spezifische Package-Coverage prüfen
# z.B. com.example.ibmmq.service -> Zeigt 92% Line Coverage
```

## Troubleshooting

### Häufige Probleme

1. **Connection refused**: IBM MQ Container noch nicht bereit
   ```bash
   docker-compose logs ibmmq
   ```

2. **Authentication failed**: Überprüfen Sie Username/Password in der Konfiguration

3. **Queue not found**: Stellen Sie sicher, dass die Queues im Queue Manager existieren

### Logs

```bash
# Payara Logs
docker-compose logs payara

# IBM MQ Logs
docker-compose logs ibmmq
```

## Produktive Umgebung

Für den produktiven Einsatz sollten Sie:

1. **Sichere Konfiguration** verwenden (Vault, Kubernetes Secrets)
2. **Connection Pooling** konfigurieren
3. **Monitoring** implementieren
4. **Error Handling** erweitern
5. **Transaktionen** nach Bedarf hinzufügen

### Beispiel Produktive Konfiguration

```properties
# Produktive IBM MQ Konfiguration
ibm.mq.queueManager=PROD_QM1
ibm.mq.hostname=mq.company.com
ibm.mq.port=1414
ibm.mq.channel=PROD.APP.SVRCONN
ibm.mq.username=${IBM_MQ_USER}
ibm.mq.password=${IBM_MQ_PASSWORD}
```

## Weitere Funktionen

Die Lösung kann erweitert werden um:

- **Message Listeners** für asynchrone Verarbeitung
- **Connection Pooling** für bessere Performance
- **Transaktionales Messaging**
- **Dead Letter Queue** Handling
- **Monitoring und Metriken**
- **SSL/TLS Verschlüsselung**