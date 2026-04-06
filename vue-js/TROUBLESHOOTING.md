# 🔧 Guide de Dépannage - Problèmes de Chargement Technicien

## ✅ Vérifications Complétées

1. **Backend Java (port 8085)**: ✅ En cours d'exécution
2. **Frontend Vite**: ✅ Démarré sur port 5174
3. **Configuration API**: ✅ Mise à jour vers `http://localhost:8085/api`
4. **Fichier .env.local**: ✅ Créé avec `VITE_API_URL=http://localhost:8085`

---

## 🔍 Si tu vois encore des erreurs 403 ou ERR_CONNECTION_REFUSED

### 1️⃣ Vérifie dans la Console du Navigateur (F12 > Console)
- Cherche le message: `🔗 API Base URL: http://localhost:8085/api`
- Si tu vois: `http://localhost:5173/api` → Le serveur n'a pas rebrulité la config
  - **Solution**: Actualise la page (Ctrl+F5) ou redémarrera Vite

### 2️⃣ Erreur 403 Forbidden
- Cela signifie que le backend **répond** mais **refuse l'accès**
- Possibles causes:
  - ❌ Token d'authentification invalide ou expiré
  - ❌ L'utilisateur n'a pas les permissions technicien
  - ❌ CORS mal configuré côté backend
  
**Solution**:
- Se reconnecter
- Vérifier que tu es connecté comme **technicien**
- Vérifier les logs du backend Java pour les erreurs CORS/Auth

### 3️⃣ Erreur ERR_CONNECTION_REFUSED
- Le frontend ne peut pas se connecter au backend
- **Solution**:
  ```bash
  # Vérifie que le backend est actif:
  netstat -ano | findstr :8085
  # Devrait afficher une ligne avec 8085
  ```

---

## 📝 Si tu dois redémarrer le serveur

```bash
# 1. Arrête le serveur actuel (Ctrl+C)

# 2. Nettoie le cache:
npm run build

# 3. Redémarre:
npm run dev

# 4. Accède à: http://localhost:5174/ (ou le port affichéa dans le terminal)
```

---

## 🚀 Checklist Finale

- [ ] Backend Java tourne sur 8085
- [ ] Serveur Vite tourne (`npm run dev` actif)
- [ ] Console du navigateur affiche: `🔗 API Base URL: http://localhost:8085/api`
- [ ] Tu es connecté comme technicien
- [ ] Les tables (Laboratoires, Équipements, Réclamations) se remplissent

---

## 📞 Si rien ne fonctionne

1. Kill tous les processus Node:
   ```bash
   taskkill /F /IM node.exe
   ```

2. Redémarrer les services:
   ```bash
   npm run dev
   ```

3. Hard refresh du navigateur: **Ctrl+Shift+Del** puis vider le cache

4. Vérifier les logs du terminal Vite pour les erreurs d'import
