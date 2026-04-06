// src/Service/ReclamationService.ts
import api from './api'

const base = '/reclamations'

function extractDateKey(rec: any): string {
  const raw = rec?.dateReclamation || rec?.dateCreation || rec?.createdAt || rec?.date
  if (!raw) return ''
  const value = String(raw)
  return value.includes('T') ? (value.split('T')[0] || '') : value.slice(0, 10)
}

function normalizeStatus(rec: any): string {
  return String(rec?.etat || rec?.statut || rec?.status || '').toUpperCase()
}

function isPending(rec: any): boolean {
  const status = normalizeStatus(rec)
  return status === 'NON_TRAITEE' || status === 'NOUVELLE' || status === 'EN_COURS'
}

function getEquipementId(rec: any): number {
  return Number(rec?.equipementId ?? rec?.equipement?.id ?? 0)
}

function getLaboratoireId(rec: any): number {
  return Number(rec?.laboratoireId ?? rec?.laboratoire?.id ?? rec?.equipement?.laboratoire?.id ?? 0)
}

// ================= Export du service =================
const ReclamationService = {
  // 🔹 Mes réclamations (étudiant / enseignant)
  getMesReclamations: () => api.get(`${base}/mes`),

  // 🔹 Réclamations de l'étudiant connecté
  getReclamationsEtudiantConnecte: () => api.get(`${base}/etudiant`),

  // 🔹 Réclamations pour le technicien (essaie /all puis /pour-technicien)
  getReclamationsTechnicienConnecte: async () => {
    try {
      return await api.get(`${base}/all`)
    } catch {
      return api.get(`${base}/technicien`)
    }
  },
  
  getReclamationsPourTechnicien: async () => {
    try {
      return await api.get(`${base}/all`)
    } catch {
      return api.get(`${base}/pour-technicien`)
    }
  },
  
  // 🔹 Réclamations pour l'enseignant connecté
  getReclamationsEnseignantConnecte: () => api.get(`${base}/pour-enseignant`),

  // 🔹 Créer une nouvelle réclamation
  // Compat: certains backends exposent POST /reclamations/create, d'autres POST /reclamations
  createReclamation: async (data: any) => {
    try {
      return await api.post(`${base}/create`, data)
    } catch (error: any) {
      const status = error?.response?.status
      // Fallback uniquement si l'endpoint /create n'existe pas (404) ou méthode non autorisée (405)
      if (status === 404 || status === 405) {
        return api.post(`${base}`, data)
      }
      throw error
    }
  },

  // 🔹 Traiter une réclamation (approuver)
  traiterReclamation: (id: number) =>
    api.put(`${base}/${id}/traiter-json`, { action: "TRAITEE" }),

  // 🔹 Traiter automatiquement les réclamations liées
  // Critères: même équipement + même laboratoire + même jour
  traiterReclamationsLiees: async (referenceRec: any, allReclamations: any[]) => {
    const refId = Number(referenceRec?.id)
    const refEquipementId = getEquipementId(referenceRec)
    const refLaboratoireId = getLaboratoireId(referenceRec)
    const refDateKey = extractDateKey(referenceRec)

    if (!Number.isFinite(refId) || refId <= 0 || !refEquipementId || !refLaboratoireId || !refDateKey) {
      return { processed: 0, failed: 0 }
    }

    const candidates = (Array.isArray(allReclamations) ? allReclamations : []).filter((rec: any) => {
      const id = Number(rec?.id)
      if (!Number.isFinite(id) || id <= 0 || id === refId) return false
      if (!isPending(rec)) return false
      if (getEquipementId(rec) !== refEquipementId) return false
      if (getLaboratoireId(rec) !== refLaboratoireId) return false
      return extractDateKey(rec) === refDateKey
    })

    if (!candidates.length) return { processed: 0, failed: 0 }

    const results = await Promise.allSettled(
      candidates.map((rec: any) =>
        api.put(`${base}/${Number(rec.id)}/traiter-json`, { action: 'TRAITEE' }),
      ),
    )

    const processed = results.filter(r => r.status === 'fulfilled').length
    const failed = results.length - processed
    return { processed, failed }
  },

  // 🔹 Annuler / Refuser une réclamation (par technicien - avec notification)
  annulerReclamation: (id: number, motif?: string) =>
    api.put(`${base}/${id}/traiter-json`, { action: "REFUSEE", motifRefus: motif || "" }),

  // 🔹 Refuser une réclamation avec motif détaillé (OBLIGATOIRE)
  refuserReclamationAvecMotif: (id: number, motif: string) =>
    api.put(`${base}/${id}/traiter-json`, { action: "REFUSEE", motifRefus: motif }),

  // 🔹 Mettre à jour le statut d'une réclamation (avec action personnalisée)
  updateReclamationStatus: (id: number, action: string, motifRefus?: string) =>
    api.put(`${base}/${id}/traiter-json`, { action, motifRefus: motifRefus || "" }),

  // 🔹 Laboratoires par département de l'utilisateur connecté
  getLaboratoiresByDepartement: () => api.get(`${base}/laboratoires/by-departement`),

  // 🔹 Équipements par laboratoire
  // Compat: si l'endpoint réclamation n'existe pas/casse, fallback vers l'endpoint équipements
  getEquipementsByLaboReclamation: async (laboId: number) => {
    try {
      return await api.get(`${base}/by-labo/${laboId}`)
    } catch (error: any) {
      const status = error?.response?.status
      if (status === 404 || status === 405 || status === 500) {
        return api.get(`/equipements/laboratoire/${laboId}`)
      }
      throw error
    }
  },

  // 🔹 Toutes les réclamations
  getAllReclamations: async () => {
    try {
      return await api.get(`${base}/all`)
    } catch {
      return api.get(`${base}/pour-technicien`)
    }
  },

  // 🔹 Modifier une réclamation
  updateReclamation: (id: number, data: { description: string; quantite?: number }) =>
    api.put(`${base}/${id}`, data),

  // 🔹 Auto-annuler (par l'auteur lui-même, sans notification)
  autoAnnulerReclamation: (id: number) =>
    api.put(`${base}/${id}/auto-annuler`),

  // 🔹 Supprimer une réclamation (fallback auto-annuler si DELETE indisponible)
  deleteReclamation: async (id: number) => {
    try {
      return await api.delete(`${base}/${id}`)
    } catch (error: any) {
      const status = error?.response?.status
      if (status === 404 || status === 405) {
        return api.put(`${base}/${id}/auto-annuler`)
      }
      throw error
    }
  }
}

export default ReclamationService
