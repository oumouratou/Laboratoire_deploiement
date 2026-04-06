<template>
  <div class="login-container">
    <h2>Connexion</h2>
    <form @submit.prevent="handleLogin">
      <div>
        <input type="email" v-model="email" placeholder="Email" required />
      </div>
      <div>
        <input type="password" v-model="password" placeholder="Mot de passe" required />
      </div>
      <button type="submit">Se connecter</button>
    </form>
    <p v-if="error">{{ error }}</p>
  </div>
</template>

<script>
import authService from '../services/authService';
import { useRouter } from 'vue-router';

export default {
  setup() {
    const router = useRouter();
    const email = ref('');
    const password = ref('');
    const error = ref('');

    const handleLogin = () => {
      authService.login({ email: email.value, password: password.value })
        .then(user => {
          // Rediriger selon le rôle
          if (user.role === 'ETUDIANT') router.push('/dashboard-etudiant');
          else if (user.role === 'ADMIN') router.push('/dashboard-admin');
          else router.push('/dashboard');
        })
        .catch(err => {
          error.value = err.response?.data || 'Erreur de connexion';
        });
    };

    return { email, password, error, handleLogin };
  }
};
</script>
