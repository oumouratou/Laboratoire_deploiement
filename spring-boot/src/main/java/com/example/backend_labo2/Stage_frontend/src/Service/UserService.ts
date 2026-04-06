import api from './api'

const tryPut = async (urls: string[]) => {
	let lastError: any = null

	for (const url of urls) {
		try {
			return await api.put(url)
		} catch (error: any) {
			lastError = error
			if (error?.response?.status !== 404) {
				throw error
			}
		}
	}

	throw lastError
}

// ================== ETUDIANTS ==================

// Créer un étudiant
export const createEtudiant = (data: any) => api.post('/users/etudiants', data)

// Modifier un étudiant
export const updateEtudiant = (id: number, data: any) => api.put(`/users/etudiants/${id}`, data)

// Récupérer tous les étudiants
export const getEtudiants = () => api.get('/users/etudiants')

// ================== ENSEIGNANTS ==================

// Créer un enseignant
export const createEnseignant = (data: any) => api.post('/users/enseignants', data)

// Modifier un enseignant
export const updateEnseignant = (id: number, data: any) => api.put(`/users/enseignants/${id}`, data)

// Récupérer tous les enseignants
export const getEnseignants = () => api.get('/users/enseignants')

// ================== TOUS LES UTILISATEURS ==================
export const getUsers = () => api.get('/users')

// ================== BLOQUER / DÉBLOQUER ==================
export const blockUser = (id: number) =>
	tryPut([
		`/users/etudiants/${id}/block`,
		`/users/${id}/block`
	])

export const unblockUser = (id: number) =>
	tryPut([
		`/users/etudiants/${id}/unblock`,
		`/users/${id}/unblock`
	])

// ================== SUPPRIMER ==================
export const deleteUser = (id: number) => api.delete(`/users/${id}`)

// ================== BLOQUER / DEBLOQUER ETUDIANT ==================
export const blockEtudiant = async (id: number) => {
	try {
		return await api.put(`/users/etudiants/${id}/block`)
	} catch {
		return api.put(`/users/${id}/block`)
	}
}

export const unblockEtudiant = async (id: number) => {
	try {
		return await api.put(`/users/etudiants/${id}/unblock`)
	} catch {
		return api.put(`/users/${id}/unblock`)
	}
}
