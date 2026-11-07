package ma.emsi.lakhsadi.tp2.llm;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.google.genai.GoogleGenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.io.Serializable;

@Dependent
public class LlmClient implements Serializable {

    @Inject
    private CompteurTokens compteurTokens;

    private String systemRole;
    private final ChatMemory chatMemory;
    private final Assistant assistant;

    public LlmClient() {
        String cle = System.getenv("GEMINI_KEY");

        StreamingChatModel modele =
                GoogleGenAiStreamingChatModel.builder()
                        .apiKey(cle)
                        .modelName("gemini-3.5-flash-lite")
                        .build();

        this.chatMemory = MessageWindowChatMemory.withMaxMessages(20);

        this.assistant = AiServices.builder(Assistant.class)
                .streamingChatModel(modele)
                .chatMemory(chatMemory)
                .build();
    }

    public void setSystemRole(String systemRole) {
        this.systemRole = systemRole;
        chatMemory.clear();

        if (systemRole != null && !systemRole.isBlank()) {
            chatMemory.add(SystemMessage.from(systemRole));
        }
    }

    public String getSystemRole() {
        return systemRole;
    }

    public TokenStream envoyerQuestion(String question) {
        return assistant.chat(question);
    }

    public void compterTokens(ChatResponse reponse) {
        if (reponse.tokenUsage() != null
                && reponse.tokenUsage().totalTokenCount() != null) {
            compteurTokens.ajouter(
                    reponse.tokenUsage().totalTokenCount()
            );
        }
    }
}