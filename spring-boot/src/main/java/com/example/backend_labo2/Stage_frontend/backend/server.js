const express = require('express')
const cors = require('cors')

const app = express()
// Le frontend (Vite) proxifie /api vers http://localhost:8085
const PORT = Number(process.env.PORT) || 8085

// Middleware
app.use(cors())
app.use(express.json())

// ==================== DONNÉES EN MÉMOIRE ====================

let departements = [
  { id: 1, nom: 'Informatique', description: 'Département d\'informatique et de génie logiciel', actif: true },
  { id: 2, nom: 'Mathématiques', description: 'Département de mathématiques et statistiques', actif: true },
  { id: 3, nom: 'Physique', description: 'Département de physique et sciences appliquées', actif: true },
  { id: 4, nom: 'Chimie', description: 'Département de chimie et biochimie', actif: true },
  { id: 5, nom: 'Génie Civil', description: 'Département de génie civil et construction', actif: true }
]

let laboratoires = [
  { id: 1, nom: 'Labo Info A', departementId: 1, capacite: 30, description: 'Laboratoire principal d\'informatique', etat: 'DISPONIBLE' },
  { id: 2, nom: 'Labo Info B', departementId: 1, capacite: 25, description: 'Laboratoire secondaire', etat: 'DISPONIBLE' },
  { id: 3, nom: 'Labo Math', departementId: 2, capacite: 20, description: 'Laboratoire de calcul', etat: 'OCCUPE' },
  { id: 4, nom: 'Labo Physique', departementId: 3, capacite: 15, description: 'Laboratoire d\'expérimentation', etat: 'EN_MAINTENANCE' },
  { id: 5, nom: 'Labo Chimie A', departementId: 4, capacite: 18, description: 'Laboratoire de chimie organique', etat: 'DISPONIBLE' }
]

let equipements = [
  { id: 1, nom: 'PC Dell OptiPlex', laboratoireId: 1, type: 'Ordinateur', statut: 'disponible', description: 'Ordinateur de bureau performant' },
  { id: 2, nom: 'Projecteur Epson', laboratoireId: 1, type: 'Projecteur', statut: 'disponible', description: 'Projecteur HD 4K' },
  { id: 3, nom: 'PC HP ProDesk', laboratoireId: 2, type: 'Ordinateur', statut: 'en_panne', description: 'Ordinateur de bureau' },
  { id: 4, nom: 'Oscilloscope', laboratoireId: 4, type: 'Instrument', statut: 'disponible', description: 'Oscilloscope numérique' },
  { id: 5, nom: 'Microscope électronique', laboratoireId: 5, type: 'Instrument', statut: 'disponible', description: 'Microscope haute résolution' },
  { id: 6, nom: 'Spectromètre', laboratoireId: 5, type: 'Instrument', statut: 'maintenance', description: 'Spectromètre UV-Visible' },
  { id: 7, nom: 'PC Lenovo ThinkCentre', laboratoireId: 3, type: 'Ordinateur', statut: 'disponible', description: 'Ordinateur de calcul' }
]

let users = [
  { id: 1, nom: 'Admin', prenom: 'Technicien', email: 'tech@univ.fr', password: 'admin123', cin: 'AB123456', role: 'TECHNICIEN', departementId: null, active: true },
  { id: 2, nom: 'Martin', prenom: 'Alice', email: 'alice@univ.fr', password: 'alice123', cin: 'CD234567', role: 'ETUDIANT', departementId: 1, niveau: 'L3', classe: 'Info-A', active: true },
  { id: 3, nom: 'Durand', prenom: 'Bob', email: 'bob@univ.fr', password: 'bob123', cin: 'EF345678', role: 'ETUDIANT', departementId: 1, niveau: 'L2', classe: 'Info-B', active: true },
  { id: 4, nom: 'Bernard', prenom: 'Claire', email: 'claire@univ.fr', password: 'claire123', cin: 'GH456789', role: 'ETUDIANT', departementId: 2, niveau: 'M1', classe: 'Math-1', active: true },
  { id: 5, nom: 'Dupont', prenom: 'Jean', email: 'jean@univ.fr', password: 'jean123', cin: 'IJ567890', role: 'ENSEIGNANT', departementId: 1, isChefDepartement: true, active: true },
  { id: 6, nom: 'Moreau', prenom: 'Marie', email: 'marie@univ.fr', password: 'marie123', cin: 'KL678901', role: 'ENSEIGNANT', departementId: 2, isChefDepartement: false, active: true }
]

let reservations = [
  { id: 1, userId: 2, laboratoireId: 1, dateReservation: '2026-01-27', heureDebut: '08:00', heureFin: '10:00', statut: 'APPROUVEE', motif: 'TP Programmation' },
  { id: 2, userId: 3, laboratoireId: 2, dateReservation: '2026-01-28', heureDebut: '14:00', heureFin: '16:00', statut: 'EN_ATTENTE', motif: 'Projet personnel' },
  { id: 3, userId: 2, laboratoireId: 1, dateReservation: '2026-01-30', heureDebut: '10:00', heureFin: '12:00', statut: 'EN_ATTENTE', motif: 'Révision exam' }
]

let reclamations = [
  { id: 1, userId: 5, equipementId: 3, dateCreation: '2026-01-15', titre: 'PC ne démarre plus', description: 'Ordinateur ne démarre plus depuis ce matin', statut: 'EN_COURS', priorite: 'HAUTE' },
  { id: 2, userId: 6, equipementId: 6, dateCreation: '2026-01-20', titre: 'Spectromètre en panne', description: 'Le spectromètre ne calibre plus correctement', statut: 'NOUVELLE', priorite: 'MOYENNE' }
]

// ID counters
let nextDepartementId = 6
let nextLaboratoireId = 6
let nextEquipementId = 8
let nextUserId = 7
let nextReservationId = 4
let nextReclamationId = 3

// Fonction helper pour enrichir les données avec les relations
function enrichLaboratoire(labo) {
  const dept = departements.find(d => d.id === labo.departementId)
  return { ...labo, departement: dept || null }
}

function enrichEquipement(equip) {
  const labo = laboratoires.find(l => l.id === equip.laboratoireId)
  return { ...equip, laboratoire: labo ? enrichLaboratoire(labo) : null }
}

function enrichUser(user) {
  const dept = departements.find(d => d.id === user.departementId)
  const { password, ...userWithoutPassword } = user
  return { ...userWithoutPassword, departement: dept || null }
}

function getUserFromAuthHeader(req) {
  const token = req.headers.authorization?.split(' ')[1]
  if (!token || !token.startsWith('fake-jwt-token-')) {
    return null
  }

  const userId = parseInt(token.split('-').pop(), 10)
  if (!Number.isFinite(userId)) return null
  return users.find(u => u.id === userId) || null
}

function enrichReservation(reservation) {
  const user = users.find(u => u.id === reservation.userId)
  const labo = laboratoires.find(l => l.id === reservation.laboratoireId)
  return { 
    ...reservation, 
    etudiant: user ? enrichUser(user) : null,
    laboratoire: labo ? enrichLaboratoire(labo) : null 
  }
}

function enrichReclamation(reclamation) {
  const user = users.find(u => u.id === reclamation.userId)
  const equip = equipements.find(e => e.id === reclamation.equipementId)
  return { 
    ...reclamation, 
    enseignant: user ? enrichUser(user) : null,
    equipement: equip ? enrichEquipement(equip) : null 
  }
}

// ==================== ROUTES AUTHENTIFICATION ====================

app.post('/api/auth/login', (req, res) => {
  const { email, password } = req.body
  
  if (!email || !password) {
    return res.status(400).json('Email et mot de passe requis')
  }

  const user = users.find(u => u.email === email && u.password === password)
  
  if (!user) {
    return res.status(401).json('Email ou mot de passe incorrect')
  }

  const enrichedUser = enrichUser(user)
  res.json({
    ...enrichedUser,
    token: 'fake-jwt-token-' + user.id
  })
})

app.post('/api/auth/register', (req, res) => {
  const {
    nom,
    prenom,
    email,
    password,
    cin,
    role,
    departementId,
    isChefDepartement,
    chefDepartement,
    niveau,
    classe,
  } = req.body
  
  // Vérifications
  if (!nom || !prenom || !email || !password || !cin || !role) {
    return res.status(400).json('Tous les champs de base sont requis')
  }

  const normalizedRole = String(role).toUpperCase()

  const parsedDepartementId = (departementId === null || departementId === '' || departementId === undefined)
    ? null
    : parseInt(departementId)

  // Département requis seulement si pas technicien
  if (normalizedRole !== 'TECHNICIEN') {
    if (!parsedDepartementId) {
      return res.status(400).json('Veuillez sélectionner un département')
    }
    // Vérifier si le département existe
    if (!departements.find(d => d.id === parsedDepartementId)) {
      return res.status(400).json('Département invalide')
    }
  }

  // Étudiant: niveau + classe requis
  if (normalizedRole === 'ETUDIANT') {
    if (!niveau || !classe) {
      return res.status(400).json('Niveau et classe requis pour les étudiants')
    }
  }

  // Vérifier si l'email existe déjà
  if (users.find(u => u.email === email)) {
    return res.status(400).json('Cet email est déjà utilisé')
  }

  // Vérifier si le CIN existe déjà
  if (users.find(u => u.cin === cin)) {
    return res.status(400).json('Ce CIN est déjà enregistré')
  }

  const newUser = {
    id: nextUserId++,
    nom,
    prenom,
    email,
    password,
    cin,
    role: normalizedRole,
    departementId: parsedDepartementId,
    niveau: normalizedRole === 'ETUDIANT' ? String(niveau) : undefined,
    classe: normalizedRole === 'ETUDIANT' ? String(classe) : undefined,
    isChefDepartement: normalizedRole === 'ENSEIGNANT' ? Boolean(isChefDepartement ?? chefDepartement) : false
  }

  users.push(newUser)

  const enrichedUser = enrichUser(newUser)
  res.status(201).json({
    ...enrichedUser,
    token: 'fake-jwt-token-' + newUser.id
  })
})

app.get('/api/auth/me', (req, res) => {
  const user = getUserFromAuthHeader(req)
  
  if (!user) {
    return res.status(401).json('Non autorisé')
  }

  res.json(enrichUser(user))
})

// Profil (utilisé par le frontend: /api/users/profil)
app.get('/api/users/profil', (req, res) => {
  const user = getUserFromAuthHeader(req)
  if (!user) {
    return res.status(401).json('Non autorisé')
  }
  res.json(enrichUser(user))
})

// ==================== ROUTES DÉPARTEMENTS ====================

app.get('/api/departements', (req, res) => {
  res.json(departements)
})

app.get('/api/departements/:id', (req, res) => {
  const dept = departements.find(d => d.id === parseInt(req.params.id))
  if (!dept) return res.status(404).json({ error: 'Département non trouvé' })
  res.json(dept)
})

app.post('/api/departements', (req, res) => {
  const newDept = { id: nextDepartementId++, actif: true, ...req.body }
  departements.push(newDept)
  res.status(201).json(newDept)
})

app.put('/api/departements/:id', (req, res) => {
  const index = departements.findIndex(d => d.id === parseInt(req.params.id))
  if (index === -1) return res.status(404).json({ error: 'Département non trouvé' })
  departements[index] = { ...departements[index], ...req.body }
  res.json(departements[index])
})

app.delete('/api/departements/:id', (req, res) => {
  const index = departements.findIndex(d => d.id === parseInt(req.params.id))
  if (index === -1) return res.status(404).json({ error: 'Département non trouvé' })
  departements.splice(index, 1)
  res.status(204).send()
})

// ==================== ROUTES LABORATOIRES ====================

app.get('/api/laboratoires', (req, res) => {
  res.json(laboratoires.map(enrichLaboratoire))
})

app.get('/api/laboratoires/:id', (req, res) => {
  const labo = laboratoires.find(l => l.id === parseInt(req.params.id))
  if (!labo) return res.status(404).json({ error: 'Laboratoire non trouvé' })
  res.json(enrichLaboratoire(labo))
})

app.post('/api/laboratoires', (req, res) => {
  const newLabo = { id: nextLaboratoireId++, etat: 'DISPONIBLE', ...req.body }
  laboratoires.push(newLabo)
  res.status(201).json(enrichLaboratoire(newLabo))
})

app.put('/api/laboratoires/:id', (req, res) => {
  const index = laboratoires.findIndex(l => l.id === parseInt(req.params.id))
  if (index === -1) return res.status(404).json({ error: 'Laboratoire non trouvé' })
  laboratoires[index] = { ...laboratoires[index], ...req.body }
  res.json(enrichLaboratoire(laboratoires[index]))
})

app.delete('/api/laboratoires/:id', (req, res) => {
  const index = laboratoires.findIndex(l => l.id === parseInt(req.params.id))
  if (index === -1) return res.status(404).json({ error: 'Laboratoire non trouvé' })
  laboratoires.splice(index, 1)
  res.status(204).send()
})

// ==================== ROUTES ÉQUIPEMENTS ====================

app.get('/api/equipements', (req, res) => {
  res.json(equipements.map(enrichEquipement))
})

app.get('/api/equipements/:id', (req, res) => {
  const equip = equipements.find(e => e.id === parseInt(req.params.id))
  if (!equip) return res.status(404).json({ error: 'Équipement non trouvé' })
  res.json(enrichEquipement(equip))
})

app.get('/api/equipements/laboratoire/:laboId', (req, res) => {
  const filtered = equipements.filter(e => e.laboratoireId === parseInt(req.params.laboId))
  res.json(filtered.map(enrichEquipement))
})

app.post('/api/equipements', (req, res) => {
  const newEquip = { id: nextEquipementId++, statut: 'disponible', ...req.body }
  equipements.push(newEquip)
  res.status(201).json(enrichEquipement(newEquip))
})

app.put('/api/equipements/:id', (req, res) => {
  const index = equipements.findIndex(e => e.id === parseInt(req.params.id))
  if (index === -1) return res.status(404).json({ error: 'Équipement non trouvé' })
  equipements[index] = { ...equipements[index], ...req.body }
  res.json(enrichEquipement(equipements[index]))
})

app.delete('/api/equipements/:id', (req, res) => {
  const index = equipements.findIndex(e => e.id === parseInt(req.params.id))
  if (index === -1) return res.status(404).json({ error: 'Équipement non trouvé' })
  equipements.splice(index, 1)
  res.status(204).send()
})

// ==================== ROUTES UTILISATEURS ====================

app.get('/api/users', (req, res) => {
  res.json(users.map(enrichUser))
})

app.get('/api/users/etudiants', (req, res) => {
  const etudiants = users.filter(u => u.role === 'ETUDIANT')
  res.json(etudiants.map(enrichUser))
})

app.get('/api/users/enseignants', (req, res) => {
  const enseignants = users.filter(u => u.role === 'ENSEIGNANT')
  res.json(enseignants.map(enrichUser))
})

app.get('/api/users/techniciens', (req, res) => {
  const techniciens = users.filter(u => u.role === 'TECHNICIEN')
  res.json(techniciens.map(enrichUser))
})

app.get('/api/users/:id', (req, res) => {
  const user = users.find(u => u.id === parseInt(req.params.id))
  if (!user) return res.status(404).json({ error: 'Utilisateur non trouvé' })
  res.json(enrichUser(user))
})

app.get('/api/etudiants', (req, res) => {
  const etudiants = users.filter(u => u.role === 'ETUDIANT')
  res.json(etudiants.map(enrichUser))
})

app.get('/api/enseignants', (req, res) => {
  const enseignants = users.filter(u => u.role === 'ENSEIGNANT')
  res.json(enseignants.map(enrichUser))
})

app.post('/api/users/etudiants', (req, res) => {
  const newUser = { id: nextUserId++, role: 'ETUDIANT', active: true, ...req.body }
  users.push(newUser)
  res.status(201).json(enrichUser(newUser))
})

app.post('/api/users/enseignants', (req, res) => {
  const newUser = { id: nextUserId++, role: 'ENSEIGNANT', active: true, ...req.body }
  users.push(newUser)
  res.status(201).json(enrichUser(newUser))
})

// Compatibilite avec le front: blocage/deblocage d'un utilisateur
app.put('/api/users/:id/block', (req, res) => {
  const index = users.findIndex(u => u.id === parseInt(req.params.id))
  if (index === -1) return res.status(404).json({ error: 'Utilisateur non trouvé' })

  users[index].active = false
  res.json({ message: 'Utilisateur bloqué', user: enrichUser(users[index]) })
})

app.put('/api/users/:id/unblock', (req, res) => {
  const index = users.findIndex(u => u.id === parseInt(req.params.id))
  if (index === -1) return res.status(404).json({ error: 'Utilisateur non trouvé' })

  users[index].active = true
  res.json({ message: 'Utilisateur débloqué', user: enrichUser(users[index]) })
})

app.put('/api/users/etudiants/:id/block', (req, res) => {
  const index = users.findIndex(u => u.id === parseInt(req.params.id) && u.role === 'ETUDIANT')
  if (index === -1) return res.status(404).json({ error: 'Étudiant non trouvé' })

  users[index].active = false
  res.json({ message: 'Étudiant bloqué', user: enrichUser(users[index]) })
})

app.put('/api/users/etudiants/:id/unblock', (req, res) => {
  const index = users.findIndex(u => u.id === parseInt(req.params.id) && u.role === 'ETUDIANT')
  if (index === -1) return res.status(404).json({ error: 'Étudiant non trouvé' })

  users[index].active = true
  res.json({ message: 'Étudiant débloqué', user: enrichUser(users[index]) })
})

app.put('/api/users/:id', (req, res) => {
  const index = users.findIndex(u => u.id === parseInt(req.params.id))
  if (index === -1) return res.status(404).json({ error: 'Utilisateur non trouvé' })
  users[index] = { ...users[index], ...req.body }
  res.json(enrichUser(users[index]))
})

app.put('/api/users/etudiants/:id', (req, res) => {
  const index = users.findIndex(u => u.id === parseInt(req.params.id) && u.role === 'ETUDIANT')
  if (index === -1) return res.status(404).json({ error: 'Étudiant non trouvé' })
  users[index] = { ...users[index], ...req.body }
  res.json(enrichUser(users[index]))
})

app.put('/api/users/enseignants/:id', (req, res) => {
  const index = users.findIndex(u => u.id === parseInt(req.params.id) && u.role === 'ENSEIGNANT')
  if (index === -1) return res.status(404).json({ error: 'Enseignant non trouvé' })
  users[index] = { ...users[index], ...req.body }
  res.json(enrichUser(users[index]))
})

app.delete('/api/users/:id', (req, res) => {
  const index = users.findIndex(u => u.id === parseInt(req.params.id))
  if (index === -1) return res.status(404).json({ error: 'Utilisateur non trouvé' })
  users.splice(index, 1)
  res.status(204).send()
})

// ==================== ROUTES RÉSERVATIONS ====================

app.get('/api/reservations', (req, res) => {
  res.json(reservations.map(enrichReservation))
})

app.get('/api/reservations/:id', (req, res) => {
  const reservation = reservations.find(r => r.id === parseInt(req.params.id))
  if (!reservation) return res.status(404).json({ error: 'Réservation non trouvée' })
  res.json(enrichReservation(reservation))
})

app.get('/api/reservations/user/:userId', (req, res) => {
  const filtered = reservations.filter(r => r.userId === parseInt(req.params.userId))
  res.json(filtered.map(enrichReservation))
})

app.get('/api/reservations/etudiant/:etudiantId', (req, res) => {
  const filtered = reservations.filter(r => r.userId === parseInt(req.params.etudiantId))
  res.json(filtered.map(enrichReservation))
})

app.post('/api/reservations', (req, res) => {
  const newReservation = { 
    id: nextReservationId++, 
    statut: 'EN_ATTENTE',
    ...req.body 
  }
  reservations.push(newReservation)
  res.status(201).json(enrichReservation(newReservation))
})

app.put('/api/reservations/:id', (req, res) => {
  const index = reservations.findIndex(r => r.id === parseInt(req.params.id))
  if (index === -1) return res.status(404).json({ error: 'Réservation non trouvée' })
  reservations[index] = { ...reservations[index], ...req.body }
  res.json(enrichReservation(reservations[index]))
})

app.patch('/api/reservations/:id/cancel', (req, res) => {
  const index = reservations.findIndex(r => r.id === parseInt(req.params.id))
  if (index === -1) return res.status(404).json({ error: 'Réservation non trouvée' })
  reservations[index].statut = 'REFUSEE'
  res.json(enrichReservation(reservations[index]))
})

app.delete('/api/reservations/:id', (req, res) => {
  const index = reservations.findIndex(r => r.id === parseInt(req.params.id))
  if (index === -1) return res.status(404).json({ error: 'Réservation non trouvée' })
  reservations.splice(index, 1)
  res.status(204).send()
})

// ==================== ROUTES RÉCLAMATIONS ====================

app.get('/api/reclamations', (req, res) => {
  res.json(reclamations.map(enrichReclamation))
})

app.get('/api/reclamations/:id', (req, res) => {
  const reclamation = reclamations.find(r => r.id === parseInt(req.params.id))
  if (!reclamation) return res.status(404).json({ error: 'Réclamation non trouvée' })
  res.json(enrichReclamation(reclamation))
})

app.get('/api/reclamations/enseignant/:enseignantId', (req, res) => {
  const filtered = reclamations.filter(r => r.userId === parseInt(req.params.enseignantId))
  res.json(filtered.map(enrichReclamation))
})

app.post('/api/reclamations', (req, res) => {
  const newReclamation = { 
    id: nextReclamationId++, 
    statut: 'NOUVELLE', 
    dateCreation: new Date().toISOString().split('T')[0],
    ...req.body 
  }
  reclamations.push(newReclamation)
  res.status(201).json(enrichReclamation(newReclamation))
})

app.put('/api/reclamations/:id', (req, res) => {
  const index = reclamations.findIndex(r => r.id === parseInt(req.params.id))
  if (index === -1) return res.status(404).json({ error: 'Réclamation non trouvée' })
  reclamations[index] = { ...reclamations[index], ...req.body }
  res.json(enrichReclamation(reclamations[index]))
})

app.delete('/api/reclamations/:id', (req, res) => {
  const index = reclamations.findIndex(r => r.id === parseInt(req.params.id))
  if (index === -1) return res.status(404).json({ error: 'Réclamation non trouvée' })
  reclamations.splice(index, 1)
  res.status(204).send()
})

// ==================== ROUTE STATS DASHBOARD ====================

app.get('/api/stats', (req, res) => {
  res.json({
    totalDepartements: departements.length,
    totalLaboratoires: laboratoires.length,
    totalEquipements: equipements.length,
    totalEtudiants: users.filter(u => u.role === 'ETUDIANT').length,
    totalEnseignants: users.filter(u => u.role === 'ENSEIGNANT').length,
    totalReservations: reservations.length,
    totalReclamations: reclamations.length,
    equipementsDisponibles: equipements.filter(e => e.statut === 'disponible').length,
    equipementsEnPanne: equipements.filter(e => e.statut === 'en_panne').length,
    reservationsEnAttente: reservations.filter(r => r.statut === 'EN_ATTENTE').length,
    reclamationsOuvertes: reclamations.filter(r => r.statut === 'NOUVELLE' || r.statut === 'EN_COURS').length
  })
})

// ==================== DÉMARRAGE SERVEUR ====================

app.listen(PORT, () => {
  console.log(`
  ╔════════════════════════════════════════════════════════════╗
  ║                                                            ║
  ║   🚀 Backend API démarré sur http://localhost:${PORT}          ║
  ║                                                            ║
  ║   Comptes de test disponibles:                             ║
  ║   - Technicien: tech@univ.fr / admin123                    ║
  ║   - Étudiant: alice@univ.fr / alice123                     ║
  ║   - Enseignant: jean@univ.fr / jean123                     ║
  ║                                                            ║
  ║   Endpoints disponibles:                                   ║
  ║   - POST /api/auth/login                                   ║
  ║   - POST /api/auth/register                                ║
  ║   - GET/POST    /api/departements                          ║
  ║   - GET/PUT/DEL /api/departements/:id                      ║
  ║   - GET/POST    /api/laboratoires                          ║
  ║   - GET/PUT/DEL /api/laboratoires/:id                      ║
  ║   - GET/POST    /api/equipements                           ║
  ║   - GET/PUT/DEL /api/equipements/:id                       ║
  ║   - GET/POST    /api/users, /api/users/etudiants, etc.     ║
  ║   - GET/POST    /api/reservations                          ║
  ║   - GET/POST    /api/reclamations                          ║
  ║   - GET         /api/stats                                 ║
  ║                                                            ║
  ╚════════════════════════════════════════════════════════════╝
  `)
})
