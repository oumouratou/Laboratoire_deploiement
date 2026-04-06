from flask import Flask, request, jsonify, Response
from flask_cors import CORS
import json
import time
import requests
import uuid
from datetime import datetime
import os


SPRING_BOOT_API = "http://localhost:8085/api"

try:
    import ollama
    OLLAMA_AVAILABLE = True
    print("✅ Ollama détecté - Mode IA activé")
except ImportError:
    OLLAMA_AVAILABLE = False
    print("⚠️ Ollama non installé - Mode simulation activé")

app = Flask(__name__)
CORS(app, resources={r"/api/*": {"origins": "*"}}, supports_credentials=True)

# Stockage persistant des sessions (fichier JSON)
SESSIONS_FILE = "chatbot_sessions.json"
user_sessions = {}

def load_persistent_sessions():
    """Charge les sessions depuis le fichier persistant"""
    global user_sessions
    try:
        if os.path.exists(SESSIONS_FILE):
            with open(SESSIONS_FILE, 'r', encoding='utf-8') as f:
                user_sessions = json.load(f)
            print(f"✅ {len(user_sessions)} utilisateurs avec sessions chargés")
    except Exception as e:
        print(f"⚠️ Erreur chargement sessions: {e}")
        user_sessions = {}

def save_persistent_sessions():
    """Sauvegarde les sessions dans le fichier persistant"""
    try:
        with open(SESSIONS_FILE, 'w', encoding='utf-8') as f:
            json.dump(user_sessions, f, ensure_ascii=False, indent=2)
    except Exception as e:
        print(f"⚠️ Erreur sauvegarde sessions: {e}")

# Charger les sessions au démarrage
load_persistent_sessions()

# ============== CONFIGURATION TOKENS OLLAMA ==============
# num_ctx : taille de la fenêtre de contexte (en tokens)
#   - Llama 3.2 supporte jusqu'à 128k tokens
#   - Plus c'est grand, plus le modèle peut "se souvenir" de la conversation
#   - Valeur par défaut de Ollama : 2048 (beaucoup trop petit)
OLLAMA_NUM_CTX = 3096  # 32k tokens de contexte

# num_predict : nombre max de tokens dans la réponse du modèle
OLLAMA_NUM_PREDICT = 2048

# Nombre max de messages d'historique envoyés au modèle
# -1 = tous les messages (illimité)
MAX_HISTORY_MESSAGES = -1

# Cache avec durée optimisée - plus courte pour être plus réactif
data_cache = {
    "laboratoires": {"data": None, "timestamp": 0},
    "equipements": {"data": None, "timestamp": 0},
    "departements": {"data": None, "timestamp": 0},
    "reservations": {"data": None, "timestamp": 0},
    "reclamations": {"data": None, "timestamp": 0},
    "users": {"data": None, "timestamp": 0}
}
CACHE_DURATION = 100  # Augmenté pour plus de rapidité

# ============== FONCTIONS DE RÉCUPÉRATION DES DONNÉES ==============

def get_auth_token():
    auth_header = request.headers.get('Authorization', '')
    if auth_header.startswith('Bearer '):
        return auth_header
    return None

def fetch_from_backend(endpoint, token=None):
    try:
        headers = {"Content-Type": "application/json"}
        if token:
            headers["Authorization"] = token
        response = requests.get(f"{SPRING_BOOT_API}/{endpoint}", headers=headers, timeout=10)
        if response.status_code == 200:
            return response.json()
        print(f"Backend {endpoint}: Status {response.status_code}")
        return None
    except Exception as e:
        print(f"Erreur connexion backend {endpoint}: {str(e)}")
        return None

def get_cached_data(data_type, token=None, force_refresh=False):
    current_time = time.time()
    cache_entry = data_cache.get(data_type)
    
    if not force_refresh and cache_entry and cache_entry["data"] is not None:
        if current_time - cache_entry["timestamp"] < CACHE_DURATION:
            return cache_entry["data"]
    
    endpoints = {
        "laboratoires": "laboratoires",
        "equipements": "equipements",
        "departements": "departements",
        "reservations": "reservations",
        "reclamations": "reclamations/all",
        "users": "users"
    }
    
    endpoint = endpoints.get(data_type)
    if endpoint:
        data = fetch_from_backend(endpoint, token)
        if data is not None:
            data_cache[data_type] = {"data": data, "timestamp": current_time}
            return data
    return []

# ============== RÉCUPÉRATION COMPLÈTE DES DONNÉES ==============

def get_all_database_data(token=None):
    """Récupère TOUTES les données de la base de données"""
    return {
        "departements": get_cached_data("departements", token),
        "laboratoires": get_cached_data("laboratoires", token),
        "equipements": get_cached_data("equipements", token),
        "users": get_cached_data("users", token),
        "reservations": get_cached_data("reservations", token),
        "reclamations": get_cached_data("reclamations", token)
    }

def get_user_info(user_id, token=None):
    """Récupère les informations complètes de l'utilisateur"""
    users = get_cached_data("users", token)
    for user in users:
        if user.get('id') == user_id:
            return user
    return None

def get_reservations_by_user(user_id, token=None):
    """Récupère les réservations d'un utilisateur"""
    reservations = get_cached_data("reservations", token)
    return [r for r in reservations if 
            r.get('etudiant', {}).get('id') == user_id or 
            r.get('etudiantId') == user_id]

def get_reclamations_by_user(user_id, token=None):
    """Récupère les réclamations d'un utilisateur"""
    reclamations = get_cached_data("reclamations", token)
    return [r for r in reclamations if 
            r.get('auteurId') == user_id or 
            r.get('auteur', {}).get('id') == user_id]

# ============== CONSTRUCTION DU CONTEXTE OPTIMISÉ ==============

def build_complete_database_context(user_id=None, user_role="", department_id=None, token=None):
    """Construit un contexte OPTIMISÉ de la base de données pour l'IA
    
    RÈGLES DE FILTRAGE:
    - ETUDIANT: Voit UNIQUEMENT son département
    - ENSEIGNANT: Voit TOUT (tous les départements)
    - TECHNICIEN: Voit TOUT (tous les départements)
    """
    context_parts = []
    
    all_data = get_all_database_data(token)
    
    # Informations utilisateur
    user_dept = None
    user_dept_id = department_id
    user_info = None
    
    if user_id:
        user_info = get_user_info(user_id, token)
        if user_info:
            user_dept = user_info.get('departement', {})
            if not user_dept_id:
                user_dept_id = user_dept.get('id') if isinstance(user_dept, dict) else None
            
            context_parts.append(f"""
📌 UTILISATEUR: {user_info.get('prenom', '')} {user_info.get('nom', '')} | Rôle: {user_role.upper()} | Département: {user_dept.get('nom', 'N/A') if isinstance(user_dept, dict) else 'N/A'}
""")
    
    # Déterminer si filtrage par département (SEULEMENT pour étudiants)
    filter_by_department = (user_role == "etudiant" and user_dept_id is not None)
    
    # DÉPARTEMENTS - Toujours afficher le nombre total
    departements = all_data.get("departements", [])
    # Supprimer les doublons basés sur l'ID
    unique_departements = {d.get('id'): d for d in departements if d.get('id')}
    departements = list(unique_departements.values())
    
    context_parts.append(f"""
🏛️ DÉPARTEMENTS: {len(departements)} au total""")
    for dept in departements:
        context_parts.append(f"  • {dept.get('nom', 'N/A')} (ID: {dept.get('id')})")
    
    # LABORATOIRES - FILTRAGE STRICT POUR ÉTUDIANTS
    laboratoires = all_data.get("laboratoires", [])
    
    if filter_by_department:
        # ÉTUDIANT: Filtrer UNIQUEMENT son département
        filtered_labs = [l for l in laboratoires if l.get('departement', {}).get('id') == user_dept_id]
        labs_actifs = [l for l in filtered_labs if l.get('actif', True)]
        
        context_parts.append(f"""
🔬 LABORATOIRES DE VOTRE DÉPARTEMENT ({len(filtered_labs)} total, {len(labs_actifs)} disponibles):""")
        for lab in filtered_labs:
            etat = "✅ Disponible" if lab.get('actif', True) else "❌ Indisponible"
            nom_labo = lab.get('nomLabo', lab.get('nom', 'N/A'))
            context_parts.append(f"  • [ID:{lab.get('id')}] {nom_labo} | Capacité: {lab.get('capacite', 'N/A')} | {etat}")
    else:
        # ENSEIGNANT/TECHNICIEN: Voir TOUS les labos
        context_parts.append(f"""
🔬 TOUS LES LABORATOIRES ({len(laboratoires)} au total):""")
        
        labs_by_dept = {}
        for lab in laboratoires:
            dept = lab.get('departement', {})
            dept_nom = dept.get('nom', 'Sans département')
            if dept_nom not in labs_by_dept:
                labs_by_dept[dept_nom] = []
            labs_by_dept[dept_nom].append(lab)
        
        for dept_nom, labs in labs_by_dept.items():
            actifs = len([l for l in labs if l.get('actif', True)])
            context_parts.append(f"\n  📁 {dept_nom}: {len(labs)} labos ({actifs} disponibles)")
            for lab in labs[:5]:
                etat = "✅" if lab.get('actif', True) else "❌"
                nom_labo = lab.get('nomLabo', lab.get('nom', 'N/A'))
                context_parts.append(f"    └─ {nom_labo} | Capacité: {lab.get('capacite', 'N/A')} | {etat}")
            if len(labs) > 5:
                context_parts.append(f"    └─ ... et {len(labs) - 5} autres")
    
    # ÉQUIPEMENTS - FILTRAGE STRICT POUR ÉTUDIANTS
    equipements = all_data.get("equipements", [])
    
    if filter_by_department:
        # ÉTUDIANT: Filtrer UNIQUEMENT son département
        filtered_equips = [e for e in equipements if e.get('laboratoire', {}).get('departement', {}).get('id') == user_dept_id]
        fonctionnels = len([e for e in filtered_equips if e.get('etat') == 'FONCTIONNEL'])
        en_panne = len([e for e in filtered_equips if e.get('etat') == 'EN_PANNE'])
        
        context_parts.append(f"""
🖥️ ÉQUIPEMENTS DE VOTRE DÉPARTEMENT ({len(filtered_equips)} total):
  📊 {fonctionnels} fonctionnels, {en_panne} en panne""")
        
        for eq in filtered_equips[:10]:
            etat_emoji = {"FONCTIONNEL": "✅", "EN_PANNE": "🔴", "EN_MAINTENANCE": "🟡"}.get(eq.get('etat'), "⚪")
            lab_nom = eq.get('laboratoire', {}).get('nomLabo', 'N/A')
            context_parts.append(f"  • {eq.get('nom', 'N/A')} | {etat_emoji} {eq.get('etat', 'N/A')} | Labo: {lab_nom}")
        if len(filtered_equips) > 10:
            context_parts.append(f"  ... et {len(filtered_equips) - 10} autres équipements")
    else:
        # ENSEIGNANT/TECHNICIEN: Voir TOUS les équipements
        fonctionnels = len([e for e in equipements if e.get('etat') == 'FONCTIONNEL'])
        en_panne = len([e for e in equipements if e.get('etat') == 'EN_PANNE'])
        en_maintenance = len([e for e in equipements if e.get('etat') == 'EN_MAINTENANCE'])
        
        context_parts.append(f"""
🖥️ TOUS LES ÉQUIPEMENTS ({len(equipements)} total):
  📊 {fonctionnels} fonctionnels | {en_panne} en panne | {en_maintenance} en maintenance""")
        
        # Grouper par labo pour les techniciens
        if user_role == "technicien":
            equips_by_lab = {}
            for eq in equipements:
                lab_nom = eq.get('laboratoire', {}).get('nomLabo', 'Sans labo')
                if lab_nom not in equips_by_lab:
                    equips_by_lab[lab_nom] = []
                equips_by_lab[lab_nom].append(eq)
            
            for lab_nom, equips in list(equips_by_lab.items())[:5]:
                context_parts.append(f"\n  🔬 {lab_nom}: {len(equips)} équipements")
    
    # UTILISATEURS (seulement pour technicien)
    if user_role == "technicien":
        users = all_data.get("users", [])
        etudiants = [u for u in users if u.get('role') == 'ETUDIANT']
        enseignants = [u for u in users if u.get('role') == 'ENSEIGNANT']
        
        context_parts.append(f"""
👥 UTILISATEURS: {len(etudiants)} étudiants | {len(enseignants)} enseignants""")
    
    # RÉSERVATIONS
    reservations = all_data.get("reservations", [])
    if user_role == "etudiant" and user_id:
        mes_reservations = get_reservations_by_user(user_id, token)
        en_attente = len([r for r in mes_reservations if r.get('statut') == 'EN_ATTENTE'])
        approuvees = len([r for r in mes_reservations if r.get('statut') == 'APPROUVEE'])
        context_parts.append(f"""
📅 VOS RÉSERVATIONS: {len(mes_reservations)} total ({en_attente} en attente, {approuvees} approuvées)""")
    elif user_role == "enseignant":
        # Les enseignants ne peuvent PAS faire de réservations dans cette application
        context_parts.append(f"""
📅 RÉSERVATIONS: Fonctionnalité non disponible pour les enseignants""")
    elif user_role == "technicien":
        en_attente = len([r for r in reservations if r.get('statut') == 'EN_ATTENTE'])
        approuvees = len([r for r in reservations if r.get('statut') == 'APPROUVEE'])
        refusees = len([r for r in reservations if r.get('statut') == 'REFUSEE'])
        context_parts.append(f"""
📅 RÉSERVATIONS: {len(reservations)} total | {approuvees} approuvées (réservés) | {en_attente} en attente | {refusees} refusées""")
    
    # RÉCLAMATIONS
    reclamations = all_data.get("reclamations", [])
    if user_role in ["etudiant", "enseignant"] and user_id:
        mes_reclamations = get_reclamations_by_user(user_id, token)
        non_traitees = len([r for r in mes_reclamations if r.get('etat') == 'NON_TRAITEE'])
        context_parts.append(f"""
⚠️ VOS RÉCLAMATIONS: {len(mes_reclamations)} total ({non_traitees} non traitées)""")
    elif user_role == "technicien":
        non_traitees = len([r for r in reclamations if r.get('etat') == 'NON_TRAITEE'])
        context_parts.append(f"""
⚠️ RÉCLAMATIONS: {len(reclamations)} total ({non_traitees} non traitées)""")
    
    return "\n".join(context_parts)

# ============== DÉTECTION DE SUJET PROJET ==============

def is_project_related(message):
    """Détecte si la question concerne les éléments du projet LabManager."""
    message_lower = message.lower()
    
    project_keywords = [
        # Laboratoires
        "labo", "laboratoire", "salle", "labos",
        # Équipements
        "équipement", "equipement", "materiel", "matériel", "appareil", "machine", "ordinateur", "pc",
        # Départements
        "département", "departement", "filière", "filiere",
        # Réservations
        "réservation", "reservation", "réserver", "reserver", "booking",
        # Réclamations
        "réclamation", "reclamation", "problème", "probleme", "panne", "signaler", "plainte",
        # Notifications
        "notification", "alerte", "notif",
        # Utilisateurs
        "étudiant", "etudiant", "enseignant", "professeur", "technicien", "utilisateur",
        # Navigation / pages
        "dashboard", "tableau de bord", "accueil", "page",
        # Statistiques projet
        "combien", "nombre", "total", "statistique", "stat",
        # Actions projet
        "disponible", "fonctionnel", "en panne", "maintenance", "approuvée", "en attente", "refusée",
        "non traitée", "traitée",
        # Contexte ISET
        "iset", "labmanager", "lab manager"
    ]
    
    return any(keyword in message_lower for keyword in project_keywords)

# ============== CONSTRUCTION DU SYSTEM PROMPT OPTIMISÉ ==============

def build_system_prompt(database_context, user_name="Utilisateur", user_role="", is_project_question=True):
    
    # Fonctionnalités disponibles par rôle
    role_features = {
        "etudiant": {
            "available": [
                "Consulter les laboratoires de votre département → /etudiant/laboratoires",
                "Consulter les équipements de votre département → /etudiant/equipements",
                "Gérer vos réservations (créer, consulter) → /etudiant/reservations",
                "Gérer vos réclamations (créer, suivre) → /etudiant/reclamations",
                "Consulter votre profil → /profil"
            ],
            "forbidden": [
                "gestion des utilisateurs", "liste des étudiants", "liste des enseignants",
                "gestion des départements", "tableaux de bord/statistiques globales",
                "voir tous les laboratoires de tous les départements",
                "approuver/refuser des réservations", "traiter des réclamations"
            ]
        },
        "enseignant": {
            "available": [
                "Consulter tous les laboratoires (tous départements) → /enseignant/laboratoires",
                "Consulter tous les équipements (tous départements) → /enseignant/equipements",
                "Gérer vos réclamations (créer, suivre) → /enseignant/reclamations",
                "Consulter vos notifications → /enseignant/notifications",
                "Consulter votre profil → /profil"
            ],
            "forbidden": [
                "faire des réservations", "gestion des utilisateurs",
                "liste des étudiants", "liste des enseignants",
                "gestion des départements", "approuver/refuser des réservations",
                "traiter des réclamations", "tableaux de bord/statistiques globales"
            ]
        },
        "technicien": {
            "available": [
                "Tableau de bord avec statistiques → /technicien/dashboard",
                "Gérer tous les laboratoires → /technicien/laboratoires",
                "Gérer tous les équipements → /technicien/equipements",
                "Gérer les étudiants → /technicien/etudiants",
                "Gérer les enseignants → /technicien/enseignants",
                "Gérer les réservations (approuver/refuser) → /technicien/reservations",
                "Gérer les réclamations (traiter) → /technicien/reclamations",
                "Gérer les départements → /technicien/departements",
                "Consulter votre profil → /profil"
            ],
            "forbidden": []
        }
    }
    
    features = role_features.get(user_role, {"available": [], "forbidden": []})
    available_list = "\n".join([f"  ✅ {f}" for f in features["available"]])
    
    role_instructions = {
        "etudiant": f"""
🎓 RÈGLES POUR ÉTUDIANT:
- Tu ne vois QUE les labos et équipements de TON département (affichés ci-dessus)
- Quand on te demande "combien", donne le NOMBRE EXACT d'abord, puis propose le lien
- Exemple: "Vous avez 3 laboratoires disponibles dans votre département. Cliquez ici pour les voir: /etudiant/laboratoires"

📌 FONCTIONNALITÉS DISPONIBLES pour cet étudiant:
{available_list}

🚫 FONCTIONNALITÉS INTERDITES (l'étudiant n'y a PAS accès):
  - Gestion des utilisateurs / liste des étudiants / liste des enseignants
  - Gestion des départements
  - Tableau de bord / statistiques globales
  - Voir les labos/équipements d'autres départements
  - Approuver/refuser des réservations
  - Traiter des réclamations

⚠️ SI l'étudiant demande une fonctionnalité INTERDITE:
→ Réponds poliment que cette fonctionnalité n'est pas disponible pour son profil.
→ Propose-lui les fonctionnalités auxquelles il a accès (listées ci-dessus).
→ Exemple: "Je suis désolé, cette fonctionnalité n'est pas disponible pour votre profil étudiant. Cependant, je peux vous aider avec : [liste des fonctionnalités disponibles]."
""",
        "enseignant": f"""
👨‍🏫 RÈGLES POUR ENSEIGNANT:
- Tu vois TOUS les laboratoires et équipements de TOUS les départements
- Quand on te demande "combien", donne le NOMBRE EXACT d'abord, puis propose le lien

📌 FONCTIONNALITÉS DISPONIBLES pour cet enseignant:
{available_list}

🚫 FONCTIONNALITÉS INTERDITES (l'enseignant n'y a PAS accès):
  - Faire des réservations (fonctionnalité réservée aux étudiants)
  - Gestion des utilisateurs / liste des étudiants / liste des enseignants
  - Gestion des départements
  - Approuver/refuser des réservations
  - Traiter des réclamations
  - Tableau de bord / statistiques globales

⚠️ SI l'enseignant demande une fonctionnalité INTERDITE:
→ Réponds poliment que cette fonctionnalité n'est pas disponible pour son profil.
→ Propose-lui les fonctionnalités auxquelles il a accès (listées ci-dessus).
→ Exemple: "Je suis désolé, cette fonctionnalité n'est pas disponible pour votre profil enseignant. Cependant, je peux vous aider avec : [liste des fonctionnalités disponibles]."
""",
        "technicien": f"""
🔧 RÈGLES POUR TECHNICIEN:
- Tu vois TOUT: tous départements, labos, équipements, utilisateurs, réservations
- Quand on te demande "combien", donne TOUJOURS le NOMBRE EXACT d'abord, puis propose le lien
- Pour les laboratoires réservés: donne le nombre de réservations approuvées comme nombre de labos réservés
- TOUJOURS inclure des liens de navigation dans tes réponses

📌 FONCTIONNALITÉS DISPONIBLES pour ce technicien:
{available_list}

✅ Le technicien a accès à TOUTES les fonctionnalités du système.
"""
    }
    
    if is_project_question:
        return f"""Tu es l'assistant intelligent LabManager de l'ISET Djerba. Tu es un assistant polyvalent et conversationnel.

📋 UTILISATEUR: {user_name} | RÔLE: {user_role.upper()}
{role_instructions.get(user_role, '')}

📊 DONNÉES DU PROJET:
{database_context}

⚡ INSTRUCTIONS:
1. La question actuelle concerne le projet LabManager → utilise les données ci-dessus pour répondre précisément.
2. Réponds en français, de manière concise (2-3 phrases).
3. Quand on demande un NOMBRE → Donne le chiffre EXACT d'abord.
4. Propose un lien de navigation pertinent quand c'est utile.
5. Format pour les données: "Vous avez X [éléments]. Pour les consulter: /chemin"
6. Si tu ne trouves pas l'info dans les données, dis-le honnêtement.
7. ⚠️ TRÈS IMPORTANT: Si l'utilisateur demande quelque chose qui n'est PAS dans ses fonctionnalités disponibles, refuse poliment et propose les alternatives accessibles.
"""
    else:
        return f"""Tu es l'assistant intelligent LabManager de l'ISET Djerba, mais tu es aussi un assistant général capable de répondre à toutes sortes de questions.

📋 UTILISATEUR: {user_name} | RÔLE: {user_role.upper()}

⚡ INSTRUCTIONS:
1. La question actuelle NE concerne PAS directement le projet LabManager (laboratoires, équipements, réservations, etc.).
2. Réponds de manière naturelle, utile et cohérente à la question posée.
3. Tu peux parler de n'importe quel sujet : culture générale, programmation, sciences, mathématiques, conseils, explications, etc.
4. Réponds TOUJOURS en français.
5. Sois conversationnel et amical. Adapte la longueur de ta réponse au sujet : courte pour une question simple, plus détaillée si nécessaire.
6. Maintiens la cohérence avec l'historique de la conversation.
7. Si l'utilisateur revient sur un sujet lié au projet, tu pourras utiliser les données projet dans les prochains messages.
"""

# ============== DÉTECTION D'INTENTION ==============

def detect_navigation_intent(message, user_role):
    message_lower = message.lower()
    base_path = f"/{user_role}" if user_role else ""
    
    navigation_keywords = ["voir", "afficher", "aller", "accéder", "montre", "ouvrir", "consulter", "liste", "va", "emmène", "dirige", "accès"]
    wants_navigation = any(word in message_lower for word in navigation_keywords)
    
    if any(word in message_lower for word in ["tableau de bord", "dashboard", "accueil", "statistiques"]):
        if wants_navigation:
            return f"{base_path}/dashboard"
    
    if any(word in message_lower for word in ["labo", "laboratoire", "salle"]):
        if wants_navigation:
            return f"{base_path}/laboratoires"
    
    if any(word in message_lower for word in ["équipement", "materiel", "appareil", "machine"]):
        if wants_navigation:
            return f"{base_path}/equipements"
    
    if any(word in message_lower for word in ["réservation", "reservation", "réserver"]):
        if user_role in ["etudiant", "technicien"]:
            if "nouvelle" in message_lower or "créer" in message_lower or "faire" in message_lower:
                return f"{base_path}/nouvelle-reservation"
            if wants_navigation or "mes" in message_lower:
                return f"{base_path}/reservations"
    
    if any(word in message_lower for word in ["réclamation", "reclamation", "problème", "panne", "signaler"]):
        if "nouvelle" in message_lower or "créer" in message_lower or "faire" in message_lower:
            return f"{base_path}/nouvelle-reclamation"
        if wants_navigation or "mes" in message_lower:
            return f"{base_path}/reclamations"
    
    if any(word in message_lower for word in ["département", "departement"]):
        if user_role == "technicien" and wants_navigation:
            return f"{base_path}/departements"
    
    if any(word in message_lower for word in ["étudiant", "etudiant"]):
        if user_role == "technicien" and wants_navigation:
            return f"{base_path}/etudiants"
    
    if any(word in message_lower for word in ["enseignant", "professeur"]):
        if user_role == "technicien" and wants_navigation:
            return f"{base_path}/enseignants"
    
    if any(word in message_lower for word in ["profil", "compte", "paramètre"]):
        return "/profil"
    
    return None

def detect_relevant_links(message, user_role):
    links = []
    message_lower = message.lower()
    base_path = f"/{user_role}" if user_role else ""
    
    if any(word in message_lower for word in ["labo", "laboratoire", "salle"]):
        links.append({"label": "📍 Laboratoires", "path": f"{base_path}/laboratoires", "icon": "bi-building"})
    
    if any(word in message_lower for word in ["équipement", "materiel", "appareil"]):
        links.append({"label": "🖥️ Équipements", "path": f"{base_path}/equipements", "icon": "bi-pc-display"})
    
    if any(word in message_lower for word in ["réservation", "reservation"]):
        if user_role in ["etudiant", "technicien"]:
            links.append({"label": "📅 Réservations", "path": f"{base_path}/reservations", "icon": "bi-calendar"})
    
    if any(word in message_lower for word in ["réclamation", "reclamation", "problème"]):
        links.append({"label": "⚠️ Réclamations", "path": f"{base_path}/reclamations", "icon": "bi-exclamation-triangle"})
    
    if any(word in message_lower for word in ["dashboard", "tableau", "stat"]):
        links.append({"label": "📊 Dashboard", "path": f"{base_path}/dashboard", "icon": "bi-speedometer2"})
    
    # Liens pour départements (technicien uniquement)
    if any(word in message_lower for word in ["département", "departement"]):
        if user_role == "technicien":
            links.append({"label": "🏛️ Départements", "path": f"{base_path}/departements", "icon": "bi-building"})
    
    # Liens pour étudiants et enseignants (technicien uniquement)
    if any(word in message_lower for word in ["étudiant", "etudiant"]):
        if user_role == "technicien":
            links.append({"label": "🎓 Étudiants", "path": f"{base_path}/etudiants", "icon": "bi-people"})
    
    if any(word in message_lower for word in ["enseignant", "professeur"]):
        if user_role == "technicien":
            links.append({"label": "👨‍🏫 Enseignants", "path": f"{base_path}/enseignants", "icon": "bi-person-badge"})
    
    # Liens sur les requêtes de comptage "combien"
    if any(word in message_lower for word in ["combien", "nombre", "total", "compte"]):
        # Ajouter des liens pertinents selon le contexte
        if any(word in message_lower for word in ["labo", "laboratoire", "réserv"]):
            if user_role == "technicien":
                links.append({"label": "📍 Laboratoires", "path": f"{base_path}/laboratoires", "icon": "bi-building"})
                links.append({"label": "📅 Réservations", "path": f"{base_path}/reservations", "icon": "bi-calendar"})
        if any(word in message_lower for word in ["département", "departement"]):
            if user_role == "technicien":
                links.append({"label": "🏛️ Départements", "path": f"{base_path}/departements", "icon": "bi-building"})
        if any(word in message_lower for word in ["équipement", "materiel"]):
            links.append({"label": "🖥️ Équipements", "path": f"{base_path}/equipements", "icon": "bi-pc-display"})
    
    # Supprimer les doublons
    seen = set()
    unique_links = []
    for link in links:
        if link["path"] not in seen:
            seen.add(link["path"])
            unique_links.append(link)
    
    return unique_links

# ============== GESTION DES SESSIONS ==============

def get_user_sessions(user_id):
    if user_id not in user_sessions:
        user_sessions[user_id] = {}
    return user_sessions[user_id]

def create_session(user_id, first_message=""):
    session_id = str(uuid.uuid4())[:8]
    title = first_message[:50] + "..." if len(first_message) > 50 else first_message
    if not title:
        title = "Nouvelle conversation"
    
    now = datetime.now().isoformat()
    session = {
        "id": session_id,
        "title": title,
        "messages": [],
        "liked": False,
        "created_at": now,
        "updated_at": now
    }
    
    if user_id not in user_sessions:
        user_sessions[user_id] = {}
    user_sessions[user_id][session_id] = session
    save_persistent_sessions()  # Sauvegarde persistante
    return session

def get_session(user_id, session_id):
    if user_id in user_sessions and session_id in user_sessions[user_id]:
        return user_sessions[user_id][session_id]
    return None

def update_session_title(user_id, session_id, new_title):
    session = get_session(user_id, session_id)
    if session:
        session["title"] = new_title[:50]
        session["updated_at"] = datetime.now().isoformat()
        save_persistent_sessions()  # Sauvegarde persistante
        return session
    return None

def toggle_session_like(user_id, session_id):
    session = get_session(user_id, session_id)
    if session:
        session["liked"] = not session["liked"]
        session["updated_at"] = datetime.now().isoformat()
        save_persistent_sessions()  # Sauvegarde persistante
        return session
    return None

def delete_session(user_id, session_id):
    if user_id in user_sessions and session_id in user_sessions[user_id]:
        del user_sessions[user_id][session_id]
        save_persistent_sessions()  # Sauvegarde persistante
        return True
    return False

def add_message_to_session(user_id, session_id, role, content):
    session = get_session(user_id, session_id)
    if session:
        session["messages"].append({
            "role": role,
            "content": content,
            "timestamp": datetime.now().isoformat()
        })
        session["updated_at"] = datetime.now().isoformat()
        
        user_messages = [m for m in session["messages"] if m["role"] == "user"]
        if len(user_messages) == 1 and role == "user":
            session["title"] = content[:50] + "..." if len(content) > 50 else content
        
        save_persistent_sessions()  # Sauvegarde persistante

def get_conversation_messages(session, system_prompt):
    """Construit la liste de messages à envoyer à Ollama avec gestion intelligente du contexte.
    
    Envoie TOUS les messages de la session (ou MAX_HISTORY_MESSAGES si configuré)
    avec un contexte de num_ctx tokens pour que le modèle ait assez de mémoire.
    """
    messages = [{'role': 'system', 'content': system_prompt}]
    
    if session and session.get("messages"):
        history = session["messages"]
        
        # Appliquer la limite si configurée (> 0)
        if MAX_HISTORY_MESSAGES > 0:
            history = history[-MAX_HISTORY_MESSAGES:]
        # Si MAX_HISTORY_MESSAGES == -1, on prend TOUT l'historique
        
        for msg in history:
            messages.append({'role': msg['role'], 'content': msg['content']})
    
    return messages

# ============== ROUTES API ==============

@app.route('/api/sessions', methods=['GET'])
def list_sessions():
    user_id = request.args.get('user_id', 'anonymous')
    sessions = get_user_sessions(user_id)
    session_list = list(sessions.values())
    session_list.sort(key=lambda x: x.get('updated_at', ''), reverse=True)
    return jsonify({"user_id": user_id, "sessions": session_list})

@app.route('/api/sessions', methods=['POST'])
def create_new_session():
    data = request.json
    user_id = str(data.get('user_id', 'anonymous'))
    first_message = data.get('first_message', '')
    session = create_session(user_id, first_message)
    return jsonify(session)

@app.route('/api/sessions/<session_id>', methods=['GET'])
def get_session_details(session_id):
    user_id = request.args.get('user_id', 'anonymous')
    session = get_session(user_id, session_id)
    if session:
        return jsonify(session)
    return jsonify({"error": "Session non trouvée"}), 404

@app.route('/api/sessions/<session_id>', methods=['PUT'])
def update_session(session_id):
    data = request.json
    user_id = str(data.get('user_id', 'anonymous'))
    new_title = data.get('title', '')
    session = update_session_title(user_id, session_id, new_title)
    if session:
        return jsonify(session)
    return jsonify({"error": "Session non trouvée"}), 404

@app.route('/api/sessions/<session_id>/like', methods=['POST'])
def toggle_like(session_id):
    data = request.json
    user_id = str(data.get('user_id', 'anonymous'))
    session = toggle_session_like(user_id, session_id)
    if session:
        return jsonify(session)
    return jsonify({"error": "Session non trouvée"}), 404

@app.route('/api/sessions/<session_id>', methods=['DELETE'])
def remove_session(session_id):
    user_id = request.args.get('user_id', 'anonymous')
    if delete_session(user_id, session_id):
        return jsonify({"message": "Session supprimée"})
    return jsonify({"error": "Session non trouvée"}), 404

@app.route('/api/chat', methods=['POST'])
def chat():
    try:
        data = request.json
        user_message = data.get('message', '')
        user_id = str(data.get('user_id', 'anonymous'))
        user_name = data.get('user_name', 'Utilisateur')
        user_role = data.get('user_role', '')
        department_id = data.get('department_id')  # Nouveau paramètre
        session_id = data.get('session_id', None)
        token = data.get('token', get_auth_token())
        
        if not user_message:
            return jsonify({'error': 'Message requis'}), 400
        
        if not session_id:
            session = create_session(user_id, user_message)
            session_id = session["id"]
        
        add_message_to_session(user_id, session_id, "user", user_message)
        
        # Détecter si la question concerne le projet ou non
        project_question = is_project_related(user_message)
        
        links = []
        navigate_to = None
        database_context = ""
        
        if project_question:
            user_id_int = int(user_id) if user_id.isdigit() else None
            dept_id_int = int(department_id) if department_id and str(department_id).isdigit() else None
            database_context = build_complete_database_context(user_id_int, user_role, dept_id_int, token)
            links = detect_relevant_links(user_message, user_role)
            navigate_to = detect_navigation_intent(user_message, user_role)
        
        if OLLAMA_AVAILABLE:
            system_prompt = build_system_prompt(database_context, user_name, user_role, is_project_question=project_question)
            session = get_session(user_id, session_id)
            messages = get_conversation_messages(session, system_prompt)
            
            response = ollama.chat(
                model='llama3.2',
                messages=messages,
                options={
                    'num_ctx': OLLAMA_NUM_CTX,
                    'num_predict': OLLAMA_NUM_PREDICT,
                }
            )
            assistant_message = response['message']['content']
        else:
            assistant_message = f"Bonjour {user_name} ! Mode simulation actif."
        
        add_message_to_session(user_id, session_id, "assistant", assistant_message)
        
        return jsonify({
            'response': assistant_message,
            'user_id': user_id,
            'session_id': session_id,
            'links': links,
            'navigate_to': navigate_to
        })
        
    except Exception as e:
        print(f"Erreur: {str(e)}")
        import traceback
        traceback.print_exc()
        return jsonify({'error': str(e)}), 500

@app.route('/api/chat/stream', methods=['POST'])
def chat_stream():
    try:
        data = request.json
        user_message = data.get('message', '')
        user_id = str(data.get('user_id', 'anonymous'))
        user_name = data.get('user_name', 'Utilisateur')
        user_role = data.get('user_role', '')
        department_id = data.get('department_id')  # Nouveau paramètre
        session_id = data.get('session_id', None)
        token = data.get('token', get_auth_token())
        
        if not user_message:
            return jsonify({'error': 'Message requis'}), 400
        
        if not session_id:
            session = create_session(user_id, user_message)
            session_id = session["id"]
        
        add_message_to_session(user_id, session_id, "user", user_message)
        
        # Détecter si la question concerne le projet ou non
        project_question = is_project_related(user_message)
        
        links = []
        navigate_to = None
        database_context = ""
        
        if project_question:
            user_id_int = int(user_id) if user_id.isdigit() else None
            dept_id_int = int(department_id) if department_id and str(department_id).isdigit() else None
            database_context = build_complete_database_context(user_id_int, user_role, dept_id_int, token)
            links = detect_relevant_links(user_message, user_role)
            navigate_to = detect_navigation_intent(user_message, user_role)
        
        def generate():
            full_response = ""
            
            if OLLAMA_AVAILABLE:
                system_prompt = build_system_prompt(database_context, user_name, user_role, is_project_question=project_question)
                session = get_session(user_id, session_id)
                messages = get_conversation_messages(session, system_prompt)
                
                try:
                    for chunk in ollama.chat(
                        model='llama3.2',
                        messages=messages,
                        stream=True,
                        options={
                            'num_ctx': OLLAMA_NUM_CTX,
                            'num_predict': OLLAMA_NUM_PREDICT,
                        }
                    ):
                        content = chunk['message']['content']
                        full_response += content
                        yield f"data: {json.dumps({'content': content})}\n\n"
                except Exception as e:
                    error_msg = f"Erreur: {str(e)}"
                    yield f"data: {json.dumps({'content': error_msg})}\n\n"
                    full_response = error_msg
                
                if navigate_to:
                    redirect_msg = "\n\n🔄 Je vous redirige..."
                    full_response += redirect_msg
                    yield f"data: {json.dumps({'content': redirect_msg})}\n\n"
            else:
                response = f"Bonjour {user_name} ! Mode simulation."
                for word in response.split(' '):
                    full_response += word + ' '
                    yield f"data: {json.dumps({'content': word + ' '})}\n\n"
                    time.sleep(0.03)
            
            add_message_to_session(user_id, session_id, "assistant", full_response)
            yield f"data: {json.dumps({'links': links, 'navigate_to': navigate_to, 'session_id': session_id, 'done': True})}\n\n"
        
        return Response(generate(), mimetype='text/event-stream')
        
    except Exception as e:
        print(f"Erreur: {str(e)}")
        import traceback
        traceback.print_exc()
        return jsonify({'error': str(e)}), 500

@app.route('/api/chat/clear', methods=['POST'])
def clear_conversation():
    data = request.json
    user_id = str(data.get('user_id', 'anonymous'))
    session_id = data.get('session_id', None)
    
    if session_id:
        session = get_session(user_id, session_id)
        if session:
            session["messages"] = []
            session["updated_at"] = datetime.now().isoformat()
    else:
        if user_id in user_sessions:
            user_sessions[user_id] = {}
    
    return jsonify({'message': 'Conversation effacée', 'user_id': user_id})

@app.route('/api/chat/history', methods=['GET'])
def get_history():
    user_id = request.args.get('user_id', 'anonymous')
    session_id = request.args.get('session_id', None)
    
    if session_id:
        session = get_session(user_id, session_id)
        if session:
            return jsonify({'user_id': user_id, 'session_id': session_id, 'messages': session["messages"]})
    
    sessions = get_user_sessions(user_id)
    return jsonify({'user_id': user_id, 'sessions': list(sessions.values())})

@app.route('/api/chat/context', methods=['GET'])
def get_context():
    """Debug: voir le contexte envoyé à l'IA"""
    user_id = request.args.get('user_id', 'anonymous')
    user_role = request.args.get('user_role', '')
    token = get_auth_token()
    user_id_int = int(user_id) if user_id.isdigit() else None
    context = build_complete_database_context(user_id_int, user_role, token)
    return jsonify({'context': context})

@app.route('/api/health', methods=['GET'])
def health_check():
    return jsonify({
        'status': 'ok', 
        'ollama_available': OLLAMA_AVAILABLE,
        'version': '2.0',
        'features': ['full_db_access', 'department_filtering', 'sessions', 'navigation']
    })

@app.route('/api/cache/refresh', methods=['POST'])
def refresh_cache():
    token = get_auth_token()
    for data_type in data_cache.keys():
        get_cached_data(data_type, token, force_refresh=True)
    return jsonify({'message': 'Cache rafraîchi'})

if __name__ == '__main__':
    print("🤖 LabManager Chatbot API v2.0")
    print("📍 URL: http://localhost:5001")
    print("✅ Accès COMPLET à la base de données")
    print("✅ Filtrage automatique par département")
    print("✅ Sessions et historique")
    print("✅ Navigation intelligente")
    app.run(host='0.0.0.0', port=5001, debug=True)
