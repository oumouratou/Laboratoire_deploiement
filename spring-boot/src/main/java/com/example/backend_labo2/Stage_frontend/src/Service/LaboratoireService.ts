import api from './api'

// Tous les labos (admin)
export const getLaboratoires = () => api.get('/laboratoires')

// 🔥 Labos liés à l'utilisateur connecté (enseignant / étudiant)
export const getMesLabos = () => api.get('/laboratoires/mes-labos')

export const createLaboratoire = (data: any) => api.post('/laboratoires', data)
export const updateLaboratoire = (id: number, data: any) => api.put(`/laboratoires/${id}`, data)
export const deleteLaboratoire = (id: number) => api.delete(`/laboratoires/${id}`)
export const getEtatsLaboratoire = () => api.get('/laboratoires/etats')
