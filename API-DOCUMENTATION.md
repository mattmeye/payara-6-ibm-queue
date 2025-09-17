# 📚 IBM MQ Integration API Documentation

Diese Anwendung bietet sowohl **synchrone REST APIs** als auch **asynchrone Message-Flows** für die IBM MQ Integration. Beide API-Typen sind vollständig spezifiziert und dokumentiert.

## 🚀 Schnellstart

Nach dem Start der Anwendung sind die API-Dokumentationen verfügbar unter:

### 📊 REST API Dokumentation (OpenAPI 3.0)
- **📱 Swagger UI**: [http://localhost:8080/api/docs/swagger-ui](http://localhost:8080/api/docs/swagger-ui)
- **📄 OpenAPI YAML**: [http://localhost:8080/api/docs/openapi.yaml](http://localhost:8080/api/docs/openapi.yaml)
- **📋 OpenAPI JSON Info**: [http://localhost:8080/api/docs/openapi.json](http://localhost:8080/api/docs/openapi.json)

### ⚡ Message Flows Dokumentation (AsyncAPI 3.0)
- **🎨 AsyncAPI Studio**: [http://localhost:8080/api/docs/asyncapi-studio](http://localhost:8080/api/docs/asyncapi-studio)
- **📄 AsyncAPI YAML**: [http://localhost:8080/api/docs/asyncapi.yaml](http://localhost:8080/api/docs/asyncapi.yaml)
- **📋 AsyncAPI JSON Info**: [http://localhost:8080/api/docs/asyncapi.json](http://localhost:8080/api/docs/asyncapi.json)

### 🏠 Dokumentations-Übersicht
- **📖 Alle Dokumentationen**: [http://localhost:8080/api/docs/](http://localhost:8080/api/docs/)

## 📖 API-Übersicht

### REST APIs (OpenAPI)

Die REST APIs bieten synchrone HTTP-Endpunkte für:

#### 🔄 Message Operations (`/api/mq`)
- `POST /api/mq/send` - Nachricht an Standard-Queue senden
- `POST /api/mq/send/{queue}` - Nachricht an spezifische Queue senden
- `GET /api/mq/receive` - Nachricht von Standard-Queue empfangen
- `GET /api/mq/receive/{queue}` - Nachricht von spezifischer Queue empfangen
- `POST /api/mq/sendreceive` - Request-Response Pattern (Nachricht senden und auf Antwort warten)
- `GET /api/mq/health` - IBM MQ Service Health Check

#### 📋 Message Repository (`/api/messages`)
- `GET /api/messages` - Alle Nachrichten abrufen
- `GET /api/messages/{id}` - Spezifische Nachricht abrufen
- `DELETE /api/messages/{id}` - Nachricht löschen
- `GET /api/messages/by-status/{status}` - Nachrichten nach Status filtern
- `GET /api/messages/by-queue/{queueName}` - Nachrichten nach Queue filtern
- `GET /api/messages/count/by-status/{status}` - Nachrichten zählen nach Status
- `POST /api/messages/cleanup` - Alte Nachrichten aufräumen
- `GET /api/messages/health` - Repository Health Check

#### ⚙️ Batch Jobs (`/api/batch`)
- Batch-Job Management und Monitoring

#### 📊 Metrics (`/api/metrics`)
- Performance- und Monitoring-Daten

### Message Flows (AsyncAPI)

Die AsyncAPI-Spezifikation dokumentiert asynchrone Message-Patterns:

#### 📨 Queue Channels
- **Request Queue** (`DEV.QUEUE.1`) - Eingehende Nachrichten
- **Response Queue** (`DEV.QUEUE.2`) - Ausgehende Antworten
- **Batch Processing Queue** (`DEV.BATCH.QUEUE`) - Batch-Verarbeitung
- **Dead Letter Queue** (`DEV.QUEUE.DLQ`) - Fehlgeschlagene Nachrichten
- **Backout Queue** (`DEV.QUEUE.1.BACKOUT`) - Backout-Nachrichten
- **Health Check Queue** (`DEV.HEALTH.QUEUE`) - Health Monitoring

#### ⚡ Operations
- **Send Message** - Nachricht senden
- **Receive Message** - Nachricht empfangen
- **Send Response** - Antwort senden
- **Process Batch Messages** - Batch-Verarbeitung
- **Handle Failed Messages** - Fehlerbehandlung
- **Handle Backout Messages** - Backout-Handling
- **Health Check** - Gesundheitsprüfung

#### 📋 Message Types
- **Incoming Messages** - Eingehende Textnachrichten
- **Outgoing Messages** - Verarbeitete Nachrichten
- **Response Messages** - Antwort-Nachrichten mit Correlation ID
- **Batch Messages** - Batch-Verarbeitungs-Nachrichten
- **Failed Messages** - Fehlgeschlagene Nachrichten mit Fehlerinformationen
- **Backout Messages** - Backout-Nachrichten
- **Health Messages** - Health-Check-Nachrichten

## 🔧 Verwendung der Spezifikationen

### OpenAPI/Swagger UI verwenden

1. Öffnen Sie [http://localhost:8080/api/docs/swagger-ui](http://localhost:8080/api/docs/swagger-ui)
2. Erkunden Sie die verfügbaren Endpunkte
3. Testen Sie APIs direkt in der Benutzeroberfläche
4. Exportieren Sie Client-Code für verschiedene Sprachen

### AsyncAPI Studio verwenden

1. Öffnen Sie [http://localhost:8080/api/docs/asyncapi-studio](http://localhost:8080/api/docs/asyncapi-studio)
2. Klicken Sie auf "Open AsyncAPI Studio"
3. Fügen Sie die URL ein: `http://localhost:8080/api/docs/asyncapi.yaml`
4. Erkunden Sie die Message-Flows und Queue-Strukturen

### Spezifikationen herunterladen

```bash
# OpenAPI YAML herunterladen
curl -o openapi.yaml http://localhost:8080/api/docs/openapi.yaml

# AsyncAPI YAML herunterladen
curl -o asyncapi.yaml http://localhost:8080/api/docs/asyncapi.yaml
```

## 🛠️ Code-Generierung

### Client-Code aus OpenAPI generieren

```bash
# JavaScript/TypeScript Client
npx @openapitools/openapi-generator-cli generate \
  -i http://localhost:8080/api/docs/openapi.yaml \
  -g typescript-fetch \
  -o ./generated-client

# Java Client
openapi-generator generate \
  -i http://localhost:8080/api/docs/openapi.yaml \
  -g java \
  -o ./java-client

# Python Client
openapi-generator generate \
  -i http://localhost:8080/api/docs/openapi.yaml \
  -g python \
  -o ./python-client
```

### Message Schemas aus AsyncAPI generieren

```bash
# JSON Schema für Messages generieren
asyncapi generate models typescript ./asyncapi.yaml

# Java POJOs für Messages generieren
asyncapi generate models java ./asyncapi.yaml
```

## 📊 Beispiele

### REST API Beispiele

```bash
# Nachricht senden
curl -X POST http://localhost:8080/api/mq/send \
  -H "Content-Type: text/plain" \
  -d "Hello World"

# Nachricht empfangen
curl http://localhost:8080/api/mq/receive

# Request-Response Pattern
curl -X POST http://localhost:8080/api/mq/sendreceive \
  -H "Content-Type: text/plain" \
  -d "Request message"

# Nachrichten nach Status abrufen
curl http://localhost:8080/api/messages/by-status/PROCESSED

# Health Check
curl http://localhost:8080/api/mq/health
```

### Message Flow Patterns

Die AsyncAPI-Spezifikation dokumentiert folgende Patterns:

1. **Fire-and-Forget**: Nachricht senden ohne Antwort zu erwarten
2. **Request-Response**: Nachricht senden und auf korrelierende Antwort warten
3. **Batch Processing**: Nachrichten in Chunks verarbeiten
4. **Error Handling**: Fehlgeschlagene Nachrichten in Dead Letter Queue
5. **Health Monitoring**: Regelmäßige Health Checks über Message Queue

## 🔍 Entwicklung und Erweiterung

### Neue REST Endpoints hinzufügen

1. Annotieren Sie Ihre REST-Klassen mit OpenAPI-Annotationen:
```java
@Operation(summary = "Neue Operation", description = "Beschreibung")
@ApiResponse(responseCode = "200", description = "Erfolg")
public Response newEndpoint() { ... }
```

2. Die OpenAPI-Spezifikation wird automatisch aktualisiert

### Neue Message Flows dokumentieren

1. Erweitern Sie die `asyncapi.yaml` Datei um neue Channels und Operations
2. Definieren Sie neue Message-Schemas in der `components.schemas` Sektion
3. Aktualisieren Sie die Dokumentation

### Automatische Generierung

Die Spezifikationen werden automatisch aus folgenden Quellen generiert:

- **OpenAPI**: Jakarta REST Annotationen + MicroProfile OpenAPI Annotationen
- **AsyncAPI**: Manuelle YAML-Datei (kann durch Code-Scanner erweitert werden)

## 🔒 Sicherheit

### OpenAPI Security

Die REST APIs unterstützen:
- **Bearer Token Authentication** (JWT)
- Konfigurierbar über `@SecurityRequirement` Annotationen

### AsyncAPI Security

IBM MQ unterstützt:
- **Basic Authentication** (Username/Password)
- **SSL/TLS** mit Zertifikaten
- **Channel-basierte Zugriffskontrolle**

## 📱 Integration in IDEs

### VS Code
- Installieren Sie die "OpenAPI (Swagger) Editor" Extension
- Öffnen Sie die `openapi.yaml` Datei für Syntax-Highlighting und Validierung

### IntelliJ IDEA
- Installieren Sie das "OpenAPI Specifications" Plugin
- Importieren Sie die Spezifikation für Auto-Completion

### Postman
- Importieren Sie die OpenAPI-Spezifikation direkt in Postman
- Automatische Collection-Generierung mit allen Endpunkten

## 🤝 Best Practices

### OpenAPI
- Verwenden Sie aussagekräftige `summary` und `description` Felder
- Definieren Sie Beispiele für Request/Response Bodies
- Gruppieren Sie verwandte Endpunkte mit `tags`
- Dokumentieren Sie alle Error Codes

### AsyncAPI
- Definieren Sie klare Channel-Namen und Beschreibungen
- Nutzen Sie Message Traits für wiederverwendbare Patterns
- Dokumentieren Sie Message-Formate und Schemas
- Beschreiben Sie Operation-Binding-Details

## 🎯 Weiterführende Ressourcen

- **OpenAPI Specification**: https://spec.openapis.org/oas/v3.0.3
- **AsyncAPI Specification**: https://www.asyncapi.com/docs/specifications/v3.0.0
- **MicroProfile OpenAPI**: https://github.com/eclipse/microprofile-open-api
- **Swagger UI**: https://swagger.io/tools/swagger-ui/
- **AsyncAPI Studio**: https://studio.asyncapi.com/

## ⚡ Troubleshooting

### OpenAPI UI lädt nicht
- Prüfen Sie, ob die Anwendung läuft: http://localhost:8080/api/docs/openapi.yaml
- Überprüfen Sie Browser-Konsole auf JavaScript-Fehler

### AsyncAPI YAML nicht verfügbar
- Überprüfen Sie, ob die Datei im Classpath liegt: `src/main/resources/asyncapi.yaml`
- Prüfen Sie Anwendungslogs auf Fehler beim Laden der Ressource

### Message-Flow Dokumentation unvollständig
- Erweitern Sie die AsyncAPI-Spezifikation um fehlende Channels
- Fügen Sie neue Message-Schemas in der `components` Sektion hinzu