package ma.emsi.lakhsadi.tp2.llm;

import dev.langchain4j.service.TokenStream;

public interface Assistant {
    TokenStream chat(String question);
}