# News Ingestion & Audio Generation System

This Spring Boot application is an autonomous agentic system designed to ingest news, summarize it using AI, and convert it into audio podcasts. It leverages specific user interests to curate content.

## 🚀 Features

*   **News Ingestion**: Fetches real-time news articles from [Event Registry](https://eventregistry.org/) based on topics.
*   **Vector Database**: Uses **PGVector** to store and retrieve news embeddings for semantic search.
*   **AI Summarization**: utilizes OpenAI's LLMs to summarize news topics into concise transcripts.
*   **Text-to-Speech**: Converts summaries into audio files for a personalized news listening experience.
*   **User Management**: Allows users to sign up, log in, and define their preferred news topics.
*   **Automated Scheduling**: A configurable scheduler periodically ingests, processes, and converts news.
*   **Reactive**: built with Spring WebFlux for efficient non-blocking operations.

## 🛠️ Technology Stack

*   **Java 17+**
*   **Spring Boot 3.x**
*   **Spring AI** (OpenAI, Vector Stores)
*   **PostgreSQL** (with `pgvector` extension)
*   **Hibernate / JPA**
*   **Lombok**
*   **Maven**

## 📋 Prerequisites

1.  **Java 17 or higher** installed (`java -version`).
2.  **PostgreSQL** database running with the `vector` extension enabled.
3.  **OpenAI API Key** for embeddings and chat completion.

## ⚙️ Configuration

The application is configured via `application.yml`. You can override these settings using environment variables.

### Key Environment Variables

| Variable | Description |
| :--- | :--- |
| `OPENAI_API_KEY` | Your OpenAI API Key. |
| `SPRING_DATASOURCE_URL` | JDBC URL for PostgreSQL (e.g., `jdbc:postgresql://localhost:5432/newsdb`). |
| `SPRING_DATASOURCE_USERNAME` | Database username. |
| `SPRING_DATASOURCE_PASSWORD` | Database password. |

### Scheduler Configuration

*   `news.ingestion.schedule`: Cron expression for the ingestion job (Default: hourly).
*   `news.ingestion.throttle-delay-ms`: Delay between processing topics to avoid rate limits (Default: 5000ms).

## 🏃‍♂️ How to Run

1.  **Clone the repository**:
    ```bash
    git clone <repository-url>
    cd news-ingestion
    ```

2.  **Build the project**:
    ```bash
    ./mvnw clean install
    ```

3.  **Run the application**:
    ```bash
    ./mvnw spring-boot:run
    ```
    *Ensure your environment variables are set before running.*

## 🔌 API Endpoints

### User Management
*   `POST /api/users/signup`: Register a new user with topic preferences.
    ```json
    {
      "email": "user@example.com",
      "password": "password123",
      "name": "John Doe",
      "topics": ["Technology", "AI", "Sports"]
    }
    ```
*   `POST /api/users/login`: Authenticate a user.

## 🗄️ Database Schema

The system uses the following key tables:
*   `users`: Stores user credentials and preferences.
*   `topics`: Managed list of news topics.
*   `user_topic_preferences`: Links users to their interested topics.
*   `news_embeddings`: PGVector table for semantic news search.
*   `news_transcripts`: Stores generated summaries.

## 🤝 Contributing

1.  Fork the repository.
2.  Create your feature branch (`git checkout -b feature/amazing-feature`).
3.  Commit your changes (`git commit -m 'Add some amazing feature'`).
4.  Push to the branch (`git push origin feature/amazing-feature`).
5.  Open a Pull Request.
