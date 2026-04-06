import { ref } from 'vue'
import type { Departement, Laboratoire, Equipement, Reservation, Reclamation, User } from '@/types'

// Données de démonstration
const departements = ref<Departement[]>([
  { id: 1, nom: 'Informatique', description: 'Département Informatique et Réseaux' },
  { id: 2, nom: 'Chimie', description: 'Département de Chimie Appliquée' },
  { id: 3, nom: 'Physique', description: 'Département de Physique Fondamentale' }
])

const laboratoires = ref<Laboratoire[]>([
  { id: 1, nom: 'Labo Développement Web', departementId: 1, capacite: 30, description: 'Laboratoire pour le développement web et mobile' },
  { id: 2, nom: 'Labo Réseaux', departementId: 1, capacite: 25, description: 'Laboratoire pour les réseaux et systèmes' },
  { id: 3, nom: 'Labo Chimie Organique', departementId: 2, capacite: 20, description: 'Laboratoire de chimie organique' },
  { id: 4, nom: 'Labo Optique', departementId: 3, capacite: 15, description: 'Laboratoire d\'optique et photonique' }
])

const equipements = ref<Equipement[]>([
  { id: 1, nom: 'Ordinateur HP EliteDesk', description: 'PC de bureau haute performance', laboratoireId: 1, etat: 'FONCTIONNEL', dateAcquisition: '2024-01-15' },
  { id: 2, nom: 'Switch Cisco 24 ports', description: 'Switch réseau manageable', laboratoireId: 2, etat: 'FONCTIONNEL', dateAcquisition: '2023-06-20' },
  { id: 3, nom: 'Microscope Optique', description: 'Microscope haute résolution', laboratoireId: 3, etat: 'en_panne', dateAcquisition: '2022-09-10' },
  { id: 4, nom: 'Spectromètre UV', description: 'Spectromètre UV-Visible', laboratoireId: 4, etat: 'EN_MAINTENANCE', dateAcquisition: '2023-03-05' },
  { id: 5, nom: 'Serveur Dell PowerEdge', description: 'Serveur rack haute disponibilité', laboratoireId: 2, etat: 'FONCTIONNEL', dateAcquisition: '2024-02-28' }
])

const reservations = ref<Reservation[]>([
  { id: 1, etudiantId: 3, laboratoireId: 1, dateReservation: '2026-01-20', heureDebut: '08:00', heureFin: '10:00', statut: 'APPROUVEE', motif: 'TP Développement Web' },
  { id: 2, etudiantId: 3, laboratoireId: 2, dateReservation: '2026-01-22', heureDebut: '14:00', heureFin: '16:00', statut: 'EN_ATTENTE', motif: 'Projet Réseaux' }
])

const reclamations = ref<Reclamation[]>([
  { id: 1, titre: 'Microscope défectueux', enseignantId: 2, equipementId: 3, description: 'Le microscope ne fonctionne plus correctement, image floue', dateCreation: '2026-01-15', statut: 'EN_COURS', priorite: 'HAUTE' },
  { id: 2, titre: 'Spectromètre à calibrer', enseignantId: 2, equipementId: 4, description: 'Calibration nécessaire pour le spectromètre', dateCreation: '2026-01-10', statut: 'NOUVELLE', priorite: 'MOYENNE' }
])

const etudiants = ref<User[]>([
  { id: 3, nom: 'Bernard', prenom: 'Lucas', email: 'lucas.bernard@univ.com', role: 'ETUDIANT', departementId: 1, niveau: 'L2', classe: 'Info-B' },
  { id: 4, nom: 'Petit', prenom: 'Emma', email: 'emma.petit@univ.com', role: 'ETUDIANT', departementId: 2, niveau: 'L3', classe: 'Chimie-A' },
  { id: 5, nom: 'Durand', prenom: 'Thomas', email: 'thomas.durand@univ.com', role: 'ETUDIANT', departementId: 1, niveau: 'M1', classe: 'Info-A' }
])

const enseignants = ref<User[]>([
  { id: 2, nom: 'Martin', prenom: 'Sophie', email: 'sophie.martin@univ.com', role: 'ENSEIGNANT', departementId: 1 },
  { id: 6, nom: 'Leroy', prenom: 'Pierre', email: 'pierre.leroy@univ.com', role: 'ENSEIGNANT', departementId: 2 },
  { id: 7, nom: 'Moreau', prenom: 'Claire', email: 'claire.moreau@univ.com', role: 'ENSEIGNANT', departementId: 3 }
])

export function useData() {
  // CRUD Départements
  function addDepartement(dept: Omit<Departement, 'id'>) {
    const newId = Math.max(...departements.value.map(d => d.id), 0) + 1
    departements.value.push({ ...dept, id: newId })
  }

  function updateDepartement(id: number, data: Partial<Departement>) {
    const item = departements.value.find(d => d.id === id)
    if (item) {
      if (data.nom !== undefined) item.nom = data.nom
      if (data.description !== undefined) item.description = data.description
    }
  }

  function deleteDepartement(id: number) {
    departements.value = departements.value.filter(d => d.id !== id)
  }

  // CRUD Laboratoires
  function addLaboratoire(labo: Omit<Laboratoire, 'id'>) {
    const newId = Math.max(...laboratoires.value.map(l => l.id), 0) + 1
    laboratoires.value.push({ ...labo, id: newId })
  }

  function updateLaboratoire(id: number, data: Partial<Laboratoire>) {
    const item = laboratoires.value.find(l => l.id === id)
    if (item) {
      if (data.nom !== undefined) item.nom = data.nom
      if (data.departementId !== undefined) item.departementId = data.departementId
      if (data.capacite !== undefined) item.capacite = data.capacite
      if (data.description !== undefined) item.description = data.description
    }
  }

  function deleteLaboratoire(id: number) {
    laboratoires.value = laboratoires.value.filter(l => l.id !== id)
  }

  // CRUD Équipements
  function addEquipement(equip: Omit<Equipement, 'id'>) {
    const newId = Math.max(...equipements.value.map(e => e.id), 0) + 1
    equipements.value.push({ ...equip, id: newId })
  }

  function updateEquipement(id: number, data: Partial<Equipement>) {
    const item = equipements.value.find(e => e.id === id)
    if (item) {
      if (data.nom !== undefined) item.nom = data.nom
      if (data.description !== undefined) item.description = data.description
      if (data.laboratoireId !== undefined) item.laboratoireId = data.laboratoireId
      if (data.etat !== undefined) item.etat = data.etat
      if (data.dateAcquisition !== undefined) item.dateAcquisition = data.dateAcquisition
    }
  }

  function deleteEquipement(id: number) {
    equipements.value = equipements.value.filter(e => e.id !== id)
  }

  // CRUD Réservations
  function addReservation(res: Omit<Reservation, 'id'>) {
    const newId = Math.max(...reservations.value.map(r => r.id), 0) + 1
    reservations.value.push({ ...res, id: newId })
  }

  function updateReservation(id: number, data: Partial<Reservation>) {
    const item = reservations.value.find(r => r.id === id)
    if (item) {
      if (data.dateReservation !== undefined) item.dateReservation = data.dateReservation
      if (data.heureDebut !== undefined) item.heureDebut = data.heureDebut
      if (data.heureFin !== undefined) item.heureFin = data.heureFin
      if (data.statut !== undefined) item.statut = data.statut
      if (data.motif !== undefined) item.motif = data.motif
    }
  }

  function cancelReservation(id: number) {
    updateReservation(id, { statut: 'REFUSEE' })
  }

  // CRUD Réclamations
  function addReclamation(rec: Omit<Reclamation, 'id'>) {
    const newId = Math.max(...reclamations.value.map(r => r.id), 0) + 1
    reclamations.value.push({ ...rec, id: newId })
  }

  function updateReclamation(id: number, data: Partial<Reclamation>) {
    const item = reclamations.value.find(r => r.id === id)
    if (item) {
      if (data.description !== undefined) item.description = data.description
      if (data.statut !== undefined) item.statut = data.statut
      if (data.priorite !== undefined) item.priorite = data.priorite
    }
  }

  function deleteReclamation(id: number) {
    reclamations.value = reclamations.value.filter(r => r.id !== id)
  }

  // CRUD Étudiants
  function addEtudiant(etud: Omit<User, 'id'>) {
    const newId = Math.max(...etudiants.value.map(e => e.id), 0) + 1
    etudiants.value.push({ ...etud, id: newId, role: 'ETUDIANT' })
  }

  function updateEtudiant(id: number, data: Partial<User>) {
    const item = etudiants.value.find(e => e.id === id)
    if (item) {
      if (data.nom !== undefined) item.nom = data.nom
      if (data.prenom !== undefined) item.prenom = data.prenom
      if (data.email !== undefined) item.email = data.email
      if (data.departementId !== undefined) item.departementId = data.departementId
    }
  }

  function deleteEtudiant(id: number) {
    etudiants.value = etudiants.value.filter(e => e.id !== id)
  }

  // CRUD Enseignants
  function addEnseignant(ens: Omit<User, 'id'>) {
    const newId = Math.max(...enseignants.value.map(e => e.id), 0) + 1
    enseignants.value.push({ ...ens, id: newId, role: 'ENSEIGNANT' })
  }

  function updateEnseignant(id: number, data: Partial<User>) {
    const item = enseignants.value.find(e => e.id === id)
    if (item) {
      if (data.nom !== undefined) item.nom = data.nom
      if (data.prenom !== undefined) item.prenom = data.prenom
      if (data.email !== undefined) item.email = data.email
      if (data.departementId !== undefined) item.departementId = data.departementId
    }
  }

  function deleteEnseignant(id: number) {
    enseignants.value = enseignants.value.filter(e => e.id !== id)
  }

  // Fonctions utilitaires
  function getDepartementById(id: number) {
    return departements.value.find(d => d.id === id)
  }

  function getLaboratoireById(id: number) {
    return laboratoires.value.find(l => l.id === id)
  }

  function getEquipementById(id: number) {
    return equipements.value.find(e => e.id === id)
  }

  function getReservationsByEtudiant(etudiantId: number) {
    return reservations.value.filter(r => r.etudiantId === etudiantId)
  }

  function getReclamationsByEnseignant(enseignantId: number) {
    return reclamations.value.filter(r => r.enseignantId === enseignantId)
  }

  function getReservationsByDepartement(departementId: number) {
    const labosIds = laboratoires.value
      .filter(l => l.departementId === departementId)
      .map(l => l.id)
    return reservations.value.filter(r => r.laboratoireId !== undefined && labosIds.includes(r.laboratoireId))
  }

  function getReclamationsByLaboratoire(laboratoireId: number) {
    const equipIds = equipements.value
      .filter(e => e.laboratoireId === laboratoireId)
      .map(e => e.id)
    return reclamations.value.filter(r => r.equipementId !== undefined && equipIds.includes(r.equipementId))
  }

  return {
    // Data
    departements,
    laboratoires,
    equipements,
    reservations,
    reclamations,
    etudiants,
    enseignants,
    // CRUD Départements
    addDepartement,
    updateDepartement,
    deleteDepartement,
    // CRUD Laboratoires
    addLaboratoire,
    updateLaboratoire,
    deleteLaboratoire,
    // CRUD Équipements
    addEquipement,
    updateEquipement,
    deleteEquipement,
    // CRUD Réservations
    addReservation,
    updateReservation,
    cancelReservation,
    // CRUD Réclamations
    addReclamation,
    updateReclamation,
    deleteReclamation,
    // CRUD Étudiants
    addEtudiant,
    updateEtudiant,
    deleteEtudiant,
    // CRUD Enseignants
    addEnseignant,
    updateEnseignant,
    deleteEnseignant,
    // Utilitaires
    getDepartementById,
    getLaboratoireById,
    getEquipementById,
    getReservationsByEtudiant,
    getReclamationsByEnseignant,
    getReservationsByDepartement,
    getReclamationsByLaboratoire
  }
}
