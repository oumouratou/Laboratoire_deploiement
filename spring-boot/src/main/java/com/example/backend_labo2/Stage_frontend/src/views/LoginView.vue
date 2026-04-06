<template>
  <div class="auth-wrapper">
    <!-- Côté gauche - Image/Branding -->
    <div class="auth-brand">
      <div class="brand-content">
        <div class="brand-logo">
          <img src="/Ressource/images/iset-djerba-logo.png" alt="ISET Djerba" class="iset-logo" />
        </div>
        <div class="brand-title">
          <h1>Lab<span class="highlight">Manager</span></h1>
          <div class="title-underline"></div>
        </div>
        <p class="brand-tagline">Plateforme de Gestion <strong>Intelligente</strong> des Laboratoires Universitaires</p>
        <div class="brand-features">
          <div class="feature">
            <div class="feature-icon">
              <i class="fas fa-calendar-check"></i>
            </div>
            <div class="feature-text">
              <span class="feature-title">Réservation Simplifiée</span>
              <span class="feature-desc">Planifiez vos séances en quelques clics</span>
            </div>
          </div>
          <div class="feature">
            <div class="feature-icon">
              <i class="fas fa-microchip"></i>
            </div>
            <div class="feature-text">
              <span class="feature-title">Gestion en Temps Réel</span>
              <span class="feature-desc">Suivez l'état des équipements instantanément</span>
            </div>
          </div>
          <div class="feature">
            <div class="feature-icon">
              <i class="fas fa-headset"></i>
            </div>
            <div class="feature-text">
              <span class="feature-title">Support Réactif</span>
              <span class="feature-desc">Réclamations traitées avec efficacité</span>
            </div>
          </div>
        </div>
      </div>
      <div class="brand-footer">
        <div class="footer-line"></div>
        <p><i class="fas fa-university mr-2"></i>ISET Djerba © 2026  LabManager v2.0</p>
      </div>
    </div>

    <!-- Côté droit - Formulaire -->
    <div class="auth-form-section">
      <div class="auth-form-container">
        
        <!-- ================= MODE RESET PASSWORD ================= -->
        <div v-if="isResetPasswordMode" class="reset-password-form">
          <div class="reset-header">
            <div class="reset-icon">
              <i class="fas fa-key"></i>
            </div>
            <h2>Nouveau mot de passe</h2>
            <p class="form-subtitle">Créez un nouveau mot de passe pour votre compte</p>
          </div>

          <!-- Message de succès -->
          <div v-if="resetPasswordSuccess" class="alert-message success">
            <i class="fas fa-check-circle"></i>
            <span>Mot de passe réinitialisé avec succès ! Redirection...</span>
          </div>

          <!-- Message d'erreur -->
          <div v-if="resetPasswordError" class="alert-message error">
            <i class="fas fa-exclamation-triangle"></i>
            <span>{{ resetPasswordError }}</span>
          </div>

          <form v-if="!resetPasswordSuccess" @submit.prevent="handleResetPassword" class="auth-form">
            <div class="register-table-container">
              <table class="register-table login-table">
                <tbody>
                  <tr>
                    <td class="field-label">
                      <label for="newPassword">
                        <i class="fas fa-lock"></i> Nouveau mot de passe
                      </label>
                    </td>
                    <td class="field-input">
                      <div class="table-input-wrapper password-field" :class="{ valid: resetPasswordForm.newPassword.length >= 6 }">
                        <input
                          :type="showNewPassword ? 'text' : 'password'"
                          id="newPassword"
                          v-model="resetPasswordForm.newPassword"
                          placeholder="Minimum 6 caractères"
                          required
                        >
                        <button type="button" class="toggle-password" @click="showNewPassword = !showNewPassword">
                          <i :class="showNewPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                        </button>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <td class="field-label">
                      <label for="confirmNewPassword">
                        <i class="fas fa-lock"></i> Confirmer le mot de passe
                      </label>
                    </td>
                    <td class="field-input">
                      <div class="table-input-wrapper password-field" :class="{ valid: resetPasswordForm.confirmPassword && resetPasswordForm.confirmPassword === resetPasswordForm.newPassword, error: resetPasswordForm.confirmPassword && resetPasswordForm.confirmPassword !== resetPasswordForm.newPassword }">
                        <input
                          :type="showConfirmNewPassword ? 'text' : 'password'"
                          id="confirmNewPassword"
                          v-model="resetPasswordForm.confirmPassword"
                          placeholder="Retapez le mot de passe"
                          required
                        >
                        <button type="button" class="toggle-password" @click="showConfirmNewPassword = !showConfirmNewPassword">
                          <i :class="showConfirmNewPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <button type="submit" class="submit-btn" :disabled="resetPasswordLoading">
              <span v-if="resetPasswordLoading" class="loader"></span>
              <span v-else>
                <i class="fas fa-save mr-2"></i>Réinitialiser le mot de passe
              </span>
            </button>
          </form>

          <div class="back-to-login">
            <a href="#" @click.prevent="backToLogin">
              <i class="fas fa-arrow-left"></i> Retour à la connexion
            </a>
          </div>
        </div>

        <!-- ================= MODE NORMAL (LOGIN/REGISTER) ================= -->
        <template v-else>
         
          <div class="auth-tabs">
            <button 
              :class="['auth-tab', { active: activeTab === 'login' }]"
              @click="switchTab('login')"
            >
              Connexion
            </button>
            <button 
              :class="['auth-tab', { active: activeTab === 'register' }]"
              @click="switchTab('register')"
            >
              Inscription
            </button>
          </div>

          <div v-if="error" class="alert-message error">
            <i class="fas fa-exclamation-triangle"></i>
            <span>{{ error }}</span>
            <button @click="clearError" class="close-btn">&times;</button>
          </div>

         
          <div v-if="successMessage" class="alert-message success">
            <i class="fas fa-check-circle"></i>
            <span>{{ successMessage }}</span>
            <button @click="successMessage = ''" class="close-btn">&times;</button>
          </div>

          <!-- Formulaire de connexion -->
          <form v-if="activeTab === 'login'" @submit.prevent="handleLogin" class="auth-form">
            <h2>Bienvenue</h2>
            <p class="form-subtitle">Connectez-vous à votre compte</p>

            <div class="register-table-container">
              <table class="register-table login-table">
                <thead>
                  <tr>
                    <th colspan="2" class="table-section-header">
                      <i class="fas fa-sign-in-alt"></i> Connexion
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td class="field-label">
                      <label for="loginEmail">
                        <i class="fas fa-envelope"></i> Adresse mail <span class="required">*</span>
                      </label>
                    </td>
                    <td class="field-input">
                      <div class="table-input-wrapper" :class="{ valid: loginForm.email }">
                        <input 
                          type="email" 
                          id="loginEmail" 
                          v-model="loginForm.email"
                          placeholder="exemple@universite.com"
                          required
                        >
                        <i v-if="loginForm.email" class="fas fa-check-circle valid-icon"></i>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <td class="field-label">
                      <label for="loginPassword">
                        <i class="fas fa-lock"></i> Mot de passe <span class="required">*</span>
                      </label>
                    </td>
                    <td class="field-input">
                      <div class="table-input-wrapper" :class="{ valid: loginForm.password }">
                        <input 
                          :type="showPassword ? 'text' : 'password'" 
                          id="loginPassword" 
                          v-model="loginForm.password"
                          placeholder="⬢⬢⬢⬢⬢⬢⬢⬢"
                          required
                        >
                        <button type="button" class="toggle-password" @click="showPassword = !showPassword">
                          <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="form-options">
              <label class="checkbox-wrapper">
                <input type="checkbox" v-model="rememberMe">
                <span class="checkmark"></span>
                Se souvenir de moi
              </label>
              <a href="#" class="forgot-link" @click.prevent="showForgotPasswordModal = true">Mot de passe oublié ?</a>
            </div>

            <button type="submit" class="submit-btn" :disabled="isLoading">
              <span v-if="isLoading" class="loader"></span>
              <span v-else>Se connecter</span>
            </button>
          </form>

        <!-- Modal Mot de passe oublié -->
        <div v-if="showForgotPasswordModal" class="modal-overlay" @click.self="closeForgotPasswordModal">
          <div class="forgot-modal">
            <div class="modal-header-forgot">
              <h3><i class="fas fa-key"></i> Réinitialiser le mot de passe</h3>
              <button class="close-modal" @click="closeForgotPasswordModal">&times;</button>
            </div>
            
            <div v-if="!forgotPasswordSent" class="modal-body-forgot">
              <p class="modal-description">
                Entrez votre adresse email. Nous vous enverrons un lien pour réinitialiser votre mot de passe.
              </p>
              
              <div class="input-group">
                <label for="forgotEmail">Email</label>
                <div class="input-wrapper">
                  <i class="fas fa-envelope"></i>
                  <input 
                    type="email" 
                    id="forgotEmail" 
                    v-model="forgotPasswordEmail"
                    placeholder="exemple@universite.com"
                    required
                  >
                </div>
              </div>
              
              <div v-if="forgotPasswordError" class="alert-message error" style="margin-top: 15px;">
                <i class="fas fa-exclamation-triangle"></i>
                <span>{{ forgotPasswordError }}</span>
              </div>
              
              <div class="modal-actions">
                <button type="button" class="btn-secondary" @click="closeForgotPasswordModal">
                  Annuler
                </button>
                <button 
                  type="button" 
                  class="btn-primary" 
                  @click="handleForgotPassword"
                  :disabled="forgotPasswordLoading || !forgotPasswordEmail"
                >
                  <span v-if="forgotPasswordLoading" class="loader-small"></span>
                  <span v-else>Envoyer le lien</span>
                </button>
              </div>
            </div>
            
            <div v-else class="modal-body-forgot success-state">
              <div class="success-icon">
                <i class="fas fa-check-circle"></i>
              </div>
              <h4>Lien de réinitialisation généré !</h4>
              
              <!-- Afficher le lien directement -->
              <div v-if="resetLink" class="reset-link-container">
                <p class="reset-link-label">Cliquez sur le lien ci-dessous pour réinitialiser votre mot de passe :</p>
                <div class="reset-link-box">
                  <a :href="resetLink" class="reset-link" target="_blank">
                    <i class="fas fa-external-link-alt mr-2"></i>
                    Réinitialiser mon mot de passe
                  </a>
                </div>
                <p class="reset-link-note">
                  <i class="fas fa-clock"></i>
                  Ce lien expire dans 1 heure
                </p>
              </div>
              
              <button type="button" class="btn-primary" @click="closeForgotPasswordModal">
                Fermer
              </button>
            </div>
          </div>
        </div>

        <!-- Formulaire d'inscription -->
        <form v-else-if="activeTab === 'register'" @submit.prevent="handleRegister" class="auth-form register-form">
          <div class="register-header">
            <h2><i class="fas fa-user-plus"></i> Créer un compte</h2>
            <p class="form-subtitle">Remplissez le formulaire pour rejoindre LabManager</p>
          </div>

          <!-- Tableau d'inscription professionnel -->
          <div class="register-table-container">
            <table class="register-table">
              <thead>
                <tr>
                  <th colspan="2" class="table-section-header">
                    <i class="fas fa-user-circle"></i> Informations personnelles
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td class="field-label">
                    <label for="registerNom">
                      <i class="fas fa-user"></i> Nom <span class="required">*</span>
                    </label>
                  </td>
                  <td class="field-input">
                    <div class="table-input-wrapper" :class="{ valid: registerForm.nom, error: !registerForm.nom && formTouched }">
                      <input 
                        type="text" 
                        id="registerNom" 
                        v-model="registerForm.nom"
                        placeholder="Votre nom de famille"
                        required
                        @blur="formTouched = true"
                      >
                      <i v-if="registerForm.nom" class="fas fa-check-circle valid-icon"></i>
                    </div>
                  </td>
                </tr>
                <tr>
                  <td class="field-label">
                    <label for="registerPrenom">
                      <i class="fas fa-user"></i> Prénom <span class="required">*</span>
                    </label>
                  </td>
                  <td class="field-input">
                    <div class="table-input-wrapper" :class="{ valid: registerForm.prenom }">
                      <input 
                        type="text" 
                        id="registerPrenom" 
                        v-model="registerForm.prenom"
                        placeholder="Votre prénom"
                        required
                      >
                      <i v-if="registerForm.prenom" class="fas fa-check-circle valid-icon"></i>
                    </div>
                  </td>
                </tr>
              </tbody>

              <thead>
                <tr>
                  <th colspan="2" class="table-section-header">
                    <i class="fas fa-address-card"></i> Coordonnées
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td class="field-label">
                    <label for="registerEmail">
                      <i class="fas fa-envelope"></i> Email <span class="required">*</span>
                    </label>
                  </td>
                  <td class="field-input">
                    <div class="table-input-wrapper" :class="{ valid: isValidEmail(registerForm.email), error: registerForm.email && !isValidEmail(registerForm.email) }">
                      <input 
                        type="email" 
                        id="registerEmail" 
                        v-model="registerForm.email"
                        placeholder="exemple@universite.com"
                        required
                      >
                      <i v-if="isValidEmail(registerForm.email)" class="fas fa-check-circle valid-icon"></i>
                    </div>
                    <small v-if="registerForm.email && !isValidEmail(registerForm.email)" class="error-text">
                      <i class="fas fa-exclamation-circle"></i> Veuillez entrer un email valide
                    </small>
                  </td>
                </tr>
                <tr>
                  <td class="field-label">
                    <label for="registerCin">
                      <i class="fas fa-id-card"></i> CIN <span class="required">*</span>
                    </label>
                  </td>
                  <td class="field-input">
                    <div class="table-input-wrapper" :class="{ valid: registerForm.cin && registerForm.cin.length >= 6 }">
                      <input 
                        type="text" 
                        id="registerCin" 
                        v-model="registerForm.cin"
                        placeholder="AB123456"
                        required
                        maxlength="10"
                      >
                      <i v-if="registerForm.cin && registerForm.cin.length >= 6" class="fas fa-check-circle valid-icon"></i>
                    </div>
                  </td>
                </tr>
              </tbody>

              <thead>
                <tr>
                  <th colspan="2" class="table-section-header">
                    <i class="fas fa-shield-alt"></i> Sécurité
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td class="field-label">
                    <label for="registerPassword">
                      <i class="fas fa-lock"></i> Mot de passe <span class="required">*</span>
                    </label>
                  </td>
                  <td class="field-input">
                    <div class="table-input-wrapper password-field" :class="{ valid: isPasswordStrong(registerForm.password) }">
                      <input 
                        :type="showRegisterPassword ? 'text' : 'password'" 
                        id="registerPassword" 
                        v-model="registerForm.password"
                        placeholder="Min. 6 caractères"
                        required
                        minlength="6"
                      >
                      <button type="button" class="toggle-password" @click="showRegisterPassword = !showRegisterPassword">
                        <i :class="showRegisterPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                      </button>
                    </div>
                    <div class="password-strength" v-if="registerForm.password">
                      <div class="strength-bars">
                        <div class="bar" :class="getPasswordStrengthClass(1)"></div>
                        <div class="bar" :class="getPasswordStrengthClass(2)"></div>
                        <div class="bar" :class="getPasswordStrengthClass(3)"></div>
                        <div class="bar" :class="getPasswordStrengthClass(4)"></div>
                      </div>
                      <span class="strength-text" :class="'strength-' + getPasswordStrength()">{{ getPasswordStrengthText() }}</span>
                    </div>
                  </td>
                </tr>
                <tr>
                  <td class="field-label">
                    <label for="confirmPassword">
                      <i class="fas fa-lock"></i> Confirmer <span class="required">*</span>
                    </label>
                  </td>
                  <td class="field-input">
                    <div class="table-input-wrapper password-field" :class="{ valid: confirmPassword && confirmPassword === registerForm.password, error: confirmPassword && confirmPassword !== registerForm.password }">
                      <input 
                        :type="showConfirmPassword ? 'text' : 'password'" 
                        id="confirmPassword" 
                        v-model="confirmPassword"
                        placeholder="Répétez le mot de passe"
                        required
                      >
                      <button type="button" class="toggle-password" @click="showConfirmPassword = !showConfirmPassword">
                        <i :class="showConfirmPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                      </button>
                    </div>
                    <small v-if="confirmPassword && confirmPassword !== registerForm.password" class="error-text">
                      <i class="fas fa-exclamation-circle"></i> Les mots de passe ne correspondent pas
                    </small>
                    <small v-if="confirmPassword && confirmPassword === registerForm.password" class="success-text">
                      <i class="fas fa-check-circle"></i> Les mots de passe correspondent
                    </small>
                  </td>
                </tr>
              </tbody>

              <thead>
                <tr>
                  <th colspan="2" class="table-section-header">
                    <i class="fas fa-briefcase"></i> Profil professionnel
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td class="field-label">
                    <label for="registerRole">
                      <i class="fas fa-user-tag"></i> Rôle <span class="required">*</span>
                    </label>
                  </td>
                  <td class="field-input">
                    <div class="table-input-wrapper select-field" :class="{ valid: registerForm.role }">
                      <select id="registerRole" v-model="registerForm.role" required @change="onRoleChange">
                        <option value="" disabled>Sélectionnez votre rôle</option>
                        <option value="ETUDIANT">x} Étudiant</option>
                        <option value="ENSEIGNANT">x⬍x Enseignant</option>
                        <option value="TECHNICIEN">x Technicien</option>
                      </select>
                      <i v-if="registerForm.role" class="fas fa-check-circle valid-icon"></i>
                    </div>
                  </td>
                </tr>

                <!-- Département (juste après le rôle, désactivé pour les techniciens) -->
                <tr>
                  <td class="field-label" :class="{ 'disabled-label': registerForm.role === 'TECHNICIEN' }">
                    <label for="registerDepartement">
                      <i class="fas fa-building"></i> Département
                      <span v-if="registerForm.role !== 'TECHNICIEN'" class="required">*</span>
                    </label>
                  </td>
                  <td class="field-input">
                    <div
                      class="table-input-wrapper select-field"
                      :class="{
                        valid: registerForm.departementId && registerForm.role !== 'TECHNICIEN',
                        disabled: registerForm.role === 'TECHNICIEN'
                      }"
                    >
                      <select
                        id="registerDepartement"
                        v-model="registerForm.departementId"
                        :required="registerForm.role !== 'TECHNICIEN'"
                        :disabled="registerForm.role === 'TECHNICIEN' || loadingDepartements || departementsError"
                        @change="onDepartementChange"
                      >
                        <option value="" disabled>
                          {{ registerForm.role === 'TECHNICIEN' ? 'Non applicable pour les techniciens' : (loadingDepartements ? 'Chargement...' : (departementsError ? 'Erreur de chargement' : (departements.length === 0 ? 'Aucun département' : 'Sélectionnez votre département'))) }}
                        </option>
                        <option
                          v-for="dept in departements"
                          :key="dept.id"
                          :value="dept.id"
                        >
                          {{ dept.nom }}
                        </option>
                      </select>
                      <button
                        v-if="(departementsError || (departements.length === 0 && !loadingDepartements)) && registerForm.role !== 'TECHNICIEN'"
                        type="button"
                        class="reload-btn"
                        @click.prevent="loadDepartements(0)"
                        title="Recharger les départements"
                      >
                        <i class="fas fa-sync-alt"></i>
                      </button>
                      <i v-if="registerForm.departementId && registerForm.role !== 'TECHNICIEN'" class="fas fa-check-circle valid-icon"></i>
                    </div>
                    <small v-if="registerForm.role === 'TECHNICIEN'" class="info-text">
                      <i class="fas fa-info-circle"></i> Les techniciens n'appartiennent pas à un département
                    </small>
                  </td>
                </tr>

                <!-- Champs spécifiques Étudiant -->
                <tr v-if="registerForm.role === 'ETUDIANT'">
                  <td class="field-label">
                    <label for="registerNiveau">
                      <i class="fas fa-layer-group"></i> Niveau <span class="required">*</span>
                    </label>
                  </td>
                  <td class="field-input">
                    <div class="table-input-wrapper select-field" :class="{ valid: registerForm.niveau }">
                      <select
                        id="registerNiveau"
                        v-model="registerForm.niveau"
                        :disabled="!registerForm.departementId"
                        @change="onNiveauChange"
                        required
                      >
                        <option value="" disabled>
                          {{ registerForm.departementId ? 'Sélectionnez un niveau' : 'Sélectionnez d\'abord un département' }}
                        </option>
                        <option v-for="opt in niveauOptions" :key="opt.value" :value="opt.value">
                          {{ opt.label }}
                        </option>
                      </select>
                      <i v-if="registerForm.niveau" class="fas fa-check-circle valid-icon"></i>
                    </div>
                  </td>
                </tr>
                <tr v-if="registerForm.role === 'ETUDIANT'">
                  <td class="field-label">
                    <label for="registerClasse">
                      <i class="fas fa-users"></i> Classe <span class="required">*</span>
                    </label>
                  </td>
                  <td class="field-input">
                    <div class="table-input-wrapper select-field" :class="{ valid: registerForm.classe }">
                      <select
                        id="registerClasse"
                        v-model="registerForm.classe"
                        :disabled="!registerForm.niveau"
                        required
                      >
                        <option value="" disabled>
                          {{ registerForm.niveau ? 'Sélectionnez une classe' : 'Sélectionnez d\'abord un niveau' }}
                        </option>
                        <option v-for="opt in classeOptions" :key="opt.value" :value="opt.value">
                          {{ opt.label }}
                        </option>
                      </select>
                      <i v-if="registerForm.classe" class="fas fa-check-circle valid-icon"></i>
                    </div>
                  </td>
                </tr>

                <!-- Option Chef de département (visible seulement pour les enseignants) -->
                <tr v-if="registerForm.role === 'ENSEIGNANT'">
                  <td class="field-label">
                    <label for="isChefDept">
                      <i class="fas fa-crown"></i> Chef de département
                    </label>
                  </td>
                  <td class="field-input">
                    <div class="table-input-wrapper chef-dept-option">
                      <label class="chef-checkbox">
                        <input 
                          type="checkbox" 
                          id="isChefDept" 
                          v-model="registerForm.isChefDepartement"
                        >
                        <span class="checkmark"></span>
                        <span class="chef-label">Je suis chef de département</span>
                      </label>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Progression -->
          <div class="register-progress">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: progressPercentage + '%' }"></div>
            </div>
            <span class="progress-text">{{ progressPercentage }}% complété</span>
          </div>

          <label class="checkbox-wrapper terms">
            <input type="checkbox" v-model="acceptTerms" required>
            <span class="checkmark"></span>
            J'accepte les <a href="#">conditions d'utilisation</a> et la <a href="#">politique de confidentialité</a>
          </label>

          <button type="submit" class="submit-btn" :disabled="isLoading || !acceptTerms || !isFormValid">
            <span v-if="isLoading" class="loader"></span>
            <span v-else>
              <i class="fas fa-user-plus mr-2"></i>Créer mon compte
            </span>
          </button>
        </form>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { storeToRefs } from 'pinia'
import { getDepartements } from '@/Service/departementService'
import AuthService from '@/Service/AuthService'
import type { Departement } from '@/types'

const router = useRouter()
const authStore = useAuthStore()
// storeToRefs pour garder la réactivité sur les propriétés d'état
const { isLoading, error } = storeToRefs(authStore)
const { login, register, clearError } = authStore

const activeTab = ref<'login' | 'register'>('login')
const showPassword = ref(false)
const showRegisterPassword = ref(false)
const showConfirmPassword = ref(false)
const rememberMe = ref(false)
const acceptTerms = ref(false)
const successMessage = ref('')
const formTouched = ref(false)

// Variables pour "Mot de passe oublié"
const showForgotPasswordModal = ref(false)
const forgotPasswordEmail = ref('')
const forgotPasswordLoading = ref(false)
const forgotPasswordSent = ref(false)
const forgotPasswordError = ref('')
const resetLink = ref('')

// Variables pour "Réinitialisation du mot de passe"
const isResetPasswordMode = ref(false)
const resetToken = ref('')
const resetPasswordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})
const resetPasswordLoading = ref(false)
const resetPasswordError = ref('')
const resetPasswordSuccess = ref(false)
const showNewPassword = ref(false)
const showConfirmNewPassword = ref(false)

const departements = ref<Departement[]>([])
const loadingDepartements = ref(false)
const departementsError = ref(false)

const loginForm = reactive({
  email: '',
  password: ''
})

const registerForm = reactive({
  nom: '',
  prenom: '',
  email: '',
  password: '',
  cin: '',
  role: '' as 'ETUDIANT' | 'ENSEIGNANT' | 'TECHNICIEN' | '',
  departementId: '' as number | '',
  isChefDepartement: false,
  // Champs spécifiques aux étudiants
  niveau: '',
  classe: ''
})

type OptionItem = { value: string; label: string }

const LICENCE_LEVELS: OptionItem[] = [
  { value: 'LICENCE1', label: 'Licence 1' },
  { value: 'LICENCE2', label: 'Licence 2' },
  { value: 'LICENCE3', label: 'Licence 3' },
]

const DEPT_CLASS_RULES: Record<string, Record<string, string[]>> = {
  TI: {
    LICENCE1: ['TI1', 'TI2', 'TI3', 'TI4', 'TI5'],
    LICENCE2: ['DSI', 'MDW'],
    LICENCE3: ['DSI', 'MDW'],
  },
  GE: {
    LICENCE1: ['GE1', 'GE2', 'GE3', 'GE4', 'GE5'],
    LICENCE2: ['EI', 'AII'],
    LICENCE3: ['EI', 'AII'],
  },
  GM: {
    LICENCE1: ['GM1', 'GM2', 'GM3', 'GM4', 'GM5'],
    LICENCE2: ['MT'],
    LICENCE3: ['MT'],
  },
  SEG: {
    LICENCE1: ['MA1', 'MA2', 'MA3', 'MA4', 'MA5', 'CD1', 'CD2', 'CD3', 'CD4', 'CD5'],
    LICENCE2: ['MA', 'DM'],
    LICENCE3: ['MA', 'DM'],
  },
}

function normalizeText(value: string): string {
  return String(value || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
}

function getDeptKey(departementNom?: string): 'TI' | 'GE' | 'GM' | 'SEG' | null {
  const n = normalizeText(departementNom || '')
  if ((n.includes('technologie') && n.includes('informatique')) || n.includes('informatique')) return 'TI'
  if (n.includes('genie') && n.includes('electrique')) return 'GE'
  if (n.includes('genie') && n.includes('mecanique')) return 'GM'
  if ((n.includes('sciences') && n.includes('economiques') && n.includes('gestion')) || (n.includes('economie') && n.includes('gestion'))) return 'SEG'
  return null
}

const selectedDepartement = computed(() => {
  const id = Number(registerForm.departementId)
  if (!Number.isFinite(id) || id <= 0) return null
  return departements.value.find(d => Number(d.id) === id) || null
})

const niveauOptions = computed<OptionItem[]>(() => {
  if (registerForm.role !== 'ETUDIANT') return []
  const deptKey = getDeptKey(selectedDepartement.value?.nom)
  return deptKey ? LICENCE_LEVELS : []
})

const classeOptions = computed<OptionItem[]>(() => {
  if (registerForm.role !== 'ETUDIANT') return []
  const deptKey = getDeptKey(selectedDepartement.value?.nom)
  if (!deptKey) return []
  const classes = DEPT_CLASS_RULES[deptKey]?.[registerForm.niveau] || []
  return classes.map(c => ({ value: c, label: c }))
})

const confirmPassword = ref('')

// Validation email
function isValidEmail(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

// Force du mot de passe
function getPasswordStrength(): number {
  const password = registerForm.password
  if (!password) return 0
  let strength = 0
  if (password.length >= 6) strength++
  if (password.length >= 8) strength++
  if (/[A-Z]/.test(password) && /[a-z]/.test(password)) strength++
  if (/\d/.test(password) || /[^A-Za-z0-9]/.test(password)) strength++
  return strength
}

function isPasswordStrong(password: string): boolean {
  return password.length >= 6
}

function getPasswordStrengthClass(level: number): string {
  const strength = getPasswordStrength()
  if (strength >= level) {
    if (strength === 1) return 'weak'
    if (strength === 2) return 'fair'
    if (strength === 3) return 'good'
    if (strength === 4) return 'strong'
  }
  return ''
}

function getPasswordStrengthText(): string {
  const strength = getPasswordStrength()
  if (strength === 1) return 'Faible'
  if (strength === 2) return 'Moyen'
  if (strength === 3) return 'Bon'
  if (strength === 4) return 'Excellent'
  return ''
}

// Fonction appelée quand le rôle change
function onRoleChange() {
  // Réinitialiser les champs spécifiques au rôle
  if (registerForm.role === 'TECHNICIEN') {
    // Les techniciens n'ont pas de département
    registerForm.departementId = ''
    registerForm.isChefDepartement = false
    registerForm.niveau = ''
    registerForm.classe = ''
  } else if (registerForm.role !== 'ENSEIGNANT') {
    // Seuls les enseignants peuvent être chefs de département
    registerForm.isChefDepartement = false
  }

  if (registerForm.role !== 'ETUDIANT') {
    registerForm.niveau = ''
    registerForm.classe = ''
  }
}

function onDepartementChange() {
  registerForm.niveau = ''
  registerForm.classe = ''
}

function onNiveauChange() {
  registerForm.classe = ''
}

// Validation globale du formulaire
const isFormValid = computed(() => {
  const baseValid = registerForm.nom &&
         registerForm.prenom &&
         isValidEmail(registerForm.email) &&
         registerForm.cin &&
         registerForm.password.length >= 6 &&
         confirmPassword.value === registerForm.password &&
         registerForm.role
  
  // Les techniciens n'ont pas besoin de département
  if (registerForm.role === 'TECHNICIEN') {
    return baseValid
  }

  // Étudiants: exiger niveau + classe
  if (registerForm.role === 'ETUDIANT') {
    return baseValid && registerForm.departementId && registerForm.niveau && registerForm.classe
  }
  
  return baseValid && registerForm.departementId
})

// Pourcentage de progression du formulaire
const progressPercentage = computed(() => {
  let completed = 0
  // Total = 7 pour technicien (pas de département), 8 pour enseignant, 10 pour étudiant (niveau+classe)
  const total = registerForm.role === 'TECHNICIEN'
    ? 7
    : registerForm.role === 'ETUDIANT'
      ? 10
      : 8

  if (registerForm.nom) completed++
  if (registerForm.prenom) completed++
  if (isValidEmail(registerForm.email)) completed++
  if (registerForm.cin && registerForm.cin.length >= 6) completed++
  if (registerForm.password.length >= 6) completed++
  if (confirmPassword.value && confirmPassword.value === registerForm.password) completed++
  if (registerForm.role) completed++
  // Département seulement si pas technicien
  if (registerForm.role !== 'TECHNICIEN' && registerForm.departementId) completed++

  // Étudiant seulement
  if (registerForm.role === 'ETUDIANT' && registerForm.niveau) completed++
  if (registerForm.role === 'ETUDIANT' && registerForm.classe) completed++

  return Math.round((completed / total) * 100)
})

async function loadDepartements(retryCount = 0) {
  loadingDepartements.value = true
  departementsError.value = false
  
  try {
    const response = await getDepartements()
    console.log('Départements chargés:', response.data)
    
    if (Array.isArray(response.data)) {
      departements.value = response.data.filter((d: any) => d.actif !== false)
    } else if (response.data && Array.isArray(response.data.content)) {
      departements.value = response.data.content.filter((d: any) => d.actif !== false)
    } else if (response.data && typeof response.data === 'object') {
      // Essayer de récupérer les départements d'un format différent
      const keys = Object.keys(response.data)
      const firstKey = keys[0]
      if (firstKey && Array.isArray((response.data as any)[firstKey])) {
        departements.value = (response.data as any)[firstKey]
      } else {
        departements.value = []
      }
    } else {
      departements.value = []
    }
    
    // Si aucun département n'a été chargé et qu'on peut réessayer
    if (departements.value.length === 0 && retryCount < 2) {
      console.log('Aucun département trouvé, nouvelle tentative...')
      setTimeout(() => loadDepartements(retryCount + 1), 1000)
    }
  } catch (err: any) {
    console.error('Erreur lors du chargement des départements:', err)
    departementsError.value = true
    departements.value = []
    
    // Réessayer automatiquement si c'est une erreur réseau (max 2 fois)
    if (retryCount < 2 && (err.code === 'ERR_NETWORK' || err.code === 'ECONNABORTED' || !err.response)) {
      console.log(`Tentative de rechargement des départements (${retryCount + 1}/2)...`)
      setTimeout(() => loadDepartements(retryCount + 1), 1500)
    }
  } finally {
    loadingDepartements.value = false
  }
}

function switchTab(tab: 'login' | 'register') {
  activeTab.value = tab
  clearError()
  successMessage.value = ''
  
  // Recharger les départements si on passe à l'onglet inscription et qu'ils ne sont pas chargés
  if (tab === 'register' && (departements.value.length === 0 || departementsError.value)) {
    loadDepartements(0)
  }
}

async function handleLogin() {
  clearError()
  successMessage.value = ''
  try {
    const success = await login(loginForm)
    if (success) {
      successMessage.value = 'Connexion réussie ! Redirection...'
      const userRole = authStore.userRole
      if (userRole) {
        setTimeout(() => router.push(`/${userRole}`), 500)
      }
    }
    // Si !success, le store a déjà rempli error (maintenant réactif grâce à storeToRefs)
  } catch (err: any) {
    console.error('Erreur handleLogin:', err)
    authStore.error = err.response?.data?.message || 'Erreur de connexion au serveur'
  }
}

async function handleRegister() {
  if (registerForm.password !== confirmPassword.value) {
    alert('Les mots de passe ne correspondent pas')
    return
  }
  // Vérifier le département seulement si ce n'est pas un technicien
  if (registerForm.role !== 'TECHNICIEN' && !registerForm.departementId) {
    alert('Veuillez sélectionner un département')
    return
  }
  if (!registerForm.role) {
    alert('Veuillez sélectionner un rôle')
    return
  }

  if (registerForm.role === 'ETUDIANT' && (!registerForm.niveau || !registerForm.classe)) {
    alert('Veuillez renseigner le niveau et la classe')
    return
  }

  const baseFields = {
    nom: registerForm.nom.trim(),
    prenom: registerForm.prenom.trim(),
    email: registerForm.email.trim().toLowerCase(),
    password: registerForm.password,
    cin: registerForm.cin.trim(),
    role: registerForm.role
  }

  // Pour ETUDIANT: envoyer en multipart/form-data + fichier `attestation`
  let payload: any = baseFields
  if (registerForm.role === 'ETUDIANT') {
    if (niveauOptions.value.length === 0) {
      alert('Ce département étudiant n\'est pas pris en charge. Choisissez Technologie de l\'informatique, Génie électrique, Génie mécanique ou Sciences économiques et gestion.')
      return
    }
    const niveauValide = niveauOptions.value.some(opt => opt.value === registerForm.niveau)
    const classeValide = classeOptions.value.some(opt => opt.value === registerForm.classe)
    if (!niveauValide || !classeValide) {
      alert('Veuillez choisir un niveau et une classe valides pour le département sélectionné')
      return
    }

    const fd = new FormData()
    fd.append('nom', baseFields.nom)
    fd.append('prenom', baseFields.prenom)
    fd.append('email', baseFields.email)
    fd.append('password', baseFields.password)
    fd.append('cin', baseFields.cin)
    fd.append('role', baseFields.role)
    fd.append('departementId', String(registerForm.departementId))
    fd.append('niveau', registerForm.niveau)
    fd.append('classe', registerForm.classe)
    payload = fd
  } else {
    // Ajouter le département seulement si ce n'est pas un technicien
    if (registerForm.role !== 'TECHNICIEN' && registerForm.departementId) {
      payload.departementId = Number(registerForm.departementId)
    } else {
      // Pour les techniciens, envoyer null explicitement (pas de département)
      payload.departementId = null
    }

    // Ajouter le flag chef de département pour les enseignants
    if (registerForm.role === 'ENSEIGNANT' && registerForm.isChefDepartement) {
      payload.isChefDepartement = true
    }
  }

  const success = await register(payload)
  
  if (success) {
    successMessage.value = 'Compte créé avec succès ! Redirection...'
    const userRole = authStore.userRole
    if (userRole) {
      setTimeout(() => router.push(`/${userRole}`), 1500)
    }
  }
}

// ================= MOT DE PASSE OUBLIÉ =================
function closeForgotPasswordModal() {
  showForgotPasswordModal.value = false
  forgotPasswordEmail.value = ''
  forgotPasswordSent.value = false
  forgotPasswordError.value = ''
  resetLink.value = ''
}

async function handleForgotPassword() {
  if (!forgotPasswordEmail.value) {
    forgotPasswordError.value = 'Veuillez entrer votre adresse email'
    return
  }
  
  if (!isValidEmail(forgotPasswordEmail.value)) {
    forgotPasswordError.value = 'Veuillez entrer une adresse email valide'
    return
  }
  
  forgotPasswordLoading.value = true
  forgotPasswordError.value = ''
  
  try {
    const response = await AuthService.forgotPassword(forgotPasswordEmail.value)
    // Le backend retourne le lien de reset directement
    if (response.data && response.data.resetLink) {
      resetLink.value = response.data.resetLink
    } else if (response.data && response.data.token) {
      // Si le backend retourne juste le token, construire le lien
      resetLink.value = `${window.location.origin}/reset-password?token=${response.data.token}`
    } else {
      // Générer un lien par défaut si pas de réponse
      resetLink.value = `${window.location.origin}/reset-password?token=temp-${Date.now()}`
    }
    forgotPasswordSent.value = true
  } catch (err: any) {
    console.error('Erreur mot de passe oublié:', err)
    // Si erreur, on affiche quand même le formulaire mais avec une erreur
    if (err.response?.status === 404) {
      forgotPasswordError.value = 'Aucun compte trouvé avec cette adresse email'
    } else {
      forgotPasswordError.value = 'Erreur lors de la demande. Veuillez réessayer.'
    }
  } finally {
    forgotPasswordLoading.value = false
  }
}

onMounted(() => {
  loadDepartements()
  
  // Vérifier si on est en mode reset-password (token dans l'URL)
  const urlParams = new URLSearchParams(window.location.search)
  const token = urlParams.get('token')
  
  if (token && router.currentRoute.value.path === '/reset-password') {
    resetToken.value = token
    isResetPasswordMode.value = true
    verifyToken(token)
  }
})

// Vérifier si le token est valide
async function verifyToken(token: string) {
  try {
    await AuthService.verifyResetToken(token)
  } catch (err: any) {
    console.error('Token invalide:', err)
    resetPasswordError.value = 'Le lien de réinitialisation est invalide ou a expiré.'
  }
}

// Réinitialiser le mot de passe
async function handleResetPassword() {
  resetPasswordError.value = ''
  
  if (!resetPasswordForm.newPassword || !resetPasswordForm.confirmPassword) {
    resetPasswordError.value = 'Veuillez remplir tous les champs'
    return
  }
  
  if (resetPasswordForm.newPassword.length < 6) {
    resetPasswordError.value = 'Le mot de passe doit contenir au moins 6 caractères'
    return
  }
  
  if (resetPasswordForm.newPassword !== resetPasswordForm.confirmPassword) {
    resetPasswordError.value = 'Les mots de passe ne correspondent pas'
    return
  }
  
  resetPasswordLoading.value = true
  
  try {
    await AuthService.resetPassword(resetToken.value, resetPasswordForm.newPassword)
    resetPasswordSuccess.value = true
    
    // Rediriger vers la page de connexion après 3 secondes
    setTimeout(() => {
      isResetPasswordMode.value = false
      router.push('/')
    }, 3000)
  } catch (err: any) {
    console.error('Erreur réinitialisation:', err)
    resetPasswordError.value = err.response?.data?.message || 'Erreur lors de la réinitialisation du mot de passe'
  } finally {
    resetPasswordLoading.value = false
  }
}

// Retour à la page de connexion
function backToLogin() {
  isResetPasswordMode.value = false
  router.push('/')
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.auth-wrapper {
  display: flex;
  min-height: 100vh;
  background: #f8fafc;
  font-family: 'Poppins', sans-serif;
}

.auth-brand {
  flex: 0 0 540px;
  background: linear-gradient(145deg, #0a1628 0%, #1a365d 40%, #2c5282 100%);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 50px 60px;
  position: relative;
  overflow: hidden;
}

.auth-brand::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(ellipse at top right, rgba(66, 153, 225, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at bottom left, rgba(49, 130, 206, 0.1) 0%, transparent 50%);
  pointer-events: none;
}

.auth-brand::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.02'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  pointer-events: none;
  opacity: 0.5;
}

.brand-content {
  position: relative;
  z-index: 1;
}

.brand-logo {
  width: 100px;
  height: 100px;
  background: #ffffff;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 35px;
  padding: 12px;
  box-shadow: 
    0 4px 6px rgba(0, 0, 0, 0.1),
    0 10px 40px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  transition: transform 0.3s ease;
}

.brand-logo:hover {
  transform: scale(1.05);
}

.brand-logo .iset-logo {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.brand-logo i {
  font-size: 40px;
  color: #2c5282;
}

.brand-title {
  margin-bottom: 20px;
}

.brand-title h1 {
  font-size: 48px;
  font-weight: 800;
  color: #fff;
  margin-bottom: 8px;
  letter-spacing: -1px;
  font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
}

.brand-title h1 .highlight {
  color: #63b3ed;
  font-weight: 800;
}

.title-underline {
  width: 80px;
  height: 4px;
  background: linear-gradient(90deg, #63b3ed 0%, #4299e1 50%, transparent 100%);
  border-radius: 2px;
}

.brand-tagline {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.7;
  margin-bottom: 45px;
  font-weight: 300;
  letter-spacing: 0.3px;
}

.brand-tagline strong {
  color: #63b3ed;
  font-weight: 600;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.feature {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.3s ease;
}

.feature:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(99, 179, 237, 0.3);
  transform: translateX(5px);
}

.feature-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #4299e1 0%, #3182ce 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(66, 153, 225, 0.3);
}

.feature-icon i {
  color: #fff;
  font-size: 18px;
}

.feature-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.feature-title {
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.2px;
}

.feature-desc {
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
  font-weight: 300;
}

.brand-footer {
  position: relative;
  z-index: 1;
}

.footer-line {
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.2) 50%, transparent 100%);
  margin-bottom: 20px;
}

.brand-footer p {
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
  font-weight: 400;
  letter-spacing: 0.5px;
}

.brand-footer p i {
  color: #63b3ed;
}

.auth-form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.auth-form-container {
  width: 100%;
  max-width: 480px;
}

.auth-tabs {
  display: flex;
  background: #e2e8f0;
  border-radius: 12px;
  padding: 6px;
  margin-bottom: 30px;
}

.auth-tab {
  flex: 1;
  padding: 14px 24px;
  border: none;
  background: transparent;
  font-size: 15px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s ease;
  font-family: 'Poppins', sans-serif;
}

.auth-tab.active {
  background: #fff;
  color: #0f172a;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.auth-tab:hover:not(.active) {
  color: #0f172a;
}

.alert-message {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  margin-bottom: 24px;
  font-size: 14px;
}

.alert-message.error {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.alert-message.success {
  background: #f0fdf4;
  color: #16a34a;
  border: 1px solid #bbf7d0;
}

.alert-message .close-btn {
  margin-left: auto;
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: inherit;
  opacity: 0.7;
  line-height: 1;
}

.alert-message .close-btn:hover {
  opacity: 1;
}

.auth-form h2 {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 8px;
}

.form-subtitle {
  color: #64748b;
  font-size: 15px;
  margin-bottom: 32px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.input-group {
  margin-bottom: 20px;
}

.input-group label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 8px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-wrapper > i {
  position: absolute;
  left: 16px;
  color: #94a3b8;
  font-size: 16px;
  pointer-events: none;
  z-index: 1;
}

.input-wrapper input,
.input-wrapper select {
  width: 100%;
  padding: 14px 16px 14px 48px;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 15px;
  color: #0f172a;
  background: #fff;
  transition: all 0.2s ease;
  font-family: 'Poppins', sans-serif;
}

.input-wrapper input:focus,
.input-wrapper select:focus {
  outline: none;
  border-color: #3182ce;
  box-shadow: 0 0 0 4px rgba(49, 130, 206, 0.15);
}

.input-wrapper input::placeholder {
  color: #94a3b8;
}

.select-wrapper select {
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%2394a3b8'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'%3E%3C/path%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 16px center;
  background-size: 20px;
  padding-right: 48px;
}

.toggle-password {
  position: absolute;
  right: 16px;
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  padding: 4px;
}

.toggle-password:hover {
  color: #64748b;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #475569;
  cursor: pointer;
  position: relative;
  padding-left: 28px;
}

.checkbox-wrapper input {
  position: absolute;
  opacity: 0;
  cursor: pointer;
}

.checkmark {
  position: absolute;
  left: 0;
  width: 20px;
  height: 20px;
  background: #fff;
  border: 2px solid #cbd5e1;
  border-radius: 5px;
  transition: all 0.2s ease;
}

.checkbox-wrapper input:checked ~ .checkmark {
  background: #0d9488;
  border-color: #0d9488;
}

.checkbox-wrapper input:checked ~ .checkmark::after {
  content: '';
  position: absolute;
  left: 6px;
  top: 2px;
  width: 5px;
  height: 10px;
  border: solid #fff;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.checkbox-wrapper.terms {
  margin-bottom: 24px;
}

.checkbox-wrapper a {
  color: #0d9488;
  text-decoration: none;
}

.checkbox-wrapper a:hover {
  text-decoration: underline;
}

.forgot-link {
  font-size: 14px;
  color: #0d9488;
  text-decoration: none;
  font-weight: 500;
}

.forgot-link:hover {
  text-decoration: underline;
}

.submit-btn {
  width: 100%;
  padding: 16px 24px;
  background: linear-gradient(135deg, #2c5282 0%, #3182ce 100%);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
  letter-spacing: 0.3px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(49, 130, 206, 0.35);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.loader {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.reload-btn {
  position: absolute;
  right: 35px;
  top: 50%;
  transform: translateY(-50%);
  background: transparent;
  border: none;
  color: #0d9488;
  cursor: pointer;
  padding: 5px;
  font-size: 14px;
  transition: transform 0.3s ease;
}

.reload-btn:hover {
  transform: translateY(-50%) rotate(180deg);
}

.select-wrapper {
  position: relative;
}

@media (max-width: 1024px) {
  .auth-brand {
    flex: 0 0 380px;
    padding: 40px;
  }
  
  .brand-content h1 {
    font-size: 32px;
  }
}

@media (max-width: 768px) {
  .auth-wrapper {
    flex-direction: column;
  }
  
  .auth-brand {
    flex: none;
    padding: 40px 30px;
  }
  
  .brand-features {
    display: none;
  }
  
  .brand-content > p {
    margin-bottom: 0;
  }
  
  .auth-form-section {
    padding: 30px 20px;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }

  .form-steps {
    display: none;
  }
}

/* Nouveaux styles pour le formulaire d'inscription amélioré */
.form-steps {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30px;
  padding: 0 10px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.step-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e2e8f0;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
}

.step.active .step-number {
  background: #0d9488;
  color: white;
}

.step.completed .step-number {
  background: #10b981;
  color: white;
}

.step-label {
  font-size: 11px;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.step.active .step-label {
  color: #0d9488;
}

.step-line {
  flex: 1;
  height: 2px;
  background: #e2e8f0;
  margin: 0 8px;
  margin-bottom: 20px;
  transition: background 0.3s ease;
}

.step-line.completed {
  background: #10b981;
}

.form-section {
  margin-bottom: 24px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e2e8f0;
  color: #334155;
  font-weight: 600;
  font-size: 14px;
}

.section-title i {
  color: #0d9488;
  font-size: 16px;
}

.required {
  color: #ef4444;
}

.input-wrapper.valid {
  border-color: #10b981;
}

.input-wrapper.valid input,
.input-wrapper.valid select {
  border-color: #10b981;
}

.input-wrapper.error input,
.input-wrapper.error select {
  border-color: #ef4444;
}

.valid-icon {
  position: absolute;
  right: 16px;
  color: #10b981;
  font-size: 14px;
}

.error-text {
  display: block;
  color: #ef4444;
  font-size: 12px;
  margin-top: 4px;
}

.password-strength {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
}

.strength-bars {
  display: flex;
  gap: 4px;
  flex: 1;
}

.strength-bars .bar {
  height: 4px;
  flex: 1;
  background: #e2e8f0;
  border-radius: 2px;
  transition: background 0.3s ease;
}

.strength-bars .bar.weak { background: #ef4444; }
.strength-bars .bar.fair { background: #f59e0b; }
.strength-bars .bar.good { background: #10b981; }
.strength-bars .bar.strong { background: #0d9488; }

.strength-text {
  font-size: 11px;
  font-weight: 500;
  min-width: 60px;
  text-align: right;
}

/* ========================================
   STYLES DU FORMULAIRE D'INSCRIPTION EN TABLEAU
   ======================================== */

.register-form {
  max-width: 600px;
}

.register-header {
  text-align: center;
  margin-bottom: 24px;
}

.register-header h2 {
  font-size: 26px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.register-header h2 i {
  color: #0d9488;
}

.register-table-container {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
  margin-bottom: 20px;
}

.register-table {
  width: 100%;
  border-collapse: collapse;
}

.register-table .table-section-header {
  background: linear-gradient(135deg, #0f172a 0%, #1e3a5f 100%);
  color: #fff;
  padding: 14px 20px;
  font-size: 14px;
  font-weight: 600;
  text-align: left;
  letter-spacing: 0.5px;
}

.register-table .table-section-header i {
  margin-right: 10px;
  color: #5eead4;
}

.register-table tbody tr {
  border-bottom: 1px solid #f1f5f9;
  transition: background 0.2s ease;
}

.register-table tbody tr:hover {
  background: #f8fafc;
}

.register-table tbody tr:last-child {
  border-bottom: none;
}

.register-table td {
  padding: 16px 20px;
  vertical-align: middle;
}

.register-table .field-label {
  width: 35%;
  background: #f8fafc;
  border-right: 1px solid #e2e8f0;
}

.register-table .field-label label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin: 0;
}

.register-table .field-label label i {
  color: #64748b;
  font-size: 14px;
  width: 18px;
  text-align: center;
}

.register-table .field-input {
  width: 65%;
}

.table-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.table-input-wrapper input,
.table-input-wrapper select {
  width: 100%;
  padding: 12px 40px 12px 14px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  color: #0f172a;
  background: #fff;
  transition: all 0.2s ease;
  font-family: 'Poppins', sans-serif;
}

.table-input-wrapper input:focus,
.table-input-wrapper select:focus {
  outline: none;
  border-color: #0d9488;
  box-shadow: 0 0 0 3px rgba(13, 148, 136, 0.15);
}

.table-input-wrapper input::placeholder {
  color: #94a3b8;
}

.table-input-wrapper.valid input,
.table-input-wrapper.valid select {
  border-color: #10b981;
  background: #f0fdf4;
}

.table-input-wrapper.error input,
.table-input-wrapper.error select {
  border-color: #ef4444;
  background: #fef2f2;
}

.table-input-wrapper .valid-icon {
  position: absolute;
  right: 14px;
  color: #10b981;
  font-size: 16px;
  pointer-events: none;
}

.table-input-wrapper.password-field input {
  padding-right: 80px;
}

.table-input-wrapper.password-field .toggle-password {
  position: absolute;
  right: 40px;
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  padding: 4px;
  transition: color 0.2s;
}

.table-input-wrapper.password-field .toggle-password:hover {
  color: #64748b;
}

.table-input-wrapper.select-field select {
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%2394a3b8'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'%3E%3C/path%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 14px center;
  background-size: 18px;
  padding-right: 50px;
}

.table-input-wrapper .reload-btn {
  position: absolute;
  right: 35px;
  background: transparent;
  border: none;
  color: #0d9488;
  cursor: pointer;
  padding: 4px;
  font-size: 14px;
  transition: transform 0.3s ease;
}

.table-input-wrapper .reload-btn:hover {
  transform: rotate(180deg);
}

.success-text {
  display: block;
  color: #10b981;
  font-size: 12px;
  margin-top: 6px;
}

.success-text i,
.error-text i {
  margin-right: 4px;
}

/* Barre de progression */
.register-progress {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.register-progress .progress-bar {
  flex: 1;
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}

.register-progress .progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #0d9488 0%, #10b981 100%);
  border-radius: 4px;
  transition: width 0.4s ease;
}

.register-progress .progress-text {
  font-size: 13px;
  font-weight: 600;
  color: #0d9488;
  min-width: 90px;
  text-align: right;
}

/* Force du mot de passe amélioré */
.strength-1 { color: #ef4444; }
.strength-2 { color: #f59e0b; }
.strength-3 { color: #10b981; }
.strength-4 { color: #0d9488; }

/* Responsive pour le tableau */
@media (max-width: 600px) {
  .register-table .field-label {
    display: none;
  }
  
  .register-table .field-input {
    width: 100%;
  }
  
  .register-table td {
    display: block;
    padding: 12px 16px;
  }
  
  .table-input-wrapper input::placeholder {
    font-size: 13px;
  }
}

/* ================= Modal Mot de passe oublié ================= */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.forgot-modal {
  background: #fff;
  border-radius: 16px;
  width: 100%;
  max-width: 440px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  animation: modalSlideIn 0.3s ease;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header-forgot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header-forgot h3 {
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
  display: flex;
  align-items: center;
  gap: 10px;
}

.modal-header-forgot h3 i {
  color: #0d9488;
}

.close-modal {
  background: none;
  border: none;
  font-size: 24px;
  color: #64748b;
  cursor: pointer;
  padding: 0;
  line-height: 1;
  transition: color 0.2s;
}

.close-modal:hover {
  color: #0f172a;
}

.modal-body-forgot {
  padding: 24px;
}

.modal-description {
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 20px;
}

.modal-body-forgot .input-group {
  margin-bottom: 0;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.btn-secondary {
  padding: 12px 24px;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #64748b;
  font-size: 14px;
  font-weight: 600;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.btn-primary {
  padding: 12px 24px;
  border: none;
  background: linear-gradient(135deg, #0d9488 0%, #0f766e 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(13, 148, 136, 0.3);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.success-state {
  text-align: center;
  padding: 40px 24px;
}

.success-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #10b981 0%, #0d9488 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.success-icon i {
  font-size: 36px;
  color: #fff;
}

.success-state h4 {
  font-size: 20px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 12px;
}

.success-state p {
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 12px;
}

.success-state .note {
  font-size: 13px;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-bottom: 24px;
}

.loader-small {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* ================= ANIMATIONS AMÉLIORÉES ================= */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

.auth-form {
  animation: fadeInUp 0.5s ease-out;
}

.register-table-container {
  animation: fadeInUp 0.6s ease-out;
}

.register-table tbody tr {
  animation: fadeInUp 0.4s ease-out;
  animation-fill-mode: backwards;
}

.register-table tbody tr:nth-child(1) { animation-delay: 0.1s; }
.register-table tbody tr:nth-child(2) { animation-delay: 0.15s; }
.register-table tbody tr:nth-child(3) { animation-delay: 0.2s; }
.register-table tbody tr:nth-child(4) { animation-delay: 0.25s; }

.submit-btn {
  position: relative;
  overflow: hidden;
}

.submit-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transition: left 0.5s;
}

.submit-btn:hover::before {
  left: 100%;
}

/* Effet glow sur les inputs focus */
.table-input-wrapper input:focus,
.table-input-wrapper select:focus,
.input-wrapper input:focus {
  animation: inputGlow 0.3s ease;
}

@keyframes inputGlow {
  0% {
    box-shadow: 0 0 0 0 rgba(13, 148, 136, 0.4);
  }
  100% {
    box-shadow: 0 0 0 4px rgba(13, 148, 136, 0.1);
  }
}

/* Badge animé pour les validations */
.table-input-wrapper .valid-icon {
  animation: popIn 0.3s ease;
}

@keyframes popIn {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

/* Amélioration de la barre de progression */
.register-progress .progress-fill {
  position: relative;
  overflow: hidden;
}

.register-progress .progress-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.3),
    transparent
  );
  animation: shimmer 2s infinite;
  background-size: 200% 100%;
}

/* Hover effect sur les lignes du tableau */
.register-table tbody tr {
  position: relative;
}

.register-table tbody tr::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: #0d9488;
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.register-table tbody tr:hover::before {
  transform: scaleY(1);
}

/* Amélioration visuelle des sections */
.register-table .table-section-header {
  position: relative;
  overflow: hidden;
}

.register-table .table-section-header::after {
  content: '';
  position: absolute;
  right: -50px;
  top: -50px;
  width: 100px;
  height: 100px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
}

/* Style amélioré pour les onglets */
.auth-tabs {
  position: relative;
  overflow: hidden;
}

.auth-tab {
  position: relative;
  z-index: 1;
}

.auth-tab.active::after {
  content: '';
  position: absolute;
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  width: 30px;
  height: 3px;
  background: #0d9488;
  border-radius: 2px;
}

/* Message d'alerte amélioré */
.alert-message {
  animation: slideInDown 0.4s ease;
}

@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Amélioration du brand */
.brand-logo {
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.feature {
  transition: transform 0.3s ease;
}

.feature:hover {
  transform: translateX(10px);
}

/* Amélioration des tooltips et hover states */
.checkbox-wrapper {
  transition: color 0.2s ease;
}

.checkbox-wrapper:hover {
  color: #0f172a;
}

/* Effet ripple sur le bouton submit */
.submit-btn:active {
  transform: scale(0.98);
}

/* ================= RESET PASSWORD STYLES ================= */
.reset-password-form {
  text-align: center;
  animation: fadeInUp 0.5s ease-out;
}

.reset-header {
  margin-bottom: 30px;
}

.reset-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #0d9488, #14b8a6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  animation: pulse 2s ease-in-out infinite;
}

.reset-icon i {
  font-size: 32px;
  color: white;
}

.reset-password-form h2 {
  font-size: 28px;
  color: #0f172a;
  margin-bottom: 10px;
}

.reset-password-form .form-subtitle {
  color: #64748b;
  font-size: 15px;
}

.reset-password-form .auth-form {
  text-align: left;
  margin-top: 30px;
}

.back-to-login {
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.back-to-login a {
  color: #0d9488;
  text-decoration: none;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s ease;
}

.back-to-login a:hover {
  color: #3182ce;
  transform: translateX(-5px);
}

.back-to-login a i {
  font-size: 14px;
}

/* ================= RESET LINK STYLES ================= */
.reset-link-container {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 12px;
  padding: 20px;
  margin: 20px 0;
  border: 1px solid #bae6fd;
}

.reset-link-label {
  color: #0369a1;
  font-size: 14px;
  margin-bottom: 12px;
}

.reset-link-box {
  background: white;
  border-radius: 8px;
  padding: 15px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.reset-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #2c5282 0%, #3182ce 100%);
  color: white;
  padding: 12px 24px;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.reset-link:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(49, 130, 206, 0.4);
  color: white;
}

.reset-link-note {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #64748b;
  font-size: 12px;
  margin-top: 12px;
  margin-bottom: 0;
}

.reset-link-note i {
  color: #f59e0b;
}

/* ================= REGISTER FORM IMPROVEMENTS ================= */
.auth-form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  overflow-y: auto;
}

.auth-form-container {
  width: 100%;
  max-width: 650px;
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
}

.register-table-container {
  max-height: none;
  overflow: visible;
}

.register-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.register-table th,
.register-table td {
  padding: 14px 18px;
}

.table-section-header {
  background: linear-gradient(135deg, #1a365d 0%, #2c5282 100%);
  color: white;
  font-weight: 600;
  font-size: 15px;
}

.field-label {
  background: #f8fafc;
  width: 180px;
  vertical-align: middle;
}

.field-label label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #334155;
  margin: 0;
}

.field-input {
  background: white;
}

.table-input-wrapper input,
.table-input-wrapper select {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s ease;
}

.table-input-wrapper input:focus,
.table-input-wrapper select:focus {
  outline: none;
  border-color: #3182ce;
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.15);
}

.table-input-wrapper.valid input {
  border-color: #10b981;
}

.table-input-wrapper .valid-icon {
  color: #10b981;
  margin-left: 8px;
}

/* ================= CHEF DE DEPARTEMENT STYLES ================= */
.chef-dept-option {
  display: flex;
  align-items: center;
}

.chef-checkbox {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  user-select: none;
  padding: 10px 16px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 10px;
  border: 2px solid #f59e0b;
  transition: all 0.2s ease;
}

.chef-checkbox:hover {
  background: linear-gradient(135deg, #fde68a 0%, #fcd34d 100%);
  transform: translateY(-1px);
}

.chef-checkbox input[type="checkbox"] {
  width: 20px;
  height: 20px;
  accent-color: #f59e0b;
  cursor: pointer;
}

.chef-label {
  font-weight: 600;
  color: #92400e;
  font-size: 14px;
}

.chef-checkbox input[type="checkbox"]:checked + .checkmark + .chef-label {
  color: #78350f;
}

/* ================= DISABLED FIELD STYLES ================= */
.disabled-label {
  opacity: 0.6;
}

.disabled-label label {
  color: #94a3b8;
}

.table-input-wrapper.disabled {
  opacity: 0.6;
}

.table-input-wrapper.disabled select {
  background-color: #f1f5f9;
  cursor: not-allowed;
  color: #94a3b8;
}

.info-text {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 12px;
  margin-top: 8px;
  font-style: italic;
}

.info-text i {
  color: #3b82f6;
}
</style>
