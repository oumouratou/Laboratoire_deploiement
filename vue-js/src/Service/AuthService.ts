import api from './api'

export default {
  register(user: any) {
    // Supporte JSON (application/json) et FormData (multipart/form-data)
    if (typeof FormData !== 'undefined' && user instanceof FormData) {
      return api.post('/auth/register', user, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    }
    return api.post('/auth/register', user)
  },

  login(credentials: { email: string; password: string }) {
    return api.post('/auth/login', credentials).then(response => {
      if (response.data.token) {
        localStorage.setItem('user', JSON.stringify(response.data))
        localStorage.setItem('token', response.data.token)
      }
      return response.data
    })
  },

  logout() {
    localStorage.removeItem('user')
    localStorage.removeItem('token')
  },

  getCurrentUser() {
    const user = localStorage.getItem('user')
    return user ? JSON.parse(user) : null
  },

  getToken() {
    return localStorage.getItem('token')
  },

  // Demander la réinitialisation du mot de passe
  forgotPassword(email: string) {
    return api.post('/auth/forgot-password', { email })
  },

  // Réinitialiser le mot de passe avec le token
  resetPassword(token: string, newPassword: string) {
    return api.post('/auth/reset-password', { token, newPassword })
  },

  // Vérifier si le token de réinitialisation est valide
  verifyResetToken(token: string) {
    return api.get('/auth/verify-reset-token', { params: { token } })
  }
}
