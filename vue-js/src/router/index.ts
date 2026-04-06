import { createRouter, createWebHistory } from 'vue-router'
import { h } from 'vue'
import ReclamationsParent from '@/views/technicien/ReclamationsParent.vue'
import { useAuthStore } from '@/stores/auth'

// Layouts
import TechnicienLayout from '@/layouts/TechnicienLayout.vue'
import EnseignantLayout from '@/layouts/EnseignantLayout.vue'
import EtudiantLayout from '@/layouts/EtudiantLayout.vue'

// Views
import LoginView from '@/views/LoginView.vue'

// Technicien Views
import TechnicienDashboard from '@/views/technicien/DashboardView.vue'
import TechnicienDepartements from '@/views/technicien/DepartementsView.vue'
import TechnicienLaboratoires from '@/views/technicien/LaboratoiresView.vue'
import TechnicienEquipements from '@/views/technicien/EquipementsView.vue'
import TechnicienEtudiants from '@/views/technicien/EtudiantsView.vue'
import TechnicienEnseignants from '@/views/technicien/EnseignantsView.vue'
import TechnicienReservations from '@/views/technicien/ReservationsView.vue'
import TechnicienNotifications from '@/views/technicien/NotificationView.vue'
import TechnicienReclamations from '@/views/technicien/ReclamationsView.vue'

// 🔹 Reclamations
import ReclamationsEtudiants from '@/views/technicien/ReclamationsEtudiants.vue'
import ReclamationsEnseignants from '@/views/technicien/ReclamationsEnseignants.vue'

// Enseignant Views
import EnseignantDashboard from '@/views/enseignant/DashboardView.vue'
import EnseignantLaboratoires from '@/views/enseignant/LaboratoiresView.vue'
import EnseignantEquipements from '@/views/enseignant/EquipementsView.vue'
import EnseignantReclamations from '@/views/enseignant/ReclamationsView.vue'
import EnseignantNouvelleReclamation from '@/views/enseignant/NouvelleReclamationView.vue'
import EnseignantNotifications from '@/views/enseignant/NotificationView.vue'
import EnseignantReservations from '@/views/enseignant/ReservationsView.vue'

// Etudiant Views
import EtudiantDashboard from '@/views/etudiant/DashboardView.vue'
import EtudiantLaboratoires from '@/views/etudiant/LaboratoiresView.vue'
import EtudiantEquipements from '@/views/etudiant/EquipementsView.vue'
import EtudiantReservations from '@/views/etudiant/ReservationsView.vue'
import EtudiantNouvelleReservation from '@/views/etudiant/NouvelleReservationView.vue'
import EtudiantNouvelleReclamation from '@/views/etudiant/NouvelleReclamation.vue'
import EtudiantReclamations from '@/views/etudiant/Reclamations.vue'
import EtudiantNotifications from '@/views/etudiant/NotificationView.vue'

// Profil (partagé)
import ProfilView from '@/views/ProfilView.vue'

const routes = [
  { path: '/', name: 'login', component: LoginView },
  { path: '/reset-password', name: 'reset-password', component: LoginView },

  // ================= TECHNICIEN =================
  {
    path: '/technicien',
    component: TechnicienLayout,
    meta: { requiresAuth: true, role: 'technicien' },
    children: [
      { path: '', component: TechnicienDashboard },
      { path: 'dashboard', component: TechnicienDashboard },
      { path: 'departements', component: TechnicienDepartements },
      { path: 'laboratoires', component: TechnicienLaboratoires },
      { path: 'equipements', component: TechnicienEquipements },
      { path: 'etudiants', component: TechnicienEtudiants },
      { path: 'enseignants', component: TechnicienEnseignants },
      { path: 'reservations', component: TechnicienReservations },
      { path: 'notifications', component: TechnicienNotifications },

      // 🔹 Reclamations - route principale avec TechnicienReclamationsView
      { path: 'reclamations', component: TechnicienReclamations },
      { path: 'reclamations/etudiants', component: ReclamationsEtudiants },
      { path: 'reclamations/enseignants', component: ReclamationsEnseignants },
      { path: 'profil', component: ProfilView }
    ]
  },

  // ================= ENSEIGNANT =================
  {
    path: '/enseignant',
    component: EnseignantLayout,
    meta: { requiresAuth: true, role: 'enseignant' },
    children: [
      { path: '', component: EnseignantDashboard },
      { path: 'laboratoires', component: EnseignantLaboratoires },
      { path: 'equipements', component: EnseignantEquipements },
      { 
        path: 'reservations', 
        component: EnseignantReservations,
        beforeEnter: (_to: any, _from: any, next: any) => {
          const authStore = useAuthStore()
          if (authStore.isChefDepartement) {
            next()
          } else {
            next('/enseignant')
          }
        }
      },
      { path: 'reclamations', component: EnseignantReclamations },
      { path: 'nouvelle-reclamation', component: EnseignantNouvelleReclamation },
      { path: 'notifications', component: EnseignantNotifications },
      { path: 'profil', component: ProfilView }
    ]
  },

  // ================= ETUDIANT =================
  {
    path: '/etudiant',
    component: EtudiantLayout,
    meta: { requiresAuth: true, role: 'etudiant' },
    children: [
      { path: '', component: EtudiantDashboard },
      { path: 'laboratoires', component: EtudiantLaboratoires },
      { path: 'equipements', component: EtudiantEquipements },
      { path: 'reservations', component: EtudiantReservations },
      { path: 'nouvelle-reservation', component: EtudiantNouvelleReservation },
      { path: 'reclamations', component: EtudiantReclamations },
      { path: 'nouvelle-reclamation', component: EtudiantNouvelleReclamation },
      { path: 'notifications', component: EtudiantNotifications },
      { path: 'profil', component: ProfilView }
    ]
  },

  { path: '/:catchAll(.*)', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

function isBlockedEtudiant(user: any): boolean {
  if (!user) return false
  if (String(user.role || '').toLowerCase() !== 'etudiant') return false
  if (user.blocked === true || user.isBlocked === true || user.bloque === true) return true
  if (user.active === false || user.isActive === false || user.enabled === false) return true
  const etat = String(user.etat || user.statut || user.accountStatus || '').toUpperCase()
  return etat === 'BLOQUE' || etat === 'BLOCKED' || etat === 'INACTIF'
}

// 🔐 Auth Guard
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  authStore.initAuth()

  if (authStore.isAuthenticated && isBlockedEtudiant(authStore.currentUser)) {
    authStore.logout()
    authStore.error = 'Votre compte étudiant est bloqué. Contactez le technicien.'
    if (to.path !== '/') {
      next('/')
      return
    }
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/')
  } else if (to.meta.role && to.meta.role !== authStore.userRole) {
    next(`/${authStore.userRole}`)
  } else if (to.path === '/' && authStore.isAuthenticated) {
    next(`/${authStore.userRole}`)
  } else {
    next()
  }
})

export default router
