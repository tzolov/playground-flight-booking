# Documentation project instructions

## About this project

- This is documentation for the **Funnair Customer Support Agent**, an AI-powered flight booking assistant
- Built with [Spring AI](https://github.com/spring-projects/spring-ai) and [Vaadin](https://vaadin.com/)
- Uses Chroma as a vector store for RAG over airline terms of service
- Supports multiple AI providers: OpenAI, Anthropic, VertexAI Gemini, Azure OpenAI, Groq, Mistral
- Documentation site powered by [Mintlify](https://mintlify.com)
- Pages are MDX files with YAML frontmatter
- Configuration lives in `docs.json`

## Terminology

- Use "Funnair" as the airline name
- Use "chat provider" when referring to the AI model backends (OpenAI, Anthropic, VertexAI, etc.)
- Use "function calling" or "tools" for the AI-invokable booking operations
- Use "RAG" or "retrieval augmented generation" for the terms of service feature
- Use "booking tools" for getBookingDetails, changeBooking, cancelBooking, changeSeatNumber

## Style preferences

- Use active voice and second person ("you")
- Keep sentences concise
- Use sentence case for headings
- Code formatting for file names, commands, paths, and code references
