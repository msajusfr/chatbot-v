# chatbot-v backend

Backend Java 17 sans Spring, basé sur `HttpServer` + Jackson + LangChain4j.

## Variables

- `OPENAI_API_KEY`
- `CHATBOTV_INTERNAL_TOKEN` (optionnel)
- `FRONTEND_ORIGIN`
- `BACKEND_PORT`
- `MODEL_NAME`
- `TEMPERATURE`
- `TIMEOUT_SECS`

## Build + run

```bash
# depuis la racine du repo
mvn -pl services/backend package
java -jar services/backend/target/chatbot-v-backend-1.0.0.jar

# ou depuis ce dossier
mvn package
java -jar target/chatbot-v-backend-1.0.0.jar
```

## Tests

```bash
# depuis la racine du repo
mvn -pl services/backend test

# ou depuis ce dossier
mvn test
```
