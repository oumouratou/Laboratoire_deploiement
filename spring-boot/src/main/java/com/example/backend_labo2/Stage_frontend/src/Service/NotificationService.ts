// src/Service/NotificationService.ts
import api from './api'

// ----------------- DTO -----------------
export interface NotificationDTO {
  id: number
  message: string
  read: boolean
  createdAt: string
  reservationId: number | null
  reclamationId?: number | null
  laboName?: string
  laboratoireNom?: string
  type: 'RESERVATION' | 'RECLAMATION' | 'ALERT' | 'INFO' | string
  // Motif de refus (si applicable)
  motifRefus?: string
  // Données enrichies du demandeur
  demandeurNom?: string
  demandeurPrenom?: string
  demandeurCin?: string
  etudiantNom?: string
  etudiantPrenom?: string
  etudiantCin?: string
}

function excludeSelfSentAlerts(rows: NotificationDTO[]): NotificationDTO[] {
  return rows.filter((n) => {
    const type = String(n?.type || '').toUpperCase()
    return type !== 'ALERT' && type !== 'ALERTE'
  })
}

// ----------------- Technicien -----------------
export async function getNotificationsTechnicien(includeAlerts: boolean = false): Promise<{ data: NotificationDTO[] }> {
  try {
    const result = await api.get<NotificationDTO[]>('/notifications/technicien', { withCredentials: true })
    const rows = Array.isArray(result.data) ? result.data : []
    result.data = includeAlerts ? rows : excludeSelfSentAlerts(rows)
    console.log('Notifications technicien:', result.data)
    return result
  } catch (error) {
    console.error('Erreur getNotificationsTechnicien:', error)
    return { data: [] }
  }
}

// ----------------- Toutes les notifications (pour technicien global) -----------------
export async function getAllNotifications(includeAlerts: boolean = false): Promise<{ data: NotificationDTO[] }> {
  // Essayer d'abord l'endpoint technicien avec historique complet
  try {
    const result = await api.get<NotificationDTO[]>('/notifications/technicien/all', { withCredentials: true })
    const rows = Array.isArray(result.data) ? result.data : []
    result.data = includeAlerts ? rows : excludeSelfSentAlerts(rows)
    console.log(`Notifications chargées via /notifications/technicien/all:`, result.data)
    return result
  } catch (error: any) {
    console.warn(`Endpoint /notifications/technicien/all échoué:`, error.response?.status)
  }

  // Fallback endpoint technicien
  try {
    const result = await api.get<NotificationDTO[]>('/notifications/technicien', { withCredentials: true })
    const rows = Array.isArray(result.data) ? result.data : []
    result.data = includeAlerts ? rows : excludeSelfSentAlerts(rows)
    console.log(`Notifications chargées via /notifications/technicien:`, result.data)
    return result
  } catch (error: any) {
    console.warn(`Endpoint /notifications/technicien échoué:`, error.response?.status)
  }
  
  // Fallback sur /notifications/all
  try {
    const result = await api.get<NotificationDTO[]>('/notifications/all', { withCredentials: true })
    const rows = Array.isArray(result.data) ? result.data : []
    result.data = includeAlerts ? rows : excludeSelfSentAlerts(rows)
    console.log(`Notifications chargées via /notifications/all:`, result.data)
    return result
  } catch (error: any) {
    console.warn(`Endpoint /notifications/all échoué:`, error.response?.status)
  }
  
  // Dernier fallback
  try {
    const result = await api.get<NotificationDTO[]>('/notifications', { withCredentials: true })
    const rows = Array.isArray(result.data) ? result.data : []
    result.data = includeAlerts ? rows : excludeSelfSentAlerts(rows)
    console.log(`Notifications chargées via /notifications:`, result.data)
    return result
  } catch (error: any) {
    console.warn(`Endpoint /notifications échoué:`, error.response?.status)
  }
  
  console.warn('Aucun endpoint de notifications disponible')
  return { data: [] }
}

// ----------------- Étudiant -----------------
export async function getNotificationsEtudiant(): Promise<{ data: NotificationDTO[] }> {
  try {
    // Essayer d'abord l'endpoint pour toutes les notifications
    const result = await api.get<NotificationDTO[]>('/notifications/etudiant/all', { withCredentials: true })
    console.log('Notifications étudiant (toutes):', result.data)
    return result
  } catch (error: any) {
    console.warn('Endpoint /etudiant/all non disponible, essai /etudiant:', error.response?.status)
  }
  
  try {
    const result = await api.get<NotificationDTO[]>('/notifications/etudiant', { withCredentials: true })
    console.log('Notifications étudiant:', result.data)
    return result
  } catch (error) {
    console.error('Erreur getNotificationsEtudiant:', error)
    return { data: [] }
  }
}

// ----------------- Enseignant -----------------
export async function getNotificationsEnseignant(): Promise<{ data: NotificationDTO[] }> {
  try {
    // Essayer d'abord l'endpoint pour toutes les notifications
    const result = await api.get<NotificationDTO[]>('/notifications/enseignant/all', { withCredentials: true })
    console.log('Notifications enseignant (toutes):', result.data)
    return result
  } catch (error: any) {
    console.warn('Endpoint /enseignant/all non disponible, essai /enseignant:', error.response?.status)
  }
  
  try {
    const result = await api.get<NotificationDTO[]>('/notifications/enseignant', { withCredentials: true })
    console.log('Notifications enseignant:', result.data)
    return result
  } catch (error) {
    console.error('Erreur getNotificationsEnseignant:', error)
    return { data: [] }
  }
}

// ----------------- Marquer comme lue -----------------
export async function markNotificationAsRead(id: number): Promise<void> {
  return api.put(`/notifications/read/${id}`, {}, { withCredentials: true })
}

// ----------------- Marquer toutes comme lues -----------------
export async function markAllNotificationsAsRead(): Promise<void> {
  return api.put('/notifications/read-all', {}, { withCredentials: true })
}

// ----------------- Supprimer une notification -----------------
export async function deleteNotification(id: number): Promise<void> {
  return api.delete(`/notifications/${id}`, { withCredentials: true })
}

// ----------------- Envoyer une alerte/notification à un utilisateur -----------------
export async function sendAlertToUser(userId: number, message: string): Promise<any> {
  const trimmedMessage = String(message || '').trim()
  if (!trimmedMessage) {
    throw new Error('Message d\'alerte vide')
  }

  // Priorité à l'endpoint backend Spring dédié
  try {
    return await api.post(`/notifications/send/${userId}`, { message: trimmedMessage }, { withCredentials: true })
  } catch (e1: any) {
    if (e1.response?.status === 404 || e1.response?.status === 405) {
      try {
        return await api.post(`/notifications/alert/${userId}`, { message: trimmedMessage }, { withCredentials: true })
      } catch (e2: any) {
        if (e2.response?.status === 404 || e2.response?.status === 405) {
          return await api.post('/notifications', { userId, message: trimmedMessage, type: 'ALERT' }, { withCredentials: true })
        }
        throw e2
      }
    }
    throw e1
  }

}

// ----------------- Alerte enseignant -----------------
export async function sendAlerteEnseignant(enseignantId: number, message: string): Promise<void> {
  const trimmedMessage = String(message || '').trim()
  if (!trimmedMessage) {
    throw new Error('Message d\'alerte vide')
  }

  // Endpoint confirmé côté backend Spring: POST /api/notifications/send/{userId}
  try {
    await api.post(`/notifications/send/${enseignantId}`, { message: trimmedMessage }, { withCredentials: true })
    return
  } catch (error: any) {
    const status = error?.response?.status
    if (![404, 405].includes(status)) {
      throw error
    }
  }

  const bodyCandidates = [
    {
      destinataireId: enseignantId,
      message: trimmedMessage,
      type: 'ALERT',
    },
    {
      userId: enseignantId,
      message: trimmedMessage,
      type: 'ALERT',
    },
    {
      enseignantId,
      message: trimmedMessage,
      type: 'ALERT',
    },
    {
      recipientId: enseignantId,
      message: trimmedMessage,
      type: 'ALERT',
    },
  ]

  const endpointCandidates = [
    `/notifications/send/${enseignantId}`,
    '/notifications/alert',
    '/notifications/alerts',
    '/notifications/manual-alert',
    '/notifications/send-alert',
    '/notifications/send-manual-alert',
    '/notifications/alerte',
    '/notifications/alerte/enseignant',
    '/notifications/enseignant/alert',
    '/notifications/enseignant/alerte',
    `/notifications/enseignant/${enseignantId}/alert`,
    `/notifications/enseignant/${enseignantId}/alerte`,
    `/notifications/enseignants/${enseignantId}/alert`,
    `/notifications/enseignants/${enseignantId}/alerte`,
    '/notifications/send',
    '/notifications',
  ]

  let lastError: any = null

  for (const endpoint of endpointCandidates) {
    for (const payload of bodyCandidates) {
      try {
        await api.post(endpoint, payload, { withCredentials: true })
        return
      } catch (error: any) {
        lastError = error
        const status = error?.response?.status
        if (![404, 405, 400, 415].includes(status)) {
          throw error
        }
      }
    }

    // Certains controllers Spring attendent des @RequestParam plutôt qu'un body JSON
    try {
      await api.post(endpoint, null, {
        withCredentials: true,
        params: {
          destinataireId: enseignantId,
          message: trimmedMessage,
        },
      })
      return
    } catch (error: any) {
      lastError = error
      const status = error?.response?.status
      if (![404, 405, 400, 415].includes(status)) {
        throw error
      }
    }
  }

  throw lastError || new Error('Aucun endpoint de notification manuelle disponible')
}
