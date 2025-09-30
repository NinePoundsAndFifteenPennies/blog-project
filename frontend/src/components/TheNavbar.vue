<template>
  <nav class="navbar">
    <div class="navbar-container">
      <router-link to="/" class="navbar-brand">我的博客</router-link>
      <ul class="navbar-nav">
        <li class="nav-item">
          <router-link to="/" class="nav-link">主页</router-link>
        </li>
        <li class="nav-item">
          <router-link to="/login" class="nav-link">登录</router-link>
        </li>
        <li class="nav-item">
          <router-link to="/register" class="nav-link">注册</router-link>
        </li>
      </ul>
    </div>
  </nav>
</template>

<script>
export default {
  name: "TheNavbar",
};
</script>

<style scoped>
.navbar {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 0.8rem 2rem;
}

.navbar-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
}

.navbar-brand {
  font-size: 1.5rem;
  font-weight: bold;
  text-decoration: none;
  color: #2c3e50;
}

.navbar-nav {
  list-style: none;
  display: flex;
  margin: 0;
  padding: 0;
}

.nav-item {
  margin-left: 1.5rem;
}

.nav-link {
  text-decoration: none;
  color: #555;
  font-size: 1rem;
  transition: color 0.3s;
}

.nav-link:hover,
.router-link-exact-active {
  color: #42b983; /* Vue's default green color */
}
</style>
