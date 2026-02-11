# chatbot-v monorepo

Monorepo full-stack avec:
- **Frontend** Next.js (App Router) + Vercel AI SDK UI `useChat`
- **Backend** Java 17 (sans Spring) avec `HttpServer`
- **SSE data stream protocol** compatible Vercel AI SDK UI
- **LangChain4j + OpenAI** (via `OPENAI_API_KEY`)
- Données SQL **fictives uniquement**, en mémoire

## Structure

```txt
chatbot-v/
  apps/web
  services/backend
```

## Prérequis

- Node.js 20+
- npm 10+
- Java 17+
- Maven 3.9+

Copiez les variables d'environnement:

```bash
cp .env.example .env
```

## Lancer en local

### Backend

Option 1 (depuis la racine, recommandé pour un monorepo Maven):

```bash
mvn -pl services/backend package
java -jar services/backend/target/chatbot-v-backend-1.0.0.jar
```

Option 2 (dans le module backend):

```bash
cd services/backend
mvn package
java -jar target/chatbot-v-backend-1.0.0.jar
```

Endpoints:
- `GET /healthz`
- `POST /api/v1/chat`
- `POST /api/v1/chat/stream`
- `DELETE /api/v1/chats/{chatId}`

### Frontend

```bash
cd apps/web
npm install
npm run dev
```

Le frontend attend `NEXT_PUBLIC_BACKEND_URL` (par défaut `http://localhost:8080`).

## Tests

### Front

```bash
cd apps/web
npm test
npm run test:e2e
```

### Back

```bash
mvn -pl services/backend test
```

## Déploiement

### Front sur Vercel

1. Importer `apps/web` comme projet.
2. Ajouter env var:
   - `NEXT_PUBLIC_BACKEND_URL=https://votre-backend.example.com`
3. Build command: `npm run build`
4. Output: Next.js standard.

### Backend (Render / Railway / VPS)

- **Render / Railway**: service Java avec `mvn package` puis `java -jar target/chatbot-v-backend-1.0.0.jar`.
- **VPS**: build via Maven, exposer `BACKEND_PORT`, reverse proxy Nginx vers le port Java.

## Exemples de dialogues

### Exemple 1 - Top 5 produits

Utilisateur: `Montre le top 5 des produits par chiffre d'affaires`.

Le backend produit un artifact `sql.query` fictif + hints:
- `presentationHints.primary = "bar"`
- table avec colonnes `product_name`, `revenue`

Le frontend affiche:
- SQL
- table
- visualisation barre via `VizRenderer`

### Exemple 2 - Pays: population vs PIB

Utilisateur: `Compare population et PIB pour 10 pays`.

Hints typiques:
- `primary = "scatter"`
- `secondary = "table"`

Le frontend choisit `scatter` puis fallback table.

## Sécurité

- Auth optionnelle avec `Authorization: Bearer CHATBOTV_INTERNAL_TOKEN`
- `GET /` et `GET /healthz` restent accessibles sans token (utile pour vérifier que le backend tourne)
- Si `CHATBOTV_INTERNAL_TOKEN` absent, mode dev ouvert.
- Rate limiting token bucket in-memory par IP.
- CORS allowlist via `FRONTEND_ORIGIN` (origine unique, liste séparée par des virgules, ou `*`; en local, `localhost` et `127.0.0.1` sont acceptés quel que soit le port).

## Docker backend

```bash
cd services/backend
docker build -t chatbotv-backend .
docker run --rm -p 8080:8080 --env-file ../../.env chatbotv-backend
```
