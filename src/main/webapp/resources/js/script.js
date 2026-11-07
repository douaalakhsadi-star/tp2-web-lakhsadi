/* Copier le contenu d'un textarea dans le clipboard */
async function copyToClipboard(idTextArea) {
    var textArea = document.getElementById("form:" + idTextArea);

    if (textArea) {
        try {
            await navigator.clipboard.writeText(textArea.value);
        } catch (err) {
            console.error("Erreur lors de la copie : ", err);
        }
    }

    // Cet ancien code est deprecated :
    // textArea.select();
    // document.execCommand('copy');
}

/* Effacer la dernière question et la dernière réponse */
function toutEffacer() {
    document.getElementById("form:question").value = "";
    document.getElementById("form:reponse").value = "";
}

/* Afficher les morceaux de réponse reçus par WebSocket */
function socketListener(message) {
    const zoneReponse = document.getElementById("form:reponse");

    if (zoneReponse) {
        zoneReponse.value += message;
        zoneReponse.scrollTop = zoneReponse.scrollHeight;
    }
}

/* Vider la réponse précédente au début de l'envoi Ajax */
function gererEnvoi(data) {
    if (data.status === "begin") {
        const zoneReponse = document.getElementById("form:reponse");

        if (zoneReponse) {
            zoneReponse.value = "";
        }
    }
}