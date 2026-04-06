import api from './api'

// Récupérer toutes les réservations (essaie /all puis fallback)
export const getReservations = async () => {
  try {
    return await api.get('/reservations/all')
  } catch {
    return api.get('/reservations')
  }
}

// Récupérer les réservations du département du chef de département connecté
export const getReservationsByMonDepartement = async () => {
  try {
    return await api.get('/reservations/mon-departement')
  } catch {
    // Fallback : récupérer toutes et filtrer côté client
    return api.get('/reservations')
  }
}

// Récupérer une réservation par ID
export const getReservationById = (id: number) => api.get(`/reservations/${id}`)

// Récupérer les réservations d'un étudiant
export const getReservationsByEtudiant = (etudiantId: number) =>
  api.get(`/reservations/etudiant/${etudiantId}`)

// Réservations de l'étudiant connecté
export const getMesReservations = () => api.get('/reservations/mes')

// Alias pour compatibilité
export const getReservationsByUser = (userId: number) =>
  getReservationsByEtudiant(userId)

// Créer une réservation
export const createReservation = (data: {
  dateReservation: string
  heureDebut: string
  heureFin: string
  motif?: string
  laboratoireId: number
  etudiantId?: number
}) => api.post('/reservations', data)

// Modifier une réservation
export const updateReservation = (id: number, data: any) =>
  api.put(`/reservations/${id}`, data)

// Supprimer une réservation
export const deleteReservation = (id: number) => api.delete(`/reservations/${id}`)

// Annuler une réservation (par technicien avec notification)
export const annulerReservation = (id: number) => api.put(`/reservations/${id}/annuler`)

// Auto-annuler une réservation (par l'auteur, SANS notification)
export const autoAnnulerReservation = (id: number) => api.put(`/reservations/${id}/auto-annuler`)

// Approuver une réservation (technicien)
export const approveReservation = (id: number) => api.put(`/reservations/${id}/approve`)

// Refuser une réservation (technicien) avec motif
export const rejectReservation = (id: number, motif?: string) => 
  api.put(`/reservations/${id}/reject`, { motifRefus: motif })

// Vérifier la disponibilité d'un laboratoire pour une date et un créneau
export const checkLaboDisponibilite = async (laboId: number, date: string, heureDebut: string, heureFin: string) => {
  try {
    const res = await api.get(`/reservations/check-disponibilite`, {
      params: { laboratoireId: laboId, date, heureDebut, heureFin }
    })
    return res.data
  } catch {
    // Fallback: récupérer toutes les réservations et vérifier manuellement
    const allRes = await getReservations()
    const reservations = allRes.data || []
    return !reservations.some((r: any) => 
      r.laboratoireId === laboId && 
      r.dateReservation === date &&
      r.statut === 'APPROUVEE' &&
      ((heureDebut >= r.heureDebut && heureDebut < r.heureFin) ||
       (heureFin > r.heureDebut && heureFin <= r.heureFin) ||
       (heureDebut <= r.heureDebut && heureFin >= r.heureFin))
    )
  }
}

// Récupérer les réservations approuvées pour un laboratoire
export const getReservationsApprouveesParLabo = async (laboId: number) => {
  const allRes = await getReservations()
  const reservations = allRes.data || []
  return reservations.filter((r: any) => 
    (r.laboratoireId === laboId || r.laboratoire?.id === laboId) && 
    r.statut === 'APPROUVEE'
  )
}
