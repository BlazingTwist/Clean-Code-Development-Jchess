Hier wird das Projekt allgemein und das Backend beschrieben.  
Siehe auch: [Frontend Dokumentation](./src/main/jchess-web/README.md)

### Server start/stop
```bash
# starts both the Backend and Frontend.
# Will be available at 'localhost:3000'
docker-compose up -d

# stops the servers
docker-compose down
```

---

## Entwicklerhilfen

[Schnittstellendokumentation](https://blazingtwist.github.io/Clean-Code-Development-Jchess/)

[Static Analysis (Codacy)](https://app.codacy.com/gh/BlazingTwist/Clean-Code-Development-Jchess)

Es finden sich einige Beispiele in [src/main/java/example](./src/main/java/example).
- [Microservices mit Undertow](/../../pull/10) | [Issue](/../../issues/7)
- [Resource-Files mit Undertow hosten](/../../pull/10) | [Issue](/../../issues/7)
- [WebSocket Kommunikation Undertow/JavaScript](/../../pull/17) | [Issue](/../../issues/5)
