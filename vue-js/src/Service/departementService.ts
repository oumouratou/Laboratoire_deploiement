import api from './api';

export const getDepartements = () => api.get('/departements');
export const getDepartementById = (id: number) => api.get(`/departements/${id}`);
export const createDepartement = (data: any) => api.post('/departements', data);
export const updateDepartement = (id: number, data: any) => api.put(`/departements/${id}`, data);
export const deleteDepartement = (id: number) => api.delete(`/departements/${id}`);

// Retourne uniquement les départements actifs (pour étudiants/enseignants)
export const getActiveDepartements = async () => {
  try {
    // Utilise l'endpoint dédié s'il existe côté backend.
    return await api.get('/departements/actifs')
  } catch {
    const res = await getDepartements()
    const data = Array.isArray(res.data) ? res.data : []
    return { ...res, data: data.filter((d: any) => d.actif !== false) }
  }
}
