package ma.emsi.lakhsadi.tp2.jsf;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.push.Push;
import jakarta.faces.push.PushContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ma.emsi.lakhsadi.tp2.llm.LlmClient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Backing bean pour la page JSF index.xhtml.
 * Conserve la conversation pendant la durée de la vue.
 */
@Named
@ViewScoped
public class Bb implements Serializable {

    private String roleSysteme;
    private boolean roleSystemeChangeable = true;
    private List<SelectItem> listeRolesSysteme;

    private String question;
    private String reponse;
    private StringBuilder conversation = new StringBuilder();

    @Inject
    private LlmClient llmClient;

    @Inject
    @Push(channel = "chat")
    private PushContext webSocketPourChat;

    @Inject
    private FacesContext facesContext;

    public Bb() {
    }

    public String getRoleSysteme() {
        return roleSysteme;
    }

    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }

    public boolean isRoleSystemeChangeable() {
        return roleSystemeChangeable;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation.toString();
    }

    public void setConversation(String conversation) {
        this.conversation = new StringBuilder(conversation);
    }

    /**
     * Envoie la question au LLM.
     * Les réponses partielles sont transmises par WebSocket.
     */
    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Texte question vide",
                    "Il manque le texte de la question"
            );
            facesContext.addMessage(null, message);
            return null;
        }

        if (this.conversation.isEmpty()) {
            this.llmClient.setSystemRole(this.roleSysteme);
            this.roleSystemeChangeable = false;
        }

        this.reponse = "";

        CountDownLatch termine = new CountDownLatch(1);
        AtomicReference<ChatResponse> resultat = new AtomicReference<>();
        AtomicReference<Throwable> erreur = new AtomicReference<>();

        try {
            TokenStream flux = llmClient.envoyerQuestion(question);

            flux.onPartialResponse(tokens -> {
                        webSocketPourChat.send(tokens);
                    })
                    .onCompleteResponse(response -> {
                        resultat.set(response);
                        termine.countDown();
                    })
                    .onError(exception -> {
                        erreur.set(exception);
                        termine.countDown();
                    })
                    .start();

            if (!termine.await(120, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "Le délai d’attente de la réponse est dépassé."
                );
            }

            if (erreur.get() != null) {
                throw new IllegalStateException(
                        "La génération de la réponse a échoué.",
                        erreur.get()
                );
            }

            ChatResponse response = resultat.get();
            this.reponse = response.aiMessage().text();
            llmClient.compterTokens(response);
            afficherConversation();

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            facesContext.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Erreur",
                    "L’attente de la réponse a été interrompue."
            ));

        } catch (RuntimeException exception) {
            facesContext.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Erreur",
                    exception.getMessage()
            ));
        }

        return null;
    }

    /**
     * Commence une nouvelle conversation.
     */
    public String nouveauChat() {
        return "index";
    }

    private void afficherConversation() {
        this.conversation
                .append("== User:\n")
                .append(question)
                .append("\n== Serveur:\n")
                .append(reponse)
                .append("\n");
    }

    public List<SelectItem> getRolesSysteme() {
        if (this.listeRolesSysteme == null) {
            this.listeRolesSysteme = new ArrayList<>();

            String role = """
                    You are a helpful assistant. You help the user to find the information they need.
                    If the user type a question, you answer it.
                    """;

            this.listeRolesSysteme.add(
                    new SelectItem(role, "Assistant")
            );

            role = """
                    You are an interpreter. You translate from English to French and from French to English.
                    If the user type a French text, you translate it into English.
                    If the user type an English text, you translate it into French.
                    If the text contains only one to three words, give some examples of usage of these words in English.
                    """;

            this.listeRolesSysteme.add(
                    new SelectItem(role, "Traducteur Anglais-Français")
            );

            role = """
                    Your are a travel guide. If the user type the name of a country or of a town,
                    you tell them what are the main places to visit in the country or the town
                    are you tell them the average price of a meal.
                    """;

            this.listeRolesSysteme.add(
                    new SelectItem(role, "Guide touristique")
            );
        }

        return this.listeRolesSysteme;
    }
}