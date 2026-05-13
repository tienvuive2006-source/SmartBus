<template>
  <div class="bg-background text-on-background font-body-lg flex min-h-screen">
    <!-- Navigation Drawer -->
    <aside class="hidden md:flex h-full w-80 rounded-r-xl bg-surface-container-low dark:bg-surface-container-lowest shadow-xl fixed inset-y-0 left-0 z-[60] flex-col py-6 gap-2 border-r border-surface-container dark:border-surface-container-highest">
      <div class="px-6 mb-6">
        <div class="flex items-center gap-4">
          <img alt="Staff Member Avatar" class="w-12 h-12 rounded-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBKe_uRBC52VlAm6N6_Q5NhZA-QoKjo6KVjiT8ROVAWlAKy-BytkenQVWoscOxech4J7xPoqaagYyvIoAsEVYCuSbt6SFrSlAZ3y9hXQJtJK0_6U-OaHF_RBIDykHc6EKD9B1DiCDcVeSv-frwcdWv-AOurv3n3Cp-dZNI8J3bcezrI4kuBJhWtQalZlPN7c1aQYZkW6IHY1ZcwO5hP1jg_UREEQ28vT1uxbO-HNHXs3Ee_7GJAt0VJTXf-eF6Ab8cUJfXBSeZLkvvJ"/>
          <div>
            <h2 class="text-headline-sm font-headline-sm font-bold text-primary dark:text-primary-fixed">Staff Portal</h2>
            <p class="text-body-md font-body-md text-on-surface-variant">Route ID: BUS-402</p>
            <span class="inline-block mt-1 px-2 py-0.5 bg-primary/10 text-primary text-label-md font-label-md rounded-full">Active Duty</span>
          </div>
        </div>
      </div>

      <nav class="flex-1 flex flex-col gap-2">
        <RouterLink to="/admin" :class="[
          'flex items-center gap-4 rounded-lg px-4 py-3 mx-2 transition-all duration-200 ease-in-out',
          $route.path === '/admin' ? 'bg-secondary-container text-on-secondary-container font-medium' : 'text-on-surface-variant hover:bg-surface-variant'
        ]">
          <span class="material-symbols-outlined" :style="$route.path === '/admin' ? 'font-variation-settings: \'FILL\' 1;' : ''">dashboard</span>
          <span class="text-body-md font-body-md">Dashboard</span>
        </RouterLink>
        
        <RouterLink to="/admin/trip-manager" :class="[
          'flex items-center gap-4 rounded-lg px-4 py-3 mx-2 transition-all duration-200 ease-in-out',
          $route.path === '/admin/trip-manager' ? 'bg-secondary-container text-on-secondary-container font-medium' : 'text-on-surface-variant hover:bg-surface-variant'
        ]">
          <span class="material-symbols-outlined" :style="$route.path === '/admin/trip-manager' ? 'font-variation-settings: \'FILL\' 1;' : ''">route</span>
          <span class="text-body-md font-body-md">Trip Manager</span>
        </RouterLink>
        
        <a href="#" class="flex items-center gap-4 text-on-surface-variant hover:bg-surface-variant rounded-lg px-4 py-3 mx-2 transition-all duration-200 ease-in-out">
          <span class="material-symbols-outlined">qr_code_scanner</span>
          <span class="text-body-md font-body-md">QR Scanner</span>
        </a>
        
        <a href="#" class="flex items-center gap-4 text-on-surface-variant hover:bg-surface-variant rounded-lg px-4 py-3 mx-2 transition-all duration-200 ease-in-out">
          <span class="material-symbols-outlined">groups</span>
          <span class="text-body-md font-body-md">Passenger Logs</span>
        </a>
        
        <RouterLink to="/admin/fleet-status" :class="[
          'flex items-center gap-4 rounded-lg px-4 py-3 mx-2 transition-all duration-200 ease-in-out',
          $route.path === '/admin/fleet-status' ? 'bg-secondary-container text-on-secondary-container font-medium' : 'text-on-surface-variant hover:bg-surface-variant'
        ]">
          <span class="material-symbols-outlined" :style="$route.path === '/admin/fleet-status' ? 'font-variation-settings: \'FILL\' 1;' : ''">directions_bus</span>
          <span class="text-body-md font-body-md">Fleet Status</span>
        </RouterLink>
        
        <a href="#" class="flex items-center gap-4 text-on-surface-variant hover:bg-surface-variant rounded-lg px-4 py-3 mx-2 transition-all duration-200 ease-in-out">
          <span class="material-symbols-outlined">settings</span>
          <span class="text-body-md font-body-md">Settings</span>
        </a>
      </nav>
    </aside>

    <!-- Main Content Area -->
    <main class="flex-1 md:ml-80 min-h-screen pb-24 md:pb-0">
      <!-- TopAppBar (Mobile Only) -->
      <header class="bg-surface dark:bg-surface-dim shadow-sm flex items-center justify-between px-container-margin h-touch-target-min w-full max-w-7xl mx-auto md:hidden sticky top-0 z-50">
        <button class="text-on-surface-variant dark:text-outline-variant hover:bg-surface-variant dark:hover:bg-on-surface-variant transition-colors duration-200 p-2 rounded-full">
          <span class="material-symbols-outlined">menu</span>
        </button>
        <h1 class="text-headline-md font-headline-md font-bold text-primary dark:text-primary-fixed">TransLink Pro</h1>
        <img alt="User profile photo" class="w-8 h-8 rounded-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBxgOUExc-xCEuCmVWbL68vLdQITc_Y0wv8U7XGef5KpT-aqdrR5fbNy4N0V7mNL6ft5hutTvSBDGjAAdPqVCaQeqJBoSe6TXOxNy1E5Hg5qzEimd2_BV76X7jygNfQe7wU9_whKbmyq67c-915xWoEj4ytKrQCQdV_sGazpI0FO_xxqhUgzSiNjFpOG_ZswDJpS-kJjepr9ZbSoQ5N2picJIQAz58JcIXXgLg9JiVzRVkt8Pe0heGRuavgG2-UEcd2KNRlNhuNC0uz"/>
      </header>

      <RouterView />
    </main>
  </div>
</template>

<script setup>
</script>
