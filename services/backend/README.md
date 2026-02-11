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
mvn package
java -jar target/chatbot-v-backend-1.0.0.jar
```

## Tests

```bash
mvn test
```
