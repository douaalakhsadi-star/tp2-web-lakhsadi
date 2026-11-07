package ma.emsi.lakhsadi.tp2.llm;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.google.genai.GoogleGenAiChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.enterprise.context.Dependent;
import dev.langchain4j.service.Result;
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

        ChatModel modele = GoogleGenAiChatModel.builder()
                .apiKey(cle)
                .modelName("gemini-3.5-flash-lite")
                .build();

        this.chatMemory = MessageWindowChatMemory.withMaxMessages(20);

        this.assistant = AiServices.builder(Assistant.class)
                .chatModel(modele)
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

    public String envoyerQuestion(String question) {
        Result<String> resultat = assistant.chat(question);

        if (resultat.tokenUsage() != null
                && resultat.tokenUsage().totalTokenCount() != null) {
            compteurTokens.ajouter(
                    resultat.tokenUsage().totalTokenCount()
            );
        }

        return resultat.content();
    }
}