import api from './api'

// Récupérer tous les équipements
export const getEquipements = () => api.get('/equipements')

// Récupérer les équipements par laboratoire
export const getEquipementsByLabo = (laboId: number) =>
  api.get(`/equipements/laboratoire/${laboId}`)

// Créer un équipement
export const createEquipement = (data: any) => api.post('/equipements', data)

// Modifier un équipement
export const updateEquipement = (id: number, data: any) => api.put(`/equipements/${id}`, data)

// Supprimer un équipement
export const deleteEquipement = (id: number) => api.delete(`/equipements/${id}`)

// Export par défaut pour compatibilité
export default {
  getEquipements,
  getEquipementsByLabo,
  createEquipement,
  updateEquipement,
  deleteEquipement
}
