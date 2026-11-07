package ma.emsi.lakhsadi.tp2.llm;

import dev.langchain4j.service.Result;

public interface Assistant {
    Result<String> chat(String question);
}