package ma.emsi.lakhsadi.tp2.llm;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;

@Named
@SessionScoped
public class CompteurTokens implements Serializable {

    private long total;

    public synchronized void ajouter(long nombre) {
        total += nombre;
    }

    public synchronized long getTotal() {
        return total;
    }
}