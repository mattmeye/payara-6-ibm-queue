# Load Testing Setup für Payara 6 IBM MQ Integration

Dieses Verzeichnis enthält JMeter Load Test Pläne für das Payara 6 IBM MQ Integrationsprojekt.

## 📋 Verfügbare Test-Pläne

### 1. **rest-api-load-test.jmx**
- **Zweck**: Load Testing aller REST-API Endpunkte
- **Threads**: 10 Threads, 30s Ramp-up, 5 Minuten Laufzeit
- **Tests**: Health Checks, Message APIs, Batch Jobs, Prometheus Metrics
- **Scenario**: Simuliert normale API-Nutzung

### 2. **ibm-mq-load-test.jmx**
- **Zweck**: Direktes IBM MQ Queue Load Testing
- **Threads**: 5 Producer Threads + 1 Monitor Thread
- **Tests**: JMS Message Producer + Queue Monitoring
- **Scenario**: Befeuert IBM MQ Queue direkt und überwacht Verarbeitung

### 3. **combined-load-test.jmx** ⭐ **EMPFOHLEN**
- **Zweck**: Kombiniertes End-to-End Testing
- **Threads**: 3 Thread Groups (Producer, API Load, Monitoring)
- **Tests**: REST API Message Sending + API Load + System Monitoring
- **Scenario**: Realistisches Produktions-Szenario

## 🚀 Quick Start

### Voraussetzungen
```bash
# 1. JMeter installieren (bereits erledigt)
brew install jmeter

# 2. Anwendung starten
mvn clean package -DskipTests
mvn payara-micro:start

# 3. Docker Compose für IBM MQ starten
docker-compose up -d
```

### Tests ausführen

#### GUI Mode (für Entwicklung):
```bash
# Kombinierter Test (empfohlen)
jmeter -t load-tests/combined-load-test.jmx

# Nur REST APIs
jmeter -t load-tests/rest-api-load-test.jmx

# Nur IBM MQ
jmeter -t load-tests/ibm-mq-load-test.jmx
```

#### Headless Mode (für CI/CD):
```bash
# Kombinierter Test mit Report
jmeter -n -t load-tests/combined-load-test.jmx \
       -l results/combined-results.jtl \
       -e -o results/html-report

# Nur REST APIs
jmeter -n -t load-tests/rest-api-load-test.jmx \
       -l results/rest-api-results.jtl

# Nur IBM MQ
jmeter -n -t load-tests/ibm-mq-load-test.jmx \
       -l results/mq-results.jtl
```

## ⚙️ Konfiguration

### Anpassbare Parameter (User Defined Variables):

| Parameter | Default | Beschreibung |
|-----------|---------|--------------|
| `app_host` | localhost | Payara Server Host |
| `app_port` | 8080 | Payara Server Port |
| `context_path` | /payara6-ibmmq/api | API Context Path |
| `mq_host` | localhost | IBM MQ Host |
| `mq_port` | 11414 | IBM MQ Port |
| `mq_queue_manager` | QM1 | Queue Manager Name |
| `mq_channel` | DEV.APP.SVRCONN | Connection Channel |
| `mq_queue_name` | DEV.QUEUE.1 | Queue Name |
| `mq_user` | app | MQ User |
| `mq_password` | passw0rd | MQ Password |

### Thread Group Konfiguration:

#### Combined Load Test:
- **MQ Message Producer**: 3 Threads, 100 Loops, 5 Min
- **REST API Load**: 5 Threads, 50 Loops, 5 Min
- **System Monitoring**: 1 Thread, 30 Loops, 5 Min

## 📊 Monitoring & Metriken

### Was wird gemessen:
1. **Durchsatz**: Messages/Requests pro Sekunde
2. **Response Times**: Min, Max, Average, 95th Percentile
3. **Error Rate**: Fehlerquote in Prozent
4. **Ressourcenverbrauch**: Via Prometheus Metrics
5. **Queue Processing**: Message Count in Database
6. **Batch Job Status**: Verarbeitungsstatus

### Wichtige Listeners:
- **View Results Tree**: Detaillierte Request/Response Analyse
- **Summary Report**: Überblick über Performance Kennzahlen
- **Aggregate Graph**: Grafische Darstellung der Ergebnisse

## 🎯 Test Szenarien

### Kombinierter Load Test Ablauf:

1. **Setup Phase** (0-30s):
   - Threads starten gestaffelt
   - System Monitoring beginnt

2. **Load Phase** (30-270s):
   - MQ Producer: Sendet Nachrichten via REST API
   - API Load: Simuliert verschiedene API Aufrufe
   - Monitor: Überwacht Queue Depth und DB Status

3. **Ramp Down** (270-300s):
   - Threads beenden Arbeit
   - Final Monitoring

## 📈 Erwartete Ergebnisse

### Performance Targets:
- **REST API Response Time**: < 200ms (95th percentile)
- **Message Processing**: > 100 messages/min
- **Error Rate**: < 1%
- **Queue Processing**: Steady state ohne Backlog

### Metriken zu beobachten:
- Database Message Count (sollte stetig steigen)
- Queue Depth (sollte verarbeitet werden)
- HTTP Response Times (stabil bleiben)
- JVM Memory Usage (in Prometheus)

## 🔧 Troubleshooting

### Häufige Probleme:

#### "Connection refused" Fehler:
```bash
# Prüfen ob Anwendung läuft
curl http://localhost:8080/payara6-ibmmq/api/simple/health

# Prüfen ob IBM MQ läuft
docker-compose ps
```

#### "JMS Connection failed":
- IBM MQ Container Status prüfen
- Queue Manager Konfiguration validieren
- Credentials in Test Plan überprüfen

#### Hohe Response Times:
- Payara Server Logs prüfen
- Database Connection Pool Größe erhöhen
- JVM Heap Size anpassen

## 📝 Reports

### HTML Reports generieren:
```bash
# Nach Test Ausführung
jmeter -g results/combined-results.jtl -o results/html-report
open results/html-report/index.html
```

### CSV Analyse:
Die `.jtl` Files können in Excel oder mit Scripts analysiert werden für:
- Trend Analyse
- Performance Regression Testing
- Capacity Planning

## 🔄 CI/CD Integration

### GitHub Actions Beispiel:
```yaml
- name: Run Load Tests
  run: |
    jmeter -n -t load-tests/combined-load-test.jmx \
           -l results/load-test-results.jtl \
           -Japp_host=${{ env.TEST_HOST }}

- name: Generate Report
  run: |
    jmeter -g results/load-test-results.jtl \
           -o results/html-report
```

## ⚠️ Wichtige Hinweise

1. **Vor Produktions-Tests**: Immer in isolierter Umgebung testen
2. **Resource Limits**: JMeter kann selbst zum Bottleneck werden
3. **Network**: Latenz zwischen JMeter und Test-Target beachten
4. **Monitoring**: Immer sowohl Client- als auch Server-Side überwachen
5. **Baseline**: Erste Tests als Baseline für Vergleiche nutzen

## 📞 Support

Bei Problemen mit den Load Tests:
1. Logs in JMeter View Results Tree prüfen
2. Payara Server Logs analysieren
3. Docker Container Status validieren
4. Network Connectivity testen