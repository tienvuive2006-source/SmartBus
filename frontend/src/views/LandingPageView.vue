<template>
  <div class="min-h-screen bg-[#FAFAFA] text-[#18181B] selection:bg-[#059669]/20 selection:text-[#059669] overflow-x-hidden font-sans">
    <LandingNavbar />
    <LandingHero />

    <LandingPopularRoutes />
    <LandingFleet />
    <LandingAmenities />
    <LandingReviews />
    <AppFooter />
  </div>
</template>

<script setup>
import LandingNavbar from '@/components/landing/LandingNavbar.vue';
import LandingHero from '@/components/landing/LandingHero.vue';

import LandingPopularRoutes from '@/components/landing/LandingPopularRoutes.vue';
import LandingFleet from '@/components/landing/LandingFleet.vue';
import LandingAmenities from '@/components/landing/LandingAmenities.vue';
import LandingReviews from '@/components/landing/LandingReviews.vue';
import AppFooter from '@/components/AppFooter.vue';
</script>

<style>
@keyframes scroll {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}
</style>
