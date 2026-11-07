# Utilisation de l’IA — TP 2, application Web

## Problème rencontré
J’avais des difficultés pour configurer le projet Web et adapter
le backing bean du TP 0 à LangChain4j.

## Raison de l’utilisation de l’IA
J’ai demandé une aide progressive pour comprendre les étapes,
configurer les dépendances et relier l’interface Web au LLM.
L’IA a également proposé du code pour LlmClient et les modifications
de la méthode envoyer() de Bb.

## Prompts utilisés
J’ai transmis le contenu de pom.xml et de Bb.java.
J’ai demandé comment créer les classes et poursuivre les étapes,
avec des questions comme « comment » et « après ».

## Réponse de l’IA
L’IA a proposé :
- une configuration Maven avec dependencyManagement ;
- une classe LlmClient utilisant AiServices, l’interface Assistant
  et MessageWindowChatMemory ;
- le remplacement de GenerateurReponse par LlmClient dans Bb ;
- des tests du rôle Traducteur et de la mémoire de conversation.

Extrait pertinent :
« Le rôle est transmis uniquement au début de la conversation,
pour conserver la mémoire entre les questions. »

La correction officielle étant inaccessible avec une erreur 403,
le code proposé s’appuie sur les indications de l’énoncé.

## Ce que j’ai appris
La mémoire conserve les messages échangés pour permettre au LLM
de tenir compte des questions précédentes. Le rôle système est
ajouté au début de la conversation. Il ne faut pas vider la mémoire
à chaque question, sinon le LLM perd le contexte.

J’ai vérifié que l’assistant retenait mon prénom pendant la
conversation et ne le connaissait plus après « Nouveau chat ».
J’ai aussi vérifié que le rôle Traducteur traduisait « Bonjour »
par « Hello ».