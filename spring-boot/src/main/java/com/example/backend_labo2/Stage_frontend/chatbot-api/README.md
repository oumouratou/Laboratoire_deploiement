# 🤖 API Chatbot avec Llama

Ce dossier contient l'API Flask pour le chatbot IA utilisant Ollama et Llama.

## Prérequis

1. **Python 3.10+** installé sur votre machine
2. **Ollama** installé et configuré

## Installation d'Ollama

### Windows
Téléchargez et installez Ollama depuis : https://ollama.ai/download

### Télécharger le modèle Llama
Après l'installation d'Ollama, ouvrez un terminal et exécutez :
```bash
ollama pull llama3.2
```

Vous pouvez aussi utiliser d'autres modèles :
```bash
# Modèle plus léger
ollama pull llama3.2:1b

# Modèle plus puissant
ollama pull llama3.1
```

## Installation de l'API Flask

1. **Créer un environnement virtuel** (recommandé) :
```bash
cd chatbot-api
python -m venv venv

# Windows
.\venv\Scripts\activate

# Linux/Mac
source venv/bin/activate
```

2. **Installer les dépendances** :
```bash
pip install -r requirements.txt
```

3. **Lancer le serveur** :
```bash
python app.py
```

Le serveur démarre sur `http://localhost:5000`

## Endpoints de l'API

### POST /api/chat
Envoie un message et reçoit une réponse.

**Request Body:**
```json
{
  "message": "Bonjour, comment réserver un laboratoire ?",
  "session_id": "optional_session_id"
}
```

**Response:**
```json
{
  "response": "Pour réserver un laboratoire, vous devez...",
  "session_id": "session_abc123"
}
```

### POST /api/chat/stream
Envoie un message et reçoit une réponse en streaming (Server-Sent Events).

### POST /api/chat/clear
Efface l'historique de conversation.

### GET /api/health
Vérifie que le service est en ligne.

## Configuration

### Changer le modèle
Dans `app.py`, modifiez la ligne :
```python
model='llama3.2'  # Changez ici
```

### Modifier le prompt système
Personnalisez le comportement du chatbot en modifiant `system_prompt` dans `app.py`.

## Dépannage

### Erreur "Connection refused"
- Vérifiez qu'Ollama est bien lancé : `ollama serve`
- Vérifiez que le modèle est téléchargé : `ollama list`

### Réponses lentes
- Essayez un modèle plus léger : `llama3.2:1b`
- Vérifiez les ressources système (RAM, CPU)

## Architecture

```
Frontend Vue.js (port 5173)
        ↓
API Flask (port 5000)
        ↓
Ollama (port 11434)
        ↓
Modèle Llama
```
